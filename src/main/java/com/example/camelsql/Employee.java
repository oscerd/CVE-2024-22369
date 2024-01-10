package com.example.camelsql;

public class Employee {

    private Object empName;

    public Object getEmpName() {
        return empName;
    }

    public void setEmpName(Object empName) {
        this.empName = empName;
    }

    @Override
    public String toString() {
        return "Employee [empName=" + empName + "]";
    }

}

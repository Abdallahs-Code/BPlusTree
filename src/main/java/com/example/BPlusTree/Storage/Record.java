package com.example.BPlusTree.Storage;

public class Record {
    private final String name;
    private final String ssn;
    private final String deptCode;
    private final String address;
    private final String phone;
    private final String birthDate;
    private final char sex;
    private final String jobCode;
    private final float salary;
    private boolean deleted;

    public Record(String name, String ssn, String deptCode, String address, String phone,
                  String birthDate, char sex, String jobCode, float salary) {
        this.name = name;
        this.ssn = ssn;
        this.deptCode = deptCode;
        this.address = address;
        this.phone = phone;
        this.birthDate = birthDate;
        this.sex = sex;
        this.jobCode = jobCode;
        this.salary = salary;
        this.deleted = false;
    }

    public String getSSN() { return ssn; }
    public boolean isDeleted() { return deleted; }
    public void markDeleted() { this.deleted = true; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s | %s | %s | %.2f",
                name, ssn, deptCode, address, phone, birthDate, jobCode, salary);
    }
}

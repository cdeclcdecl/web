package ru.msu.web.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employeeid")
    private Long employeeId;

    @Column(name = "fullname", nullable = false)
    private String fullName;

    @Column(name = "address")
    private String address;

    @Column(name = "birthdate", nullable = false)
    private LocalDate birthDate;

    @Column(name = "degree")
    private String degree;

    @Column(name = "hiredate", nullable = false)
    private LocalDate hireDate;

    @Column(name = "leavedate")
    private LocalDate leaveDate;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phonenumber", length = 50)
    private String phoneNumber;

    @OneToMany(mappedBy = "employee")
    private List<Assignment> assignments = new ArrayList<>();

    public Employee() {}

    public boolean isActive() {
        return leaveDate == null;
    }

    public long getSeniorityYears() {
        LocalDate end = leaveDate != null ? leaveDate : LocalDate.now();
        return ChronoUnit.YEARS.between(hireDate, end);
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public LocalDate getLeaveDate() { return leaveDate; }
    public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public List<Assignment> getAssignments() { return assignments; }
    public void setAssignments(List<Assignment> assignments) { this.assignments = assignments; }
}

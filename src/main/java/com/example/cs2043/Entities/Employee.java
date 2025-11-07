package com.example.cs2043.Entities;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee")  // Changed from "Employees" to match db.sql
@Getter
@Setter
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "employeeid")
    private UUID employeeID;
    
    @Column(name = "firstname", nullable = false)
    private String firstName;
    
    @Column(name = "lastname", nullable = false)
    private String lastName;
    
    @Column(name = "salary")
    private Double salary;
    
    @Column(name = "dob", nullable = false)
    private LocalDate dob;
    
    @Column(name = "roletype")
    private String roleType;

    public Employee(String firstName, String lastName, Double salary, LocalDate dob, String roleType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.dob = dob;
        this.roleType = roleType;
    }

    public void displayInfo() {
        //some implementation
    }

    public LeaveRequest requestLeave(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return null;
    }
}
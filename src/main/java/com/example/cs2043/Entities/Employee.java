package com.example.cs2043.Entities;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Employees")
@Getter
@Setter
@NoArgsConstructor
public class Employee {
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int employeeID;
    @Column
    private double missedDays;
    @Column
    private LeaveRequest leaveRequest;


    public Employee(String firstName, String lastName, 
        double missedDays, LeaveRequest request) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.missedDays = missedDays;
            this.leaveRequest = request;
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
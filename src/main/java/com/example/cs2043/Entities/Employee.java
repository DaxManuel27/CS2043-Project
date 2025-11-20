package com.example.cs2043.Entities;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Employees")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "EMPLOYEE_TYPE")
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
    @Column(nullable = false)
    private double salary;
    @Column
    private int missedDays;
    @OneToOne
    @JoinColumn(name = "leaveRequestID")
    private LeaveRequest leaveRequest;


    public Employee(String firstName, String lastName, double salary, int missedDays, LeaveRequest request) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.salary = salary;
            this.missedDays = missedDays;
            this.leaveRequest = request;
            
    }

    public void displayInfo() {
        //for now will just print to console
        System.out.println("Employee ID: " + this.employeeID);
        System.out.println("First Name: " + this.firstName);
        System.out.println("Last Name: " + this.lastName);
        System.out.println("Missed Days: " + this.missedDays);
        if (this.leaveRequest != null) {
            System.out.println("Leave Request ID: " + this.leaveRequest.getRequestID());
            System.out.println("Leave Start Date: " + this.leaveRequest.getStartDate());
            System.out.println("Leave End Date: " + this.leaveRequest.getEndDate());
            System.out.println("Leave Approved: " + this.leaveRequest.isApproved());
            System.out.println("Leave Total Days: " + this.leaveRequest.getTotalDays());
        }
    }

    public LeaveRequest requestLeave(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LeaveRequest lr = new LeaveRequest(start, end);
        return lr;
    }

}
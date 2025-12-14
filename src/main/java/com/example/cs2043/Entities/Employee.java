package com.example.cs2043.Entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    
    // One employee can have many leave requests
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LeaveRequest> leaveRequests = new ArrayList<>();


    public Employee(String firstName, String lastName, double salary, int missedDays) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.salary = salary;
            this.missedDays = missedDays;
    }

    public void displayInfo() {
        //for now will just print to console
        System.out.println("Employee ID: " + this.employeeID);
        System.out.println("First Name: " + this.firstName);
        System.out.println("Last Name: " + this.lastName);
        System.out.println("Missed Days: " + this.missedDays);
        if (this.leaveRequests != null && !this.leaveRequests.isEmpty()) {
            System.out.println("Leave Requests: " + this.leaveRequests.size());
            for (LeaveRequest lr : this.leaveRequests) {
                System.out.println("  - Request ID: " + lr.getRequestID() + 
                    ", Dates: " + lr.getStartDate() + " to " + lr.getEndDate() +
                    ", Approved: " + lr.isApproved());
            }
        }
    }

    public LeaveRequest requestLeave(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LeaveRequest lr = new LeaveRequest(start, end, this);
        this.leaveRequests.add(lr);
        return lr;
    }

}

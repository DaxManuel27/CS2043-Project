package com.example.cs2043.Entities;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "LeaveRequests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {
    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int requestID;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Column(nullable = false)
    private boolean approved;
    
    @Column(nullable = false)
    private int totalDays;
    
    // Status field: PENDING, APPROVED, REJECTED
    @Column(columnDefinition = "varchar(255) default 'PENDING'")
    private String status = "PENDING";
    
    // Many leave requests belong to one employee
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    @JsonIgnore  // Prevent circular reference in JSON
    private Employee employee;

    public LeaveRequest(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.approved = false;
        this.status = "PENDING";
        this.totalDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }
    
    public LeaveRequest(LocalDate startDate, LocalDate endDate, Employee employee) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.approved = false;
        this.status = "PENDING";
        this.totalDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        this.employee = employee;
    }
    
    // Helper method to get employee ID for JSON response
    public Integer getEmployeeId() {
        return employee != null ? employee.getEmployeeID() : null;
    }
    
    // Helper method to get employee name for JSON response
    public String getEmployeeFirstName() {
        return employee != null ? employee.getFirstName() : null;
    }
    
    public String getEmployeeLastName() {
        return employee != null ? employee.getLastName() : null;
    }
    
    // Convenience method to get full name
    public String getEmployeeName() {
        if (employee != null) {
            return employee.getFirstName() + " " + employee.getLastName();
        }
        return null;
    }

}

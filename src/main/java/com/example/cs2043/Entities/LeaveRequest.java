package com.example.cs2043.Entities;

import java.time.LocalDate;


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

    public LeaveRequest(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.approved = false;
        this.totalDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isApproved() {
        return approved;
    }

    public int getTotalDays() {
        return totalDays;
    }
}

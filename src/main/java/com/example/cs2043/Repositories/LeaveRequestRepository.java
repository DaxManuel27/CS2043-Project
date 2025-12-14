package com.example.cs2043.Repositories;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.cs2043.Entities.LeaveRequest;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    
    // Find all leave requests for a specific employee
    List<LeaveRequest> findByEmployeeEmployeeID(int employeeId);
    
}

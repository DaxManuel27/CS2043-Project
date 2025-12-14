package com.example.cs2043.controllers;

import com.example.cs2043.Entities.Employee;
import com.example.cs2043.Entities.LeaveRequest;
import com.example.cs2043.Repositories.EmployeeRepository;
import com.example.cs2043.Repositories.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/leave-requests")
@CrossOrigin(origins = "*")
public class LeaveRequestApiController {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    // GET: All Leave Requests from database
    @GetMapping
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }
    
    // GET: Leave Requests by Employee ID
    @GetMapping("/employee/{employeeId}")
    public List<LeaveRequest> getLeaveRequestsByEmployee(@PathVariable int employeeId) {
        return leaveRequestRepository.findByEmployeeEmployeeID(employeeId);
    }

    // POST: Create new leave request with employee association
    @PostMapping
    public ResponseEntity<?> createLeaveRequest(@RequestBody Map<String, String> requestData) {
        try {
            LocalDate startDate = LocalDate.parse(requestData.get("startDate"));
            LocalDate endDate = LocalDate.parse(requestData.get("endDate"));
            
            Employee employee = null;
            
            // Try to find employee by ID if provided
            String employeeIdStr = requestData.get("employeeId");
            if (employeeIdStr != null && !employeeIdStr.isEmpty()) {
                int employeeId = Integer.parseInt(employeeIdStr);
                Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
                if (employeeOpt.isPresent()) {
                    employee = employeeOpt.get();
                }
            }
            
            // If no employee found by ID, use the first available employee
            if (employee == null) {
                List<Employee> allEmployees = employeeRepository.findAll();
                if (!allEmployees.isEmpty()) {
                    employee = allEmployees.get(0);
                } else {
                    return ResponseEntity.badRequest().body(Map.of("error", "No employees exist in the system"));
                }
            }
            
            LeaveRequest leaveRequest = new LeaveRequest(startDate, endDate, employee);
            LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // POST: Approve Leave Request
    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, String>> approveLeave(@PathVariable int id) {
        Optional<LeaveRequest> leaveRequestOpt = leaveRequestRepository.findById(id);
        
        if (leaveRequestOpt.isPresent()) {
            LeaveRequest leaveRequest = leaveRequestOpt.get();
            leaveRequest.setApproved(true);
            leaveRequest.setStatus("APPROVED");
            leaveRequestRepository.save(leaveRequest);
            return ResponseEntity.ok(Map.of("message", "Leave request approved"));
        }
        
        return ResponseEntity.notFound().build();
    }
    
    // POST: Reject Leave Request
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectLeave(@PathVariable int id) {
        Optional<LeaveRequest> leaveRequestOpt = leaveRequestRepository.findById(id);
        
        if (leaveRequestOpt.isPresent()) {
            LeaveRequest leaveRequest = leaveRequestOpt.get();
            leaveRequest.setApproved(false);
            leaveRequest.setStatus("REJECTED");
            leaveRequestRepository.save(leaveRequest);
            return ResponseEntity.ok(Map.of("message", "Leave request rejected"));
        }
        
        return ResponseEntity.notFound().build();
    }

    // DELETE: Delete leave request
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeaveRequest(@PathVariable int id) {
        if (leaveRequestRepository.existsById(id)) {
            leaveRequestRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // POST: Migrate existing data to fix status inconsistencies
    @PostMapping("/migrate-status")
    public ResponseEntity<Map<String, Object>> migrateStatus() {
        List<LeaveRequest> allRequests = leaveRequestRepository.findAll();
        int updated = 0;
        
        for (LeaveRequest request : allRequests) {
            boolean needsUpdate = false;
            
            // If approved is true but status isn't APPROVED, fix it
            if (request.isApproved() && !"APPROVED".equals(request.getStatus())) {
                request.setStatus("APPROVED");
                needsUpdate = true;
            }
            // If status is null or empty, set based on approved flag
            else if (request.getStatus() == null || request.getStatus().isEmpty()) {
                request.setStatus(request.isApproved() ? "APPROVED" : "PENDING");
                needsUpdate = true;
            }
            
            if (needsUpdate) {
                leaveRequestRepository.save(request);
                updated++;
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "message", "Migration complete",
            "totalRecords", allRequests.size(),
            "updatedRecords", updated
        ));
    }
}

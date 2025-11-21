package com.example.cs2043;

import com.example.cs2043.Repositories.EmployeeRepository;
import com.example.cs2043.Repositories.LeaveRequestRepository;
import com.example.cs2043.Entities.Employee;
import com.example.cs2043.Entities.LeaveRequest;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final EmployeeRepository employeeRepo;
    private final LeaveRequestRepository leaveRepo;

    public DashboardController(EmployeeRepository employeeRepo, 
		LeaveRequestRepository leaveRepo) {
        this.employeeRepo = employeeRepo;
        this.leaveRepo = leaveRepo;
    }

    @GetMapping("/employees")
    public List<Employee> employees() {
        return employeeRepo.findAll();
    }

    @GetMapping("/admins")
    public List<Employee> admins() {
        return List.of();
    }

    @GetMapping("/leave-requests")
    public List<LeaveRequest> leaveRequests() {
        return leaveRepo.findAll();
    }
	
	@GetMapping("/summary")
	public Map<String,Object> summary() {
		Map<String,Object> data = new HashMap<>();
		data.put("totalEmployees", employeeRepo.count());
		data.put("totalAdmins", 0);  // you removed admin counting
		data.put("totalLeaveRequests", leaveRepo.count());
		return data;
	}

}

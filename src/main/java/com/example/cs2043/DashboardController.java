package com.example.cs2043;

import com.example.cs2043.Entities.Administrator;
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
        List<Employee> all = employeeRepo.findAll();
        List<Employee> admins = new ArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            Employee e = all.get(i);
            if (e instanceof Administrator) {
                admins.add(e);
            }
        }

        return admins;
    }

    @GetMapping("/leave-requests")
    public List<LeaveRequest> leaveRequests() {
        return leaveRepo.findAll();
    }

    @GetMapping("/summary")
    public Map<String,Object> summary() {
        List<Employee> all = employeeRepo.findAll();
        int adminCount = 0;

        for (int i = 0; i < all.size(); i++) {
            Employee e = all.get(i);
            if (e instanceof Administrator) {
                adminCount++;
            }
        }

        Map<String,Object> data = new HashMap<>();
        data.put("totalEmployees", all.size());
        data.put("totalAdmins", adminCount);
        data.put("totalLeaveRequests", leaveRepo.count());

        return data;
    }
}

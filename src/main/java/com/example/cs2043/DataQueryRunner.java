package com.example.cs2043;

import com.example.cs2043.Entities.Administrator;
import com.example.cs2043.Entities.Employee;
import com.example.cs2043.Entities.LeaveRequest;
import com.example.cs2043.Repositories.AdministratorRepository;
import com.example.cs2043.Repositories.EmployeeRepository;
import com.example.cs2043.Repositories.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DataQueryRunner implements CommandLineRunner {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========================================");
        System.out.println("PROGRAMMATIC QUERY RESULTS");
        System.out.println("========================================\n");

        // Query all employees
        System.out.println("--- ALL EMPLOYEES ---");
        List<Employee> employees = employeeRepository.findAll();
        System.out.println("Total employees: " + employees.size());
        for (Employee emp : employees) {
            System.out.println("ID: " + emp.getEmployeeID() + 
                             " | Name: " + emp.getFirstName() + " " + emp.getLastName() + 
                             " | Salary: $" + emp.getSalary() + 
                             " | Missed Days: " + emp.getMissedDays());
        }
        System.out.println();

        // Query all administrators
        System.out.println("--- ALL ADMINISTRATORS ---");
        List<Administrator> admins = administratorRepository.findAll();
        System.out.println("Total administrators: " + admins.size());
        for (Administrator admin : admins) {
            System.out.println("ID: " + admin.getEmployeeID() + 
                             " | Name: " + admin.getFirstName() + " " + admin.getLastName() + 
                             " | Salary: $" + admin.getSalary());
        }
        System.out.println();

        // Query all leave requests
        System.out.println("--- ALL LEAVE REQUESTS ---");
        List<LeaveRequest> leaveRequests = leaveRequestRepository.findAll();
        System.out.println("Total leave requests: " + leaveRequests.size());
        for (LeaveRequest lr : leaveRequests) {
            System.out.println("ID: " + lr.getRequestID() + 
                             " | Start: " + lr.getStartDate() + 
                             " | End: " + lr.getEndDate() + 
                             " | Approved: " + lr.isApproved() +
                             " | Total Days: " + lr.getTotalDays());
        }
        System.out.println();

        // Query by ID example (if any employees exist)
        if (!employees.isEmpty()) {
            Integer firstEmployeeId = employees.get(0).getEmployeeID();
            System.out.println("--- QUERY BY ID EXAMPLE ---");
            Optional<Employee> employee = employeeRepository.findById(firstEmployeeId);
            if (employee.isPresent()) {
                Employee emp = employee.get();
                System.out.println("Found employee with ID " + firstEmployeeId + ": " + 
                                 emp.getFirstName() + " " + emp.getLastName());
            }
            System.out.println();
        }

        // Count queries
        System.out.println("--- COUNT QUERIES ---");
        System.out.println("Employee count: " + employeeRepository.count());
        System.out.println("Administrator count: " + administratorRepository.count());
        System.out.println("Leave request count: " + leaveRequestRepository.count());
        System.out.println();

        // Check if specific ID exists
        System.out.println("--- EXISTS CHECK ---");
        System.out.println("Employee with ID 1 exists: " + employeeRepository.existsById(1));
        System.out.println("Employee with ID 999 exists: " + employeeRepository.existsById(999));

        System.out.println("\n========================================");
        System.out.println("END OF QUERY RESULTS");
        System.out.println("========================================\n");
    }
}
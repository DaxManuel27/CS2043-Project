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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Profile;

@Component
@Profile("!test")
public class DataQueryRunner implements CommandLineRunner {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Load data from CSV files first
        loadDataFromCSV();
        
        // Then run queries
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
            String employeeName = lr.getEmployee() != null 
                ? lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName()
                : "Unassigned";
            System.out.println("ID: " + lr.getRequestID() + 
                             " | Employee: " + employeeName +
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

    private void loadDataFromCSV() {
        System.out.println("\n========================================");
        System.out.println("LOADING DATA FROM CSV FILES");
        System.out.println("========================================\n");

        try {
            // Check if data already exists
            long leaveRequestCount = leaveRequestRepository.count();
            long employeeCount = employeeRepository.count();
            System.out.println("Current database state - Leave Requests: " + leaveRequestCount + ", Employees: " + employeeCount);
            
            if (leaveRequestCount > 0 || employeeCount > 0) {
                System.out.println("Database already contains data. Skipping CSV load.");
                System.out.println();
                return;
            }

            // Load admin IDs first to know which employees should be administrators
            System.out.println("Loading administrator row numbers...");
            Set<Integer> adminRowNumbers = loadAdminRowNumbers();
            System.out.println("Found " + adminRowNumbers.size() + " administrator row numbers: " + adminRowNumbers);
            
            // Load Employees first (they no longer have leave request references)
            System.out.println("Loading employees and administrators...");
            Map<Integer, Employee> employeesByRow = loadEmployees(adminRowNumbers);
            System.out.println("Loaded " + employeesByRow.size() + " employees");

            // Load Leave Requests and link them to employees
            System.out.println("Loading leave requests...");
            loadLeaveRequests(employeesByRow);
            
            // Verify what was loaded
            long finalLeaveRequestCount = leaveRequestRepository.count();
            long finalEmployeeCount = employeeRepository.count();
            long finalAdminCount = administratorRepository.count();
            System.out.println("Final counts - Leave Requests: " + finalLeaveRequestCount + 
                             ", Employees: " + finalEmployeeCount + 
                             ", Administrators: " + finalAdminCount);

            System.out.println("\nData loading completed successfully!\n");
        } catch (Exception e) {
            System.err.println("ERROR loading data from CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Set<Integer> loadAdminRowNumbers() throws Exception {
        Set<Integer> adminRowNumbers = new HashSet<>();
        
        String resourcePath = "csvdata/administrator.csv";
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            resourcePath = "/csvdata/administrator.csv";
            is = getClass().getResourceAsStream(resourcePath);
        }
        if (is == null) {
            throw new RuntimeException("Could not find administrator.csv file. Make sure it's in src/main/resources/csvdata/");
        }
        
        System.out.println("Successfully opened administrator.csv from: " + resourcePath);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                try {
                    int adminRowNumber = Integer.parseInt(line);
                    adminRowNumbers.add(adminRowNumber);
                } catch (NumberFormatException e) {
                    // Skip invalid lines
                }
            }
        }
        
        return adminRowNumbers;
    }

    private Map<Integer, Employee> loadEmployees(Set<Integer> adminRowNumbers) throws Exception {
        Map<Integer, Employee> employeesByRow = new HashMap<>();
        
        String resourcePath = "csvdata/employee.csv";
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            resourcePath = "/csvdata/employee.csv";
            is = getClass().getResourceAsStream(resourcePath);
        }
        if (is == null) {
            throw new RuntimeException("Could not find employee.csv file. Make sure it's in src/main/resources/csvdata/");
        }
        
        System.out.println("Successfully opened employee.csv from: " + resourcePath);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            int rowNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                rowNumber++;

                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String firstName = parts[0].trim();
                    String lastName = parts[1].trim();
                    double salary = Double.parseDouble(parts[2].trim());
                    int missedDays = Integer.parseInt(parts[3].trim());

                    Employee emp;
                    if (adminRowNumbers.contains(rowNumber)) {
                        emp = new Administrator(firstName, lastName, salary, missedDays);
                        administratorRepository.saveAndFlush((Administrator) emp);
                    } else {
                        emp = new Employee(firstName, lastName, salary, missedDays);
                        employeeRepository.saveAndFlush(emp);
                    }
                    
                    employeesByRow.put(rowNumber, emp);
                }
            }
        }
        
        return employeesByRow;
    }

    private void loadLeaveRequests(Map<Integer, Employee> employeesByRow) throws Exception {
        String resourcePath = "csvdata/leave_requests.csv";
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            resourcePath = "/csvdata/leave_requests.csv";
            is = getClass().getResourceAsStream(resourcePath);
        }
        if (is == null) {
            System.err.println("Could not find leave_requests.csv file");
            throw new RuntimeException("Could not find leave_requests.csv file. Make sure it's in src/main/resources/csvdata/");
        }
        
        System.out.println("Successfully opened leave_requests.csv from: " + resourcePath);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    LocalDate startDate = LocalDate.parse(parts[0].trim());
                    LocalDate endDate = LocalDate.parse(parts[1].trim());
                    boolean approved = Boolean.parseBoolean(parts[2].trim());
                    int totalDays = Integer.parseInt(parts[3].trim());

                    LeaveRequest lr = new LeaveRequest(startDate, endDate);
                    lr.setApproved(approved);
                    lr.setTotalDays(totalDays);
                    
                    // Link to employee if employee_id is provided (column 5)
                    if (parts.length >= 5 && !parts[4].trim().isEmpty()) {
                        try {
                            int employeeRowNumber = Integer.parseInt(parts[4].trim());
                            Employee employee = employeesByRow.get(employeeRowNumber);
                            if (employee != null) {
                                lr.setEmployee(employee);
                            }
                        } catch (NumberFormatException e) {
                            // Employee ID is invalid, leave it null
                        }
                    }
                    
                    leaveRequestRepository.saveAndFlush(lr);
                }
            }
        }
    }
}

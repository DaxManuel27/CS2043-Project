package com.example.cs2043;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.example.cs2043.Entities.Employee;
import com.example.cs2043.Repositories.EmployeeRepository;
import java.time.LocalDate;

@SpringBootApplication
public class Cs2043Application {

	public static void main(String[] args) {
		SpringApplication.run(Cs2043Application.class, args);
	}
	
	@Bean
	CommandLineRunner initDatabase(EmployeeRepository employeeRepository) {
		return args -> {
			// Create employees with: firstName, lastName, salary, dob, roleType
			Employee emp1 = new Employee("John", "Doe", 50000.0, LocalDate.of(1990, 1, 15), "Employee");
			Employee emp2 = new Employee("Jane", "Smith", 60000.0, LocalDate.of(1985, 5, 20), "Employee");
			Employee emp3 = new Employee("Bob", "Johnson", 75000.0, LocalDate.of(1988, 3, 10), "Admin");
			
			// Save to database
			employeeRepository.save(emp1);
			employeeRepository.save(emp2);
			employeeRepository.save(emp3);
			
			System.out.println("Employees created successfully!");
		};
	}
}
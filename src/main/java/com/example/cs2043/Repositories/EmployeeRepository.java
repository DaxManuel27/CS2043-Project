package com.example.cs2043.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.cs2043.Entities.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    
}

package com.example.cs2043.controllers;

import com.example.cs2043.Entities.Employee;
import com.example.cs2043.Repositories.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(EmployeeApiController.class)
class EmployeeApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeRepository employeeRepository;

    private Employee testEmployee1;
    private Employee testEmployee2;

    @BeforeEach
    void setUp() {
        testEmployee1 = new Employee("John", "Doe", 50000.0, 5);
        testEmployee1.setEmployeeID(1);
        
        testEmployee2 = new Employee("Jane", "Smith", 60000.0, 3);
        testEmployee2.setEmployeeID(2);
    }

    @Test
    void getAllEmployees_ShouldReturnListOfEmployees() throws Exception {
        // Given
        List<Employee> employees = Arrays.asList(testEmployee1, testEmployee2);
        when(employeeRepository.findAll()).thenReturn(employees);

        // When & Then
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].salary").value(50000.0))
                .andExpect(jsonPath("$[1].firstName").value("Jane"))
                .andExpect(jsonPath("$[1].lastName").value("Smith"));

        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void getAllEmployees_WhenNoEmployees_ShouldReturnEmptyList() throws Exception {
        // Given
        when(employeeRepository.findAll()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
        // Given
        Employee newEmployee = new Employee("Bob", "Johnson", 55000.0, 2);
        Employee savedEmployee = new Employee("Bob", "Johnson", 55000.0, 2);
        savedEmployee.setEmployeeID(3);
        
        when(employeeRepository.save(any(Employee.class))).thenReturn(savedEmployee);

        // When & Then
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEmployee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeID").value(3))
                .andExpect(jsonPath("$.firstName").value("Bob"))
                .andExpect(jsonPath("$.lastName").value("Johnson"))
                .andExpect(jsonPath("$.salary").value(55000.0))
                .andExpect(jsonPath("$.missedDays").value(2));

        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployee_WhenEmployeeExists_ShouldReturnUpdatedEmployee() throws Exception {
        // Given
        Employee updatedData = new Employee("John", "Updated", 75000.0, 10);
        testEmployee1.setFirstName("John");
        testEmployee1.setLastName("Updated");
        testEmployee1.setSalary(75000.0);
        testEmployee1.setMissedDays(10);
        
        when(employeeRepository.findById(1)).thenReturn(Optional.of(testEmployee1));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee1);

        // When & Then
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Updated"))
                .andExpect(jsonPath("$.salary").value(75000.0))
                .andExpect(jsonPath("$.missedDays").value(10));

        verify(employeeRepository, times(1)).findById(1);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void updateEmployee_WhenEmployeeDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        Employee updatedData = new Employee("John", "Updated", 75000.0, 10);
        when(employeeRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/employees/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isNotFound());

        verify(employeeRepository, times(1)).findById(999);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteEmployee_WhenEmployeeExists_ShouldReturnOk() throws Exception {
        // Given
        when(employeeRepository.existsById(1)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1);

        // When & Then
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isOk());

        verify(employeeRepository, times(1)).existsById(1);
        verify(employeeRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteEmployee_WhenEmployeeDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(employeeRepository.existsById(999)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/employees/999"))
                .andExpect(status().isNotFound());

        verify(employeeRepository, times(1)).existsById(999);
        verify(employeeRepository, never()).deleteById(anyInt());
    }
}

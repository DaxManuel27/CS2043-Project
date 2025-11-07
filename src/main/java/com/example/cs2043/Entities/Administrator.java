package com.example.cs2043.Entities;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
// REMOVED @Table annotation - it inherits from Employee's table
@Getter
@Setter
@NoArgsConstructor
public class Administrator extends Employee {

    public Administrator(String firstName, String lastName, Double salary, LocalDate dob) {
        super(firstName, lastName, salary, dob, "Admin");
    }

    public void addEmployee(Employee e) {
        //later
    }

    public void editFullEmployee(Employee e, String firstName, String lastName, double salary, int employeeID) {
        //add later
    }

    public void editSalary(Employee e, double salary) {
        //add later lol
    }

    public void editFirstName(Employee e, String firstName) {
        //placeholder
    }

    public void editLastName(Employee e, String lastName) {
        //placeholder
    }

    public void editMissedDays(Employee e, int missedDays) {
        //placeholder
    }

    public void approveLeave(Employee e, boolean approved) {
        //placeholder
    }
}
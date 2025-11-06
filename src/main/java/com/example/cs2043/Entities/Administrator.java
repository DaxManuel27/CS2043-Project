package com.example.cs2043.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administrators")
@Getter
@Setter
@NoArgsConstructor
public class Administrator extends Employee {

    public Administrator(String firstName, String lastName, double missedDays, LeaveRequest request) {
        super(firstName, lastName, missedDays, request);
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

package com.example.cs2043.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("ADMIN")
@Getter
@Setter
@NoArgsConstructor
public class Administrator extends Employee {

    public Administrator(String firstName, String lastName, double salary, int missedDays, LeaveRequest request) {
        super(firstName, lastName, salary, missedDays, request);
    }
    //most of these are temporary placeholders for now untill we set up controllers for the front end integration

    public void editFullEmployee(Employee e, String firstName, String lastName, double salary, int missedDays) {
        e.setFirstName(firstName);
        e.setLastName(lastName);
        e.setSalary(salary);
        e.setMissedDays(missedDays);
    }

    public void editSalary(Employee e, double salary) {
        e.setSalary(salary);
    }

    public void editFirstName(Employee e, String firstName) {
        e.setFirstName(firstName);
    }

    public void editLastName(Employee e, String lastName) {
        e.setLastName(lastName);
    }

    public void editMissedDays(Employee e, int missedDays) {
        e.setMissedDays(missedDays);
    }

    public void approveLeave(Employee e, boolean approved) {
        e.getLeaveRequest().setApproved(approved);
    }
}

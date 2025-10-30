import java.time.LocalDate;

public class Employee {
    private String firstName;
    private String lastName;
    private static int employeeIDCounter = 1000;
    private int employeeID;
    private double missedDays;
    private LeaveRequest leaveRequest;

    public Employee(String firstName, String lastName, 
        double missedDays, LeaveRequest request) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.missedDays = missedDays;
            this.leaveRequest = leaveRequest;
            this.employeeID = employeeIDCounter++;
    }

    public void displayInfo() {
        //some implementation
    }

    public LeaveRequest requestLeave(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        return null;
    }

}
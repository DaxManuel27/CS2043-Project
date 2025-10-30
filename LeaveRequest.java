import java.time.LocalDate;


public class LeaveRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean approved;
    private int totalDays;

    public LeaveRequest(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.approved = false;
        this.totalDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isApproved() {
        return approved;
    }

    public int getTotalDays() {
        return totalDays;
    }
}

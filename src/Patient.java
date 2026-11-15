public class Patient {
    private int tokenNumber;
    private String name;
    private int age;
    private String priority; // {"EMERGENCY", "SENIOR", "NORMAL"}
    private String doctorId;
    private String status;   // {"WAITING", "IN_CONSULTATION", "COMPLETED", "SKIPPED"}
    private int skippedTurns;
    private double feeCharged;
    private boolean recalled;

    public Patient(int tokenNumber, String name, int age, String priority, String doctorId, double feeCharged) {
        this.tokenNumber = tokenNumber;
        this.name = name;
        this.age = age;
        this.priority = priority;
        this.doctorId = doctorId;
        this.status = "WAITING";
        this.skippedTurns = 0;
        this.feeCharged = feeCharged;
        this.recalled = false;
    }

    public int getTokenNumber() { return tokenNumber; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getPriority() { return priority; }
    public String getDoctorId() { return doctorId; }
    public String getStatus() { return status; }
    public int getSkippedTurns() { return skippedTurns; }
    public double getFeeCharged() { return feeCharged; }
    public boolean isRecalled() { return recalled; }

    public void setStatus(String status) { this.status = status; }
    public void setRecalled(boolean recalled) { this.recalled = recalled; }
    public void incrementSkippedTurns() { this.skippedTurns++; }

    @Override
    public String toString() {
        String recallTag = recalled ? " [RECALLED]" : "";
        return String.format("Token #%d | %s (Age: %d) | Priority: %s%s | Doctor: %s | Fee: Rs.%.2f | Status: %s",
                tokenNumber, name, age, priority, recallTag, doctorId, feeCharged, status);
    }
}
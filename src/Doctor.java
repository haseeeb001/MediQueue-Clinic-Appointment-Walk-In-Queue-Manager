public class Doctor {
    private String id;
    private String name;
    private String department;
    private String roomNumber;
    private int avgConsultMinutes;
    private double baseFee;
    private Patient currentPatient;
    private int patientsSeen;
    private int totalWaitMinutes;

    public Doctor(String id, String name, String department, String roomNumber, int avgConsultMinutes, double baseFee) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.roomNumber = roomNumber;
        this.avgConsultMinutes = avgConsultMinutes;
        this.baseFee = baseFee;
        this.currentPatient = null;
        this.patientsSeen = 0;
        this.totalWaitMinutes = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public String getRoomNumber() { return roomNumber; }
    public int getAvgConsultMinutes() { return avgConsultMinutes; }
    public double getBaseFee() { return baseFee; }
    public Patient getCurrentPatient() { return currentPatient; }
    public int getPatientsSeen() { return patientsSeen; }
    public int getTotalWaitMinutes() { return totalWaitMinutes; }

    public void setCurrentPatient(Patient currentPatient) {
        this.currentPatient = currentPatient;
    }

    public void recordPatientServed(int waitMinutes) {
        this.patientsSeen++;
        this.totalWaitMinutes += waitMinutes;
    }
}
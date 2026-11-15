import java.util.ArrayList;
import java.util.List;

public class ClinicManager {
    private List<Doctor> doctors;
    private List<Patient> allPatients;
    private int nextTokenNumber;
    private static final int STARVATION_LIMIT = 3; // Normal patients get promoted after being skipped 3 times

    public ClinicManager() {
        this.doctors = new ArrayList<>();
        this.allPatients = new ArrayList<>();
        this.nextTokenNumber = 101;
        seedClinicStaff();
    }

    private void seedClinicStaff() {
        doctors.add(new Doctor("D1", "Dr. Rajesh Kumar", "General Medicine", "Room 101", 10, 500.0));
        doctors.add(new Doctor("D2", "Dr. Ananya Nair", "General Medicine", "Room 102", 10, 500.0));
        doctors.add(new Doctor("D3", "Dr. Vikram Shetty", "Orthopedics", "Room 103", 15, 600.0));
        doctors.add(new Doctor("D4", "Dr. Meera Joshi", "Orthopedics", "Room 104", 15, 600.0));
        doctors.add(new Doctor("D5", "Dr. Sneha Rao", "Pediatrics", "Room 105", 12, 500.0));
        doctors.add(new Doctor("D6", "Dr. Karthik Menon", "Dermatology", "Room 106", 10, 550.0));
        doctors.add(new Doctor("T1", "Mr. Suresh Babu", "X-Ray & Scanning", "Lab 01", 15, 800.0));
    }

    public List<Doctor> getDoctors() {
        return doctors;
    }

    public Doctor findDoctorById(String id) {
        for (Doctor d : doctors) {
            if (d.getId().equalsIgnoreCase(id.trim())) {
                return d;
            }
        }
        return null;
    }

    public Patient findPatientByToken(int tokenNumber) {
        for (Patient p : allPatients) {
            if (p.getTokenNumber() == tokenNumber) {
                return p;
            }
        }
        return null;
    }

    public int getWaitingCount(String doctorId) {
        int count = 0;
        for (Patient p : allPatients) {
            if (p.getDoctorId().equalsIgnoreCase(doctorId) && p.getStatus().equals("WAITING")) {
                count++;
            }
        }
        return count;
    }

    public List<Doctor> getDoctorsByDepartment(String department) {
        List<Doctor> result = new ArrayList<>();
        for (Doctor d : doctors) {
            if (d.getDepartment().equalsIgnoreCase(department.trim())) {
                result.add(d);
            }
        }
        return result;
    }

    public Doctor getShortestQueueDoctor(String department) {
        List<Doctor> deptDoctors = getDoctorsByDepartment(department);
        if (deptDoctors.isEmpty()) return null;

        Doctor best = deptDoctors.get(0);
        int minQueue = getWaitingCount(best.getId());

        for (Doctor d : deptDoctors) {
            int qSize = getWaitingCount(d.getId());
            if (qSize < minQueue) {
                minQueue = qSize;
                best = d;
            }
        }
        return best;
    }

    public double calculateFee(Doctor doctor, int age, boolean isEmergency) {
        if (isEmergency && age < 60) {
            return doctor.getBaseFee() * 1.20; // 20% surcharge for non-senior emergency
        }
        return doctor.getBaseFee();
    }

    // Core Feature 1: Register patient & issue token
    public Patient registerPatient(String name, int age, boolean isEmergency, String doctorId) {
        Doctor doc = findDoctorById(doctorId);
        if (doc == null) return null;

        String priority;
        if (isEmergency) {
            priority = "EMERGENCY";
        } else if (age >= 60) {
            priority = "SENIOR";
        } else {
            priority = "NORMAL";
        }

        double fee = calculateFee(doc, age, isEmergency);
        Patient patient = new Patient(nextTokenNumber++, name, age, priority, doc.getId(), fee);
        allPatients.add(patient);
        return patient;
    }

    // Helper: Select next patient according to priority + recalled + anti-starvation without modifying state
    public Patient peekNextPatient(String doctorId) {
        List<Patient> waiting = new ArrayList<>();
        for (Patient p : allPatients) {
            if (p.getDoctorId().equalsIgnoreCase(doctorId) && p.getStatus().equals("WAITING")) {
                waiting.add(p);
            }
        }
        if (waiting.isEmpty()) return null;

        // 1. Emergency priority
        for (Patient p : waiting) {
            if (p.getPriority().equals("EMERGENCY")) {
                return p;
            }
        }

        // 2. Recalled patients (missed their call earlier and notified reception upon return)
        for (Patient p : waiting) {
            if (p.isRecalled()) {
                return p;
            }
        }

        // 3. Anti-starvation check: any NORMAL patient skipped >= STARVATION_LIMIT times
        for (Patient p : waiting) {
            if (p.getPriority().equals("NORMAL") && p.getSkippedTurns() >= STARVATION_LIMIT) {
                return p;
            }
        }

        // 4. Senior Citizen priority
        for (Patient p : waiting) {
            if (p.getPriority().equals("SENIOR")) {
                return p;
            }
        }

        // 5. Normal FIFO
        return waiting.get(0);
    }

    // Core Features 2 & 3: Confirm and admit the next present patient into consultation
    public Patient callNextPatient(String doctorId) {
        Doctor doc = findDoctorById(doctorId);
        if (doc == null) return null;

        if (doc.getCurrentPatient() != null) {
            doc.getCurrentPatient().setStatus("COMPLETED");
            doc.setCurrentPatient(null);
        }

        Patient next = peekNextPatient(doctorId);
        if (next == null) {
            return null;
        }

        // Estimate how long this patient waited before being called
        int waitedMinutes = doc.getPatientsSeen() == 0 ? 0 : next.getSkippedTurns() * doc.getAvgConsultMinutes();

        // Increment skippedTurns for NORMAL patients still waiting behind this call
        for (Patient p : allPatients) {
            if (p.getDoctorId().equalsIgnoreCase(doctorId)
                    && p.getStatus().equals("WAITING")
                    && p.getTokenNumber() != next.getTokenNumber()
                    && p.getPriority().equals("NORMAL")) {
                p.incrementSkippedTurns();
            }
        }

        next.setStatus("IN_CONSULTATION");
        next.setRecalled(false); // Clear recall flag once admitted
        doc.setCurrentPatient(next);
        doc.recordPatientServed(waitedMinutes);
        return next;
    }

    // Stretch Feature 1A: Mark absent patient as skipped
    public void markPatientSkipped(Patient p) {
        if (p != null) {
            p.setStatus("SKIPPED");
        }
    }

    // Stretch Feature 1B: Recall a skipped patient so they go right after the present patient
    public Patient recallSkippedPatient(int tokenNumber) {
        Patient p = findPatientByToken(tokenNumber);
        if (p != null && p.getStatus().equals("SKIPPED")) {
            p.setStatus("WAITING");
            p.setRecalled(true);
            return p;
        }
        return null;
    }

    // Core Feature 4 & Stretch Feature 2: Live queue position and estimated wait time
    public int getLiveQueuePosition(int tokenNumber) {
        Patient target = findPatientByToken(tokenNumber);
        if (target == null || !target.getStatus().equals("WAITING")) return -1;

        int aheadCount = 0;
        for (Patient p : allPatients) {
            if (p.getDoctorId().equals(target.getDoctorId())
                    && p.getStatus().equals("WAITING")
                    && p.getTokenNumber() != target.getTokenNumber()) {
                if (hasHigherEffectivePriority(p, target)) {
                    aheadCount++;
                }
            }
        }
        return aheadCount + 1;
    }

    private boolean hasHigherEffectivePriority(Patient candidate, Patient target) {
        int rankA = getPriorityRank(candidate);
        int rankB = getPriorityRank(target);
        if (rankA != rankB) {
            return rankA < rankB; // Lower rank number = higher priority
        }
        return candidate.getTokenNumber() < target.getTokenNumber();
    }

    private int getPriorityRank(Patient p) {
        if (p.getPriority().equals("EMERGENCY")) return 0;
        if (p.isRecalled()) return 1;
        if (p.getPriority().equals("NORMAL") && p.getSkippedTurns() >= STARVATION_LIMIT) return 2;
        if (p.getPriority().equals("SENIOR")) return 3;
        return 4;
    }

    public int getEstimatedWaitMinutes(int tokenNumber) {
        Patient target = findPatientByToken(tokenNumber);
        if (target == null || !target.getStatus().equals("WAITING")) return -1;

        Doctor doc = findDoctorById(target.getDoctorId());
        int pos = getLiveQueuePosition(tokenNumber);
        int activeConsultationOffset = (doc.getCurrentPatient() != null) ? 1 : 0;
        return (pos - 1 + activeConsultationOffset) * doc.getAvgConsultMinutes();
    }

    // Door Display Board
    public void printDoorDisplays() {
        System.out.println("\n================ MEDICARE CLINIC DOOR DISPLAYS ================");
        System.out.printf("%-6s | %-18s | %-10s | %-15s | %-18s%n",
                "ID", "Doctor/Tech", "Room", "NOW SERVING", "NEXT UP");
        System.out.println("--------------------------------------------------------------------");
        for (Doctor d : doctors) {
            String nowServing = (d.getCurrentPatient() != null)
                    ? "Token #" + d.getCurrentPatient().getTokenNumber()
                    : "None";
            Patient next = peekNextPatient(d.getId());
            String nextUp;
            if (next != null) {
                String tag = next.isRecalled() ? "RECALLED" : String.valueOf(next.getPriority().charAt(0));
                nextUp = "Token #" + next.getTokenNumber() + " (" + tag + ")";
            } else {
                nextUp = "None";
            }
            System.out.printf("%-6s | %-18s | %-10s | %-15s | %-18s%n",
                    d.getId(), d.getName(), d.getRoomNumber(), nowServing, nextUp);
        }
        System.out.println("====================================================================\n");
    }

    // Core Feature 5: Day Summary
    public void printDaySummary() {
        System.out.println("\n==================== END-OF-DAY CLINIC SUMMARY ====================");
        System.out.printf("%-6s | %-18s | %-18s | %-13s | %-12s%n",
                "ID", "Name", "Department", "Patients Seen", "Avg Wait");
        System.out.println("---------------------------------------------------------------------");
        for (Doctor d : doctors) {
            double avgWait = (d.getPatientsSeen() == 0)
                    ? 0.0
                    : (double) d.getTotalWaitMinutes() / d.getPatientsSeen();
            System.out.printf("%-6s | %-18s | %-18s | %-13d | %.1f mins%n",
                    d.getId(), d.getName(), d.getDepartment(), d.getPatientsSeen(), avgWait);
        }
        System.out.println("=====================================================================\n");
    }
}
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ClinicManager manager = new ClinicManager();

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("  Welcome to  Bangalore Baptist Hospital");
        System.out.println("  MediQueue Reception & Queue System");
        System.out.print("============================================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Select an option: ");

            switch (choice) {
                case 1 -> handleWalkInRegistration();
                case 2 -> handleXRayReferral();
                case 3 -> handleCallNextPatient();
                case 4 -> manager.printDoorDisplays();
                case 5 -> handleCheckWaitTime();
                case 6 -> handleRecallPatient();
                case 7 -> manager.printDaySummary();
                case 0 -> {
                    running = false;
                    System.out.println("Closing MediQueue Reception System. Goodbye!");
                }
                default -> System.out.println("Invalid option. Please choose between 0 and 7.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n--- RECEPTION DESK MENU ---");
        System.out.println("1. Register Walk-In Patient & Issuing Token");
        System.out.println("2. Referring Existing Patient to X-Ray & Scanning (T1)");
        System.out.println("3. Call Next Patient for Doctor / Lab Technician");
        System.out.println("4. View Room Door Display Screen (Now Serving & Next Up)");
        System.out.println("5. Check Token Live Position & Estimated Wait Time");
        System.out.println("6. Recall Missed / Skipped Patient");
        System.out.println("7. Print Day Summary");
        System.out.println("0. Exit");
    }

    private static void handleWalkInRegistration() {
        System.out.print("Enter Patient Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        int age = readInt("Enter Patient Age: ");
        if (age <= 0 || age > 120) {
            System.out.println("Invalid age entered.");
            return;
        }

        System.out.println("\nAvailable Departments:");
        System.out.println("1. General Medicine (D1, D2)");
        System.out.println("2. Orthopedics (D3, D4)");
        System.out.println("3. Pediatrics (D5)");
        System.out.println("4. Dermatology (D6)");
        System.out.println("5. X-Ray & Scanning (T1)");
        int departmentChoice = readInt("Choose Department (1-5): ");

        String department = switch (departmentChoice) {
            case 1 -> "General Medicine";
            case 2 -> "Orthopedics";
            case 3 -> "Pediatrics";
            case 4 -> "Dermatology";
            case 5 -> "X-Ray & Scanning";
            default -> null;
        };

        if (department == null) {
            System.out.println("Invalid department choice.");
            return;
        }

        List<Doctor> deptDoctors = manager.getDoctorsByDepartment(department);
        System.out.println("\nDoctors in " + department + ":");
        for (Doctor d : deptDoctors) {
            int qCount = manager.getWaitingCount(d.getId());
            System.out.printf("  [%s] %s (%s) | Waiting: %d | Base Fee: Rs.%.2f%n",
                    d.getId(), d.getName(), d.getRoomNumber(), qCount, d.getBaseFee());
        }

        Doctor selectedDoctor;
        if (deptDoctors.size() > 1) {
            System.out.print("Enter Doctor ID (e.g., D3) or type AUTO for shortest queue: ");
            String docInput = scanner.nextLine().trim();
            if (docInput.equalsIgnoreCase("AUTO")) {
                selectedDoctor = manager.getShortestQueueDoctor(department);
                System.out.println("Auto-assigned to shortest queue: " + selectedDoctor.getName() + " (" + selectedDoctor.getId() + ")");
            } else {
                selectedDoctor = manager.findDoctorById(docInput);
                if (selectedDoctor == null || !selectedDoctor.getDepartment().equalsIgnoreCase(department)) {
                    System.out.println("Invalid Doctor ID for this department.");
                    return;
                }
            }
        } else {
            selectedDoctor = deptDoctors.get(0);
        }

        boolean isEmergency = askEmergencyWithFeeCheck(selectedDoctor, age);
        Patient token = manager.registerPatient(name, age, isEmergency, selectedDoctor.getId());

        System.out.println("\n>>> TOKEN ISSUED <<<");
        System.out.println(token);
        System.out.println("Proceed to: " + selectedDoctor.getRoomNumber());
        System.out.println("Estimated Wait Time: " + manager.getEstimatedWaitMinutes(token.getTokenNumber()) + " mins");
    }

    private static void handleXRayReferral() {
        int existingToken = readInt("Enter Patient's Existing Consultation Token Number: ");
        Patient existing = manager.findPatientByToken(existingToken);
        if (existing == null) {
            System.out.println("Token #" + existingToken + " is not found.");
            return;
        }

        Doctor xrayTech = manager.findDoctorById("T1");
        System.out.println("Referring Patient: " + existing.getName() + " (Age: " + existing.getAge() + ") to X-Ray & Scanning (Lab 01)");
        boolean isEmergency = askEmergencyWithFeeCheck(xrayTech, existing.getAge());

        Patient xrayToken = manager.registerPatient(existing.getName(), existing.getAge(), isEmergency, "T1");
        System.out.println("\n>>> X-RAY TOKEN ISSUED <<<");
        System.out.println(xrayToken);
        System.out.println("Estimated Waiting Time: " + manager.getEstimatedWaitMinutes(xrayToken.getTokenNumber()) + " mins");
    }

    private static boolean askEmergencyWithFeeCheck(Doctor doctor, int age) {
        System.out.print("Is this an EMERGENCY case? (Y/N): ");
        String ans = scanner.nextLine().trim();
        if (!ans.equalsIgnoreCase("Y")) {
            return false;
        }

        if (age >= 60) {
            System.out.printf("Senior Citizen Emergency Exemption applied! Fee remains Rs.%.2f%n", doctor.getBaseFee());
            return true;
        } else {
            double emergencyFee = doctor.getBaseFee() * 1.20;
            System.out.printf("Emergency fee includes a 20%% surcharge: Rs.%.2f (Normal fee: Rs.%.2f).%n",
                    emergencyFee, doctor.getBaseFee());
            System.out.print("Confirm Emergency booking at Rs." + String.format("%.2f", emergencyFee) + "? (Y for Emergency / N for Normal queue): ");
            String confirm = scanner.nextLine().trim();
            return confirm.equalsIgnoreCase("Y");
        }
    }

    private static void handleCallNextPatient() {
        System.out.print("Enter Doctor/Technician ID (D1-D6, T1): ");
        String docId = scanner.nextLine().trim();
        Doctor doc = manager.findDoctorById(docId);
        if (doc == null) {
            System.out.println("Invalid Doctor/Technician ID.");
            return;
        }

        // Complete the previous patient's consultation if someone was inside
        if (doc.getCurrentPatient() != null) {
            doc.getCurrentPatient().setStatus("COMPLETED");
            doc.setCurrentPatient(null);
        }

        while (true) {
            Patient candidate = manager.peekNextPatient(docId);
            if (candidate == null) {
                System.out.println("No waiting patients left for " + doc.getName() + ".");
                return;
            }

            System.out.printf("Calling Token #%d (%s - %s). Is the patient present? (Y/N): ",
                    candidate.getTokenNumber(), candidate.getName(), candidate.getPriority());
            String present = scanner.nextLine().trim();

            if (present.equalsIgnoreCase("Y")) {
                Patient called = manager.callNextPatient(docId);
                System.out.println("\n>>> NOW SERVING <<<");
                System.out.println(doc.getRoomNumber() + " (" + doc.getName() + ") -> Token #"
                        + called.getTokenNumber() + " - " + called.getName() + " [" + called.getPriority() + "]");
                return;
            } else {
                manager.markPatientSkipped(candidate);
                System.out.println("Token #" + candidate.getTokenNumber() + " marked as SKIPPED. Calling next patient...");
            }
        }
    }

    private static void handleCheckWaitTime() {
        int tokenNum = readInt("Enter Token Number: ");
        Patient p = manager.findPatientByToken(tokenNum);
        if (p == null) {
            System.out.println("Token #" + tokenNum + " not found.");
            return;
        }

        if (!p.getStatus().equals("WAITING")) {
            System.out.println("Token #" + tokenNum + " (" + p.getName() + ") status is currently: " + p.getStatus());
            return;
        }

        int pos = manager.getLiveQueuePosition(tokenNum);
        int wait = manager.getEstimatedWaitMinutes(tokenNum);
        System.out.printf("Token #%d (%s) | Doctor: %s | Live Queue Position: %d | Estimated Wait: %d mins%n",
                p.getTokenNumber(), p.getName(), p.getDoctorId(), pos, wait);
    }

    private static void handleRecallPatient() {
        int tokenNum = readInt("Enter Missed Patient's Token Number: ");
        Patient p = manager.recallSkippedPatient(tokenNum);
        if (p == null) {
            System.out.println("Token #" + tokenNum + " was not found in SKIPPED status.");
        } else {
            Doctor doc = manager.findDoctorById(p.getDoctorId());
            System.out.printf("Token #%d (%s) is back! Placed in NEXT UP for %s (%s).%n",
                    p.getTokenNumber(), p.getName(), doc.getName(), doc.getRoomNumber());
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }
}
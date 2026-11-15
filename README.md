# CP-021: MediQueue — Clinic Appointment & Walk-In Queue Manager

A console-based Java queue and token management system built for **MediCare Multispeciality Clinic, Bengaluru**, designed to replace manual paper slips with priority-aware scheduling, live room door displays, and automated wait-time estimation.

---

## Team Details
* **Project ID:** CP-021 (MediQueue — Foundation Level)
* **Domain:** MediCare Multispeciality Clinic, Bengaluru
* **Team Members:**
    1. Ahmed Abdul Haseeb — *[Enter USN]*
    2. *[Partner Name]* — *[Enter USN]*

---

## Project Architecture
The codebase is structured into four object-oriented Java classes inside the `src/` directory:
* **`Doctor.java`** — Models clinic doctors (`D1`–`D6`) and the X-Ray technician (`T1`), tracking room numbers, consultation durations, base fees, active patient status, and daily statistics.
* **`Patient.java`** — Represents an issued patient token (`101+`), storing priority tier (`EMERGENCY`, `SENIOR`, `NORMAL`), queue status (`WAITING`, `IN_CONSULTATION`, `COMPLETED`, `SKIPPED`), fee charged, and anti-starvation/recall flags.
* **`ClinicManager.java`** — Core scheduling engine managing staff initialization, shortest-queue doctor assignment, priority sorting, anti-starvation promotion, wait-time calculation, door displays, and day summaries.
* **`Main.java`** — Interactive reception desk console menu with input validation and boundary checks.

---

## Clinic Staffing & Departments
| ID | Name | Department | Room / Lab | Avg Consult | Base Fee |
|---|---|---|---|---|---|
| `D1` | Dr. Rajesh Kumar | General Medicine | Room 101 | 10 mins | Rs. 500.00 |
| `D2` | Dr. Ananya Nair | General Medicine | Room 102 | 10 mins | Rs. 500.00 |
| `D3` | Dr. Vikram Shetty | Orthopedics | Room 103 | 15 mins | Rs. 600.00 |
| `D4` | Dr. Meera Joshi | Orthopedics | Room 104 | 15 mins | Rs. 600.00 |
| `D5` | Dr. Sneha Rao | Pediatrics | Room 105 | 12 mins | Rs. 500.00 |
| `D6` | Dr. Karthik Menon | Dermatology | Room 106 | 10 mins | Rs. 550.00 |
| `T1` | Mr. Suresh Babu | X-Ray & Scanning | Lab 01 | 15 mins | Rs. 800.00 |

---

## Working Features
1. **Walk-In Registration & Auto-Token Generation:** Issues sequential tokens (`#101`, `#102`, ...) with automatic `SENIOR` classification for patients aged 60 and above.
2. **Shortest-Queue Department Auto-Assignment (`AUTO`):** Evaluates waiting counts across doctors in multi-doctor departments (General Medicine, Orthopedics) and assigns the doctor with the shortest queue.
3. **Priority Queue with Anti-Starvation:** Serves patients in strict priority order (`EMERGENCY` > `RECALLED` > Anti-Starvation Promoted `NORMAL` > `SENIOR` > `NORMAL`). Normal patients bypassed 3 times (`STARVATION_LIMIT = 3`) are automatically promoted so they never starve.
4. **Dynamic Emergency Surcharge & Senior Exemption:** Applies a 20% emergency surcharge for non-senior patients (`age < 60`) with an option to fall back to the normal queue at base price, while exempting senior citizens (`age >= 60`) from extra charges.
5. **Live Room Door Displays:** Displays `NOW SERVING` and `NEXT UP` tokens for all 7 rooms, dynamically updating when emergencies arrive or patients are recalled.
6. **Absent-Patient Skip & Recall Workflow:** Marks absent patients as `SKIPPED` when called and places them into `NEXT UP` (right behind emergencies) once they return and notify reception.
7. **Post-Consultation X-Ray Referral:** Reuses existing consultation token details to refer patients directly to X-Ray & Scanning (`T1`).
8. **Live Position, Wait-Time Estimator & End-of-Day Summary:** Calculates real-time queue position and estimated wait minutes, and prints total patients seen and average wait time per doctor at closing.

---

## How to Run

### Prerequisites
* Java Development Kit (**JDK 17** or higher)

### Using Terminal / Command Prompt
1. Clone or download this repository and navigate to the project root folder:
   ```bash
   cd MediQueue

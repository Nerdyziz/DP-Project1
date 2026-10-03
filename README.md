# Mercy General Hospital & Health Network - Hospital Management System (HMS)

A realistic, full-stack Hospital Management System (HMS) developed as a standard Java college lab project. It implements **all 7 Gang of Four (GoF) Design Patterns** (exceeding the required 5/7 minimum).

> [!NOTE]
> **Clean Separation of Concerns**:
> - **Frontend Web Portal (`http://localhost:8080`)**: Looks and functions 100% like a real-world, modern clinical hospital management system (EHR charts, triage queue, lab orders, staff directory, security audit log). **Zero design pattern jargon, meta-labels, or code snippets appear on the UI.**
> - **Pure Java Backend (`src/com/hospital/`)**: Each clinical module is driven strictly under the hood by its corresponding Gang of Four design pattern in textbook-compliant Java with zero external dependencies.

---

## 🏛️ Clinical Features & Backend Design Pattern Mapping

| Hospital Feature on Web Portal | Real-World Clinical Workflow | Underlying GoF Design Pattern | Key Java Implementation |
| :--- | :--- | :--- | :--- |
| **Electronic Health Records (EHR)** | Attending Dr. Smith accesses assigned patient Alice Henderson (#REC-101) seamlessly. When Dr. Smith attempts to open unassigned Bob Vance (#REC-102), a realistic **HIPAA Restricted Access** security barrier is triggered. | **Protection Proxy Pattern** | [`MedicalRecordProxy.java`](file:///c:/DP-Project1/src/com/hospital/proxy/MedicalRecordProxy.java)<br>[`RealMedicalRecordService.java`](file:///c:/DP-Project1/src/com/hospital/proxy/RealMedicalRecordService.java) |
| **Emergency Triage & Intake** | Walk-in patient acuity routing: General Query $\rightarrow$ Admissions Desk; Routine Care $\rightarrow$ Triage Nurse; Acute Fever/Pain $\rightarrow$ Attending Physician; Critical Cardiac Trauma $\rightarrow$ ICU Specialist. | **Chain of Responsibility Pattern** | [`RequestHandler.java`](file:///c:/DP-Project1/src/com/hospital/chain/RequestHandler.java)<br>[`ReceptionistHandler.java`](file:///c:/DP-Project1/src/com/hospital/chain/ReceptionistHandler.java)<br>[`NurseHandler.java`](file:///c:/DP-Project1/src/com/hospital/chain/NurseHandler.java)<br>[`DoctorHandler.java`](file:///c:/DP-Project1/src/com/hospital/chain/DoctorHandler.java)<br>[`SpecialistHandler.java`](file:///c:/DP-Project1/src/com/hospital/chain/SpecialistHandler.java) |
| **Clinical Lab Orders & Alerts** | Finalizing pathology orders automatically dispatches real-time SMS alerts to patient devices and clinical beeper alerts to physician consoles. | **Observer Pattern** | [`LabService.java`](file:///c:/DP-Project1/src/com/hospital/observer/LabService.java) (Subject)<br>[`PatientObserver.java`](file:///c:/DP-Project1/src/com/hospital/observer/PatientObserver.java)<br>[`DoctorObserver.java`](file:///c:/DP-Project1/src/com/hospital/observer/DoctorObserver.java) |
| **Medical Document Generator** | Exporting patient records (Lab Assays, Discharge Summaries, Prescriptions) dynamically across hospital formats (Official PDF, Web HTML, EHR Interoperability JSON). | **Bridge Pattern** | [`Report.java`](file:///c:/DP-Project1/src/com/hospital/bridge/Report.java) (Abstraction)<br>[`ReportFormatter.java`](file:///c:/DP-Project1/src/com/hospital/bridge/ReportFormatter.java) (Implementor)<br>[`PdfFormatter.java`](file:///c:/DP-Project1/src/com/hospital/bridge/PdfFormatter.java)<br>[`HtmlFormatter.java`](file:///c:/DP-Project1/src/com/hospital/bridge/HtmlFormatter.java)<br>[`JsonFormatter.java`](file:///c:/DP-Project1/src/com/hospital/bridge/JsonFormatter.java) |
| **Department Diagnostic Suites** | Specialized departments (Radiology & Imaging vs. Cardiovascular Medicine) utilizing strictly matched families of Diagnostic Tools, Study Reports, and Legal Informed Consent forms. | **Abstract Factory Pattern** | [`DepartmentEquipmentFactory.java`](file:///c:/DP-Project1/src/com/hospital/abstractfactory/DepartmentEquipmentFactory.java)<br>[`RadiologyEquipmentFactory.java`](file:///c:/DP-Project1/src/com/hospital/abstractfactory/RadiologyEquipmentFactory.java)<br>[`CardiologyEquipmentFactory.java`](file:///c:/DP-Project1/src/com/hospital/abstractfactory/CardiologyEquipmentFactory.java) |
| **Medical Personnel Onboarding** | Admin onboards new Doctors, Registered Nurses, and Imaging Technicians with polymorphically assigned duty rosters and salary compensation. | **Factory Method Pattern** | [`StaffFactory.java`](file:///c:/DP-Project1/src/com/hospital/factory/StaffFactory.java)<br>[`DoctorFactory.java`](file:///c:/DP-Project1/src/com/hospital/factory/DoctorFactory.java)<br>[`NurseFactory.java`](file:///c:/DP-Project1/src/com/hospital/factory/NurseFactory.java)<br>[`TechnicianFactory.java`](file:///c:/DP-Project1/src/com/hospital/factory/TechnicianFactory.java)<br>[`StaffFactoryProvider.java`](file:///c:/DP-Project1/src/com/hospital/factory/StaffFactoryProvider.java) |
| **Hospital Compliance Audit Log** | Centralized, thread-safe compliance registry recording all electronic protected health info (ePHI) accesses, unauthorized security violations, triage intakes, and lab dispatches. | **Singleton Pattern** | [`AuditLogger.java`](file:///c:/DP-Project1/src/com/hospital/singleton/AuditLogger.java) (Double-Checked Locking)<br>[`HospitalConfig.java`](file:///c:/DP-Project1/src/com/hospital/singleton/HospitalConfig.java) |

---

## 🚀 How to Run

### Method 1: 1-Click Launch (Web Portal & Browser)
Double-click [`run-web.bat`](file:///c:/DP-Project1/run-web.bat).  
It compiles all source files, starts the server, and automatically opens `http://localhost:8080` in your web browser!

### Method 2: Dual Web + Interactive Terminal Menu
Double-click [`run.bat`](file:///c:/DP-Project1/run.bat) or run from PowerShell:
```powershell
javac -encoding UTF-8 -d bin src\com\hospital\model\*.java src\com\hospital\singleton\*.java src\com\hospital\factory\*.java src\com\hospital\abstractfactory\*.java src\com\hospital\bridge\*.java src\com\hospital\chain\*.java src\com\hospital\observer\*.java src\com\hospital\proxy\*.java src\com\hospital\*.java
java -cp bin com.hospital.Main
```
- Open `http://localhost:8080` in Chrome, Edge, or Firefox.
- In terminal, select options `[1-7]` to run individual pattern unit demos, or option `[8]` to run the full automated sequence.

### Method 3: Automated CLI Verification (All Patterns)
```powershell
java -cp bin com.hospital.Main auto
```
Executes all 7 GoF design patterns in under 1 second with full console tracing.

---

## 🖥️ Web Portal Clinical Walkthrough

When you open `http://localhost:8080`, you will see:

1. **Hospital Dashboard & Census**:
   - Inpatients count, bed occupancy (88%), on-duty staff, pending triage cases, live clinical operations feed.
2. **Patient Charts (EHR) & HIPAA Access Control**:
   - Switch active clinician in top right between `Dr. Robert Smith (Cardiology)` and `Emily Clark (Admissions)` or `Dr. Gregory Jones`.
   - Opening Alice Henderson (#REC-101) succeeds for Dr. Smith.
   - Opening Bob Vance (#REC-102) while logged in as Dr. Smith immediately triggers the **HIPAA Security Restriction Alert** (intercepted by the backend `MedicalRecordProxy`).
3. **Emergency Intake & Triage Pipeline**:
   - Enter a walk-in patient name and complaint with acuity level.
   - Submitting routes the request through the clinical chain (`Receptionist` $\rightarrow$ `Nurse` $\rightarrow$ `Doctor` $\rightarrow$ `Specialist`).
4. **Clinical Laboratories & Document Export**:
   - Finalize a lab test: triggers real-time alerts dispatched to the patient SMS and doctor beeper.
   - Generate reports: choose Lab Assay, Discharge Summary, or Prescription across PDF, HTML, or JSON.
5. **Specialized Department Diagnostic Suites**:
   - Switch between Radiology and Cardiology to run calibrated equipment, generate study reports, and view informed consent protocols.
6. **Medical Staff Directory & Onboarding**:
   - View on-duty personnel and onboard new Physicians, Nurses, or Technicians with automatic salary calculation.
7. **Security & Compliance Audit Registry**:
   - Live stream of all hospital access events, breaches, and clinical transactions backed by the `AuditLogger` singleton.

---

## 📁 Package Directory Structure

```
c:\DP-Project1\
├── run.bat                          # 1-Click compiler & runner (Web + CLI)
├── run-web.bat                      # 1-Click launcher opening browser immediately
├── compile.bat                      # 1-Click compiler
├── README.md                        # Project documentation
├── VIVA_QUESTIONS.md                # Viva voce examination prep guide
└── src/
    └── com/hospital/
        ├── Main.java                # Main entry point & interactive menu
        ├── WebServer.java           # Built-in REST API & realistic Single-Page Web App
        ├── model/                   # Domain entities (User, Patient, Role, Record, Request)
        ├── singleton/               # 1. SINGLETON (AuditLogger, HospitalConfig)
        ├── factory/                 # 2. FACTORY METHOD (Staff, Doctor, Nurse, Tech + Factories)
        ├── abstractfactory/         # 3. ABSTRACT FACTORY (DepartmentEquipmentFactory suites)
        ├── bridge/                  # 4. BRIDGE (Report abstraction + PDF/HTML/JSON formatters)
        ├── chain/                   # 5. CHAIN OF RESPONSIBILITY (Triage escalation pipeline)
        ├── observer/                # 6. OBSERVER (LabService Subject -> Patient, Doctor observers)
        └── proxy/                   # 7. PROXY (MedicalRecordProxy protection proxy)
```

---

## 🎓 Viva Voce Exam Preparation
Before your lab evaluation, review [`VIVA_QUESTIONS.md`](file:///c:/DP-Project1/VIVA_QUESTIONS.md) for 25+ standard examiner questions covering pattern classifications, intent, real-world justification, and design pattern trade-offs.

package com.hospital;

import com.hospital.abstractfactory.*;
import com.hospital.bridge.*;
import com.hospital.chain.*;
import com.hospital.factory.*;
import com.hospital.model.*;
import com.hospital.observer.*;
import com.hospital.proxy.*;
import com.hospital.singleton.*;

import java.util.Scanner;

/**
 * =====================================================================
 * HOSPITAL MANAGEMENT SYSTEM - DESIGN PATTERNS COLLEGE LAB PROJECT
 * =====================================================================
 * Demonstrates all 7 Core Gang of Four (GoF) Design Patterns:
 *  1. Singleton Pattern (AuditLogger & HospitalConfig)
 *  2. Factory Method Pattern (StaffFactory hierarchy)
 *  3. Abstract Factory Pattern (DepartmentEquipmentFactory families)
 *  4. Bridge Pattern (Report Abstraction & Formatter Implementors)
 *  5. Chain of Responsibility Pattern (Patient Triage Routing)
 *  6. Observer Pattern (Clinical Event Notification)
 *  7. Proxy Pattern (Protection Proxy for Medical Records)
 * =====================================================================
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Automatically start the built-in web server so browser portal is immediately live
        try {
            WebServer.start(8080);
        } catch (Exception e) {
            System.err.println("Note: WebServer could not bind to port 8080 (may already be in use): " + e.getMessage());
        }

        // If passed "auto" argument, run all demos automatically and exit
        if (args.length > 0 && (args[0].equalsIgnoreCase("auto") || args[0].equalsIgnoreCase("--auto"))) {
            runAllDemonstrations();
            WebServer.stop();
            System.exit(0);
        }

        printBanner();

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Select an option [0-9]: ");
            String input = scanner.nextLine().trim();

            System.out.println();
            switch (input) {
                case "1" -> demoSingleton();
                case "2" -> demoFactoryMethod();
                case "3" -> demoAbstractFactory();
                case "4" -> demoBridge();
                case "5" -> demoChainOfResponsibility();
                case "6" -> demoObserver();
                case "7" -> demoProxy();
                case "8" -> runAllDemonstrations();
                case "9" -> startWebDemo();
                case "0" -> {
                    System.out.println("Exiting Hospital Management System. Thank you!");
                    WebServer.stop();
                    System.exit(0);
                }
                default -> System.out.println("Invalid option. Please enter a number between 0 and 9.");
            }

            if (running) {
                System.out.println("\nPress ENTER to return to main menu...");
                scanner.nextLine();
            }
        }
    }

    private static void printBanner() {
        System.out.println("======================================================================");
        System.out.println("     MERCY GENERAL HOSPITAL MANAGEMENT SYSTEM - COLLEGE LAB DEMO     ");
        System.out.println("           Featuring 7 Gang of Four (GoF) Design Patterns             ");
        System.out.println("======================================================================");
        System.out.println("  >>> WEB PORTAL RUNNING: http://localhost:8080 <<<                   ");
        System.out.println("  Open your browser to view the realistic Hospital Management Portal. ");
        System.out.println("======================================================================");
    }

    private static void printMenu() {
        System.out.println("\n----------------------------- LAB MENU -------------------------------");
        System.out.println(" [1] Pattern 1: SINGLETON (AuditLogger & HospitalConfig)");
        System.out.println(" [2] Pattern 2: FACTORY METHOD (Staff Onboarding Hierarchy)");
        System.out.println(" [3] Pattern 3: ABSTRACT FACTORY (Department Equipment & Protocols)");
        System.out.println(" [4] Pattern 4: BRIDGE (Medical Reports x PDF / HTML / JSON Formats)");
        System.out.println(" [5] Pattern 5: CHAIN OF RESPONSIBILITY (Triage Escalation Pipeline)");
        System.out.println(" [6] Pattern 6: OBSERVER (Clinical Event Notification System)");
        System.out.println(" [7] Pattern 7: PROXY (EHR Protection Proxy & Access Control)");
        System.out.println(" [8] RUN FULL AUTOMATED DEMO (All 7 Patterns in sequence)");
        System.out.println(" [9] START WEB DEMO (View in Browser at http://localhost:8080)");
        System.out.println(" [0] Exit Application");
        System.out.println("----------------------------------------------------------------------");
    }

    // =================================================================
    // 1. SINGLETON DEMO
    // =================================================================
    public static void demoSingleton() {
        System.out.println("======================================================================");
        System.out.println(">>> DEMO 1: SINGLETON PATTERN (Creational)");
        System.out.println("Role: Centralized, thread-safe audit logging & hospital configuration.");
        System.out.println("======================================================================");

        System.out.println("\nStep 1: Requesting instance 1 of AuditLogger...");
        AuditLogger logger1 = AuditLogger.getInstance();

        System.out.println("Step 2: Requesting instance 2 of AuditLogger...");
        AuditLogger logger2 = AuditLogger.getInstance();

        System.out.println("\nVerification of Singleton Identity:");
        System.out.println("  logger1 HashCode: " + System.identityHashCode(logger1));
        System.out.println("  logger2 HashCode: " + System.identityHashCode(logger2));
        boolean same = (logger1 == logger2);
        System.out.println("  Are both references identical (logger1 == logger2)? " + (same ? "YES (PASSED)" : "NO (FAILED)"));

        System.out.println("\nStep 3: Logging sensitive operations through Singleton instance:");
        logger1.log("Dr. Smith", "LOGIN_SUCCESS", "Hospital Main Console");
        logger2.log("Nurse Sarah", "ADMINISTER_MED", "Patient Alice (Bed 4B)");

        System.out.println("\nTotal entries in single audit log: " + logger1.getLogEntries().size());

        System.out.println("\nStep 4: Centralized Hospital Configuration Registry:");
        HospitalConfig config = HospitalConfig.getInstance();
        System.out.println("  Hospital Name: " + config.getConfig("hospitalName"));
        System.out.println("  Standard Consultation Fee: " + config.getConfig("standardConsultationFee"));
        System.out.println("  Emergency Hotline: " + config.getConfig("emergencyContact"));
    }

    // =================================================================
    // 2. FACTORY METHOD DEMO
    // =================================================================
    public static void demoFactoryMethod() {
        System.out.println("======================================================================");
        System.out.println(">>> DEMO 2: FACTORY METHOD PATTERN (Creational)");
        System.out.println("Role: Polymorphic onboarding of specialized healthcare personnel.");
        System.out.println("======================================================================");

        System.out.println("\nOnboarding staff using StaffFactoryProvider:\n");

        // 1. Create a Doctor using DoctorFactory
        StaffFactory doctorFactory = StaffFactoryProvider.getFactory("DOCTOR");
        Staff doctor = doctorFactory.registerStaff("Dr. Gregory House", "Internal Medicine & Diagnostics", "Department of Diagnostics");
        System.out.println("   -> Duty: " + doctor.getDuty());

        // 2. Create a Nurse using NurseFactory
        StaffFactory nurseFactory = StaffFactoryProvider.getFactory("NURSE");
        Staff nurse = nurseFactory.registerStaff("Sarah Jenkins", "Intensive Critical Care", "ICU Ward 2");
        System.out.println("   -> Duty: " + nurse.getDuty());

        // 3. Create a Technician using TechnicianFactory
        StaffFactory techFactory = StaffFactoryProvider.getFactory("TECHNICIAN");
        Staff technician = techFactory.registerStaff("Gordon Freeman", "Magnetic Resonance Imaging", "Radiology Imaging Suite");
        System.out.println("   -> Duty: " + technician.getDuty());

        System.out.println("\nBenefit Demonstrated: Client code calls createStaff() without knowing");
        System.out.println("concrete product classes. Each subclass encapsulates its own compensation and duties.");
    }

    // =================================================================
    // 3. ABSTRACT FACTORY DEMO
    // =================================================================
    public static void demoAbstractFactory() {
        System.out.println("======================================================================");
        System.out.println(">>> DEMO 3: ABSTRACT FACTORY PATTERN (Creational)");
        System.out.println("Role: Produces matched families of Diagnostic Tools, Reports & Consents.");
        System.out.println("======================================================================");

        // Suite 1: Radiology
        System.out.println("\n--- [SUITE 1: RADIOLOGY DEPARTMENT] ---");
        DepartmentEquipmentFactory radFactory = new RadiologyEquipmentFactory();
        DiagnosticTool radTool = radFactory.createDiagnosticTool();
        ReportGenerator radReport = radFactory.createReportGenerator();
        ConsentForm radConsent = radFactory.createConsentForm();

        System.out.println("1. Diagnostic Tool: " + radTool.getToolName());
        System.out.println("   Telemetry: " + radTool.runDiagnosticScan("PATIENT-001"));
        System.out.println("2. Legal Consent: " + radConsent.getFormTitle());
        System.out.println("   Required Disclosures: " + radConsent.getRequiredDisclosures());
        System.out.println("3. Report Sample:\n" + radReport.generateReport("Alice Henderson", "Clear lung fields, no acute infiltration."));

        // Suite 2: Cardiology
        System.out.println("\n--- [SUITE 2: CARDIOLOGY DEPARTMENT] ---");
        DepartmentEquipmentFactory cardFactory = new CardiologyEquipmentFactory();
        DiagnosticTool cardTool = cardFactory.createDiagnosticTool();
        ReportGenerator cardReport = cardFactory.createReportGenerator();
        ConsentForm cardConsent = cardFactory.createConsentForm();

        System.out.println("1. Diagnostic Tool: " + cardTool.getToolName());
        System.out.println("   Telemetry: " + cardTool.runDiagnosticScan("PATIENT-002"));
        System.out.println("2. Legal Consent: " + cardConsent.getFormTitle());
        System.out.println("   Required Disclosures: " + cardConsent.getRequiredDisclosures());
        System.out.println("3. Report Sample:\n" + cardReport.generateReport("Bob Vance", "Normal sinus rhythm, HR 72 bpm."));

        System.out.println("\nBenefit Demonstrated: Entire family of products changes together consistently.");
    }

    // =================================================================
    // 4. BRIDGE DEMO
    // =================================================================
    public static void demoBridge() {
        System.out.println("======================================================================");
        System.out.println(">>> DEMO 4: BRIDGE PATTERN (Structural)");
        System.out.println("Role: Decouples Report Abstraction from Formatter Implementors.");
        System.out.println("======================================================================");

        // Concrete Implementors
        ReportFormatter pdfFormatter = new PdfFormatter();
        ReportFormatter htmlFormatter = new HtmlFormatter();
        ReportFormatter jsonFormatter = new JsonFormatter();

        // Abstraction: LabReport
        LabReport labReport = new LabReport(
            pdfFormatter, "Alice Henderson",
            "Serum Troponin I & Lipid Panel", "0.01 ng/mL", "Normal: < 0.04"
        );

        System.out.println("\n--- 1. Generating Lab Report in PDF Format (Default) ---");
        System.out.println(labReport.generate());

        System.out.println("\n--- 2. Switching Bridge Implementor to HTML dynamically ---");
        labReport.setFormatter(htmlFormatter);
        System.out.println(labReport.generate());

        System.out.println("\n--- 3. Switching Bridge Implementor to JSON dynamically ---");
        labReport.setFormatter(jsonFormatter);
        System.out.println(labReport.generate());

        // Another Abstraction: DischargeSummary with PDF
        System.out.println("\n--- 4. Different Abstraction (DischargeSummary) with PDF Formatter ---");
        DischargeSummary discharge = new DischargeSummary(
            pdfFormatter, "Bob Vance",
            "Acute Lumbar Strain", "Resolved - Ambulatory",
            "Bed rest 48 hours, avoid lifting > 10 lbs."
        );
        System.out.println(discharge.generate());

        System.out.println("\nBenefit Demonstrated: We can add new reports or new formats independently");
        System.out.println("without an M x N combinatorial explosion of classes!");
    }

    // =================================================================
    // 5. CHAIN OF RESPONSIBILITY DEMO
    // =================================================================
    public static void demoChainOfResponsibility() {
        System.out.println("======================================================================");
        System.out.println(">>> DEMO 5: CHAIN OF RESPONSIBILITY PATTERN (Behavioral)");
        System.out.println("Role: Sequential patient request triage and escalation pipeline.");
        System.out.println("======================================================================");

        // Build the chain: Receptionist -> Nurse -> Doctor -> Specialist
        RequestHandler receptionist = new ReceptionistHandler();
        RequestHandler nurse = new NurseHandler();
        RequestHandler doctor = new DoctorHandler();
        RequestHandler specialist = new SpecialistHandler();

        receptionist.setNext(nurse).setNext(doctor).setNext(specialist);

        System.out.println("Pipeline configured: Receptionist -> Nurse -> General Doctor -> Specialist\n");

        // Case 1: General Query
        PatientRequest req1 = new PatientRequest("REQ-1", "Alice Henderson", PatientRequest.RequestType.GENERAL_QUERY, "What are the outpatient visiting hours and parking fees?");
        System.out.println("Test Case 1: " + req1);
        receptionist.handle(req1);
        System.out.println("Result: " + (req1.isResolved() ? "RESOLVED by " + req1.getResolvedBy() : "ESCALATED") + "\n");

        // Case 2: Vitals Triage
        PatientRequest req2 = new PatientRequest("REQ-2", "David Miller", PatientRequest.RequestType.VITALS_TRIAGE, "Need wound dressing change and blood pressure check.");
        System.out.println("Test Case 2: " + req2);
        receptionist.handle(req2);
        System.out.println("Result: " + (req2.isResolved() ? "RESOLVED by " + req2.getResolvedBy() : "ESCALATED") + "\n");

        // Case 3: Primary Care
        PatientRequest req3 = new PatientRequest("REQ-3", "Carol White", PatientRequest.RequestType.PRIMARY_CARE, "Persistent dry cough and mild sore throat for 3 days.");
        System.out.println("Test Case 3: " + req3);
        receptionist.handle(req3);
        System.out.println("Result: " + (req3.isResolved() ? "RESOLVED by " + req3.getResolvedBy() : "ESCALATED") + "\n");

        // Case 4: Specialized Care
        PatientRequest req4 = new PatientRequest("REQ-4", "Bob Vance", PatientRequest.RequestType.SPECIALIZED_CARE, "Severe sudden chest pain radiating to left jaw.");
        System.out.println("Test Case 4: " + req4);
        receptionist.handle(req4);
        System.out.println("Result: " + (req4.isResolved() ? "RESOLVED by " + req4.getResolvedBy() : "ESCALATED") + "\n");

        System.out.println("Benefit Demonstrated: Requests are handled at the lowest qualified competence level");
        System.out.println("without coupling the sender to any specific concrete handler.");
    }

    // =================================================================
    // 6. OBSERVER DEMO
    // =================================================================
    public static void demoObserver() {
        System.out.println("======================================================================");
        System.out.println(">>> DEMO 6: OBSERVER PATTERN (Behavioral)");
        System.out.println("Role: Event-driven clinical notifications with decoupled subscribers.");
        System.out.println("======================================================================");

        // Subject
        LabService labService = new LabService();

        // Observers
        Observer patientAlice = new PatientObserver("alice@patient.com");
        Observer patientBob = new PatientObserver("bob@patient.com");
        Observer doctorSmith = new DoctorObserver("Robert Smith");

        // Register observers
        labService.attach(patientAlice);
        labService.attach(doctorSmith);

        // Event 1: Lab report completed for Alice
        labService.completeLabReport("Alice Henderson", "RPT-8891");

        // Dynamic Unregister / Register
        System.out.println("\nDynamic subscription changes:");
        labService.detach(patientAlice);
        labService.attach(patientBob);

        // Event 2: Lab report completed for Bob
        labService.completeLabReport("Bob Vance", "RPT-8892");

        System.out.println("\nBenefit Demonstrated: Subject notifies all interested observers");
        System.out.println("without knowing concrete details of notification mechanisms (SMS, Console, Email).");
    }

    // =================================================================
    // 7. PROXY DEMO
    // =================================================================
    public static void demoProxy() {
        System.out.println("======================================================================");
        System.out.println(">>> DEMO 7: PROXY PATTERN (Structural - Protection Proxy)");
        System.out.println("Role: Enforces HIPAA access control & audits all access attempts.");
        System.out.println("======================================================================");

        // Client interacts with the Proxy, NOT RealService directly!
        MedicalRecordAccess recordProxy = new MedicalRecordProxy();

        // Define users
        User drSmith = new User("DOC-1", "Dr. Robert Smith", "dr.smith@hospital.com", Role.DOCTOR);
        User drJones = new User("DOC-2", "Dr. Elena Jones", "dr.jones@hospital.com", Role.DOCTOR);
        User receptionist = new User("REC-1", "John Receptionist", "reception@hospital.com", Role.RECEPTIONIST);
        User patientAlice = new User("P-1", "Alice Henderson", "alice@patient.com", Role.PATIENT);
        User admin = new User("ADM-1", "Chief Administrator", "admin@hospital.com", Role.ADMIN);

        System.out.println("\n--- SCENARIO A: Dr. Smith reads Record REC-101 (Assigned Patient) ---");
        try {
            MedicalRecord record = recordProxy.getRecord("REC-101", drSmith);
            System.out.println("  -> SUCCESS: " + record);
            System.out.println("     Diagnosis: " + record.getDiagnosis());
        } catch (SecurityException e) {
            System.out.println("  -> " + e.getMessage());
        }

        System.out.println("\n--- SCENARIO B: Receptionist attempts to read Record REC-101 ---");
        try {
            MedicalRecord record = recordProxy.getRecord("REC-101", receptionist);
            System.out.println("  -> SUCCESS: " + record);
        } catch (SecurityException e) {
            System.out.println("  -> SECURITY BLOCKED: " + e.getMessage());
        }

        System.out.println("\n--- SCENARIO C: Dr. Smith attempts to read Record REC-102 (Unassigned Doctor) ---");
        try {
            MedicalRecord record = recordProxy.getRecord("REC-102", drSmith);
            System.out.println("  -> SUCCESS: " + record);
        } catch (SecurityException e) {
            System.out.println("  -> SECURITY BLOCKED: " + e.getMessage());
        }

        System.out.println("\n--- SCENARIO D: System Admin reads Record REC-102 (Compliance Override) ---");
        try {
            MedicalRecord record = recordProxy.getRecord("REC-102", admin);
            System.out.println("  -> SUCCESS: " + record);
        } catch (SecurityException e) {
            System.out.println("  -> " + e.getMessage());
        }

        System.out.println("\nBenefit Demonstrated: Transparent protection proxy intercepting requests,");
        System.out.println("evaluating access rules, blocking violations, and writing to AuditLogger.");
    }

    // =================================================================
    // 8. RUN ALL AUTOMATED DEMONSTRATIONS
    // =================================================================
    public static void runAllDemonstrations() {
        System.out.println("\n**********************************************************************");
        System.out.println("         STARTING FULL AUTOMATED DESIGN PATTERNS LAB DEMO             ");
        System.out.println("**********************************************************************\n");

        demoSingleton();
        System.out.println("\n----------------------------------------------------------------------\n");

        demoFactoryMethod();
        System.out.println("\n----------------------------------------------------------------------\n");

        demoAbstractFactory();
        System.out.println("\n----------------------------------------------------------------------\n");

        demoBridge();
        System.out.println("\n----------------------------------------------------------------------\n");

        demoChainOfResponsibility();
        System.out.println("\n----------------------------------------------------------------------\n");

        demoObserver();
        System.out.println("\n----------------------------------------------------------------------\n");

        demoProxy();

        System.out.println("\n**********************************************************************");
        System.out.println("           ALL 7 DESIGN PATTERNS DEMONSTRATED SUCCESSFULLY!           ");
        System.out.println("**********************************************************************\n");
    }

    // =================================================================
    // 9. START BUILT-IN ZERO-DEPENDENCY WEB SERVER
    // =================================================================
    public static void startWebDemo() {
        System.out.println("Checking Hospital Management Web Portal on port 8080...");
        try {
            WebServer.start(8080);
            System.out.println(">>> Server active! Open your browser at: http://localhost:8080");
            try {
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:8080"));
                    System.out.println(">>> Web browser opened automatically to http://localhost:8080");
                }
            } catch (Exception ignored) {
                // If headless or not supported, user can open the URL manually
            }
        } catch (Exception e) {
            System.err.println("Could not start web server: " + e.getMessage());
        }
    }
}

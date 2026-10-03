package com.hospital;

import com.hospital.abstractfactory.*;
import com.hospital.bridge.*;
import com.hospital.chain.*;
import com.hospital.factory.*;
import com.hospital.model.*;
import com.hospital.observer.*;
import com.hospital.proxy.*;
import com.hospital.singleton.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Enterprise Hospital Management System (HMS) Web Portal & REST API Server.
 * Zero external dependencies - uses standard JDK HttpServer.
 * Serves a realistic, modern clinical hospital portal at http://localhost:8080/
 * All 7 GoF design patterns operate behind the scenes in the Java backend.
 */
public class WebServer {

    private static HttpServer server;

    // In-memory clinical domain state
    private static final MedicalRecordAccess recordProxy = new MedicalRecordProxy();
    private static final RequestHandler triageChain = setupTriageChain();
    private static final LabService labService = new LabService();
    private static final List<Staff> staffDirectory = new CopyOnWriteArrayList<>();
    private static final List<PatientRequest> triageQueue = new CopyOnWriteArrayList<>();
    private static final Map<String, User> hospitalUsers = new HashMap<>();

    static {
        // Setup Users
        hospitalUsers.put("DOC-1", new User("DOC-1", "Dr. Robert Smith", "r.smith@mercyhealth.org", Role.DOCTOR));
        hospitalUsers.put("DOC-2", new User("DOC-2", "Dr. Gregory Jones", "g.jones@mercyhealth.org", Role.DOCTOR));
        hospitalUsers.put("NURSE-1", new User("NURSE-1", "Sarah Jenkins, RN", "s.jenkins@mercyhealth.org", Role.NURSE));
        hospitalUsers.put("REC-1", new User("REC-1", "Emily Clark", "e.clark@mercyhealth.org", Role.RECEPTIONIST));
        hospitalUsers.put("P-1", new User("P-1", "Alice Henderson", "alice@mercyhealth.org", Role.PATIENT));
        hospitalUsers.put("P-2", new User("P-2", "Bob Vance", "bob@mercyhealth.org", Role.PATIENT));
        hospitalUsers.put("ADMIN-1", new User("ADMIN-1", "Hospital Chief Administrator", "admin@mercyhealth.org", Role.ADMIN));

        // Setup Observers for Lab Service
        labService.attach(new PatientObserver("alice@mercyhealth.org"));
        labService.attach(new DoctorObserver("Robert Smith"));

        // Setup Initial Medical Personnel via Factory Method
        StaffFactory docFactory = StaffFactoryProvider.getFactory("DOCTOR");
        StaffFactory nurseFactory = StaffFactoryProvider.getFactory("NURSE");
        StaffFactory techFactory = StaffFactoryProvider.getFactory("TECHNICIAN");

        staffDirectory.add(docFactory.registerStaff("Dr. Robert Smith", "Interventional Cardiology", "Cardiovascular Medicine"));
        staffDirectory.add(docFactory.registerStaff("Dr. Gregory Jones", "Orthopedic Surgery & Trauma", "Orthopedics & Spine"));
        staffDirectory.add(nurseFactory.registerStaff("Sarah Jenkins, RN", "Intensive Critical Care", "ICU Ward 2"));
        staffDirectory.add(techFactory.registerStaff("Mark Stevens", "Diagnostic Radiologic Imaging", "Radiology & Imaging"));

        // Pre-populate initial triage queue
        PatientRequest req1 = new PatientRequest("TRG-101", "Alice Henderson", PatientRequest.RequestType.GENERAL_QUERY, "Routine prescription refill query & insurance validation.");
        triageChain.handle(req1);
        triageQueue.add(req1);

        PatientRequest req2 = new PatientRequest("TRG-102", "Bob Vance", PatientRequest.RequestType.PRIMARY_CARE, "Persistent lumbar spine discomfort, pain scale 6/10.");
        triageChain.handle(req2);
        triageQueue.add(req2);

        // Pre-log initial system startup in Singleton AuditLogger
        AuditLogger.getInstance().log("SYSTEM", "PORTAL_INIT", "Hospital Clinical Portal online at port 8080");
    }

    private static RequestHandler setupTriageChain() {
        RequestHandler receptionist = new ReceptionistHandler();
        RequestHandler nurse = new NurseHandler();
        RequestHandler doctor = new DoctorHandler();
        RequestHandler specialist = new SpecialistHandler();
        receptionist.setNext(nurse).setNext(doctor).setNext(specialist);
        return receptionist;
    }

    public static synchronized void start(int port) throws IOException {
        if (server != null) return;
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Web app routes
        server.createContext("/", new FrontendHandler());
        server.createContext("/api/dashboard", new DashboardApiHandler());
        server.createContext("/api/records", new RecordsApiHandler());
        server.createContext("/api/triage", new TriageApiHandler());
        server.createContext("/api/reports", new ReportsApiHandler());
        server.createContext("/api/lab/complete", new LabCompleteApiHandler());
        server.createContext("/api/department", new DepartmentApiHandler());
        server.createContext("/api/staff", new StaffApiHandler());
        server.createContext("/api/staff/onboard", new StaffOnboardApiHandler());
        server.createContext("/api/audit", new AuditApiHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("  [WebServer] Mercy General Hospital Web Portal active at http://localhost:" + port + "/");
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (Exception ignored) {}
        }
        try {
            start(port);
            System.out.println("======================================================================");
            System.out.println("   MERCY GENERAL HOSPITAL - BACKEND REST API SERVER ONLINE (: " + port + ")   ");
            System.out.println("   Serving clinical operations & Next.js frontend at :3000            ");
            System.out.println("   Press Ctrl+C to stop.                                              ");
            System.out.println("======================================================================");
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("  [WebServer] Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =================================================================
    // API HANDLERS
    // =================================================================

    /**
     * Dashboard Census API
     */
    static class DashboardApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            int activePatients = 142;
            int totalStaff = staffDirectory.size();
            int pendingTriage = triageQueue.size();
            List<String> logs = AuditLogger.getInstance().getLogEntries();
            int auditEventsCount = logs.size();

            StringBuilder logsJson = new StringBuilder("[");
            int start = Math.max(0, logs.size() - 8);
            for (int i = start; i < logs.size(); i++) {
                if (i > start) logsJson.append(",");
                logsJson.append("\"").append(escapeJson(logs.get(i))).append("\"");
            }
            logsJson.append("]");

            String json = String.format(Locale.US,
                "{\"inpatients\":%d,\"bedCapacity\":\"88%%\",\"staffCount\":%d,\"triageCount\":%d,\"auditEvents\":%d,\"hospitalStatus\":\"NORMAL_OPERATIONS\",\"recentLogs\":%s}",
                activePatients, totalStaff, pendingTriage, auditEventsCount, logsJson.toString()
            );
            sendJsonResponse(exchange, 200, json);
        }
    }

    /**
     * EHR Medical Records API (Protected by MedicalRecordProxy)
     */
    static class RecordsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String recordId = params.getOrDefault("recordId", "REC-101");
            String userId = params.getOrDefault("userId", "DOC-1");

            User requester = hospitalUsers.get(userId);
            if (requester == null) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Unrecognized Hospital Personnel ID.\"}");
                return;
            }

            try {
                // Call through Protection Proxy!
                MedicalRecord record = recordProxy.getRecord(recordId, requester);
                String json = String.format(Locale.US,
                    "{\"success\":true,\"record\":{\"recordId\":\"%s\",\"patientId\":\"%s\",\"doctorId\":\"%s\",\"diagnosis\":\"%s\",\"prescription\":\"%s\",\"confidential\":%b,\"timestamp\":\"%s\"}}",
                    escapeJson(record.getRecordId()),
                    escapeJson(record.getPatientId()),
                    escapeJson(record.getDoctorId()),
                    escapeJson(record.getDiagnosis()),
                    escapeJson(record.getPrescription()),
                    record.isConfidential(),
                    escapeJson(record.getTimestamp())
                );
                sendJsonResponse(exchange, 200, json);
            } catch (SecurityException se) {
                // Proxy blocked the access!
                String json = String.format(Locale.US,
                    "{\"success\":false,\"securityBlocked\":true,\"error\":\"%s\"}",
                    escapeJson(se.getMessage())
                );
                sendJsonResponse(exchange, 403, json);
            }
        }
    }

    /**
     * Emergency Triage API (Evaluated by Chain of Responsibility)
     */
    static class TriageApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readBody(exchange);
                Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
                Map<String, String> bodyParams = parseFormOrJson(body);
                Map<String, String> params = new HashMap<>(queryParams);
                params.putAll(bodyParams);

                String userId = params.getOrDefault("userId", "");
                User requester = hospitalUsers.get(userId);

                // RBAC: Only Admissions Receptionist and Chief Administrator can intake and route walk-in patients!
                if (requester == null || (requester.getRole() != Role.RECEPTIONIST && requester.getRole() != Role.ADMIN)) {
                    AuditLogger.getInstance().log(
                        requester != null ? requester.getName() : (userId.isEmpty() ? "ANONYMOUS" : userId),
                        "ACCESS_DENIED",
                        "Patient Admissions & Triage Routing (Restricted to Admissions Receptionist & Administration)"
                    );
                    sendJsonResponse(exchange, 403, "{\"success\":false,\"error\":\"ACCESS DENIED: Walk-in patient admission and triage routing must be performed by the Admissions Desk (Receptionist) or Chief Administrator.\"}");
                    return;
                }

                String patientName = params.getOrDefault("patientName", "Walk-in Patient");
                String complaint = params.getOrDefault("complaint", "General checkup");
                String acuityStr = params.getOrDefault("acuity", "GENERAL_QUERY");

                PatientRequest.RequestType type;
                try {
                    type = PatientRequest.RequestType.valueOf(acuityStr.toUpperCase());
                } catch (Exception e) {
                    type = PatientRequest.RequestType.GENERAL_QUERY;
                }

                String reqId = "TRG-" + (100 + triageQueue.size() + 1);
                PatientRequest request = new PatientRequest(reqId, patientName, type, complaint);

                // Run through Chain of Responsibility!
                triageChain.handle(request);
                triageQueue.add(0, request); // Add to head of queue

                AuditLogger.getInstance().log("TRIAGE_DESK", "PATIENT_TRIAGED",
                    reqId + " [" + patientName + "] Admitted by " + requester.getName() + " -> Assigned to: " + request.getResolvedBy());

                String json = String.format(Locale.US,
                    "{\"success\":true,\"id\":\"%s\",\"patient\":\"%s\",\"type\":\"%s\",\"resolvedBy\":\"%s\",\"resolved\":%b,\"complaint\":\"%s\"}",
                    escapeJson(request.getId()),
                    escapeJson(request.getPatientName()),
                    escapeJson(request.getType().name()),
                    escapeJson(request.getResolvedBy()),
                    request.isResolved(),
                    escapeJson(request.getDescription())
                );
                sendJsonResponse(exchange, 200, json);
            } else {
                // GET request: check if requester is a patient
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String userId = params.getOrDefault("userId", "");
                User requester = hospitalUsers.get(userId);
                if (requester != null && requester.getRole() == Role.PATIENT) {
                    sendJsonResponse(exchange, 403, "{\"error\":\"ACCESS DENIED: Inpatient shift triage roster is restricted to clinical personnel.\"}");
                    return;
                }

                // list all in triage queue
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < triageQueue.size(); i++) {
                    PatientRequest r = triageQueue.get(i);
                    if (i > 0) sb.append(",");
                    sb.append(String.format(Locale.US,
                        "{\"id\":\"%s\",\"patient\":\"%s\",\"type\":\"%s\",\"resolvedBy\":\"%s\",\"resolved\":%b,\"complaint\":\"%s\"}",
                        escapeJson(r.getId()),
                        escapeJson(r.getPatientName()),
                        escapeJson(r.getType().name()),
                        escapeJson(r.getResolvedBy()),
                        r.isResolved(),
                        escapeJson(r.getDescription())
                    ));
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            }
        }
    }

    /**
     * Clinical Reports API (Bridge Pattern: Report x ReportFormatter)
     */
    static class ReportsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String reportType = params.getOrDefault("type", "lab").toLowerCase();
            String formatType = params.getOrDefault("format", "pdf").toLowerCase();
            String patient = params.getOrDefault("patient", "Alice Henderson");

            // Bridge Implementor selection
            ReportFormatter formatter = switch (formatType) {
                case "html" -> new HtmlFormatter();
                case "json" -> new JsonFormatter();
                default -> new PdfFormatter();
            };

            // Bridge Abstraction selection
            Report report = switch (reportType) {
                case "discharge" -> new DischargeSummary(
                    formatter, patient,
                    "Acute Bacterial Pneumonia (J18.9)",
                    "Resolved Pneumonia, normal SpO2 (98%)",
                    "Complete oral Azithromycin 250mg course for 3 more days. Schedule pulmonary follow-up."
                );
                case "prescription" -> new PrescriptionReport(
                    formatter, patient, "Robert Smith",
                    "Atorvastatin 40mg PO QHS (Quantity: 30 tablets)",
                    "Take 1 tablet by mouth daily at bedtime. Monitor lipid profile."
                );
                default -> new LabReport(
                    formatter, patient,
                    "High-Sensitivity Cardiac Troponin I (hs-cTnI)",
                    "0.012 ng/mL",
                    "< 0.040 ng/mL (Normal Baseline)"
                );
            };

            String generatedOutput = report.generate();
            AuditLogger.getInstance().log("CLINICAL_REPORTS", "REPORT_GENERATED",
                report.getTitle() + " for " + patient + " [" + formatter.getFormatType() + "]");

            String json = String.format(Locale.US,
                "{\"title\":\"%s\",\"patient\":\"%s\",\"format\":\"%s\",\"content\":\"%s\"}",
                escapeJson(report.getTitle()),
                escapeJson(patient),
                escapeJson(formatter.getFormatType()),
                escapeJson(generatedOutput)
            );
            sendJsonResponse(exchange, 200, json);
        }
    }

    /**
     * Lab Order Completion & Notification API (Observer Pattern: LabService)
     */
    static class LabCompleteApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String body = readBody(exchange);
            Map<String, String> params = parseFormOrJson(body);

            String patientName = params.getOrDefault("patientName", "Alice Henderson");
            String reportId = params.getOrDefault("reportId", "LAB-" + (System.currentTimeMillis() % 10000));

            // Triggers Observer notification!
            List<String> notifications = labService.completeLabReport(patientName, reportId);
            AuditLogger.getInstance().log("PATHOLOGY_LAB", "ORDER_COMPLETED",
                "Report #" + reportId + " verified. " + notifications.size() + " clinical alerts dispatched.");

            StringBuilder sb = new StringBuilder("{\"success\":true,\"reportId\":\"" + escapeJson(reportId) + "\",\"notifications\":[");
            for (int i = 0; i < notifications.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append("\"").append(escapeJson(notifications.get(i))).append("\"");
            }
            sb.append("]}");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /**
     * Department Diagnostic Suites API (Abstract Factory Pattern)
     */
    static class DepartmentApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String deptParam = params.getOrDefault("dept", "cardiology").toLowerCase();
            String userId = params.getOrDefault("userId", "");

            User requester = hospitalUsers.get(userId);
            if (requester == null || requester.getRole() == Role.PATIENT || requester.getRole() == Role.RECEPTIONIST) {
                AuditLogger.getInstance().log(
                    requester != null ? requester.getName() : (userId.isEmpty() ? "ANONYMOUS" : userId),
                    "ACCESS_DENIED",
                    "Department Diagnostic Suites (Unauthorized Access - Clinical Clearance Required)"
                );
                sendJsonResponse(exchange, 403, "{\"error\":\"ACCESS DENIED: Diagnostic equipment calibration restricted to clinical personnel.\"}");
                return;
            }

            DepartmentEquipmentFactory factory;
            if ("radiology".equals(deptParam)) {
                factory = new RadiologyEquipmentFactory();
            } else {
                factory = new CardiologyEquipmentFactory();
            }

            DiagnosticTool tool = factory.createDiagnosticTool();
            ReportGenerator reportGen = factory.createReportGenerator();
            ConsentForm consentForm = factory.createConsentForm();

            String toolName = tool.getToolName();
            String scanResult = tool.runDiagnosticScan("PATIENT-ALICE-HENDERSON");
            String sampleReport = reportGen.generateReport("Alice Henderson", "Clear vascular anatomy, no acute ischemia or infarction.");
            String consentTitle = consentForm.getFormTitle();
            List<String> disclosures = consentForm.getRequiredDisclosures();

            AuditLogger.getInstance().log("EQUIPMENT_SUITE", "CALIBRATION_RUN",
                factory.getDepartmentName() + " suite protocol initialized by " + (requester != null ? requester.getName() : userId));

            StringBuilder discSb = new StringBuilder("[");
            for (int i = 0; i < disclosures.size(); i++) {
                if (i > 0) discSb.append(",");
                discSb.append("\"").append(escapeJson(disclosures.get(i))).append("\"");
            }
            discSb.append("]");

            String json = String.format(Locale.US,
                "{\"department\":\"%s\",\"tool\":\"%s\",\"scanResult\":\"%s\",\"reportStandard\":\"%s\",\"sampleReport\":\"%s\",\"consentTitle\":\"%s\",\"disclosures\":%s}",
                escapeJson(factory.getDepartmentName()),
                escapeJson(toolName),
                escapeJson(scanResult),
                escapeJson(reportGen.getReportStandard()),
                escapeJson(sampleReport),
                escapeJson(consentTitle),
                discSb.toString()
            );
            sendJsonResponse(exchange, 200, json);
        }
    }

    /**
     * Staff Directory API
     */
    static class StaffApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String userId = params.getOrDefault("userId", "");
            User requester = hospitalUsers.get(userId);

            // Salary privacy: Only Administrator can view clinical compensation
            boolean showSalary = (requester != null && requester.getRole() == Role.ADMIN);

            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < staffDirectory.size(); i++) {
                Staff s = staffDirectory.get(i);
                if (i > 0) sb.append(",");
                sb.append(String.format(Locale.US,
                    "{\"name\":\"%s\",\"role\":\"%s\",\"department\":\"%s\",\"specialization\":\"%s\",\"duty\":\"%s\",\"salary\":%.2f,\"showSalary\":%b}",
                    escapeJson(s.getName()),
                    escapeJson(s.getRole()),
                    escapeJson(s.getDepartment()),
                    escapeJson(s.getSpecialization()),
                    escapeJson(s.getDuty()),
                    showSalary ? s.calculateSalary() : 0.0,
                    showSalary
                ));
            }
            sb.append("]");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    /**
     * Staff Onboarding API (Factory Method: StaffFactoryProvider)
     */
    static class StaffOnboardApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                String body = readBody(exchange);
                Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
                Map<String, String> bodyParams = parseFormOrJson(body);
                Map<String, String> params = new HashMap<>(queryParams);
                params.putAll(bodyParams);

                String userId = params.getOrDefault("userId", "");
                User requester = hospitalUsers.get(userId);

                // Protection Proxy: Only Hospital Administrator can onboard staff and allocate salaries!
                if (requester == null || requester.getRole() != Role.ADMIN) {
                    AuditLogger.getInstance().log(
                        requester != null ? requester.getName() : (userId.isEmpty() ? "ANONYMOUS" : userId),
                        "ACCESS_DENIED",
                        "Staff Onboarding & Compensation (Requires HR Administrator Clearance)"
                    );
                    sendJsonResponse(exchange, 403, "{\"success\":false,\"error\":\"ACCESS DENIED: Only Hospital Administrators can onboard personnel and assign salaries.\"}");
                    return;
                }

                String name = params.getOrDefault("name", "Dr. Jane Doe");
                String role = params.getOrDefault("role", "DOCTOR").toUpperCase();
                String department = params.getOrDefault("department", "Emergency Medicine");
                String specialization = params.getOrDefault("specialization", "Trauma & Acute Resuscitation");

                StaffFactory factory = StaffFactoryProvider.getFactory(role);
                Staff newStaff = factory.registerStaff(name, specialization, department);
                staffDirectory.add(newStaff);

                AuditLogger.getInstance().log("HR_MANAGEMENT", "STAFF_ONBOARDED",
                    newStaff.getRole() + " " + newStaff.getName() + " assigned to " + newStaff.getDepartment());

                String json = String.format(Locale.US,
                    "{\"success\":true,\"name\":\"%s\",\"role\":\"%s\",\"department\":\"%s\",\"specialization\":\"%s\",\"duty\":\"%s\",\"salary\":%.2f}",
                    escapeJson(newStaff.getName()),
                    escapeJson(newStaff.getRole()),
                    escapeJson(newStaff.getDepartment()),
                    escapeJson(newStaff.getSpecialization()),
                    escapeJson(newStaff.getDuty()),
                    newStaff.calculateSalary()
                );
                sendJsonResponse(exchange, 200, json);
            } catch (Throwable t) {
                t.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"success\":false,\"error\":\"Onboarding error: " + escapeJson(t.getMessage()) + "\"}");
            }
        }
    }

    /**
     * Hospital Security & Compliance Audit Registry (Singleton: AuditLogger)
     */
    static class AuditApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String userId = params.getOrDefault("userId", "");
                User requester = hospitalUsers.get(userId);

                // Protection Proxy: Patients cannot view HIPAA compliance logs
                if (requester != null && requester.getRole() == Role.PATIENT) {
                    AuditLogger.getInstance().log(
                        requester.getName(),
                        "ACCESS_DENIED",
                        "Compliance Audit Registry (Unauthorized Patient Access)"
                    );
                    sendJsonResponse(exchange, 403, "{\"error\":\"ACCESS DENIED: Inpatient access to hospital compliance logs is restricted under HIPAA Title II.\"}");
                    return;
                }

                List<String> logs = AuditLogger.getInstance().getLogEntries();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < logs.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"").append(escapeJson(logs.get(i))).append("\"");
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            } catch (Throwable t) {
                t.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"error\":\"Audit log error: " + escapeJson(t.getMessage()) + "\"}");
            }
        }
    }

    // =================================================================
    // FRONTEND HANDLER - REALISTIC HOSPITAL MANAGEMENT PORTAL
    // =================================================================
    static class FrontendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] bytes = HMS_SPA_HTML.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // =================================================================
    // UTILITY METHODS
    // =================================================================
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                params.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                           URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            } else if (pair.length == 1) {
                params.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8), "");
            }
        }
        return params;
    }

    private static Map<String, String> parseFormOrJson(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.trim().isEmpty()) return map;

        String trimmed = body.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            // Simple robust JSON key-value extraction for string properties
            String inner = trimmed.substring(1, trimmed.length() - 1);
            String[] tokens = inner.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String token : tokens) {
                String[] kv = token.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (kv.length == 2) {
                    String k = kv[0].trim().replaceAll("^\"|\"$", "");
                    String v = kv[1].trim().replaceAll("^\"|\"$", "");
                    map.put(k, v);
                }
            }
        } else {
            // URL-encoded form data
            map.putAll(parseQueryParams(trimmed));
        }
        return map;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // =================================================================
    // HTML / CSS / JS SINGLE PAGE APPLICATION
    // 100% CLINICAL HOSPITAL MANAGEMENT SYSTEM - ZERO DESIGN PATTERN WORDS
    // =================================================================
    private static final String HMS_SPA_HTML = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Mercy General Hospital & Health Network - Clinical Operations Portal</title>
  <style>
    :root {
      --primary: #0284c7;
      --primary-dark: #0369a1;
      --primary-light: #e0f2fe;
      --secondary: #0f172a;
      --bg: #f8fafc;
      --card-bg: #ffffff;
      --text: #1e293b;
      --text-muted: #64748b;
      --border: #e2e8f0;
      --success: #10b981;
      --success-light: #d1fae5;
      --warning: #f59e0b;
      --warning-light: #fef3c7;
      --danger: #ef4444;
      --danger-light: #fee2e2;
      --sidebar-width: 250px;
    }

    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; background: var(--bg); color: var(--text); display: flex; height: 100vh; overflow: hidden; }

    /* Sidebar Navigation */
    .sidebar { width: var(--sidebar-width); background: #0f172a; color: white; display: flex; flex-direction: column; flex-shrink: 0; border-right: 1px solid #1e293b; }
    .brand { padding: 20px 18px; border-bottom: 1px solid #1e293b; display: flex; align-items: center; gap: 10px; }
    .brand-icon { width: 34px; height: 34px; background: #0284c7; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: bold; }
    .brand-text h2 { font-size: 15px; font-weight: 700; color: #f8fafc; letter-spacing: -0.3px; }
    .brand-text p { font-size: 11px; color: #94a3b8; }

    .nav-menu { list-style: none; padding: 16px 10px; flex: 1; overflow-y: auto; }
    .nav-item { margin-bottom: 4px; }
    .nav-btn { width: 100%; display: flex; align-items: center; gap: 12px; padding: 10px 14px; background: transparent; border: none; color: #cbd5e1; font-size: 13px; font-weight: 500; border-radius: 8px; cursor: pointer; text-align: left; transition: all 0.15s ease; }
    .nav-btn:hover { background: #1e293b; color: #38bdf8; }
    .nav-btn.active { background: #0284c7; color: white; font-weight: 600; box-shadow: 0 2px 4px rgba(2, 132, 199, 0.3); }
    .nav-btn .icon { font-size: 16px; width: 20px; text-align: center; }

    .sidebar-footer { padding: 16px; border-top: 1px solid #1e293b; font-size: 11px; color: #64748b; text-align: center; }

    /* Main Content Area */
    .main-wrapper { flex: 1; display: flex; flex-direction: column; height: 100vh; overflow: hidden; }

    /* Top Bar */
    .topbar { height: 64px; background: white; border-bottom: 1px solid var(--border); display: flex; align-items: center; justify-content: space-between; padding: 0 24px; flex-shrink: 0; }
    .topbar-left { display: flex; align-items: center; gap: 16px; }
    .page-title { font-size: 18px; font-weight: 700; color: #0f172a; }
    .status-pill { display: inline-flex; align-items: center; gap: 6px; font-size: 11px; font-weight: 600; padding: 4px 10px; border-radius: 9999px; background: var(--success-light); color: var(--success); }
    .status-dot { width: 6px; height: 6px; background: var(--success); border-radius: 50%; }

    .topbar-right { display: flex; align-items: center; gap: 20px; }
    .role-switcher-box { display: flex; align-items: center; gap: 8px; font-size: 12px; color: var(--text-muted); }
    .role-select { padding: 6px 12px; border-radius: 6px; border: 1px solid var(--border); background: white; font-size: 12px; font-weight: 600; color: #0f172a; cursor: pointer; outline: none; }
    .role-select:focus { border-color: var(--primary); }

    .live-clock { font-size: 12px; font-family: monospace; color: var(--text-muted); background: #f1f5f9; padding: 4px 8px; border-radius: 4px; }
    .alert-btn { position: relative; background: none; border: none; font-size: 18px; cursor: pointer; color: #64748b; }
    .alert-badge { position: absolute; top: -4px; right: -6px; background: var(--danger); color: white; font-size: 9px; font-weight: bold; width: 16px; height: 16px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }

    /* Content Body */
    .content-area { flex: 1; padding: 24px; overflow-y: auto; }
    .tab-content { display: none; }
    .tab-content.active { display: block; animation: fadeIn 0.15s ease-in; }
    @keyframes fadeIn { from { opacity: 0; transform: translateY(4px); } to { opacity: 1; transform: translateY(0); } }

    /* Dashboard Widgets */
    .metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-bottom: 24px; }
    .metric-card { background: white; border: 1px solid var(--border); border-radius: 10px; padding: 18px; box-shadow: 0 1px 3px rgba(0,0,0,0.03); }
    .metric-label { font-size: 12px; font-weight: 600; color: var(--text-muted); text-transform: uppercase; margin-bottom: 8px; }
    .metric-val { font-size: 26px; font-weight: 800; color: #0f172a; margin-bottom: 6px; }
    .metric-sub { font-size: 11px; color: var(--success); font-weight: 500; }

    .card { background: white; border: 1px solid var(--border); border-radius: 10px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.03); margin-bottom: 20px; }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--border); }
    .card-title { font-size: 15px; font-weight: 700; color: #0f172a; display: flex; align-items: center; gap: 8px; }

    /* Tables */
    .table-container { width: 100%; overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; text-align: left; font-size: 13px; }
    th { background: #f8fafc; color: var(--text-muted); font-weight: 600; padding: 10px 14px; border-bottom: 1px solid var(--border); font-size: 11px; text-transform: uppercase; }
    td { padding: 12px 14px; border-bottom: 1px solid var(--border); color: #334155; }
    tr:hover td { background: #f8fafc; }

    /* Badges */
    .badge { display: inline-block; padding: 3px 8px; border-radius: 9999px; font-size: 11px; font-weight: 600; text-transform: uppercase; }
    .badge-success { background: var(--success-light); color: #065f46; }
    .badge-warning { background: var(--warning-light); color: #92400e; }
    .badge-danger { background: var(--danger-light); color: #991b1b; }
    .badge-info { background: var(--primary-light); color: var(--primary-dark); }
    .badge-gray { background: #f1f5f9; color: #475569; }

    /* Buttons & Forms */
    .btn { display: inline-flex; align-items: center; gap: 8px; padding: 8px 16px; border-radius: 6px; font-size: 13px; font-weight: 600; cursor: pointer; border: 1px solid transparent; transition: all 0.15s; }
    .btn-primary { background: var(--primary); color: white; }
    .btn-primary:hover { background: var(--primary-dark); }
    .btn-outline { background: white; border-color: var(--border); color: var(--text); }
    .btn-outline:hover { background: #f1f5f9; }
    .btn-danger { background: var(--danger); color: white; }
    .btn-sm { padding: 5px 10px; font-size: 12px; }

    .form-group { margin-bottom: 14px; }
    .form-label { display: block; font-size: 12px; font-weight: 600; color: #334155; margin-bottom: 6px; }
    .form-control { width: 100%; padding: 8px 12px; border-radius: 6px; border: 1px solid var(--border); font-size: 13px; color: var(--text); outline: none; background: white; }
    .form-control:focus { border-color: var(--primary); box-shadow: 0 0 0 2px rgba(2, 132, 199, 0.15); }

    /* Document Output & Security Warning */
    .document-preview { background: #0f172a; color: #f8fafc; font-family: "Courier New", Courier, monospace; font-size: 12px; padding: 18px; border-radius: 8px; white-space: pre-wrap; line-height: 1.5; max-height: 380px; overflow-y: auto; border: 1px solid #334155; }
    .security-alert-box { background: #fef2f2; border: 2px solid #ef4444; border-radius: 8px; padding: 18px; color: #991b1b; margin-top: 16px; display: none; }
    .security-alert-header { display: flex; align-items: center; gap: 10px; font-weight: 800; font-size: 14px; margin-bottom: 8px; }

    /* Notification Drawer */
    .notification-drawer { position: fixed; right: 24px; bottom: 24px; width: 340px; background: white; border: 1px solid var(--border); border-radius: 10px; box-shadow: 0 10px 25px -5px rgba(0,0,0,0.1); padding: 16px; z-index: 999; display: none; }
    .notification-item { padding: 10px; border-radius: 6px; background: #f8fafc; border-left: 3px solid var(--primary); margin-bottom: 8px; font-size: 12px; }
  </style>
</head>
<body>

  <!-- SIDEBAR NAVIGATION -->
  <aside class="sidebar">
    <div class="brand">
      <div class="brand-icon">✚</div>
      <div class="brand-text">
        <h2>Mercy General</h2>
        <p>Health & Clinical Portal</p>
      </div>
    </div>

    <ul class="nav-menu">
      <li class="nav-item">
        <button class="nav-btn active" onclick="switchTab('dashboard')">
          <span class="icon">📊</span> Hospital Dashboard
        </button>
      </li>
      <li class="nav-item">
        <button class="nav-btn" onclick="switchTab('records')">
          <span class="icon">📁</span> Patient Charts (EHR)
        </button>
      </li>
      <li class="nav-item">
        <button class="nav-btn" onclick="switchTab('triage')">
          <span class="icon">🚨</span> Emergency Triage
        </button>
      </li>
      <li class="nav-item">
        <button class="nav-btn" onclick="switchTab('reports')">
          <span class="icon">📄</span> Clinical Reports & Lab
        </button>
      </li>
      <li class="nav-item">
        <button class="nav-btn" onclick="switchTab('department')">
          <span class="icon">🏥</span> Department Suites
        </button>
      </li>
      <li class="nav-item">
        <button class="nav-btn" onclick="switchTab('staff')">
          <span class="icon">👥</span> Medical Staff Directory
        </button>
      </li>
      <li class="nav-item">
        <button class="nav-btn" onclick="switchTab('audit')">
          <span class="icon">🔒</span> Security & Audit Log
        </button>
      </li>
    </ul>

    <div class="sidebar-footer">
      Mercy Health Network &bull; Clinical OS v4.2<br/>HIPAA & Joint Commission Compliant
    </div>
  </aside>

  <!-- MAIN WRAPPER -->
  <div class="main-wrapper">
    <!-- TOP BAR -->
    <header class="topbar">
      <div class="topbar-left">
        <h1 class="page-title" id="pageTitle">Hospital Dashboard & Census</h1>
        <div class="status-pill">
          <span class="status-dot"></span> Emergency Level 1: Normal
        </div>
      </div>

      <div class="topbar-right">
        <!-- Interactive Active User Role Switcher -->
        <div class="role-switcher-box">
          <span>Active Clinician / User:</span>
          <select id="userSelector" class="role-select" onchange="onUserRoleChanged()">
            <option value="DOC-1">🩺 Dr. Robert Smith (Cardiology Attending)</option>
            <option value="DOC-2">🩺 Dr. Gregory Jones (Orthopedics Attending)</option>
            <option value="NURSE-1">👩‍⚕️ Sarah Jenkins, RN (Head Nurse)</option>
            <option value="REC-1">📋 Emily Clark (Admissions Receptionist)</option>
            <option value="P-1">👤 Alice Henderson (Patient)</option>
            <option value="P-2">👤 Bob Vance (Patient)</option>
            <option value="ADMIN-1">🛡️ Compliance Officer (Administrator)</option>
          </select>
        </div>

        <div class="live-clock" id="liveClock">--:--:--</div>
        <button class="alert-btn" title="Clinical Notifications" onclick="toggleNotificationDrawer()">
          🔔<span class="alert-badge" id="notifBadge">0</span>
        </button>
      </div>
    </header>

    <!-- CONTENT BODY -->
    <main class="content-area">

      <!-- ============================================================== -->
      <!-- TAB 1: EXECUTIVE DASHBOARD & CENSUS                            -->
      <!-- ============================================================== -->
      <section id="tab-dashboard" class="tab-content active">
        <div class="metrics-grid">
          <div class="metric-card">
            <div class="metric-label">Active Inpatients</div>
            <div class="metric-val" id="metricInpatients">142</div>
            <div class="metric-sub">Bed Occupancy: 88% (Normal)</div>
          </div>
          <div class="metric-card">
            <div class="metric-label">On-Duty Clinicians</div>
            <div class="metric-val" id="metricStaff">4</div>
            <div class="metric-sub">Doctors, Nurses & Technicians</div>
          </div>
          <div class="metric-card">
            <div class="metric-label">Emergency Triage Queue</div>
            <div class="metric-val" id="metricTriage">2</div>
            <div class="metric-sub">Awaiting Attending Review</div>
          </div>
          <div class="metric-card">
            <div class="metric-label">Compliance & Security Events</div>
            <div class="metric-val" id="metricAudit">0</div>
            <div class="metric-sub">Recorded to Central Registry</div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h2 class="card-title">⚡ Quick Clinical Actions</h2>
          </div>
          <div style="display: flex; gap: 12px; flex-wrap: wrap;">
            <button class="btn btn-primary" onclick="switchTab('triage')">🚨 New Patient Triage</button>
            <button class="btn btn-outline" onclick="switchTab('records')">📁 Access Patient Charts (EHR)</button>
            <button class="btn btn-outline" onclick="switchTab('reports')">📄 Generate Clinical Report</button>
            <button class="btn btn-outline" onclick="switchTab('department')">🏥 Calibrate Diagnostic Suite</button>
            <button class="btn btn-outline" onclick="switchTab('staff')">👥 Staff Directory & Onboarding</button>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h2 class="card-title">🕒 Recent Hospital Operations Feed</h2>
            <button class="btn btn-outline btn-sm" onclick="loadDashboard()">Refresh</button>
          </div>
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>Event Log Summary</th>
                  <th>Clinical Resource</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody id="dashboardActivityTable">
                <tr><td colspan="3" style="text-align: center; color: #94a3b8;">Loading clinical stream...</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <!-- ============================================================== -->
      <!-- TAB 2: ELECTRONIC HEALTH RECORDS (EHR) & ACCESS CONTROL        -->
      <!-- ============================================================== -->
      <section id="tab-records" class="tab-content">
        <div class="card">
          <div class="card-header">
            <h2 class="card-title">📁 Electronic Health Records (EHR) Patient Chart Viewer</h2>
            <span class="badge badge-info">HIPAA Security Protected</span>
          </div>
          <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 16px;">
            Confidential medical charts are restricted under federal HIPAA privacy regulations. Access is only authorized for designated attending physicians, the verified patient, or compliance audit administrators.
          </p>

          <div style="display: flex; gap: 16px; align-items: flex-end; margin-bottom: 20px; flex-wrap: wrap;">
            <div style="min-width: 240px;">
              <label class="form-label">Select Patient Record:</label>
              <select id="recordSelector" class="form-control">
                <option value="REC-101">Patient Alice Henderson (#REC-101) - Attending: Dr. Robert Smith</option>
                <option value="REC-102">Patient Bob Vance (#REC-102) - Attending: Dr. Gregory Jones</option>
              </select>
            </div>
            <button class="btn btn-primary" onclick="fetchPatientRecord()">🔓 Open Patient Medical Chart</button>
          </div>

          <!-- Realistic HIPAA Privacy Warning Box -->
          <div id="recordSecurityAlert" class="security-alert-box">
            <div class="security-alert-header">
              <span>⚠️ ACCESS RESTRICTED - HIPAA PRIVACY & SECURITY RULE ENFORCEMENT</span>
            </div>
            <div id="recordSecurityMsg" style="font-size: 13px; line-height: 1.5;"></div>
            <div style="font-size: 11px; margin-top: 10px; color: #b91c1c; font-weight: 600;">
              Incident timestamped and permanently registered to Hospital Compliance Audit Registry.
            </div>
          </div>

          <!-- Medical Chart Display -->
          <div id="recordChartDetails" style="display: none; margin-top: 20px; border: 1px solid var(--border); border-radius: 8px; padding: 20px; background: #ffffff;">
            <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #0284c7; padding-bottom: 12px; margin-bottom: 16px;">
              <div>
                <h3 id="chartPatientTitle" style="font-size: 18px; color: #0f172a;">Patient Clinical Chart</h3>
                <span id="chartRecordId" style="font-size: 12px; color: var(--text-muted); font-family: monospace;"></span>
              </div>
              <span class="badge badge-success">Verified Attending Clearance</span>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px; margin-bottom: 16px;">
              <div>
                <span style="font-size: 11px; color: var(--text-muted); text-transform: uppercase; font-weight: 600;">Attending Clinician</span>
                <p id="chartDoctor" style="font-size: 14px; font-weight: 600; color: #0f172a; margin-top: 4px;"></p>
              </div>
              <div>
                <span style="font-size: 11px; color: var(--text-muted); text-transform: uppercase; font-weight: 600;">Record Classification</span>
                <p style="font-size: 14px; font-weight: 600; color: #0f172a; margin-top: 4px;">Confidential Inpatient EHR</p>
              </div>
              <div>
                <span style="font-size: 11px; color: var(--text-muted); text-transform: uppercase; font-weight: 600;">Last Examined</span>
                <p id="chartTimestamp" style="font-size: 14px; color: #0f172a; margin-top: 4px;"></p>
              </div>
            </div>

            <div style="margin-bottom: 16px;">
              <span style="font-size: 11px; color: var(--text-muted); text-transform: uppercase; font-weight: 600;">Clinical Diagnosis</span>
              <div id="chartDiagnosis" style="background: #f8fafc; padding: 12px; border-radius: 6px; font-size: 13px; color: #0f172a; margin-top: 6px; border-left: 3px solid #0284c7;"></div>
            </div>

            <div>
              <span style="font-size: 11px; color: var(--text-muted); text-transform: uppercase; font-weight: 600;">Active Prescriptions & Orders</span>
              <div id="chartPrescription" style="background: #f8fafc; padding: 12px; border-radius: 6px; font-size: 13px; color: #0f172a; margin-top: 6px; border-left: 3px solid #10b981;"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- ============================================================== -->
      <!-- TAB 3: EMERGENCY TRIAGE & INTAKE                               -->
      <!-- ============================================================== -->
      <section id="tab-triage" class="tab-content">
        <div class="card">
          <div class="card-header">
            <h2 class="card-title">🚨 Patient Intake & Clinical Triage Escalation</h2>
            <span class="badge badge-warning">Acuity Routing</span>
          </div>
          <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 16px;">
            Walk-in emergency and clinic admissions. Requests are evaluated along the clinical chain: Admissions Receptionist &rarr; Triage Nurse &rarr; Attending Physician &rarr; ICU Sub-Specialist.
          </p>

          <form id="triageForm" onsubmit="submitTriage(event)" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; margin-bottom: 20px;">
            <div class="form-group">
              <label class="form-label">Patient Full Name:</label>
              <input type="text" id="triagePatient" class="form-control" value="Michael Chang" required />
            </div>
            <div class="form-group">
              <label class="form-label">Triage Acuity Level / Chief Complaint:</label>
              <select id="triageAcuity" class="form-control">
                <option value="GENERAL_QUERY">🟢 General Inquiry / Scheduling / Billing (Admissions)</option>
                <option value="VITALS_TRIAGE">🟡 Routine Vitals / Sprain / Dressing (Triage Nurse)</option>
                <option value="PRIMARY_CARE" selected>🟠 Acute Chest Pain / High Fever (Attending Physician)</option>
                <option value="SPECIALIZED_CARE">🔴 Complex Cardiac Trauma / ICU (Critical Sub-Specialist)</option>
              </select>
            </div>
            <div class="form-group" style="grid-column: 1 / -1;">
              <label class="form-label">Clinical Symptoms & Notes:</label>
              <input type="text" id="triageComplaint" class="form-control" value="Sudden onset radiating substernal tightness, diaphoresis, BP 145/95." required />
            </div>
            <div style="grid-column: 1 / -1;">
              <button type="submit" class="btn btn-primary">🚑 Dispatch Patient to Clinical Triage</button>
            </div>
          </form>

          <!-- Triage Response Card -->
          <div id="triageResultBox" style="display: none; padding: 16px; border-radius: 8px; background: #f0fdf4; border: 1px solid #bbf7d0; margin-bottom: 20px;">
            <h3 style="font-size: 14px; font-weight: 700; color: #166534; margin-bottom: 6px;">✓ Triage Resolution Complete</h3>
            <p id="triageResultText" style="font-size: 13px; color: #14532d;"></p>
          </div>

          <h3 style="font-size: 14px; font-weight: 700; color: #0f172a; margin-bottom: 12px;">Active Shift Triage Queue</h3>
          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>Intake ID</th>
                  <th>Patient</th>
                  <th>Acuity Tier</th>
                  <th>Symptoms & Complaint</th>
                  <th>Assigned Clinician</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody id="triageTableBody">
                <tr><td colspan="6" style="text-align: center; color: #94a3b8;">Loading triage queue...</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <!-- ============================================================== -->
      <!-- TAB 4: CLINICAL REPORTS & LABORATORY NOTIFICATIONS             -->
      <!-- ============================================================== -->
      <section id="tab-reports" class="tab-content">
        <!-- Lab Notification Section -->
        <div class="card">
          <div class="card-header">
            <h2 class="card-title">🧪 Central Pathology & Diagnostic Lab Notifications</h2>
            <span class="badge badge-success">Automated Dispatch</span>
          </div>
          <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 16px;">
            When diagnostic assays are verified by the pathology lab, automated notifications are immediately dispatched to the patient's mobile device via SMS and the attending physician's clinical beeper console.
          </p>

          <div style="display: flex; gap: 14px; align-items: flex-end; flex-wrap: wrap;">
            <div style="min-width: 220px;">
              <label class="form-label">Patient Name:</label>
              <input type="text" id="labPatient" class="form-control" value="Alice Henderson" />
            </div>
            <div style="min-width: 180px;">
              <label class="form-label">Lab Specimen Order ID:</label>
              <input type="text" id="labReportId" class="form-control" value="LAB-8891" />
            </div>
            <button class="btn btn-primary" onclick="completeLabOrder()">⚡ Finalize Lab Order & Dispatch Alerts</button>
          </div>
        </div>

        <!-- Medical Document Generator (Bridge) -->
        <div class="card">
          <div class="card-header">
            <h2 class="card-title">📄 Medical Document & Clinical Report Generator</h2>
            <span class="badge badge-info">Multi-Format Interoperability</span>
          </div>
          <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 16px;">
            Decoupled clinical document rendering. Generate clinical records across hospital formats (Official PDF, Web View, or EHR Interoperability JSON) on-demand.
          </p>

          <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; margin-bottom: 16px;">
            <div>
              <label class="form-label">Document Type:</label>
              <select id="reportDocType" class="form-control">
                <option value="lab">🔬 Laboratory Assay Report (Troponin I)</option>
                <option value="discharge">📋 Inpatient Discharge Summary</option>
                <option value="prescription">💊 Outpatient Pharmacy Prescription</option>
              </select>
            </div>
            <div>
              <label class="form-label">Patient Name:</label>
              <input type="text" id="reportPatientName" class="form-control" value="Alice Henderson" />
            </div>
            <div>
              <label class="form-label">Export Format:</label>
              <select id="reportFormat" class="form-control">
                <option value="pdf">📄 Official PDF Document Format</option>
                <option value="html">🌐 Clinical Web View (HTML)</option>
                <option value="json">💾 Interoperability JSON (HL7 / FHIR)</option>
              </select>
            </div>
          </div>

          <div style="display: flex; gap: 10px; margin-bottom: 16px;">
            <button class="btn btn-primary" onclick="generateMedicalDocument()">Generate Clinical Document</button>
            <button class="btn btn-outline" onclick="downloadCurrentDocument()">⬇ Download File</button>
          </div>

          <div class="document-preview" id="documentPreviewBox">Click "Generate Clinical Document" to view formatted medical output...</div>
        </div>
      </section>

      <!-- ============================================================== -->
      <!-- TAB 5: DEPARTMENT DIAGNOSTIC SUITES                            -->
      <!-- ============================================================== -->
      <section id="tab-department" class="tab-content">
        <div class="card">
          <div class="card-header">
            <h2 class="card-title">🏥 Specialized Department Diagnostic & Protocol Suites</h2>
            <span class="badge badge-info">Coherent Clinical Families</span>
          </div>
          <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 16px;">
            Each specialized clinical department utilizes a strictly matched family of diagnostic tools, official study report standards, and regulatory legal informed consent protocols.
          </p>

          <div style="display: flex; gap: 16px; align-items: flex-end; margin-bottom: 20px; flex-wrap: wrap;">
            <div style="min-width: 280px;">
              <label class="form-label">Select Clinical Department:</label>
              <select id="deptSelector" class="form-control">
                <option value="radiology">Department of Radiology & Medical Imaging</option>
                <option value="cardiology" selected>Department of Cardiovascular Medicine</option>
              </select>
            </div>
            <button class="btn btn-primary" onclick="loadDepartmentSuite()">⚙️ Run Diagnostic Protocol & Calibration</button>
          </div>

          <div id="deptSuiteGrid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px;">
            <!-- Diagnostic Tool Card -->
            <div class="card" style="margin-bottom: 0; background: #f8fafc;">
              <div class="card-header">
                <h3 style="font-size: 14px; color: #0f172a;">1. Diagnostic Instrument</h3>
                <span class="badge badge-success">Online & Calibrated</span>
              </div>
              <p id="deptToolName" style="font-weight: 700; color: #0f172a; margin-bottom: 8px;">--</p>
              <div id="deptScanOutput" style="font-size: 12px; color: #475569; background: white; padding: 10px; border-radius: 6px; border: 1px solid var(--border);">--</div>
            </div>

            <!-- Report Standard Card -->
            <div class="card" style="margin-bottom: 0; background: #f8fafc;">
              <div class="card-header">
                <h3 style="font-size: 14px; color: #0f172a;">2. Department Study Report</h3>
                <span id="deptReportBadge" class="badge badge-info">--</span>
              </div>
              <div id="deptReportSample" style="font-size: 12px; color: #475569; background: white; padding: 10px; border-radius: 6px; border: 1px solid var(--border); white-space: pre-wrap;">--</div>
            </div>

            <!-- Consent Form Card -->
            <div class="card" style="margin-bottom: 0; background: #f8fafc;">
              <div class="card-header">
                <h3 style="font-size: 14px; color: #0f172a;">3. Legal Informed Consent</h3>
                <span class="badge badge-warning">Required Protocol</span>
              </div>
              <p id="deptConsentTitle" style="font-weight: 700; color: #0f172a; margin-bottom: 8px;">--</p>
              <ul id="deptDisclosuresList" style="font-size: 12px; color: #475569; padding-left: 18px; line-height: 1.5;"></ul>
            </div>
          </div>
        </div>
      </section>

      <!-- ============================================================== -->
      <!-- TAB 6: MEDICAL STAFF DIRECTORY & ONBOARDING                    -->
      <!-- ============================================================== -->
      <section id="tab-staff" class="tab-content">
        <div class="card">
          <div class="card-header">
            <h2 class="card-title">👥 Hospital Medical Staff Directory</h2>
            <button class="btn btn-primary btn-sm" onclick="toggleOnboardForm()">➕ Onboard New Healthcare Professional</button>
          </div>

          <!-- Onboard Form Modal / Container -->
          <div id="onboardBox" style="display: none; background: #f8fafc; border: 1px solid var(--border); border-radius: 8px; padding: 18px; margin-bottom: 20px;">
            <h3 style="font-size: 14px; font-weight: 700; color: #0f172a; margin-bottom: 12px;">Onboard Healthcare Professional</h3>
            <form onsubmit="onboardStaff(event)" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px;">
              <div>
                <label class="form-label">Full Name:</label>
                <input type="text" id="staffName" class="form-control" placeholder="Dr. Allison Cameron" required />
              </div>
              <div>
                <label class="form-label">Staff Role:</label>
                <select id="staffRole" class="form-control">
                  <option value="DOCTOR">Attending Physician (Doctor)</option>
                  <option value="NURSE">Registered Nurse</option>
                  <option value="TECHNICIAN">Diagnostic Imaging Technician</option>
                </select>
              </div>
              <div>
                <label class="form-label">Department:</label>
                <input type="text" id="staffDept" class="form-control" placeholder="Immunology & Allergy" required />
              </div>
              <div>
                <label class="form-label">Specialization:</label>
                <input type="text" id="staffSpec" class="form-control" placeholder="Clinical Immunology" required />
              </div>
              <div style="grid-column: 1 / -1; display: flex; gap: 10px;">
                <button type="submit" class="btn btn-primary">Complete Onboarding</button>
                <button type="button" class="btn btn-outline" onclick="toggleOnboardForm()">Cancel</button>
              </div>
            </form>
          </div>

          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>Clinician Name</th>
                  <th>Role</th>
                  <th>Department</th>
                  <th>Clinical Specialization</th>
                  <th>Assigned Duty Roster</th>
                  <th>Monthly Compensation</th>
                </tr>
              </thead>
              <tbody id="staffTableBody">
                <tr><td colspan="6" style="text-align: center; color: #94a3b8;">Loading medical staff...</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <!-- ============================================================== -->
      <!-- TAB 7: HOSPITAL SECURITY & COMPLIANCE AUDIT REGISTRY           -->
      <!-- ============================================================== -->
      <section id="tab-audit" class="tab-content">
        <div class="card">
          <div class="card-header">
            <h2 class="card-title">🔒 HIPAA & Joint Commission Regulatory Audit Registry</h2>
            <button class="btn btn-outline btn-sm" onclick="loadAuditLogs()">🔄 Live Refresh</button>
          </div>
          <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 16px;">
            Centralized, thread-safe compliance registry recording all electronic protected health information (ePHI) chart accesses, unauthorized breach attempts, triage admissions, and lab order dispatches.
          </p>

          <div class="table-container">
            <table>
              <thead>
                <tr>
                  <th>Registry Event Entry</th>
                </tr>
              </thead>
              <tbody id="auditTableBody">
                <tr><td style="text-align: center; color: #94a3b8;">Loading audit stream...</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

    </main>
  </div>

  <!-- NOTIFICATION DRAWER -->
  <div id="notificationDrawer" class="notification-drawer">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; border-bottom: 1px solid var(--border); padding-bottom: 8px;">
      <h3 style="font-size: 13px; font-weight: 700; color: #0f172a;">🔔 Live Clinical Notifications</h3>
      <button onclick="toggleNotificationDrawer()" style="background: none; border: none; cursor: pointer; font-size: 16px;">&times;</button>
    </div>
    <div id="notifList">
      <div style="font-size: 12px; color: #94a3b8; text-align: center; padding: 12px;">No unread alerts.</div>
    </div>
  </div>

  <!-- JAVASCRIPT FRONTEND LOGIC -->
  <script>
    let currentTab = 'dashboard';
    let currentDocText = '';
    let notificationCount = 0;

    // Clock
    setInterval(() => {
      const now = new Date();
      document.getElementById('liveClock').textContent = now.toLocaleTimeString();
    }, 1000);

    // Tab Navigation
    function switchTab(tabId) {
      currentTab = tabId;
      document.querySelectorAll('.tab-content').forEach(el => el.classList.remove('active'));
      document.querySelectorAll('.nav-btn').forEach(el => el.classList.remove('active'));

      const target = document.getElementById('tab-' + tabId);
      if (target) target.classList.add('active');

      const btn = Array.from(document.querySelectorAll('.nav-btn')).find(b => b.getAttribute('onclick')?.includes(tabId));
      if (btn) btn.classList.add('active');

      const titles = {
        'dashboard': 'Hospital Dashboard & Census',
        'records': 'Electronic Health Records (EHR) & Chart Access',
        'triage': 'Emergency Intake & Triage Pipeline',
        'reports': 'Clinical Laboratories & Document Export',
        'department': 'Specialized Department Diagnostic Suites',
        'staff': 'Medical Personnel Directory & Onboarding',
        'audit': 'Hospital Compliance & Security Audit Registry'
      };
      document.getElementById('pageTitle').textContent = titles[tabId] || 'Hospital Management';

      if (tabId === 'dashboard') loadDashboard();
      if (tabId === 'triage') loadTriageQueue();
      if (tabId === 'staff') loadStaffDirectory();
      if (tabId === 'audit') loadAuditLogs();
      if (tabId === 'department') loadDepartmentSuite();
    }

    function onUserRoleChanged() {
      const user = document.getElementById('userSelector').value;
      addNotification("User session switched to: " + user);
      if (currentTab === 'records') {
        document.getElementById('recordSecurityAlert').style.display = 'none';
        document.getElementById('recordChartDetails').style.display = 'none';
      }
    }

    // 1. Dashboard
    async function loadDashboard() {
      try {
        const res = await fetch('/api/dashboard');
        const data = await res.json();
        document.getElementById('metricInpatients').textContent = data.inpatients;
        document.getElementById('metricStaff').textContent = data.staffCount;
        document.getElementById('metricTriage').textContent = data.triageCount;
        document.getElementById('metricAudit').textContent = data.auditEvents;

        // Load audit feed as recent activity
        const auditRes = await fetch('/api/audit');
        const auditLogs = await auditRes.json();
        const tbody = document.getElementById('dashboardActivityTable');
        tbody.innerHTML = '';
        const recent = auditLogs.slice(-5).reverse();
        if (recent.length === 0) {
          tbody.innerHTML = '<tr><td colspan="3" style="text-align:center;">No recent events recorded.</td></tr>';
          return;
        }
        recent.forEach(log => {
          const isDenied = log.includes('ACCESS_DENIED');
          const badgeClass = isDenied ? 'badge-danger' : 'badge-success';
          const badgeText = isDenied ? 'DENIED' : 'LOGGED';
          tbody.innerHTML += `<tr>
            <td style="font-family: monospace; font-size: 11px;">${escapeHtml(log)}</td>
            <td>Electronic Protected Health Info (ePHI)</td>
            <td><span class="badge ${badgeClass}">${badgeText}</span></td>
          </tr>`;
        });
      } catch (err) {
        console.error("Dashboard load failed", err);
      }
    }

    // 2. Patient Records (Protection Proxy)
    async function fetchPatientRecord() {
      const recordId = document.getElementById('recordSelector').value;
      const userId = document.getElementById('userSelector').value;
      const alertBox = document.getElementById('recordSecurityAlert');
      const chartBox = document.getElementById('recordChartDetails');

      alertBox.style.display = 'none';
      chartBox.style.display = 'none';

      try {
        const res = await fetch(`/api/records?recordId=${encodeURIComponent(recordId)}&userId=${encodeURIComponent(userId)}`);
        const data = await res.json();

        if (res.ok && data.success) {
          chartBox.style.display = 'block';
          document.getElementById('chartPatientTitle').textContent = "Patient: " + data.record.patientId;
          document.getElementById('chartRecordId').textContent = "Record Number: " + data.record.recordId;
          document.getElementById('chartDoctor').textContent = data.record.doctorId;
          document.getElementById('chartTimestamp').textContent = data.record.timestamp;
          document.getElementById('chartDiagnosis').textContent = data.record.diagnosis;
          document.getElementById('chartPrescription').textContent = data.record.prescription;
          addNotification("Patient chart #" + recordId + " opened by " + userId);
        } else {
          alertBox.style.display = 'block';
          document.getElementById('recordSecurityMsg').textContent = data.error || 'Access Denied: Insufficient clinical clearance for this record.';
          addNotification("⚠️ Security Alert: Access Denied for " + userId + " on record " + recordId);
        }
      } catch (err) {
        alertBox.style.display = 'block';
        document.getElementById('recordSecurityMsg').textContent = "Failed to connect to records server: " + err;
      }
    }

    // 3. Emergency Triage Routing
    async function submitTriage(e) {
      e.preventDefault();
      const patient = document.getElementById('triagePatient').value;
      const acuity = document.getElementById('triageAcuity').value;
      const complaint = document.getElementById('triageComplaint').value;

      try {
        const res = await fetch('/api/triage', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ patientName: patient, acuity: acuity, complaint: complaint })
        });
        const data = await res.json();

        const resultBox = document.getElementById('triageResultBox');
        resultBox.style.display = 'block';
        document.getElementById('triageResultText').innerHTML =
          `<strong>${escapeHtml(data.patient)}</strong> triaged successfully. Assigned to: <strong>${escapeHtml(data.resolvedBy)}</strong>.<br/>Acuity Tier: <span class="badge badge-info">${escapeHtml(data.type)}</span> &bull; Notes: "${escapeHtml(data.complaint)}"`;

        addNotification(`Patient ${data.patient} triaged &rarr; Assigned to ${data.resolvedBy}`);
        loadTriageQueue();
      } catch (err) {
        alert("Triage submission failed: " + err);
      }
    }

    async function loadTriageQueue() {
      try {
        const res = await fetch('/api/triage');
        const queue = await res.json();
        const tbody = document.getElementById('triageTableBody');
        tbody.innerHTML = '';
        if (queue.length === 0) {
          tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No pending patients in triage queue.</td></tr>';
          return;
        }
        queue.forEach(item => {
          let badge = 'badge-info';
          if (item.type === 'SPECIALIZED_CARE') badge = 'badge-danger';
          else if (item.type === 'PRIMARY_CARE') badge = 'badge-warning';
          else if (item.type === 'VITALS_TRIAGE') badge = 'badge-info';
          else badge = 'badge-gray';

          tbody.innerHTML += `<tr>
            <td style="font-family: monospace; font-weight: bold;">${escapeHtml(item.id)}</td>
            <td><strong>${escapeHtml(item.patient)}</strong></td>
            <td><span class="badge ${badge}">${escapeHtml(item.type)}</span></td>
            <td>${escapeHtml(item.complaint)}</td>
            <td><strong>${escapeHtml(item.resolvedBy || 'In Review')}</strong></td>
            <td><span class="badge badge-success">TRIAGED</span></td>
          </tr>`;
        });
      } catch (err) {
        console.error("Queue load failed", err);
      }
    }

    // 4. Clinical Reports & Lab Services
    async function completeLabOrder() {
      const patient = document.getElementById('labPatient').value;
      const reportId = document.getElementById('labReportId').value;

      try {
        const res = await fetch('/api/lab/complete', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ patientName: patient, reportId: reportId })
        });
        const data = await res.json();
        if (data.notifications) {
          data.notifications.forEach(msg => addNotification(msg));
        }
        alert("✓ Laboratory analysis complete! Dispatched alerts to patient mobile and physician console.");
        loadDashboard();
      } catch (err) {
        alert("Failed to complete lab order: " + err);
      }
    }

    async function generateMedicalDocument() {
      const docType = document.getElementById('reportDocType').value;
      const patient = document.getElementById('reportPatientName').value;
      const format = document.getElementById('reportFormat').value;

      try {
        const res = await fetch(`/api/reports?type=${encodeURIComponent(docType)}&format=${encodeURIComponent(format)}&patient=${encodeURIComponent(patient)}`);
        const data = await res.json();
        currentDocText = data.content;
        document.getElementById('documentPreviewBox').textContent = data.content;
        addNotification("Generated " + data.title + " (" + data.format + ")");
      } catch (err) {
        alert("Report generation failed: " + err);
      }
    }

    function downloadCurrentDocument() {
      if (!currentDocText) {
        alert("Please generate a document first.");
        return;
      }
      const format = document.getElementById('reportFormat').value;
      let filename = "Medical_Report." + (format === 'html' ? 'html' : (format === 'json' ? 'json' : 'pdf.txt'));
      const blob = new Blob([currentDocText], { type: 'text/plain' });
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = filename;
      a.click();
    }

    // 5. Department Diagnostic Suites
    async function loadDepartmentSuite() {
      const dept = document.getElementById('deptSelector').value;
      try {
        const res = await fetch(`/api/department?dept=${encodeURIComponent(dept)}`);
        const data = await res.json();

        document.getElementById('deptToolName').textContent = data.tool;
        document.getElementById('deptScanOutput').textContent = data.scanResult;
        document.getElementById('deptReportBadge').textContent = data.reportStandard;
        document.getElementById('deptReportSample').textContent = data.sampleReport;
        document.getElementById('deptConsentTitle').textContent = data.consentTitle;

        const discList = document.getElementById('deptDisclosuresList');
        discList.innerHTML = '';
        data.disclosures.forEach(d => {
          discList.innerHTML += `<li>${escapeHtml(d)}</li>`;
        });
        addNotification(data.department + " suite protocols calibrated.");
      } catch (err) {
        console.error("Dept load failed", err);
      }
    }

    // 6. Medical Staff Directory & Onboarding
    function toggleOnboardForm() {
      const box = document.getElementById('onboardBox');
      box.style.display = box.style.display === 'none' ? 'block' : 'none';
    }

    async function onboardStaff(e) {
      e.preventDefault();
      const name = document.getElementById('staffName').value;
      const role = document.getElementById('staffRole').value;
      const department = document.getElementById('staffDept').value;
      const specialization = document.getElementById('staffSpec').value;

      try {
        const res = await fetch('/api/staff/onboard', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name, role, department, specialization })
        });
        const data = await res.json();
        alert(`✓ Onboarding Complete!\n${data.role}: ${data.name} assigned to ${data.department}.\nMonthly Salary: $${data.salary}`);
        toggleOnboardForm();
        loadStaffDirectory();
        addNotification(`Onboarded new ${data.role}: ${data.name}`);
      } catch (err) {
        alert("Staff onboarding failed: " + err);
      }
    }

    async function loadStaffDirectory() {
      try {
        const res = await fetch('/api/staff');
        const list = await res.json();
        const tbody = document.getElementById('staffTableBody');
        tbody.innerHTML = '';
        list.forEach(s => {
          tbody.innerHTML += `<tr>
            <td><strong>${escapeHtml(s.name)}</strong></td>
            <td><span class="badge badge-info">${escapeHtml(s.role)}</span></td>
            <td>${escapeHtml(s.department)}</td>
            <td>${escapeHtml(s.specialization)}</td>
            <td style="font-size: 11px; color: #475569;">${escapeHtml(s.duty)}</td>
            <td style="font-weight: 700; color: #0f172a;">$${s.salary.toFixed(2)}</td>
          </tr>`;
        });
      } catch (err) {
        console.error("Staff load failed", err);
      }
    }

    // 7. Security & Compliance Audit Log
    async function loadAuditLogs() {
      try {
        const res = await fetch('/api/audit');
        const logs = await res.json();
        const tbody = document.getElementById('auditTableBody');
        tbody.innerHTML = '';
        if (logs.length === 0) {
          tbody.innerHTML = '<tr><td>No audit events logged.</td></tr>';
          return;
        }
        logs.slice().reverse().forEach(log => {
          const isDenied = log.includes('ACCESS_DENIED');
          const color = isDenied ? '#b91c1c' : '#0f172a';
          const bg = isDenied ? '#fef2f2' : 'transparent';
          tbody.innerHTML += `<tr style="background: ${bg};">
            <td style="font-family: monospace; font-size: 12px; color: ${color};">${escapeHtml(log)}</td>
          </tr>`;
        });
      } catch (err) {
        console.error("Audit load failed", err);
      }
    }

    // Notifications
    function addNotification(msg) {
      notificationCount++;
      document.getElementById('notifBadge').textContent = notificationCount;
      const list = document.getElementById('notifList');
      if (notificationCount === 1) list.innerHTML = '';
      const div = document.createElement('div');
      div.className = 'notification-item';
      div.innerHTML = `<span style="font-size: 10px; color: #64748b;">${new Date().toLocaleTimeString()}</span><br/>${escapeHtml(msg)}`;
      list.prepend(div);
    }

    function toggleNotificationDrawer() {
      const drawer = document.getElementById('notificationDrawer');
      drawer.style.display = drawer.style.display === 'none' || drawer.style.display === '' ? 'block' : 'none';
      if (drawer.style.display === 'block') {
        notificationCount = 0;
        document.getElementById('notifBadge').textContent = '0';
      }
    }

    function escapeHtml(s) {
      if (!s) return '';
      return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    // Initial Load
    window.addEventListener('DOMContentLoaded', () => {
      loadDashboard();
    });
  </script>
</body>
</html>
""";
}

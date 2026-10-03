# Design Patterns Lab - Viva Questions & Quick Reference

This document provides ready-to-answer explanations and textbook definitions for all **7 Design Patterns** implemented in this **Hospital Management System** lab project.

---

## 📋 Quick Pattern Classification Matrix

| # | Design Pattern | Category | Intent (Textbook Definition) | Hospital Role |
|---|---|---|---|---|
| **1** | **Singleton** | Creational | Ensure a class only has one instance, and provide a global point of access to it. | `AuditLogger` & `HospitalConfig` |
| **2** | **Factory Method** | Creational | Define an interface for creating an object, but let subclasses decide which class to instantiate. | `StaffFactory` (`DoctorFactory`, `NurseFactory`, `TechnicianFactory`) |
| **3** | **Abstract Factory** | Creational | Provide an interface for creating families of related or dependent objects without specifying their concrete classes. | `DepartmentEquipmentFactory` (Radiology vs. Cardiology matched suites) |
| **4** | **Bridge** | Structural | Decouple an abstraction from its implementation so that the two can vary independently. | `Report` abstraction $\leftrightarrow$ `ReportFormatter` implementor |
| **5** | **Chain of Responsibility** | Behavioral | Avoid coupling the sender of a request to its receiver by giving more than one object a chance to handle the request. | Patient triage pipeline (`Receptionist` $\rightarrow$ `Nurse` $\rightarrow$ `Doctor` $\rightarrow$ `Specialist`) |
| **6** | **Observer** | Behavioral | Define a one-to-many dependency between objects so that when one object changes state, all its dependents are notified automatically. | `LabService` (Subject) $\rightarrow$ `PatientObserver`, `DoctorObserver` |
| **7** | **Proxy** | Structural | Provide a surrogate or placeholder for another object to control access to it. | `MedicalRecordProxy` (Protection Proxy for EHR data) |

---

## 🎓 Viva Questions & Answers

### 1. Singleton Pattern
**Q: Why do we need Double-Checked Locking in `AuditLogger`?**  
**A:** Synchronizing the entire `getInstance()` method degrades performance because every single read locks the method. With Double-Checked Locking (DCL), we only synchronize the block when the instance is `null` (during the very first creation). The `volatile` keyword is critical because it prevents CPU instruction reordering, ensuring that the object is fully constructed before other threads see a non-null reference.

**Q: Why is the constructor `private`?**  
**A:** A private constructor prevents client code from using the `new` keyword to create additional instances from outside the class.

---

### 2. Factory Method Pattern
**Q: What is the core difference between Simple Factory and Factory Method?**  
**A:** A Simple Factory uses a single static method with `if-else` or `switch` statements to instantiate objects. The **Factory Method Pattern** relies on inheritance: an abstract creator class (`StaffFactory`) defines an abstract method (`createStaff`), and subclasses (`DoctorFactory`, `NurseFactory`, `TechnicianFactory`) override it to instantiate concrete products. This complies with the **Open/Closed Principle** (OCP)—you can add a new staff type by adding a new factory class without modifying existing code.

---

### 3. Abstract Factory Pattern
**Q: What is the difference between Factory Method and Abstract Factory?**  
**A:**
- **Factory Method** creates **one product** using inheritance (one factory method per creator).
- **Abstract Factory** creates **families of related products** using composition. In our hospital, `DepartmentEquipmentFactory` creates three related objects simultaneously: a `DiagnosticTool`, a `ReportGenerator`, and a `ConsentForm`. It ensures that a department never mixes a Cardiology consent form with an X-Ray machine.

---

### 4. Bridge Pattern
**Q: How does the Bridge Pattern prevent the "Combinatorial Class Explosion"?**  
**A:** If we have $M$ report types (`LabReport`, `DischargeSummary`, `PrescriptionReport`) and $N$ output formats (`PDF`, `HTML`, `JSON`), using traditional inheritance would require $M \times N = 3 \times 3 = 9$ classes (`PdfLabReport`, `HtmlLabReport`, `JsonLabReport`, etc.).  
With the Bridge Pattern, we separate the **Abstraction** (`Report`) from the **Implementor** (`ReportFormatter`). We only need $M + N = 3 + 3 = 6$ classes. Adding a new format (e.g., `CsvFormatter`) requires adding only 1 class, which works with all existing reports immediately!

---

### 5. Chain of Responsibility Pattern
**Q: How does request handling work in Chain of Responsibility?**  
**A:** Each handler holds a reference to the `nextHandler` in the chain. When a request arrives, the current handler checks if it can resolve it:
- If yes: It resolves the request and stops the chain.
- If no: It delegates the request to `nextHandler.handle(request)`.  
In our hospital: Clerical queries are solved by the `ReceptionistHandler`; vitals triage by the `NurseHandler`; routine symptoms by the `DoctorHandler`; and acute emergencies escalate to the `SpecialistHandler`.

---

### 6. Observer Pattern
**Q: Who are the Subject and Observers in your project?**  
**A:**
- **Subject (Observable)**: `LabService`, which maintains a list of registered observers and exposes `attach()`, `detach()`, and `notifyObservers()`.
- **Observers**: `PatientObserver` (sends SMS/portal alert) and `DoctorObserver` (sends beeper alert).  
When a lab report is completed, `labService.completeLabReport(...)` automatically notifies all attached observers without being coupled to how each observer displays or sends the message.

---

### 7. Proxy Pattern
**Q: What type of Proxy is used, and what is its purpose?**  
**A:** We implemented a **Protection Proxy** (`MedicalRecordProxy`). It implements the same interface (`MedicalRecordAccess`) as the real service (`RealMedicalRecordService`). Before delegating calls to the real service, the proxy verifies the user's role:
- Attending doctors and system admins are granted access.
- Receptionists and unassigned doctors are blocked with a `SecurityException`.
- Every attempt (granted or denied) is automatically logged via `AuditLogger.getInstance()`.

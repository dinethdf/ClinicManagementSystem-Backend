-- ==================================================================================
-- DATA SEEDING SCRIPT (MULTI-TENANT READY)
-- ==================================================================================
USE clinicmsystem;

-- 1. Create the Default Tenant (Clinic)
INSERT INTO clinics (id, name, subdomain, status, max_doctors, max_patients, createdDate)
VALUES (1, 'City Hospital', 'city', 'ACTIVE', 10, 50000, NOW());

-- 2. Master Roles
INSERT INTO user_role (id, name) VALUES (1, 'ADMIN'), (2, 'USER'), (3, 'DOCTOR'), (4, 'PATIENT'), (5, 'STAFF');

-- 3. Users (Linked to Clinic 1)
-- Note: clinic_id = 1 added to all auth records
INSERT INTO `userAuth` (`clinic_id`, `password`, `email`, `isEnable`, `role`) VALUES
(1, '$2a$12$Z/h2MTBuOrefYxbKkQGhcOxc5in8vvq0JY0yyxUnCncw8uL7HQQNm', 'dineth@admin.com', '1', '1'),
(1, '$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test1@user.com', '1', '2'),
(1, '$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test2@user.com', '1', '2'),
(1, '$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test2@staff.com', '1', '5'),
(1, '$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test1@staff.com', '1', '5');

-- Doctor User (We need to insert Doctor first to get ID, or insert Auth first and update.
-- Standard flow: Auth first -> Doctor Profile -> Link.
-- For seeding, we'll insert Auth, then Doctor with Auth ID 3, then update Auth with Details ID 1)
INSERT INTO `userAuth` (`clinic_id`, `password`, `email`, `isEnable`, `role`, `detailsId`) VALUES
(1, '$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test1@doctor.com', '1', '3', NULL);


-- 4. Specializations (Global)
INSERT INTO specialization (name, note) VALUES
('General Practitioner', 'Family Doctor'),
('Dermatologist', 'Skin Specialist'),
('Orthopedic Surgeon', 'Bone and Joint Specialist'),
('Cardiologist', 'Heart Specialist'),
('Endocrinologist', 'Diabetes & Hormone Specialist'),
('Nephrologist', 'Kidney Specialist'),
('Neurologist', 'Brain & Nerve Specialist'),
('Surgeon ', 'Surgical operations'),
('Ophthalmologist', 'Eye Specialist'),
('Dental Surgeon', 'Teeth & Oral Specialist');


-- 5. Doctors (Linked to Clinic 1)
INSERT INTO doctor (id, clinic_id, auth_id, firstName, lastName, nameWithInitials, gender, dob, nic, email, mobileNo, address, slmcNumber, specialization_id, qualification, experienceYears, availability_status, isApproved, createdDate, createdBy) VALUES
(1, 1, 6, 'Kamal', 'Perera', 'K. Perera', 1, '1980-05-12', '809934567V', 'test1@doctor.com', '0711234567', '123, Galle Road, Colombo', 'DOC001', '1', 'MBBS, MD (Cardiology)', 15, 'AVAILABLE', 1, NOW(), 'admin'),
(2, 1, NULL, 'Saman', 'Fernando', 'S. Fernando', 1, '1985-09-22', '851299567V', 'saman.fernando@clinic.com', '0779876543', '45, Temple Road, Kandy', 'DOC002', '2', 'MBBS, MD (Dermatology)', 10, 'AVAILABLE', 1, NOW(), 'admin'),
(3, 1, NULL, 'Nimali', 'Jayasinghe', 'N. Jayasinghe', 2, '1990-11-10', '901234599V', 'nimali.jayasinghe@clinic.com', '0705674321', '67, Main Street, Gampaha', 'DOC003', '3', 'MBBS, DCH', 8, 'ON_LEAVE', 1, NOW(), 'admin');


-- 6. Patients (Linked to Clinic 1)
INSERT INTO patientAuth (`clinic_id`, `password`, `email`, `isEnable`, `isTempCredential`, `role`) VALUES
(1, '$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test@patient.com', '1', '1', '4');

INSERT INTO patient (id, clinic_id, patient_auth_id, patientNo, firstName, lastName, fullName, ethnicity, gender, maritalStatus, nic, dob, address, city, district, email, mobileNo, landNo, emergencyContactName, emergencyContactRelationship, emergencyContactMobile, emergencyContactEmail, bloodGroup, registrationDate, profileImg, isActive, isApproved, createdDate, createdBy, updatedDate, updatedBy)
VALUES
(50, 1, 1, 'P250050', 'Fasdad', 'Qweqwe', 'Fasdad Qweqwe', 1, 1, 1, '201232345678', '2000-10-24', '123 Main Street', 'Colombo', 'Colombo', 'fasdad@example.com', '0776935124', '0112345678', 'Jane Doe', 'Spouse', '+94787654321', 'janedoe@example.com', 'AB-', '2025-09-30 10:30:00', 'https://example.com/images/patient1.png', 1, 1, '2025-10-24 12:20:41', 'test1@user.com', '2025-10-24 12:20:41', 'test1@user.com'),
(51, 1, NULL, 'P250002', 'Kamal', 'Perera', 'Kamal Perera', 2, 1, 2, '201232345679', '1998-05-12', '45 Lake Road', 'Kandy', 'Kandy', 'kamalp@demo.com', '0776935124', '0812233445', 'Sunil Perera', 'Father', '+94775543210', 'sunilp@demo.com', 'B+', '2025-10-01 11:00:00', 'https://example.com/images/patient2.png', 1, 1, '2025-10-24 12:25:00', 'admin@demo.com', '2025-10-24 12:25:00', 'admin@demo.com'),
(60, 1, NULL, 'P250011', 'Isuru', 'Abeywickrama', 'Isuru Abeywickrama', 2, 1, 1, '201232345688', '1992-03-12', '7 Lotus Avenue', 'Kalutara', 'Kalutara', 'isuru@demo.com', '0776935124', '0343344556', 'Suresh Abeywickrama', 'Father', '+94778881234', 'suresha@demo.com', 'B-', '2025-10-06 10:45:00', 'https://example.com/images/patient11.png', 1, 1, '2025-10-24 13:15:00', 'staff@demo.com', '2025-10-24 13:15:00', 'staff@demo.com'),
(61, 1, NULL, 'P250012', 'Thilini', 'De Silva', 'Thilini De Silva', 3, 2, 1, '201232345689', '1996-09-20', '12 Rose Lane', 'Colombo', 'Colombo', 'thilini@demo.com', '0776935124', '0117788990', 'Sameera De Silva', 'Husband', '+94775554433', 'sameerad@demo.com', 'A+', '2025-10-06 15:20:00', 'https://example.com/images/patient12.png', 1, 1, '2025-10-24 13:20:00', 'staff@demo.com', '2025-10-24 13:20:00', 'staff@demo.com'),
(62, 1, NULL, 'P250013', 'Madushan', 'Ranasinghe', 'Madushan Ranasinghe', 1, 1, 1, '201232345690', '1989-01-10', '25 Beach Road', 'Gampaha', 'Gampaha', 'madushan@demo.com', '0776935124', '0332233445', 'Lakmini Ranasinghe', 'Wife', '+94770002211', 'lakmini@demo.com', 'AB-', '2025-10-06 14:10:00', 'https://example.com/images/patient13.png', 1, 1, '2025-10-24 13:25:00', 'staff@demo.com', '2025-10-24 13:25:00', 'staff@demo.com');


-- 7. Schedules (Linked to Clinic 1)
INSERT INTO doctorSchedule (id, clinic_id, doctor_id, scheduleDate, startTime, endTime, doctorFee, clinicFee, maxPatientCount, isActive, createdDate, createdBy) VALUES
(1, 1, 1, '2025-12-10', '09:00:00', '12:00:00', 3500.00, 1500.00, 35, 1, NOW(), 'kamal.perera'),
(2, 1, 1, '2025-12-12', '14:00:00', '17:00:00', 3500.00, 1500.00, 12, 1, NOW(), 'kamal.perera'),
(11, 1, 2, '2025-12-10', '10:00:00', '12:00:00', 2500.00, 1500.00, 10, 1, NOW(), 'saman.fernando'),
(21, 1, 3, '2025-12-11', '08:30:00', '10:00:00', 3000.00, 1500.00, 8, 1, NOW(), 'nimali.jayasinghe');


-- 8. Global Items
INSERT INTO item_types (typeName, description) VALUES
('APPOINTMENT', 'Doctor Consultation or Booking'),
('LAB_TEST', 'Laboratory Test Request'),
('PHARMACY', 'Pharmacy Sale or Prescription'),
('DOCTOR_DISCOUNT', 'Discount given by the doctor'),
('CLINIC_DISCOUNT', 'Discount or promotion from the clinic');


-- 9. Transactional Data for Clinic 1 (Schedule 2)

-- A. Create Appointments
-- Note: 'clinic_id' added to appointments
INSERT INTO appointments (clinic_id, doctor_schedule_id, patient_id, queuePosition, status, invoice_id, createdDate, createdBy) VALUES
(1, 2, 60, 1, 'COMPLETED', NULL, NOW(), 'admin'),
(1, 2, 61, 2, 'COMPLETED', NULL, NOW(), 'admin'),
(1, 2, 62, 3, 'COMPLETED', NULL, NOW(), 'admin');

-- B. Create Invoices
INSERT INTO invoices (clinic_id, patient_id, status, subtotalAmount, totalDiscount, finalAmount, createdDate, createdBy) VALUES
(1, 60, 'PAID', 5000.00, 1000.00, 4000.00, NOW(), 'admin'),
(1, 61, 'PAID', 5000.00, 0.00, 5000.00, NOW(), 'admin'),
(1, 62, 'PAID', 5000.00, 0.00, 5000.00, NOW(), 'admin');

-- C. Link Appointments to Invoices
-- (Safe update using clinic_id filter)
UPDATE appointments a
JOIN invoices i ON a.patient_id = i.patient_id AND a.clinic_id = i.clinic_id
SET a.invoice_id = i.id
WHERE a.doctor_schedule_id = 2 AND a.clinic_id = 1;


-- D. Invoice Items
INSERT INTO invoice_items (clinic_id, invoice_id, item_type_id, description, amount, createdDate, createdBy) VALUES
(1, (SELECT id FROM invoices WHERE patient_id = 60 AND clinic_id = 1 ORDER BY id DESC LIMIT 1), 1, 'Consultation Fee', 5000.00, NOW(), 'admin'),
(1, (SELECT id FROM invoices WHERE patient_id = 60 AND clinic_id = 1 ORDER BY id DESC LIMIT 1), 4, 'Doctor Discount', -500.00, NOW(), 'admin'),
(1, (SELECT id FROM invoices WHERE patient_id = 61 AND clinic_id = 1 ORDER BY id DESC LIMIT 1), 1, 'Consultation Fee', 5000.00, NOW(), 'admin'),
(1, (SELECT id FROM invoices WHERE patient_id = 62 AND clinic_id = 1 ORDER BY id DESC LIMIT 1), 1, 'Consultation Fee', 5000.00, NOW(), 'admin');


-- E. Payments
INSERT INTO payments (clinic_id, invoice_id, amountPaid, paymentMethod, paymentStatus, transactionId, createdDate, createdBy) VALUES
(1, (SELECT id FROM invoices WHERE patient_id = 60 AND clinic_id = 1 ORDER BY id DESC LIMIT 1), 4000.00, 'CASH', 'COMPLETED', 'TRX-01', NOW(), 'admin'),
(1, (SELECT id FROM invoices WHERE patient_id = 61 AND clinic_id = 1 ORDER BY id DESC LIMIT 1), 5000.00, 'CARD', 'COMPLETED', 'TRX-02', NOW(), 'admin'),
(1, (SELECT id FROM invoices WHERE patient_id = 62 AND clinic_id = 1 ORDER BY id DESC LIMIT 1), 5000.00, 'CASH', 'COMPLETED', 'TRX-03', NOW(), 'admin');


-- 10. System Config (Tenant Specific)
-- Primary Key is (clinic_id, configKey)
INSERT INTO systemConfig (clinic_id, configKey, configValue, createdDate, createdBy)
VALUES
(1, 'SMS_OTP_ENABLED', 'true', NOW(), 'SYSTEM'),
(1, 'EMAIL_OTP_ENABLED', 'true', NOW(), 'SYSTEM'),
(1, 'CLINIC_THEME', 'BLUE', NOW(), 'SYSTEM');

-- 11. Expense Categories (Global)
INSERT INTO expense_categories (name, description) VALUES
('RENT', 'Monthly Building Rent'),
('UTILITY', 'Electricity, Water, Internet'),
('SALARY', 'Staff Salaries'),
('MAINTENANCE', 'Repairs and Cleaning');

-- 12. Final Update for Schedule Counts
UPDATE `doctorSchedule` SET `registeredPatientCount` = '3', `lastQueueNumber` = '3' WHERE (`id` = '2' AND `clinic_id` = 1);
-- ==================================================================================
-- DATABASE INITIALIZATION
-- ==================================================================================
DROP DATABASE IF EXISTS clinicmsystem;
CREATE DATABASE IF NOT EXISTS clinicmsystem CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE clinicmsystem;

-- ==================================================================================
-- MASTER TENANT TABLE (NEW)
-- ==================================================================================
CREATE TABLE clinics (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    subdomain VARCHAR(50) UNIQUE NOT NULL, -- e.g., 'cityhospital'

    status ENUM('ACTIVE', 'SUSPENDED', 'EXPIRED') DEFAULT 'ACTIVE',
    subscription_plan VARCHAR(50) DEFAULT 'BASIC',

    -- Configs
    max_doctors INT DEFAULT 5,
    max_patients INT DEFAULT 10000,
    expiry_date DATE,

    createdDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    updatedDate DATETIME DEFAULT NULL
) ENGINE=InnoDB;


CREATE TABLE user_role (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL -- 'ADMIN', 'DOCTOR', 'RECEPTIONIST'
);

CREATE TABLE specialization (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    note VARCHAR(100)
);

CREATE TABLE item_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    typeName VARCHAR(50) NOT NULL UNIQUE, -- 'APPOINTMENT', 'PHARMACY'
    description VARCHAR(255) NULL
) ENGINE=InnoDB;

CREATE TABLE expense_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE, -- 'RENT', 'SALARY'
    description VARCHAR(255) NULL,
    isActive TINYINT DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE systemConfig (
    clinic_id INT NOT NULL, -- Tenant Link
    configKey VARCHAR(100) NOT NULL,
    configValue VARCHAR(255) NOT NULL,

    createdDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50) DEFAULT NULL,

    -- PRIMARY KEY is now Composite
    -- You identify a config by "Which Clinic?" + "Which Key?"
    PRIMARY KEY (clinic_id, configKey),

    CONSTRAINT fk_config_clinic
        FOREIGN KEY (clinic_id)
        REFERENCES clinics(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;


CREATE TABLE userAuth (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link

    password VARCHAR(255) DEFAULT NULL,
    email VARCHAR(255) DEFAULT NULL,
    isEnable TINYINT NOT NULL DEFAULT 1,
    role INT DEFAULT NULL,
    detailsId INT DEFAULT NULL,

    CONSTRAINT fk_userAuth_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    FOREIGN KEY (role) REFERENCES user_role(id) ON DELETE SET NULL,

    -- Email is unique per clinic (Siloed Approach)
    UNIQUE INDEX idx_email_per_clinic (clinic_id, email)
) ENGINE=InnoDB;

CREATE TABLE patientAuth (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link

    password VARCHAR(255) DEFAULT NULL,
    email VARCHAR(255) DEFAULT NULL,
    isEnable TINYINT NOT NULL DEFAULT 1,
    isTempCredential TINYINT NOT NULL DEFAULT 1,
    role INT DEFAULT NULL,

    CONSTRAINT fk_patientAuth_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    FOREIGN KEY (role) REFERENCES user_role(id) ON DELETE SET NULL,

    UNIQUE INDEX idx_patient_email_per_clinic (clinic_id, email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE otpVerification (
    id INT AUTO_INCREMENT NOT NULL,
    mobileNo VARCHAR(20) NOT NULL,
    email VARCHAR(100) DEFAULT NULL,
    otpCode VARCHAR(10) NOT NULL,
    expiresAt DATETIME NOT NULL,
    verified TINYINT DEFAULT 0,
    dailySmsCount INT DEFAULT 0,
    lastSmsDate DATE DEFAULT NULL,

    createdDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50) DEFAULT NULL,

    PRIMARY KEY (id),

    UNIQUE KEY uk_mobileNo (mobileNo)
);


CREATE TABLE insurance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link (Optional but good for filtering)

    provider_name VARCHAR(100) NOT NULL,
    policy_number VARCHAR(100) NOT NULL,
    coverage_type VARCHAR(50),
    isActive TINYINT NOT NULL DEFAULT 0,
    expiration_date DATE,

    CONSTRAINT fk_insurance_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE patient (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL,

    patient_auth_id INT DEFAULT NULL,
    patientNo VARCHAR(100) NULL, -- Unique Constraint moved to Index
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    fullName VARCHAR(255),
    ethnicity TINYINT UNSIGNED NULL,
    gender TINYINT UNSIGNED NOT NULL,
    maritalStatus TINYINT UNSIGNED NULL,
    nic VARCHAR(20), -- Unique Constraint moved to Index
    dob DATE NOT NULL,
    address TEXT DEFAULT NULL,
    city VARCHAR(100),
    district VARCHAR(100),
    email VARCHAR(150) DEFAULT NULL,
    mobileNo VARCHAR(20) DEFAULT NULL,
    landNo VARCHAR(20),

    guardian_id INT DEFAULT NULL,
    relationship_to_guardian VARCHAR(50),

    emergencyContactName VARCHAR(100),
    emergencyContactRelationship VARCHAR(50),
    emergencyContactMobile VARCHAR(20),
    emergencyContactEmail VARCHAR(150),

    bloodGroup ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') DEFAULT NULL,
    registrationDate DATETIME,
    profileImg VARCHAR(255),
    isActive TINYINT NOT NULL DEFAULT 0,
    isApproved TINYINT NOT NULL DEFAULT 0,
    insurance_id INT DEFAULT NULL,

    -- System
    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    -- Constraints
    CONSTRAINT fk_patient_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_patient_guardian FOREIGN KEY (guardian_id) REFERENCES patient(id),
    CONSTRAINT fk_patient_auth FOREIGN KEY (patient_auth_id) REFERENCES patientAuth(id) ON DELETE SET NULL,
    CONSTRAINT fk_patient_insurance FOREIGN KEY (insurance_id) REFERENCES insurance(id) ON DELETE SET NULL,

    UNIQUE INDEX idx_patientNo_per_clinic (clinic_id, patientNo),
    UNIQUE INDEX idx_nic_per_clinic (clinic_id, nic)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE patient_allergies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link
    patient_id INT NOT NULL,
    allergyName VARCHAR(100) NOT NULL,
    notes TEXT,

    CONSTRAINT fk_allergy_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE
);

CREATE TABLE patient_chronic_diseases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link
    patient_id INT NOT NULL,
    diseaseName VARCHAR(100) NOT NULL,
    notes TEXT,

    CONSTRAINT fk_disease_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE
);

CREATE TABLE doctor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link

    auth_id INT DEFAULT NULL,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    nameWithInitials VARCHAR(150),
    gender TINYINT UNSIGNED NOT NULL,
    dob DATE,
    nic VARCHAR(20), -- Scoped Unique
    email VARCHAR(150), -- Scoped Unique
    mobileNo VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    district VARCHAR(100),
    registrationDate DATETIME,
    profileImg VARCHAR(255),

    slmcNumber VARCHAR(50), -- Scoped Unique
    specialization_id INT NOT NULL,
    qualification VARCHAR(255),
    experienceYears INT,
    availability_status ENUM('AVAILABLE', 'ON_LEAVE', 'UNAVAILABLE') DEFAULT 'AVAILABLE' NOT NULL,
    isApproved TINYINT NOT NULL DEFAULT 0,

    -- System
    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    -- Constraints
    CONSTRAINT fk_doctor_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_doctor_specialization FOREIGN KEY (specialization_id) REFERENCES specialization(id) ON DELETE RESTRICT ON UPDATE CASCADE,

    -- Multi-Tenant Uniqueness
    UNIQUE INDEX idx_slmc_per_clinic (clinic_id, slmcNumber),
    UNIQUE INDEX idx_doc_email_per_clinic (clinic_id, email),
    UNIQUE INDEX idx_doc_nic_per_clinic (clinic_id, nic)
);

CREATE TABLE clinic_staff (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link

    auth_id INT DEFAULT NULL,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    nameWithInitials VARCHAR(150),
    gender TINYINT UNSIGNED NOT NULL,
    dob DATE,
    nic VARCHAR(20),
    email VARCHAR(150),
    mobileNo VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    district VARCHAR(100),
    joinDate DATETIME,
    profileImg VARCHAR(255),
    designation VARCHAR(100),
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE' NOT NULL,

    -- System
    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),

    CONSTRAINT fk_staff_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    UNIQUE INDEX idx_staff_email_per_clinic (clinic_id, email),
    UNIQUE INDEX idx_staff_nic_per_clinic (clinic_id, nic)
);

CREATE TABLE doctorSchedule (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link
    doctor_id INT NOT NULL,
    scheduleDate DATE NOT NULL,
    startTime TIME NOT NULL,
    endTime TIME NOT NULL,
    doctorFee DECIMAL(10,2) NOT NULL,
    clinicFee DECIMAL(10,2) NOT NULL,
    maxPatientCount INT NOT NULL,
    registeredPatientCount INT NOT NULL DEFAULT 0,
    lastQueueNumber INT NOT NULL DEFAULT 0,
    isActive TINYINT DEFAULT 1,
    status ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    bookingStatus ENUM('OPEN', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    payoutStatus ENUM('NOT_PAID', 'PENDING', 'PAID') NOT NULL DEFAULT 'NOT_PAID',
    isApproved TINYINT NOT NULL DEFAULT 0,

    -- System
    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    CONSTRAINT fk_schedule_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),

    -- Prevent overlapping schedules or duplicate slots for same doc in same clinic
    INDEX idx_schedule_lookup (clinic_id, doctor_id, scheduleDate)
);

CREATE TABLE invoices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link
    patient_id INT NOT NULL,

    status ENUM('UNPAID', 'PAID', 'PARTIAL', 'CANCELLED') NOT NULL DEFAULT 'UNPAID',
    subtotalAmount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    totalDiscount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    finalAmount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    dueDate DATE NULL,
    isApproved TINYINT NOT NULL DEFAULT 0,

    -- System
    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    CONSTRAINT fk_invoice_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_invoice_patient FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE invoice_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link (Optional, inherited from invoice, but safer for direct queries)

    invoice_id INT NOT NULL,
    item_type_id INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    isApproved TINYINT NOT NULL DEFAULT 0,

    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    CONSTRAINT fk_inv_item_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_type FOREIGN KEY (item_type_id) REFERENCES item_types(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link

    invoice_id INT NOT NULL,
    amountPaid DECIMAL(10, 2) NOT NULL,
    paymentMethod ENUM('CASH', 'CARD', 'BANK_TRANSFER', 'ONLINE', 'INSURANCE') NOT NULL,
    paymentStatus ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    transactionId VARCHAR(255) NULL,
    paymentTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    isApproved TINYINT NOT NULL DEFAULT 0,

    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    CONSTRAINT fk_payment_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link

    queuePosition INT NOT NULL,
    note TEXT NULL,
    status ENUM('PENDING_PAYMENT', 'BOOKED', 'ONGOING', 'COMPLETED', 'CANCELLED', 'NOSHOW') NOT NULL DEFAULT 'PENDING_PAYMENT',
    isApproved TINYINT NOT NULL DEFAULT 0,

    doctor_schedule_id INT NOT NULL,
    patient_id INT NOT NULL,
    invoice_id INT NULL,

    -- System
    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    -- Foreign Keys
    CONSTRAINT fk_appt_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_appointment_schedule FOREIGN KEY (doctor_schedule_id) REFERENCES doctorSchedule(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL,

    -- Critical Multi-Tenant Index: Unique queue position PER Schedule
    UNIQUE INDEX idx_unique_queue (clinic_id, doctor_schedule_id, queuePosition),
    INDEX idx_patient_lookup (clinic_id, patient_id)
) ENGINE=InnoDB;

CREATE TABLE prescriptions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link

    appointment_id INT NOT NULL,
    status ENUM('ISSUED', 'DISPENSED', 'CANCELLED') DEFAULT 'ISSUED',
    note TEXT,

    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    CONSTRAINT fk_pres_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_prescriptions_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    UNIQUE INDEX idx_appointment_unique (appointment_id)
) ENGINE=InnoDB;

CREATE TABLE prescription_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link (For safer queries)

    prescription_id INT NOT NULL,
    medicineName VARCHAR(255) NOT NULL,
    dosage VARCHAR(100),
    quantity VARCHAR(50),
    duration VARCHAR(50),
    instructions TEXT,

    CONSTRAINT fk_pres_item_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_items_prescription FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ==================================================================================
-- FINANCIAL LIABILITY TABLES
-- ==================================================================================

CREATE TABLE doctor_schedule_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link

    doctor_schedule_id INT NOT NULL,
    totalDoctorFee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    totalDoctorDiscounts DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    finalPayableAmount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status ENUM('PENDING', 'PARTIAL', 'PAID') NOT NULL DEFAULT 'PENDING',

    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    CONSTRAINT fk_dsp_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_dsp_schedule FOREIGN KEY (doctor_schedule_id) REFERENCES doctorSchedule(id) ON DELETE RESTRICT,
    UNIQUE INDEX idx_unique_schedule_payment (clinic_id, doctor_schedule_id)
) ENGINE=InnoDB;

CREATE TABLE lab_vendor_payouts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link

    vendor_name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    fromDate DATE NULL,
    toDate DATE NULL,
    totalBillAmount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    totalDiscount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    finalPayableAmount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status ENUM('PENDING', 'PARTIAL', 'PAID') NOT NULL DEFAULT 'PENDING',

    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    CONSTRAINT fk_lab_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id)
) ENGINE=InnoDB;

CREATE TABLE operational_expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link

    expense_category_id INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    dueDate DATE NULL,
    status ENUM('PENDING', 'PARTIAL', 'PAID') NOT NULL DEFAULT 'PENDING',

    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    CONSTRAINT fk_opex_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_expense_category FOREIGN KEY (expense_category_id) REFERENCES expense_categories(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE payout_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    clinic_id INT NOT NULL, -- Tenant Link

    doctor_payment_id INT NULL,
    lab_payout_id INT NULL,
    expense_id INT NULL,

    amountPaid DECIMAL(10, 2) NOT NULL,
    paymentMethod ENUM('CASH', 'CARD', 'BANK_TRANSFER', 'ONLINE', 'CHEQUE') NOT NULL,
    transactionReference VARCHAR(255) NULL,
    paymentDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    CONSTRAINT fk_tx_clinic FOREIGN KEY (clinic_id) REFERENCES clinics(id),
    CONSTRAINT fk_tx_doctor FOREIGN KEY (doctor_payment_id) REFERENCES doctor_schedule_payments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tx_lab FOREIGN KEY (lab_payout_id) REFERENCES lab_vendor_payouts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_tx_expense FOREIGN KEY (expense_id) REFERENCES operational_expenses(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
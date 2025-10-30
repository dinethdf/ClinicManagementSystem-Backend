-- Create the database if it does not exist
drop database clinicmsystem;
CREATE DATABASE IF NOT EXISTS clinicmsystem;

-- Use the database
USE clinicmsystem;


CREATE TABLE user_role (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) UNIQUE NOT NULL, -- e.g., 'USER', 'ADMIN'
  PRIMARY KEY (id)
);

CREATE TABLE userAuth (
  id INT NOT NULL AUTO_INCREMENT,
  password VARCHAR(255) DEFAULT NULL,
  email VARCHAR(255) UNIQUE DEFAULT NULL,
  isEnable TINYINT(4) NOT NULL DEFAULT 1,
  role INT DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (role) REFERENCES user_role(id) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;



CREATE TABLE patientAuth (
  id INT NOT NULL AUTO_INCREMENT,
  password VARCHAR(255) DEFAULT NULL,
  email VARCHAR(255) UNIQUE DEFAULT NULL,
  isEnable TINYINT(4) NOT NULL DEFAULT 1,
  isTempCredential TINYINT(4) NOT NULL DEFAULT 1,
  role INT DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (role) REFERENCES user_role(id) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;



CREATE TABLE insurance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    provider_name VARCHAR(100) NOT NULL,
    policy_number VARCHAR(100) NOT NULL,
    coverage_type VARCHAR(50),
    isActive TINYINT(4) NOT NULL DEFAULT 0,
    expiration_date DATE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE patient (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_auth_id INT DEFAULT NULL,
    patientNo VARCHAR(100) NULL UNIQUE,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    fullName VARCHAR(255),
    ethnicity TINYINT UNSIGNED NOT NULL,
    gender TINYINT UNSIGNED NOT NULL,
    maritalStatus TINYINT UNSIGNED NOT NULL,
    nic VARCHAR(20) UNIQUE,
    dob DATE NOT NULL,
    address TEXT DEFAULT NULL,
    city VARCHAR(100),
    district VARCHAR(100),
    email VARCHAR(150) DEFAULT NULL,
    mobileNo VARCHAR(20) DEFAULT NULL,
    landNo VARCHAR(20),

	guardian_id INT DEFAULT NULL,  -- link to another patient
    relationship_to_guardian VARCHAR(50),  -- e.g., 'Father', 'Mother', 'Guardian'

    emergencyContactName VARCHAR(100) NOT NULL,
    emergencyContactRelationship VARCHAR(50),
    emergencyContactMobile VARCHAR(20),
    emergencyContactEmail VARCHAR(150),

	bloodGroup ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') default NULL,

    registrationDate DATETIME,
    profileImg VARCHAR(255),

    isActive TINYINT(1) NOT NULL DEFAULT 0,
    isApproved TINYINT(1) NOT NULL DEFAULT 0,

	insurance_id INT DEFAULT NULL,

    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
	updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

	CONSTRAINT fk_patient_guardian FOREIGN KEY (guardian_id) REFERENCES patient(id),
    CONSTRAINT fk_patient_auth FOREIGN KEY (patient_auth_id) REFERENCES PatientAuth(id) ON DELETE SET NULL,
    CONSTRAINT fk_insurance FOREIGN KEY (insurance_id) REFERENCES insurance(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE patient_allergies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_details_id INT NOT NULL,
    allergyName VARCHAR(100) NOT NULL,
    notes TEXT,
    FOREIGN KEY (patient_details_id) REFERENCES patient(id) ON DELETE CASCADE
);
CREATE TABLE patient_chronic_diseases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_details_id INT NOT NULL,
    diseaseName VARCHAR(100) NOT NULL,
    notes TEXT,
    FOREIGN KEY (patient_details_id) REFERENCES patient(id) ON DELETE CASCADE
);

CREATE TABLE specialization (
	id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    note VARCHAR(100));

CREATE TABLE doctor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    auth_id INT DEFAULT NULL,

    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    nameWithInitials VARCHAR(150),
    gender TINYINT UNSIGNED NOT NULL,
    dob DATE,
    nic VARCHAR(20) UNIQUE,
    email VARCHAR(150) UNIQUE,
    mobileNo VARCHAR(20),
    address VARCHAR(255),
    city VARCHAR(100),
    district VARCHAR(100),

    registrationDate DATETIME,
    profileImg VARCHAR(255),

    slmcNumber VARCHAR(50) UNIQUE,
    specialization_id INT NOT NULL,
    qualification VARCHAR(255),
    experienceYears INT,
    availability_status ENUM('AVAILABLE', 'ON_LEAVE', 'UNAVAILABLE') DEFAULT 'AVAILABLE' NOT NULL,
    isApproved TINYINT(1) NOT NULL DEFAULT 0,

    -- System Columns
    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
	updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    -- Relationships

--     CONSTRAINT fk_doctor_auth
--         FOREIGN KEY (auth_id) REFERENCES userAuth(id)
--         ON DELETE CASCADE
--         ON UPDATE CASCADE\

		CONSTRAINT fk_doctor_specialization FOREIGN KEY (specialization_id) REFERENCES specialization(id) ON DELETE CASCADE ON UPDATE CASCADE

);

CREATE TABLE doctorSchedule (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id INT NOT NULL,
    scheduleDate DATE NOT NULL,
    startTime TIME NOT NULL,
    endTime TIME NOT NULL,
    payment DECIMAL(10,2) NOT NULL,
    maxPatientCount INT NOT NULL,
    registeredPatientCount INT  NOT NULL default 0,
    isActive TINYINT(1) DEFAULT 1,
    isApproved TINYINT(1) NOT NULL DEFAULT 0,

    -- System Columns
    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
	updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    FOREIGN KEY (doctor_id) REFERENCES doctor(id)
);

CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    appointmentNumber VARCHAR(50) NOT NULL UNIQUE,
    queuePosition INT NOT NULL,
    note TEXT NULL,
    status ENUM(
    'PENDING_PAYMENT', -- New status (replaces 'Booked')
    'BOOKED',       -- New status (after payment)
    'COMPLETED',
    'CANCELLED',
    'NOSHOW'
) NOT NULL DEFAULT 'PENDING_PAYMENT',
    doctor_schedule_id INT NOT NULL,
    patient_id INT NOT NULL,

    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50) DEFAULT NULL,
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50) DEFAULT NULL,

    CONSTRAINT fk_appointment_schedule
        FOREIGN KEY (doctor_schedule_id)
        REFERENCES doctorSchedule(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_appointment_patient
        FOREIGN KEY (patient_id)
        REFERENCES patient(id)
        ON DELETE RESTRICT,

    INDEX idx_schedule_patient (doctor_schedule_id, patient_id),
    INDEX idx_patient (patient_id)

) ENGINE=InnoDB;

CREATE TABLE invoices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    status ENUM('UNPAID', 'PAID', 'PARTIAL', 'CANCELLED') NOT NULL DEFAULT 'UNPAID',
    totalAmount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    dueDate DATE NULL,

    -- Your System Columns
    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50) DEFAULT NULL,
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50) DEFAULT NULL,

    -- Link to the patient table
    CONSTRAINT fk_invoice_patient
        FOREIGN KEY (patient_id)
        REFERENCES patient(id)
        ON DELETE RESTRICT,

    -- Index for fast lookup
    INDEX idx_patient (patient_id)

) ENGINE=InnoDB;

CREATE TABLE item_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    -- 'APPOINTMENT', 'LABTEST', 'SUBSCRIPTION', 'PHARMACY', DOCTOR_DISCOUNT, CLINIC_DISCOUNT
    typeName VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL
) ENGINE=InnoDB;

CREATE TABLE invoice_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    item_type_id INT NOT NULL,
    item_id INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,

    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50) DEFAULT NULL,
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50) DEFAULT NULL,

    CONSTRAINT fk_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE RESTRICT,
    CONSTRAINT fk_item_type FOREIGN KEY (item_type_id) REFERENCES item_types(id) ON DELETE RESTRICT,

    INDEX idx_invoice (invoice_id),
    INDEX idx_item (item_type_id, item_id)

) ENGINE=InnoDB;


CREATE TABLE payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    amountPaid DECIMAL(10, 2) NOT NULL,
    paymentMethod ENUM('Cash', 'Card', 'OnlineGateway', 'Insurance') NOT NULL,
    paymentStatus ENUM('Pending', 'Completed', 'Failed', 'Refunded') NOT NULL DEFAULT 'Pending',
    transactionId VARCHAR(255) NULL,
    paymentTimestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50) DEFAULT NULL,
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50) DEFAULT NULL,

    CONSTRAINT fk_payment_invoice
        FOREIGN KEY (invoice_id)
        REFERENCES invoices(id)
        ON DELETE RESTRICT,

    INDEX idx_invoice (invoice_id)

) ENGINE=InnoDB;
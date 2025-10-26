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

INSERT INTO user_role (name) VALUES ('ADMIN'), ('USER'), ('PATIENT'),('STAFF');

INSERT INTO `userAuth` (`password`, `email`, `isEnable`, `role`) VALUES ('$2a$12$Z/h2MTBuOrefYxbKkQGhcOxc5in8vvq0JY0yyxUnCncw8uL7HQQNm', 'dineth@admin.com', '1', '1');
INSERT INTO `userAuth` (`password`, `email`, `isEnable`, `role`) VALUES ('$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test1@user.com', '1', '2');
INSERT INTO `userAuth` (`password`, `email`, `isEnable`, `role`) VALUES ('$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test1@staff.com', '1', '4');

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

INSERT INTO PatientAuth (`password`, `email`, `isEnable`, `isTempCredential`, `role`) VALUES ('$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test@patient.com', '1', '1', '3');

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
    profileImg VARCHAR(255),

    registrationNumber VARCHAR(50) UNIQUE,
    specialization VARCHAR(100),
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
    approvedBy VARCHAR(50)

    -- Relationships

--     CONSTRAINT fk_doctor_auth
--         FOREIGN KEY (auth_id) REFERENCES userAuth(id)
--         ON DELETE CASCADE
--         ON UPDATE CASCADE

);

CREATE TABLE doctorSchedule (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id INT NOT NULL,
    scheduleDate DATE NOT NULL,
    scheduleTime TIME NOT NULL,
    payment DECIMAL(10,2) NOT NULL,
    maxPatientCount INT NOT NULL,
    isActive TINYINT(1) DEFAULT 1,

    -- System Columns
    createdDate DATETIME NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
	updatedDate DATETIME DEFAULT NULL,
    updatedBy VARCHAR(50),
    approvedDate DATETIME DEFAULT NULL,
    approvedBy VARCHAR(50),

    FOREIGN KEY (doctor_id) REFERENCES doctor(id)
);

INSERT INTO doctor (
    id, auth_id, firstName, lastName, nameWithInitials, gender, dob, nic, email, mobileNo, address,
    profileImg, registrationNumber, specialization, qualification, experienceYears, availability_status,
    isApproved, createdDate, createdBy
) VALUES
(1, 10, 'Kamal', 'Perera', 'K. Perera', 1, '1980-05-12', '801234567V', 'kamal.perera@clinic.com', '0711234567',
 '123, Galle Road, Colombo', NULL, 'DOC001', 'Cardiology', 'MBBS, MD (Cardiology)', 15, 'AVAILABLE', 1,
 NOW(), 'admin'),
(2, 11, 'Saman', 'Fernando', 'S. Fernando', 1, '1985-09-22', '851234567V', 'saman.fernando@clinic.com', '0779876543',
 '45, Temple Road, Kandy', NULL, 'DOC002', 'Dermatology', 'MBBS, MD (Dermatology)', 10, 'AVAILABLE', 1,
 NOW(), 'admin'),
(3, 12, 'Nimali', 'Jayasinghe', 'N. Jayasinghe', 2, '1990-11-10', '901234567V', 'nimali.jayasinghe@clinic.com', '0705674321',
 '67, Main Street, Gampaha', NULL, 'DOC003', 'Pediatrics', 'MBBS, DCH', 8, 'ON_LEAVE', 1,
 NOW(), 'admin');

 INSERT INTO doctorSchedule (
    id, doctor_id, scheduleDate, scheduleTime, payment, maxPatientCount, isActive,
    createdDate, createdBy
) VALUES
(1, 1, '2025-10-21', '09:00:00', 3500.00, 15, 1, NOW(), 'kamal.perera'),
(2, 1, '2025-10-22', '14:00:00', 3500.00, 12, 1, NOW(), 'kamal.perera'),
(3, 2, '2025-10-20', '10:30:00', 2500.00, 10, 1, NOW(), 'saman.fernando'),
(4, 3, '2025-10-25', '08:30:00', 3000.00, 8, 1, NOW(), 'nimali.jayasinghe');


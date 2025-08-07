-- Create the database if it does not exist
CREATE DATABASE IF NOT EXISTS clinicmsystem;

-- Use the database
USE clinicmsystem;


CREATE TABLE user_role (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) UNIQUE NOT NULL, -- e.g., 'USER', 'ADMIN'
  PRIMARY KEY (id)
);

CREATE TABLE user (
  id INT NOT NULL AUTO_INCREMENT,
  password VARCHAR(255) DEFAULT NULL,
  email VARCHAR(255) UNIQUE DEFAULT NULL,
  is_enable TINYINT(4) NOT NULL DEFAULT 1,
  role INT DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (role) REFERENCES user_role(id) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

INSERT INTO user_role (name) VALUES ('ADMIN'), ('USER'), ('PATIENT');

INSERT INTO `user` (`password`, `email`, `is_enable`, `role`) VALUES ('$2a$12$Z/h2MTBuOrefYxbKkQGhcOxc5in8vvq0JY0yyxUnCncw8uL7HQQNm', 'dineth@bit.com', '1', '1');
INSERT INTO `user` (`password`, `email`, `is_enable`, `role`) VALUES ('$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test1@user.com', '1', '2');


CREATE TABLE Patient (
  id INT NOT NULL AUTO_INCREMENT,
  password VARCHAR(255) DEFAULT NULL,
  email VARCHAR(255) UNIQUE DEFAULT NULL,
  is_enable TINYINT(4) NOT NULL DEFAULT 1,
  role INT DEFAULT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (role) REFERENCES user_role(id) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

INSERT INTO Patient (`password`, `email`, `is_enable`, `role`) VALUES ('$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test@patient.com', '1', '3');




CREATE TABLE PatientDetails (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    patientNo VARCHAR(100) NOT NULL UNIQUE,
	fullName VARCHAR(100),
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    dob DATE NOT NULL,
    nic VARCHAR(20) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    emergencyContact VARCHAR(100) NOT NULL,
    bloodGroup ENUM('A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-') NOT NULL,
    maritalStatus ENUM('SINGLE', 'MARRIED', 'DIVORCED', 'WIDOWED') NOT NULL,
    insurance_id VARCHAR(100) DEFAULT NULL,
	registration_date DATE DEFAULT CURRENT_TIMESTAMP,
    profile_photo VARCHAR(255),
    isActive TINYINT(4) NOT NULL DEFAULT 0,
    isApproved  TINYINT(4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	created_by VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50)
);

CREATE TABLE PatientAllergies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_details_id INT NOT NULL,
    allergyName VARCHAR(100) NOT NULL,
    notes TEXT,
    FOREIGN KEY (patient_details_id) REFERENCES PatientDetails(id) ON DELETE CASCADE
);

CREATE TABLE PatientChronicDiseases (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_details_id INT NOT NULL,
    diseaseName VARCHAR(100) NOT NULL,
    notes TEXT,

    FOREIGN KEY (patient_details_id) REFERENCES PatientDetails(id) ON DELETE CASCADE
);

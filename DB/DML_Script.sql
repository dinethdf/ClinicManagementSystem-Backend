INSERT INTO user_role (name) VALUES ('ADMIN'), ('USER'), ('DOCTOR'),('PATIENT'),('STAFF');

INSERT INTO `userAuth` (`password`, `email`, `isEnable`, `role`) VALUES ('$2a$12$Z/h2MTBuOrefYxbKkQGhcOxc5in8vvq0JY0yyxUnCncw8uL7HQQNm', 'dineth@admin.com', '1', '1');
INSERT INTO `userAuth` (`password`, `email`, `isEnable`, `role`) VALUES ('$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test1@user.com', '1', '2');
INSERT INTO `userAuth` (`password`, `email`, `isEnable`, `role`) VALUES ('$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test1@doctor.com', '1', '3');
INSERT INTO `userAuth` (`password`, `email`, `isEnable`, `role`) VALUES ('$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test1@staff.com', '1', '5');

INSERT INTO PatientAuth (`password`, `email`, `isEnable`, `isTempCredential`, `role`) VALUES ('$2a$12$WrwKqV6/IEkh0rIyLS9Xm.5uQ4N7/pm0M.P80whu/lExwvVwhui3K', 'test@patient.com', '1', '1', '3');

INSERT INTO patient (
    id, patient_auth_id, patientNo, firstName, lastName, fullName,
    ethnicity, gender, maritalStatus, nic, dob, address, city, district,
    email, mobileNo, landNo, guardian_id, relationship_to_guardian,
    emergencyContactName, emergencyContactRelationship, emergencyContactMobile, emergencyContactEmail,
    bloodGroup, registrationDate, profileImg, isActive, isApproved,
    insurance_id, createdDate, createdBy, updatedDate, updatedBy, approvedDate, approvedBy
)
VALUES
(50, NULL, 'P250050', 'Fasdad', 'Qweqwe', 'Fasdad Qweqwe', 1, 1, 1, '201232345678', '2000-10-24', '123 Main Street', 'Colombo', 'Colombo', 'fasdad@example.com', '0713232345', '0112345678', NULL, NULL, 'Jane Doe', 'Spouse', '+94787654321', 'janedoe@example.com', 'AB-', '2025-09-30 10:30:00', 'https://example.com/images/patient1.png', 1, 0, NULL, '2025-10-24 12:20:41', 'test1@user.com', '2025-10-24 12:20:41', 'test1@user.com', NULL, NULL),
(51, NULL, 'P250002', 'Kamal', 'Perera', 'Kamal Perera', 2, 1, 2, '201232345679', '1998-05-12', '45 Lake Road', 'Kandy', 'Kandy', 'kamalp@demo.com', '0771122334', '0812233445', NULL, NULL, 'Sunil Perera', 'Father', '+94775543210', 'sunilp@demo.com', 'B+', '2025-10-01 11:00:00', 'https://example.com/images/patient2.png', 1, 1, NULL, '2025-10-24 12:25:00', 'admin@demo.com', '2025-10-24 12:25:00', 'admin@demo.com', NULL, NULL),
(52, NULL, 'P250123', 'Nadeesha', 'Fernando', 'Nadeesha Fernando', 3, 2, 1, '201232345680', '1995-03-18', '22 Beach Avenue', 'Galle', 'Galle', 'nadee@demo.com', '0789988776', '0913344556', NULL, NULL, 'Ruwan Fernando', 'Brother', '+94761234567', 'ruwanf@demo.com', 'O+', '2025-10-02 09:45:00', 'https://example.com/images/patient3.png', 1, 0, NULL, '2025-10-24 12:30:00', 'staff@demo.com', '2025-10-24 12:30:00', 'staff@demo.com', NULL, NULL),
(53, NULL, 'P250004', 'Sithara', 'Wijesinghe', 'Sithara Wijesinghe', 1, 2, 1, '201232345681', '1999-07-10', '56 Palm Grove', 'Negombo', 'Gampaha', 'sithara@example.com', '0702233445', '0312233445', NULL, NULL, 'Kasun Wijesinghe', 'Brother', '+94771234567', 'kasunw@demo.com', 'A+', '2025-10-03 14:00:00', 'https://example.com/images/patient4.png', 1, 0, NULL, '2025-10-24 12:40:00', 'staff@demo.com', '2025-10-24 12:40:00', 'staff@demo.com', NULL, NULL),
(54, NULL, 'P250005', 'Ruwan', 'Silva', 'Ruwan Silva', 2, 1, 2, '201232345682', '1997-01-21', '10 River Street', 'Matara', 'Matara', 'ruwans@demo.com', '0765544332', '0413344556', NULL, NULL, 'Priya Silva', 'Mother', '+94761234568', 'priyas@demo.com', 'O-', '2025-10-03 09:00:00', 'https://example.com/images/patient5.png', 1, 1, NULL, '2025-10-24 12:45:00', 'staff@demo.com', '2025-10-24 12:45:00', 'staff@demo.com', NULL, NULL),
(55, NULL, 'P250006', 'Amaya', 'Jayasinghe', 'Amaya Jayasinghe', 3, 2, 1, '201232345683', '2001-04-25', '12 Green Park', 'Kurunegala', 'Kurunegala', 'amaya@demo.com', '0776677889', '0373344556', NULL, NULL, 'Dinesh Jayasinghe', 'Father', '+94774567890', 'dineshj@demo.com', 'B-', '2025-10-03 09:15:00', 'https://example.com/images/patient6.png', 1, 0, NULL, '2025-10-24 12:50:00', 'staff@demo.com', '2025-10-24 12:50:00', 'staff@demo.com', NULL, NULL),
(56, NULL, 'P250007', 'Tharindu', 'Ekanayake', 'Tharindu Ekanayake', 1, 1, 1, '201232345684', '1994-09-15', '8 Temple Road', 'Jaffna', 'Jaffna', 'tharindu@demo.com', '0718887776', '0212233445', NULL, NULL, 'Rasika Ekanayake', 'Spouse', '+94775553322', 'rasikae@demo.com', 'A-', '2025-10-04 10:00:00', 'https://example.com/images/patient7.png', 1, 1, NULL, '2025-10-24 12:55:00', 'staff@demo.com', '2025-10-24 12:55:00', 'staff@demo.com', NULL, NULL),
(57, NULL, 'P250008', 'Chathura', 'Bandara', 'Chathura Bandara', 2, 1, 2, '201232345685', '1988-11-22', '77 Lake View', 'Badulla', 'Badulla', 'chathura@demo.com', '0754455667', '0552233445', NULL, NULL, 'Kumari Bandara', 'Mother', '+94773334455', 'kumari@demo.com', 'B+', '2025-10-05 13:00:00', 'https://example.com/images/patient8.png', 1, 0, NULL, '2025-10-24 13:00:00', 'staff@demo.com', '2025-10-24 13:00:00', 'staff@demo.com', NULL, NULL),
(58, NULL, 'P250009', 'Sajith', 'Gunasekara', 'Sajith Gunasekara', 3, 1, 1, '201232345686', '1990-08-11', '5 Hill Street', 'Ratnapura', 'Ratnapura', 'sajith@demo.com', '0787654321', '0453344556', NULL, NULL, 'Indika Gunasekara', 'Father', '+94770001122', 'indika@demo.com', 'AB+', '2025-10-05 09:30:00', 'https://example.com/images/patient9.png', 1, 0, NULL, '2025-10-24 13:05:00', 'staff@demo.com', '2025-10-24 13:05:00', 'staff@demo.com', NULL, NULL),
(59, NULL, 'P250010', 'Nilusha', 'Dissanayake', 'Nilusha Dissanayake', 1, 2, 2, '201232345687', '1993-06-08', '90 Flower Road', 'Anuradhapura', 'Anuradhapura', 'nilusha@demo.com', '0745566778', '0252233445', NULL, NULL, 'Kusum Dissanayake', 'Mother', '+94778889900', 'kusumd@demo.com', 'O+', '2025-10-06 11:00:00', 'https://example.com/images/patient10.png', 1, 1, NULL, '2025-10-24 13:10:00', 'staff@demo.com', '2025-10-24 13:10:00', 'staff@demo.com', NULL, NULL),
(60, NULL, 'P250011', 'Isuru', 'Abeywickrama', 'Isuru Abeywickrama', 2, 1, 1, '201232345688', '1992-03-12', '7 Lotus Avenue', 'Kalutara', 'Kalutara', 'isuru@demo.com', '0779911223', '0343344556', NULL, NULL, 'Suresh Abeywickrama', 'Father', '+94778881234', 'suresha@demo.com', 'B-', '2025-10-06 10:45:00', 'https://example.com/images/patient11.png', 1, 0, NULL, '2025-10-24 13:15:00', 'staff@demo.com', '2025-10-24 13:15:00', 'staff@demo.com', NULL, NULL),
(61, NULL, 'P250012', 'Thilini', 'De Silva', 'Thilini De Silva', 3, 2, 1, '201232345689', '1996-09-20', '12 Rose Lane', 'Colombo', 'Colombo', 'thilini@demo.com', '0766677888', '0117788990', NULL, NULL, 'Sameera De Silva', 'Husband', '+94775554433', 'sameerad@demo.com', 'A+', '2025-10-06 15:20:00', 'https://example.com/images/patient12.png', 1, 1, NULL, '2025-10-24 13:20:00', 'staff@demo.com', '2025-10-24 13:20:00', 'staff@demo.com', NULL, NULL),
(62, NULL, 'P250013', 'Madushan', 'Ranasinghe', 'Madushan Ranasinghe', 1, 1, 1, '201232345690', '1989-01-10', '25 Beach Road', 'Gampaha', 'Gampaha', 'madushan@demo.com', '0719988776', '0332233445', NULL, NULL, 'Lakmini Ranasinghe', 'Wife', '+94770002211', 'lakmini@demo.com', 'AB-', '2025-10-06 14:10:00', 'https://example.com/images/patient13.png', 1, 0, NULL, '2025-10-24 13:25:00', 'staff@demo.com', '2025-10-24 13:25:00', 'staff@demo.com', NULL, NULL),
(63, NULL, 'P250014', 'Rashmi', 'Perera', 'Rashmi Perera', 2, 2, 1, '201232345691', '1994-11-25', '8 Church Street', 'Matale', 'Matale', 'rashmi@demo.com', '0754433221', '0662233445', NULL, NULL, 'Chandana Perera', 'Father', '+94775553344', 'chandanap@demo.com', 'O-', '2025-10-07 09:00:00', 'https://example.com/images/patient14.png', 1, 0, NULL, '2025-10-24 13:30:00', 'staff@demo.com', '2025-10-24 13:30:00', 'staff@demo.com', NULL, NULL),
(64, NULL, 'P250015', 'Kasun', 'Jayawardena', 'Kasun Jayawardena', 3, 1, 2, '201232345692', '1995-05-16', '66 Temple Lane', 'Polonnaruwa', 'Polonnaruwa', 'kasun@demo.com', '0789988777', '0272233445', NULL, NULL, 'Manel Jayawardena', 'Mother', '+94774445566', 'manelj@demo.com', 'A-', '2025-10-07 10:10:00', 'https://example.com/images/patient15.png', 1, 1, NULL, '2025-10-24 13:35:00', 'staff@demo.com', '2025-10-24 13:35:00', 'staff@demo.com', NULL, NULL),
(65, NULL, 'P250016', 'Dinithi', 'Wickramasinghe', 'Dinithi Wickramasinghe', 1, 2, 1, '201232345693', '1997-02-18', '22 Sea View', 'Trincomalee', 'Trincomalee', 'dinithi@demo.com', '0702233446', '0262233445', NULL, NULL, 'Nalin Wickramasinghe', 'Father', '+94773334422', 'nalinw@demo.com', 'B+', '2025-10-08 09:00:00', 'https://example.com/images/patient16.png', 1, 0, NULL, '2025-10-24 13:40:00', 'staff@demo.com', '2025-10-24 13:40:00', 'staff@demo.com', NULL, NULL),
(66, NULL, 'P250017', 'Shehan', 'Hettiarachchi', 'Shehan Hettiarachchi', 2, 1, 2, '201232345694', '1991-12-22', '33 New Road', 'Nuwara Eliya', 'Nuwara Eliya', 'shehan@demo.com', '0758877665', '0522233445', NULL, NULL, 'Kavinda Hettiarachchi', 'Brother', '+94770002233', 'kavindah@demo.com', 'O+', '2025-10-08 11:15:00', 'https://example.com/images/patient17.png', 1, 1, NULL, '2025-10-24 13:45:00', 'staff@demo.com', '2025-10-24 13:45:00', 'staff@demo.com', NULL, NULL),
(67, NULL, 'P250018', 'Pavani', 'Abeykoon', 'Pavani Abeykoon', 3, 2, 1, '201232345695', '2000-07-07', '88 Market Street', 'Hambantota', 'Hambantota', 'pavani@demo.com', '0713344556', '0472233445', NULL, NULL, 'Lal Abeykoon', 'Father', '+94774443322', 'lala@demo.com', 'AB+', '2025-10-09 12:00:00', 'https://example.com/images/patient18.png', 1, 0, NULL, '2025-10-24 13:50:00', 'staff@demo.com', '2025-10-24 13:50:00', 'staff@demo.com', NULL, NULL),
(68, NULL, 'P250019', 'Chamara', 'Dias', 'Chamara Dias', 1, 1, 2, '201232345696', '1987-10-11', '14 Hill Top', 'Puttalam', 'Puttalam', 'chamara@demo.com', '0774455667', '0322233445', NULL, NULL, 'Shanika Dias', 'Wife', '+94770005544', 'shanika@demo.com', 'B-', '2025-10-09 10:30:00', 'https://example.com/images/patient19.png', 1, 1, NULL, '2025-10-24 13:55:00', 'staff@demo.com', '2025-10-24 13:55:00', 'staff@demo.com', NULL, NULL),
(69, NULL, 'P250020', 'Lakmini', 'Senanayake', 'Lakmini Senanayake', 2, 2, 1, '201232345697', '1999-09-01', '2 Garden Street', 'Galle', 'Galle', 'lakmini@demo.com', '0787766554', '0913344557', NULL, NULL, 'Indika Senanayake', 'Mother', '+94778889977', 'indikas@demo.com', 'A+', '2025-10-10 09:45:00', 'https://example.com/images/patient20.png', 1, 0, NULL, '2025-10-24 14:00:00', 'staff@demo.com', '2025-10-24 14:00:00', 'staff@demo.com', NULL, NULL);

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


INSERT INTO doctor (
    id, auth_id, firstName, lastName, nameWithInitials, gender, dob, nic, email, mobileNo, address,
    profileImg, slmcNumber, specialization_id, qualification, experienceYears, availability_status,
    isApproved, createdDate, createdBy
) VALUES
(1, 3, 'Kamal', 'Perera', 'K. Perera', 1, '1980-05-12', '809934567V', 'kamal.perera@clinic.com', '0711234567',
 '123, Galle Road, Colombo', NULL, 'DOC001', '1', 'MBBS, MD (Cardiology)', 15, 'AVAILABLE', 1,
 NOW(), 'admin'),
(2, 11, 'Saman', 'Fernando', 'S. Fernando', 1, '1985-09-22', '851299567V', 'saman.fernando@clinic.com', '0779876543',
 '45, Temple Road, Kandy', NULL, 'DOC002', '2', 'MBBS, MD (Dermatology)', 10, 'AVAILABLE', 1,
 NOW(), 'admin'),
(3, 12, 'Nimali', 'Jayasinghe', 'N. Jayasinghe', 2, '1990-11-10', '901234599V', 'nimali.jayasinghe@clinic.com', '0705674321',
 '67, Main Street, Gampaha', NULL, 'DOC003', '3', 'MBBS, DCH', 8, 'ON_LEAVE', 1,
 NOW(), 'admin');

 INSERT INTO doctor (
    id, auth_id, firstName, lastName, nameWithInitials, gender, dob, nic, email, mobileNo, address,
    profileImg, slmcNumber, specialization_id, qualification, experienceYears, availability_status,
    isApproved, createdDate, createdBy
) VALUES
(4, 13, 'Ravi', 'Silva', 'R. Silva', 1, '1978-01-20', '780201234V', 'ravi.silva@clinic.com', '0761234567',
 '88, Park Street, Matara', NULL, 'DOC004', 4, 'MBBS, MD (Neurology)', 18, 'AVAILABLE', 1,
 NOW(), 'admin'),

(5, 14, 'Anusha', 'Fonseka', 'A. Fonseka', 2, '1982-07-30', '825304567V', 'anusha.fonseka@clinic.com', '0722345678',
 '22, Lake Road, Anuradhapura', NULL, 'DOC005', 5, 'MBBS, MD (Oncology)', 12, 'AVAILABLE', 1,
 NOW(), 'admin'),

(6, 15, 'Sunil', 'Bandara', 'S. Bandara', 1, '1975-03-15', '750750123V', 'sunil.bandara@clinic.com', '0718887766',
 '101, Cross Street, Jaffna', NULL, 'DOC006', 1, 'MBBS, MRCP (Cardiology)', 20, 'AVAILABLE', 1,
 NOW(), 'admin'),

(7, 16, 'Deepa', 'Ratnayake', 'D. Ratnayake', 2, '1988-12-01', '888012345V', 'deepa.ratnayake@clinic.com', '0789990001',
 '34, Hill View, Nuwara Eliya', NULL, 'DOC007', 2, 'MBBS, Dip (Dermatology)', 9, 'AVAILABLE', 1,
 NOW(), 'admin');

 INSERT INTO doctor (
    id, auth_id, firstName, lastName, nameWithInitials, gender, dob, nic, email, mobileNo, address,
    profileImg, slmcNumber, specialization_id, qualification, experienceYears, availability_status,
    isApproved, createdDate, createdBy
) VALUES
(8, 17, 'Lakshani', 'Peries', 'L. Peries', 2, '1992-02-14', '925451234V', 'lakshani.peries@clinic.com', '0771122334',
 '14, Beach Road, Negombo', NULL, 'DOC008', 3, 'MBBS, DCH', 6, 'AVAILABLE', 1,
 NOW(), 'admin'),

(9, 18, 'Mohamed', 'Rizwan', 'M. Rizwan', 1, '1983-06-05', '831572345V', 'mohamed.rizwan@clinic.com', '0752233445',
 '77, Mosque Lane, Puttalam', NULL, 'DOC009', 4, 'MBBS, MD (Neurology)', 11, 'AVAILABLE', 1,
 NOW(), 'admin'),

(10, 19, 'Bimal', 'Jayasuriya', 'B. Jayasuriya', 1, '1976-11-25', '763301234V', 'bimal.jayasuriya@clinic.com', '0719876543',
 '23, 1st Cross Street, Colombo 11', NULL, 'DOC010', 5, 'MBBS, MD (Oncology)', 19, 'AVAILABLE', 1,
 NOW(), 'admin'),

(11, 20, 'Shanthi', 'Kumar', 'S. Kumar', 2, '1987-08-19', '876321234V', 'shanthi.kumar@clinic.com', '0763344556',
 '5, Kovil Street, Jaffna', NULL, 'DOC011', 1, 'MBBS (Cardiology)', 10, 'AVAILABLE', 1,
 NOW(), 'admin'),

(12, 21, 'Kasun', 'Pathirana', 'K. Pathirana', 1, '1991-04-02', '910931234V', 'kasun.pathirana@clinic.com', '0701122334',
 '19, River Side, Panadura', NULL, 'DOC012', 2, 'MBBS, MD (Dermatology)', 7, 'AVAILABLE', 1,
 NOW(), 'admin'),

(13, 22, 'Fathima', 'Zuhair', 'F. Zuhair', 2, '1989-10-18', '897921234V', 'fathima.zuhair@clinic.com', '0774455667',
 '90, Main Street, Akurana', NULL, 'DOC013', 3, 'MBBS, MRCPCH', 9, 'AVAILABLE', 1,
 NOW(), 'admin'),

(14, 23, 'Dinesh', 'Weerasinghe', 'D. Weerasinghe', 1, '1984-05-30', '841511234V', 'dinesh.weerasinghe@clinic.com', '0715566778',
 '45, Station Road, Mount Lavinia', NULL, 'DOC014', 4, 'MBBS, MD', 11, 'AVAILABLE', 1,
 NOW(), 'admin'),

(15, 24, 'Ishara', 'Madhuwanthi', 'I. Madhuwanthi', 2, '1993-01-28', '935281234V', 'ishara.madhuwanthi@clinic.com', '0786677889',
 '62, Temple Road, Homagama', NULL, 'DOC015', 5, 'MBBS (Oncology)', 5, 'AVAILABLE', 1,
 NOW(), 'admin'),

(16, 25, 'Arun', 'Prakash', 'A. Prakash', 1, '1981-09-07', '812511234V', 'arun.prakash@clinic.com', '0777890123',
 '33, Sea Street, Trincomalee', NULL, 'DOC016', 1, 'MBBS, MD (Cardiology)', 16, 'AVAILABLE', 1,
 NOW(), 'admin'),

(17, 26, 'Chamari', 'Atapattu', 'C. Atapattu', 2, '1986-07-11', '866931234V', 'chamari.atapattu@clinic.com', '0712341234',
 '12, Freedom Lane, Kurunegala', NULL, 'DOC017', 2, 'MBBS', 10, 'AVAILABLE', 1,
 NOW(), 'admin'),

(18, 27, 'Pradeep', 'Samarawickrama', 'P. Samarawickrama', 1, '1979-12-12', '793471234V', 'pradeep.samarawickrama@clinic.com', '0723452345',
 '7, Lake View, Ratnapura', NULL, 'DOC018', 3, 'MBBS, DCH', 17, 'AVAILABLE', 1,
 NOW(), 'admin'),

(19, 28, 'Nadeesha', 'Hemal', 'N. Hemal', 2, '1990-03-22', '905821234V', 'nadeesha.hemal@clinic.com', '0754563456',
 '200, Kandy Road, Kegalle', NULL, 'DOC019', 4, 'MBBS, MRCP (Neurology)', 8, 'AVAILABLE', 1,
 NOW(), 'admin'),

(20, 29, 'Ruwan', 'Kumara', 'R. Kumara', 1, '1985-11-02', '853071234V', 'ruwan.kumara@clinic.com', '0765674567',
 '31, Galle Road, Kalutara', NULL, 'DOC020', 5, 'MBBS, MD (Oncology)', 10, 'AVAILABLE', 1,
 NOW(), 'admin');

INSERT INTO doctorSchedule (
    id, doctor_id, scheduleDate, startTime, endTime, payment, maxPatientCount, isActive,
    createdDate, createdBy
) VALUES
-- Doctor 1: Kamal Perera (Cardiology)
(1, 1, '2025-11-01', '09:00:00', '12:00:00', 3500.00, 15, 1, NOW(), 'kamal.perera'),
(2, 1, '2025-11-03', '14:00:00', '17:00:00', 3500.00, 12, 1, NOW(), 'kamal.perera'),
(3, 1, '2025-11-05', '09:00:00', '12:00:00', 3500.00, 14, 1, NOW(), 'kamal.perera'),
(4, 1, '2025-11-07', '14:00:00', '17:00:00', 3500.00, 10, 1, NOW(), 'kamal.perera'),
(5, 1, '2025-11-09', '09:00:00', '12:00:00', 3500.00, 15, 1, NOW(), 'kamal.perera'),
(6, 1, '2025-11-11', '14:00:00', '17:00:00', 3500.00, 12, 1, NOW(), 'kamal.perera'),
(7, 1, '2025-11-13', '09:00:00', '12:00:00', 3500.00, 15, 1, NOW(), 'kamal.perera'),
(8, 1, '2025-11-15', '14:00:00', '17:00:00', 3500.00, 10, 1, NOW(), 'kamal.perera'),
(9, 1, '2025-11-17', '09:00:00', '12:00:00', 3500.00, 14, 1, NOW(), 'kamal.perera'),
(10, 1, '2025-11-19', '14:00:00', '17:00:00', 3500.00, 15, 1, NOW(), 'kamal.perera'),

-- Doctor 2: Saman Fernando (Dermatology)
(11, 2, '2025-11-02', '10:00:00', '12:00:00', 2500.00, 10, 1, NOW(), 'saman.fernando'),
(12, 2, '2025-11-04', '13:00:00', '15:00:00', 2500.00, 12, 1, NOW(), 'saman.fernando'),
(13, 2, '2025-11-06', '10:00:00', '12:00:00', 2500.00, 15, 1, NOW(), 'saman.fernando'),
(14, 2, '2025-11-08', '13:00:00', '15:00:00', 2500.00, 10, 1, NOW(), 'saman.fernando'),
(15, 2, '2025-11-10', '10:00:00', '12:00:00', 2500.00, 12, 1, NOW(), 'saman.fernando'),
(16, 2, '2025-11-12', '13:00:00', '15:00:00', 2500.00, 15, 1, NOW(), 'saman.fernando'),
(17, 2, '2025-11-14', '10:00:00', '12:00:00', 2500.00, 10, 1, NOW(), 'saman.fernando'),
(18, 2, '2025-11-16', '13:00:00', '15:00:00', 2500.00, 12, 1, NOW(), 'saman.fernando'),
(19, 2, '2025-11-18', '10:00:00', '12:00:00', 2500.00, 15, 1, NOW(), 'saman.fernando'),
(20, 2, '2025-11-20', '13:00:00', '15:00:00', 2500.00, 10, 1, NOW(), 'saman.fernando'),

-- Doctor 3: Nimali Jayasinghe (Pediatrics)
(21, 3, '2025-11-01', '08:30:00', '10:00:00', 3000.00, 8, 1, NOW(), 'nimali.jayasinghe'),
(22, 3, '2025-11-03', '15:00:00', '17:00:00', 3000.00, 10, 1, NOW(), 'nimali.jayasinghe'),
(23, 3, '2025-11-05', '08:30:00', '10:00:00', 3000.00, 12, 1, NOW(), 'nimali.jayasinghe'),
(24, 3, '2025-11-07', '15:00:00', '17:00:00', 3000.00, 8, 1, NOW(), 'nimali.jayasinghe'),
(25, 3, '2025-11-09', '08:30:00', '10:00:00', 3000.00, 10, 1, NOW(), 'nimali.jayasinghe'),
(26, 3, '2025-11-11', '15:00:00', '17:00:00', 3000.00, 12, 1, NOW(), 'nimali.jayasinghe'),
(27, 3, '2025-11-13', '08:30:00', '10:00:00', 3000.00, 8, 1, NOW(), 'nimali.jayasinghe'),
(28, 3, '2025-11-15', '15:00:00', '17:00:00', 3000.00, 10, 1, NOW(), 'nimali.jayasinghe'),
(29, 3, '2025-11-17', '08:30:00', '10:00:00', 3000.00, 12, 1, NOW(), 'nimali.jayasinghe'),
(30, 3, '2025-11-19', '15:00:00', '17:00:00', 3000.00, 8, 1, NOW(), 'nimali.jayasinghe');

INSERT INTO item_types
    (typeName, description)
VALUES
    ('APPOINTMENT', 'Doctor Consultation or Booking'),
    ('LAB_TEST', 'Laboratory Test Request'),
    ('SUBSCRIPTION', 'Health Package Subscription'),
    ('PHARMACY', 'Pharmacy Sale or Prescription'),
    ('DOCTOR_DISCOUNT', 'Discount given by the doctor'),
    ('CLINIC_DISCOUNT', 'Discount or promotion from the clinic'),
    ('OTHER_DISCOUNT', 'Any other type of special discount');
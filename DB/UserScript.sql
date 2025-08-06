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
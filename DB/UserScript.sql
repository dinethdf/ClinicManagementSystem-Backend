CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `password` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `isApproved` tinyint(4) NOT NULL DEFAULT '0',
  `name` varchar(100) DEFAULT NULL,
  `role` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;


CREATE TABLE finalpro.`user_role` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL, -- e.g., 'USER', 'ADMIN'
  PRIMARY KEY (`id`)
);

INSERT INTO finalpro.user_role (name) VALUES ('USER'), ('ADMIN');


ALTER TABLE finalpro.`user`
  MODIFY COLUMN `role` INT,
  ADD CONSTRAINT `fk_user_role`
  FOREIGN KEY (`role`) REFERENCES `user_role`(`id`);

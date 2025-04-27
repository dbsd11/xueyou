--
-- Table structure for table `account_records`
--

-- DROP TABLE IF EXISTS `account_records`;
CREATE TABLE IF NOT EXISTS `account_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` varchar(50) DEFAULT NULL,
  `details` varchar(255)  DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `ai_suggestion` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `create_by` varchar(50)  NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=44 ;

--
-- Table structure for table `student`
--

-- DROP TABLE IF EXISTS `student`;
CREATE TABLE IF NOT EXISTS `student` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `college` varchar(255) DEFAULT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `major` varchar(255) DEFAULT NULL,
  `grade` varchar(255) DEFAULT NULL,
  `expect_monthly_spend` decimal(10,3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9363717 ;

--
-- Table structure for table `study_plan`
--

-- DROP TABLE IF EXISTS `study_plan`;
CREATE TABLE IF NOT EXISTS  `study_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `event_content` varchar(500)  DEFAULT NULL,
  `duration` int DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `create_by` varchar(50)  NOT NULL,
  `event_start_time` varchar(50) DEFAULT NULL,
  `weekday` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 ;
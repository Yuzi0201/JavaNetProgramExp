CREATE DATABASE  IF NOT EXISTS `HONGDB` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `HONGDB`;
-- MySQL dump 10.13  Distrib 8.0.30, for Linux (x86_64)
--
-- Host: localhost    Database: HONGDB
-- ------------------------------------------------------
-- Server version	8.0.29

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `questions`
--

DROP TABLE IF EXISTS `questions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `questions` (
  `id` int NOT NULL,
  `question` varchar(100) DEFAULT NULL,
  `A` varchar(45) DEFAULT NULL,
  `B` varchar(45) DEFAULT NULL,
  `C` varchar(45) DEFAULT NULL,
  `answer` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `questions`
--

LOCK TABLES `questions` WRITE;
/*!40000 ALTER TABLE `questions` DISABLE KEYS */;
INSERT INTO `questions` VALUES (1,'标志着中国工人阶级开始以独立的政治力量登上历史舞台的事件是(      )','中国共产党的成立','五四运动','二七大罢工','B'),(2,'1921年7月下旬至8月初,中国共产党第一次全国代表大会先后在(      )召开。','上海、天津','北京、上海','上海、嘉兴','C'),(3,'参加中国共产党第一次代表大会的5位湖北籍共产党人，除了董必武、陈潭秋外，其他三位是(      )','李汉俊、恽代英、林育南','李汉俊、包惠僧、刘仁静','包惠僧、刘仁静、李求实','B'),(4,'1923年6月，中国共产党第三次全国代表大会在广州召开，会议的中心议题是(      )','讨论与国民党合作、建立革命统一战线的问题','如何进一步推动工人运动的发展','无产阶级领导权问题','A'),(5,'中国工人运动史上持续时间最长的一次罢工是(      )','香港海员罢工','广州沙面工人罢工','省港大罢工','C');
/*!40000 ALTER TABLE `questions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `record`
--

DROP TABLE IF EXISTS `record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `record` (
  `username` varchar(20) DEFAULT NULL,
  `questionID` varchar(45) DEFAULT NULL,
  `userAnswer` varchar(45) DEFAULT NULL,
  `correctAnswer` varchar(45) DEFAULT NULL,
  KEY `fk_record_1_idx` (`username`),
  CONSTRAINT `fk_record_1` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `record`
--

LOCK TABLES `record` WRITE;
/*!40000 ALTER TABLE `record` DISABLE KEYS */;
INSERT INTO `record` VALUES ('Yuzi','3,2,5,4,1','C,C,C,C,C','B,C,C,A,B'),('Yuzi','4,3,1,2,5','A,B,A,C,B','A,B,B,C,C'),('Yuzi','2,4,1,5,3','C,C,B,C,C','C,A,B,C,B'),('Yuzi','2,5,4,1,3','C,C,C,C,C','C,C,A,B,B'),('Yuzi','5,1,3,2,4','B,B,C,C,A','C,B,B,C,A'),('Yuzi','4,5,1,2,3','C,C,A,B,C','A,C,B,C,B'),('Yuzi','1,4,2,3,5','C,B,C,B,A','B,A,C,B,C'),('Yuzi','1,3,4,2,5','B,B,A,C,C','B,B,A,C,C'),('yuzi','1,2,3,4,5','A,B,C,B,A','A,C,C,B,A'),('yuzi114','4,3,3,2,2','A,A,A,A,A','A,B,B,C,C'),('yuzi114','5,4,2,2,3','A,A,A,A,A','C,A,C,C,B'),('yuzi114','5,4,5,2,4','B,B,B,B,B','C,A,C,C,A'),('yuzi114','4,5,1,5,1','C,C,C,C,C','A,C,B,C,B'),('yuzi114','3,5,3,4,5','A,B,C,A,B','B,C,B,A,C');
/*!40000 ALTER TABLE `record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `username` varchar(20) NOT NULL,
  `password` varchar(45) NOT NULL,
  `token` char(10) DEFAULT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('yuz','1','CRiXDlRqGf'),('yuzi','19198100','mOVbFYzzAd'),('yuzi114','114514','GwfJj0u2Ik'),('Yuzi19','11111111',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-11-05 17:58:06

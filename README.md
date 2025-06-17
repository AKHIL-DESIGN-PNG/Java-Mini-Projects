# Java-Mini-Projects
# Java Data Structure & CRUD Application Suite

## Overview

This project is a modular Java application suite that showcases:
- Data structure-based arithmetic calculators using *Queue, **LinkedList, and **ArrayList*.
- Console-based and JavaFX-based CRUD applications with Oracle database integration.

---

## 📁 Project Structure

<pre>
MainHub.java
│
├── DataStructureCalculatorApp.java
│   ├── QueueCalculator.java
│   ├── LinkedListCalculator.java
│   └── ArrayListCalculator.java
│
├── CrudConsoleApp.java
│   └── [Uses: create.txt, read.txt, update.txt, delete.txt, drop.txt]
│
└── CrudGuiApp.java (JavaFX + Oracle DB)
    └── [Uses: create.txt, read.txt, update.txt, delete.txt, drop.txt]</pre>

---

## 🔧 Components

### 🧮 DataStructureCalculatorApp

A menu-driven application that allows users to perform arithmetic operations using different data structures:
- *QueueCalculator.java* – Uses Queue to evaluate expressions.
- *LinkedListCalculator.java* – Uses LinkedList for computation flow.
- *ArrayListCalculator.java* – Uses ArrayList to manage operands and operators.

### 🖥 CrudConsoleApp

A console-based CRUD (Create, Read, Update, Delete, Drop) application that:
- Simulates CRUD operations by writing and reading from plain .txt files.
- Files used: create.txt, read.txt, update.txt, delete.txt, drop.txt.

### 🎨 CrudGuiApp (JavaFX + Oracle DB)

A GUI-based CRUD application built using *JavaFX, connected to an **Oracle Database*.
- User-friendly interface for performing database operations.
- Handles *Create, **Read, **Update, **Delete, and **Drop* functionality.
- Requires valid Oracle DB credentials and setup.

---

## 🛠 Technologies Used

- *Java SE 8+*
- *JavaFX (for GUI)*
- *Oracle Database*
- *JDBC* for database connectivity
- *Text files* for simulated console operations

---

## 🔌 Prerequisites

- Java Development Kit (JDK) 8 or higher
- Oracle Database installed or accessible
- JavaFX SDK installed and configured (for GUI)
- Oracle JDBC Driver (ojdbc8.jar) added to classpath

---

## 🚀 How to Run

### 1. Compile All Classes

bash
javac *.java


### 2. Run MainHub (Entry Point)

bash
java MainHub


### 3. JavaFX GUI Application (CrudGuiApp)

Ensure JavaFX and Oracle DB are properly configured:

bash
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp .:ojdbc8.jar CrudGuiApp


> Replace /path/to/javafx-sdk/lib and ojdbc8.jar path as per your setup.

---

## 💾 Oracle DB Configuration

Make sure you create a schema/table in Oracle DB with appropriate structure. Sample SQL:

sql
CREATE TABLE users (
  id NUMBER PRIMARY KEY,
  name VARCHAR2(50),
  email VARCHAR2(100)
);


Update the connection parameters in CrudGuiApp.java:

java
String url = "jdbc:oracle:thin:@localhost:1521:xe";
String user = "your_username";
String password = "your_password";


---

## 📄 Sample Text Files (for CrudConsoleApp)

Ensure the following text files exist in the same directory:
- create.txt
- read.txt
- update.txt
- delete.txt
- drop.txt

These files simulate basic storage and display operations for CRUD functionality.

---

## 🧑‍💻 Author

*Yanamala Akhil Kumar Reddy*

---

## 📜 License

This project is for academic and educational use only. No commercial license provided.

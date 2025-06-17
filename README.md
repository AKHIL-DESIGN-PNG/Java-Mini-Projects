```markdown
# 💻 Java Data Structure & CRUD Application Suite

## 🧾 Overview

This project is a modular Java application suite that demonstrates:
- 🧮 Data structure-based arithmetic calculators using **Queue**, **LinkedList**, and **ArrayList**.
- 🖥️ Console-based and 🎨 GUI-based (JavaFX) CRUD applications with 🗄️ Oracle Database integration.

---

## 📁 Project Structure

```

📦 MainHub.java
│
├── 🧮 DataStructureCalculatorApp.java
│   ├── 🔁 QueueCalculator.java
│   ├── 🔗 LinkedListCalculator.java
│   └── 📋 ArrayListCalculator.java
│
├── 🖥️ CrudConsoleApp.java
│   └── 📄 \[Uses: create.txt, read.txt, update.txt, delete.txt, drop.txt]
│
└── 🎨 CrudGuiApp.java (JavaFX + Oracle DB)
└── 🗃️ \[Uses: create.txt, read.txt, update.txt, delete.txt, drop.txt]

````

---

## 🔧 Components

### 🧮 DataStructureCalculatorApp
Menu-driven program that lets users perform arithmetic using data structures:
- 🔁 `QueueCalculator.java` – Uses Queue.
- 🔗 `LinkedListCalculator.java` – Uses LinkedList.
- 📋 `ArrayListCalculator.java` – Uses ArrayList.

### 🖥️ CrudConsoleApp
- A terminal-based CRUD application using `.txt` files.
- Files used: `create.txt`, `read.txt`, `update.txt`, `delete.txt`, `drop.txt`.

### 🎨 CrudGuiApp (JavaFX + Oracle DB)
- GUI CRUD App using **JavaFX** and **Oracle DB**.
- Supports: 🆕 Create | 🔍 Read | 📝 Update | ❌ Delete | 🗑️ Drop
- Requires Oracle DB credentials and JavaFX setup.

---

## 🛠️ Technologies Used

- ☕ Java SE 8+
- 🎨 JavaFX
- 🛢 Oracle Database
- 🔌 JDBC
- 📄 Text files

---

## 🔌 Prerequisites

- ✅ JDK 8 or above
- 🛢 Oracle Database installed
- 🎨 JavaFX SDK configured
- 🧩 Oracle JDBC Driver (`ojdbc8.jar`) added to classpath

---

## 🚀 How to Run

### 📦 1. Compile All Classes
```
javac *.java
````

### ▶️ 2. Run MainHub (Entry Point)

```
java MainHub
```

### 🖼️ 3. Run JavaFX GUI (CrudGuiApp)

Make sure JavaFX and Oracle DB are properly configured:

``
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp .:ojdbc8.jar CrudGuiApp
```

> 🛠 Replace `/path/to/javafx-sdk/lib` and `ojdbc8.jar` with your local paths.

---

## 💾 Oracle DB Configuration

Run this SQL to create a basic `users` table:

```sql
CREATE TABLE users (
  id NUMBER PRIMARY KEY,
  name VARCHAR2(50),
  email VARCHAR2(100)
);
```

🔐 Update credentials in `CrudGuiApp.java`:

```java
String url = "jdbc:oracle:thin:@localhost:1521:xe";
String user = "your_username";
String password = "your_password";
```

---

## 📂 Required Text Files

Ensure these files are present for `CrudConsoleApp`:

* 📝 `create.txt`
* 🔍 `read.txt`
* ✏️ `update.txt`
* ❌ `delete.txt`
* 🗑 `drop.txt`

These simulate storage for CRUD operations.

---

## 👨‍💻 Author

**Yanamala Akhil Kumar Reddy** 🚀

---

## 📜 License

📚 *This project is for academic and educational purposes only.*
❌ No commercial use is permitted.

```

---

✅ Just copy this into a file named `README.md` in your GitHub repository root folder.  
Want a `LICENSE` or `.gitignore` file next?
```

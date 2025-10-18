# WorldWanderer – Assessment 4 

## 👤 Student Details
**Name:** Ramit Sarkar 
**Student ID:** S4186768
**Course:** Software Engineering Fundamentals  
**Assessment 4:** Implementation, Unit Testing, and Git Integration  

---

## 🧩 Project Overview
This project implements and tests the `FlightSearch` class for the *WorldWanderer* website.  
The goal is to validate user flight search criteria according to eleven specified conditions and recent lecturer clarifications.

### ✅ Implementation Highlights
- Strict date validation using `DateTimeFormatter` and `ResolverStyle.STRICT`
- Validation of allowed airport codes and seating classes
- Passenger totals between **1 and 9**
- Children and infants restricted from emergency rows or certain classes
- **Negative child/infant counts** handled as invalid (clarification update)
- Return date must be on or after departure date
- Only **economy class can have emergency row seating**, but *all classes can be non-emergency* (updated Condition 10 wording)
- Class attributes update **only when validation succeeds** — proven via pre/post assertions in tests

---

## 🧪 Unit Testing (JUnit 5)
All tests are written with **JUnit 5** and executed using the standalone console launcher.

### Test Coverage
| Group | Description |
|--------|-------------|
| Condition 1 | Passenger totals 1–9  +  negative child/infant counts |
| Condition 2 | Children + emergency / first class restrictions |
| Condition 3 | Infants + emergency / business restrictions |
| Condition 4 | ≤ 2 children per adult |
| Condition 5 | ≤ 1 infant per adult |
| Condition 6 | Departure date not in past |
| Condition 7 | Strict date format validation |
| Condition 8 | Return ≥ Departure |
| Condition 9 | Allowed seating classes |
| Condition 10 | Only economy can be emergency row (all classes non-emergency OK) |
| Condition 11 | Valid and distinct airport codes |
| Attribute Proof | Pre/Post validation test verifying class updates only on valid input |

**Total tests:** 34 (successful)  

---


## ⚙️ How to Compile and Run (Windows PowerShell / VS Code Terminal)

### 1️⃣ Compile
```powershell
javac -cp ".;lib\junit-platform-console-standalone-1.10.2.jar" -d out src\flight\*.java
```

### 2️⃣ Run All Tests
```powershell
java -jar lib\junit-platform-console-standalone-1.10.2.jar -cp out --scan-classpath
```

**Expected output:**
```
[        34 tests successful      ]
[         0 tests failed          ]
```

---

## 💻 GitHub Repository
This project is hosted in a **private GitHub repository**.  
Commits demonstrate a realistic development timeline:
1. Initial project setup and skeleton
2. Implementation of validation logic
3. JUnit 5 integration
4. Complete test suite for Conditions 1–11
5. Final updates and documentation  

My timetabled tutor has been added as a collaborator  

---

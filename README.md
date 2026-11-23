# Railway Reservation and Management System

``Group No: 5``

Team Members:

| S.No | Name | ID No |
|------|------|-------|
| 1 | Ghule Aniketh | AP24110011410 |
| 2 | Mrutyunjay Manas Sahu | AP2411001110101 |
| 3 | Navya Sri Pabbineedi | AP24110011370 |
| 4 | Neeli Harini | AP24110011347 |
| 5 | Akshaya Kandhagatla | AP24110011495 |
| 6 | Bhima Sai Venya | AP24110011527 |

Technologies: Java, Swing, JDBC, PostgreSQL

## Project Structure
```
RailwayReservationSystem/
├── lib/                       (Put postgresql-42.x.x.jar here)
├── sql/
│   └── railway_db.sql         (PostgreSQL script)
├── src/
│   ├── Main.java
│   ├── DBConnection.java
│   ├── Session.java
│   ├── LoginFrame.java
│   ├── SignUpFrame.java
│   ├── TrainSearchFrame.java
│   ├── BookingFrame.java
│   ├── MyBookingsFrame.java
│   └── AdminPanelFrame.java
├── bin/
├── .gitignore
└── README.md
```

 How to Setup and Run

### 1. Database Setup
1. Install **PostgreSQL** and open **pgAdmin**.
2. In pgAdmin, right-click **Databases** -> **Create** -> **Database...** and name it `railway_db`.
3. Select `railway_db`, open the **Query Tool** (Tools -> Query Tool).
4. Open or paste the contents of `sql/railway_db.sql` and click **Execute** (or press F5).
5. Open `src/DBConnection.java` and change the password if your PostgreSQL password is not `password`:
   ```java
   private static final String PASSWORD = "your_postgres_password";
   ```

### 2. Add PostgreSQL Driver
Download the PostgreSQL JDBC driver jar (e.g. `postgresql-42.7.3.jar` from https://jdbc.postgresql.org/ or Maven Central) and put it into the `lib/` folder.

### 3. Compile and Run

#### Mac / Linux:
```bash
cd RailwayReservationSystem
mkdir -p bin
javac -cp "lib/*" -d bin src/*.java
java -cp "bin:lib/*" Main
```

#### Windows:
```cmd
cd RailwayReservationSystem
if not exist bin mkdir bin
javac -cp "lib/*" -d bin src/*.java
java -cp "bin;lib/*" Main
```

# Library Management System (Java)

A menu-driven, console-based Library Management System built in plain
Java (no external libraries required). Data is persisted to simple
CSV files under `data/`, so your books, members, and transaction
history are saved between runs.

## Features

- **Book Management**
  - Add a new book (adding an existing ISBN again tops up its copy count)
  - Remove a book (blocked while any copy is on loan)
  - Update a book's title / author / genre / publication year
  - List all books, or only currently available ones
- **Member Management**
  - Register a new member (auto-assigned ID like `M001`, `M002`, ...)
  - Remove a member (blocked while they still have books out)
  - Update member details
  - List all members
- **Borrowing**
  - Borrow a book by ISBN + Member ID
  - Enforces a max of **3 books per member** at a time
  - Enforces availability (can't borrow a book with 0 copies free)
  - Prevents a member from double-borrowing the same title
  - Assigns a 14-day due date automatically
- **Returning**
  - Return by Transaction ID, or by ISBN + Member ID
  - Automatically calculates a late fine (₹5/day, configurable) if
    returned after the due date
- **Search**
  - Case-insensitive search across books (ISBN / title / author / genre)
  - Case-insensitive search across members (ID / name / email / phone)
- **Reports**
  - Library summary (titles, total copies, available copies, members,
    active loans, overdue loans)
  - Full transaction history
  - Currently active (borrowed) loans
  - Overdue loans, with days-overdue shown
  - Transaction history filtered by member or by book

## Project Structure

```
LibraryManagementSystem/
├── src/
│   ├── Book.java                    # Book entity + CSV (de)serialization
│   ├── Member.java                  # Member entity + CSV (de)serialization
│   ├── Transaction.java             # Borrow/return record, due dates, fines
│   ├── CsvUtil.java                 # Small CSV escaping/parsing helper
│   ├── FileHandler.java             # Reads/writes data/*.csv
│   ├── Library.java                 # Core business logic (the "service" layer)
│   └── LibraryManagementSystem.java # Main class / console menu (entry point)
├── data/                            # CSV data files live here (auto-created)
├── README.md
└── run.sh                           # Convenience compile-and-run script (macOS/Linux)
```

## Requirements

- Java Development Kit (JDK) 8 or newer (works fine on JDK 17/21 too).
  Check with:
  ```
  java -version
  javac -version
  ```

## How to Compile & Run

### Option A — using the provided script (macOS/Linux/Git Bash)
```bash
chmod +x run.sh
./run.sh
```

### Option B — manual commands (any OS)
From inside the `LibraryManagementSystem` folder:

```bash
# 1. Compile all source files into a bin/ folder
javac -d bin src/*.java

# 2. Run the program (must run from this same folder so it can find/create data/)
java -cp bin LibraryManagementSystem
```

On Windows (Command Prompt), the same two commands work as-is.

The first time you run it, `data/books.csv`, `data/members.csv`, and
`data/transactions.csv` are created automatically (empty, with just a
header row). Everything you add is saved there immediately, so you
can close the program and reopen it later without losing data.

## Sample Walkthrough

```
1 -> 1                      (Book Management -> Add New Book)
   ISBN: 9780134685991
   Title: Effective Java
   Author: Joshua Bloch
   Genre: Programming
   Publication Year: 2018
   Number of Copies: 2

2 -> 1                      (Member Management -> Register New Member)
   Name: Vishesh Tripathi
   Email: vishesh@example.com
   Phone: 9876543210
   -> Assigned Member ID: M001

3                            (Borrow a Book)
   Enter Book ISBN: 9780134685991
   Enter Member ID: M001
   -> Transaction ID: T0001, due in 14 days

4 -> 1                      (Return a Book -> by Transaction ID)
   Enter Transaction ID: T0001
   -> Returned, fine calculated automatically if late

6 -> 1                      (Reports -> Library Summary)
```

## Design Notes / Extending the Project

- **Why CSV instead of a database?** Keeps the project dependency-free
  and easy to inspect/edit by hand, while still demonstrating real
  file I/O and persistence — a common requirement at this difficulty
  level. Swapping `FileHandler` for a JDBC-backed implementation (e.g.
  SQLite or MySQL) later would not require changing `Library.java` or
  the menu code at all, since they only depend on the `List<Book>` /
  `List<Member>` / `List<Transaction>` contracts.
- **Borrow limit / loan period / fine rate** are constants
  (`Member.MAX_BOOKS_ALLOWED`, `Transaction.LOAN_PERIOD_DAYS`,
  `Transaction.FINE_PER_DAY`) — change them in one place to retune the
  system's policy.
- **IDs**: Member IDs (`M001`, `M002`, ...) and Transaction IDs
  (`T0001`, `T0002`, ...) auto-increment and are resumed correctly on
  restart by scanning the existing CSV data.
- Possible extensions: a GUI (Swing/JavaFX), multiple copies tracked
  individually (per-copy condition/location), email/SMS due-date
  reminders, reservation/waitlist queue for popular titles, an admin
  vs. member login system, or exporting reports to PDF/CSV.

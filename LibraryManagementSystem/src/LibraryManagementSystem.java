import java.util.List;
import java.util.Scanner;

/**
 * LibraryManagementSystem.java
 * Entry point. Presents a menu-driven console interface over the
 * Library service class. Run with:
 *      java LibraryManagementSystem
 * (from the compiled "bin" / "out" directory, with the working
 * directory containing the "data" folder so CSV files persist).
 */
public class LibraryManagementSystem {
    private static final Scanner sc = new Scanner(System.in);
    private static final Library library = new Library();

    public static void main(String[] args) {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": bookMenu(); break;
                case "2": memberMenu(); break;
                case "3": borrowBookFlow(); break;
                case "4": returnBookFlow(); break;
                case "5": searchMenu(); break;
                case "6": reportsMenu(); break;
                case "0":
                    running = false;
                    System.out.println("\nThank you for using the Library Management System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        sc.close();
    }

    private static void printBanner() {
        System.out.println("=========================================");
        System.out.println("     LIBRARY MANAGEMENT SYSTEM (Java)   ");
        System.out.println("=========================================");
    }

    private static void printMainMenu() {
        System.out.println("\n----------------- MAIN MENU -----------------");
        System.out.println("1. Book Management");
        System.out.println("2. Member Management");
        System.out.println("3. Borrow a Book");
        System.out.println("4. Return a Book");
        System.out.println("5. Search");
        System.out.println("6. Reports");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    // =====================================================================
    // BOOK MANAGEMENT MENU
    // =====================================================================

    private static void bookMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n------------- BOOK MANAGEMENT -------------");
            System.out.println("1. Add New Book");
            System.out.println("2. Remove Book");
            System.out.println("3. Update Book Details");
            System.out.println("4. List All Books");
            System.out.println("5. List Available Books Only");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": addBookFlow(); break;
                case "2": removeBookFlow(); break;
                case "3": updateBookFlow(); break;
                case "4": listBooks(library.getAllBooks()); break;
                case "5": listBooks(library.getAvailableBooks()); break;
                case "0": back = true; break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void addBookFlow() {
        System.out.println("\n-- Add New Book --");
        String isbn = prompt("ISBN: ");
        String title = prompt("Title: ");
        String author = prompt("Author: ");
        String genre = prompt("Genre: ");
        int year = promptInt("Publication Year: ");
        int copies = promptInt("Number of Copies: ");
        Library.OperationResult res = library.addBook(isbn, title, author, genre, year, copies);
        printResult(res);
    }

    private static void removeBookFlow() {
        System.out.println("\n-- Remove Book --");
        String isbn = prompt("Enter ISBN of the book to remove: ");
        printResult(library.removeBook(isbn));
    }

    private static void updateBookFlow() {
        System.out.println("\n-- Update Book Details --");
        String isbn = prompt("Enter ISBN of the book to update: ");
        if (!library.findBookByIsbn(isbn).isPresent()) {
            System.out.println("No book found with that ISBN.");
            return;
        }
        System.out.println("Leave a field blank to keep it unchanged.");
        String title = prompt("New Title: ");
        String author = prompt("New Author: ");
        String genre = prompt("New Genre: ");
        String yearStr = prompt("New Publication Year: ");
        Integer year = yearStr.isEmpty() ? null : Integer.parseInt(yearStr);
        printResult(library.updateBook(isbn, title, author, genre, year));
    }

    private static void listBooks(List<Book> books) {
        System.out.println("\n-- Book List (" + books.size() + ") --");
        if (books.isEmpty()) {
            System.out.println("No books to display.");
            return;
        }
        for (Book b : books) System.out.println(b);
    }

    // =====================================================================
    // MEMBER MANAGEMENT MENU
    // =====================================================================

    private static void memberMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n------------- MEMBER MANAGEMENT -------------");
            System.out.println("1. Register New Member");
            System.out.println("2. Remove Member");
            System.out.println("3. Update Member Details");
            System.out.println("4. List All Members");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": addMemberFlow(); break;
                case "2": removeMemberFlow(); break;
                case "3": updateMemberFlow(); break;
                case "4": listMembers(library.getAllMembers()); break;
                case "0": back = true; break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void addMemberFlow() {
        System.out.println("\n-- Register New Member --");
        String name = prompt("Name: ");
        String email = prompt("Email: ");
        String phone = prompt("Phone: ");
        Member m = library.addMember(name, email, phone);
        System.out.println("Member registered successfully. Assigned Member ID: " + m.getMemberId());
    }

    private static void removeMemberFlow() {
        System.out.println("\n-- Remove Member --");
        String id = prompt("Enter Member ID to remove: ");
        printResult(library.removeMember(id));
    }

    private static void updateMemberFlow() {
        System.out.println("\n-- Update Member Details --");
        String id = prompt("Enter Member ID to update: ");
        if (!library.findMemberById(id).isPresent()) {
            System.out.println("No member found with that ID.");
            return;
        }
        System.out.println("Leave a field blank to keep it unchanged.");
        String name = prompt("New Name: ");
        String email = prompt("New Email: ");
        String phone = prompt("New Phone: ");
        printResult(library.updateMember(id, name, email, phone));
    }

    private static void listMembers(List<Member> members) {
        System.out.println("\n-- Member List (" + members.size() + ") --");
        if (members.isEmpty()) {
            System.out.println("No members to display.");
            return;
        }
        for (Member m : members) System.out.println(m);
    }

    // =====================================================================
    // BORROW / RETURN FLOWS
    // =====================================================================

    private static void borrowBookFlow() {
        System.out.println("\n-- Borrow a Book --");
        String isbn = prompt("Enter Book ISBN: ");
        String memberId = prompt("Enter Member ID: ");
        printResult(library.borrowBook(isbn, memberId));
    }

    private static void returnBookFlow() {
        System.out.println("\n-- Return a Book --");
        System.out.println("1. Return using Transaction ID");
        System.out.println("2. Return using Book ISBN + Member ID");
        String choice = prompt("Enter your choice: ");
        if (choice.equals("1")) {
            String txnId = prompt("Enter Transaction ID: ");
            printResult(library.returnBook(txnId));
        } else if (choice.equals("2")) {
            String isbn = prompt("Enter Book ISBN: ");
            String memberId = prompt("Enter Member ID: ");
            printResult(library.returnBookByIsbnAndMember(isbn, memberId));
        } else {
            System.out.println("Invalid choice.");
        }
    }

    // =====================================================================
    // SEARCH MENU
    // =====================================================================

    private static void searchMenu() {
        System.out.println("\n----------------- SEARCH -----------------");
        System.out.println("1. Search Books (ISBN / Title / Author / Genre)");
        System.out.println("2. Search Members (ID / Name / Email / Phone)");
        System.out.print("Enter your choice: ");
        String choice = sc.nextLine().trim();
        if (choice.equals("1")) {
            String query = prompt("Enter search term: ");
            listBooks(library.searchBooks(query));
        } else if (choice.equals("2")) {
            String query = prompt("Enter search term: ");
            listMembers(library.searchMembers(query));
        } else {
            System.out.println("Invalid choice.");
        }
    }

    // =====================================================================
    // REPORTS MENU
    // =====================================================================

    private static void reportsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n----------------- REPORTS -----------------");
            System.out.println("1. Library Summary");
            System.out.println("2. All Transactions");
            System.out.println("3. Currently Borrowed (Active Loans)");
            System.out.println("4. Overdue Books");
            System.out.println("5. Transaction History for a Member");
            System.out.println("6. Transaction History for a Book");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": librarySummary(); break;
                case "2": printTransactions(library.getAllTransactions()); break;
                case "3": printTransactions(library.getActiveLoans()); break;
                case "4": printTransactions(library.getOverdueLoans()); break;
                case "5": {
                    String memberId = prompt("Enter Member ID: ");
                    printTransactions(library.getTransactionsForMember(memberId));
                    break;
                }
                case "6": {
                    String isbn = prompt("Enter Book ISBN: ");
                    printTransactions(library.getTransactionsForBook(isbn));
                    break;
                }
                case "0": back = true; break;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void librarySummary() {
        System.out.println("\n-- Library Summary --");
        System.out.println("Total distinct book titles : " + library.totalBookTitles());
        System.out.println("Total physical copies       : " + library.totalCopiesInLibrary());
        System.out.println("Currently available copies  : " + library.totalAvailableCopies());
        System.out.println("Total registered members    : " + library.totalMembers());
        System.out.println("Active loans                 : " + library.getActiveLoans().size());
        System.out.println("Overdue loans                : " + library.getOverdueLoans().size());
    }

    private static void printTransactions(List<Transaction> txns) {
        System.out.println("\n-- Transactions (" + txns.size() + ") --");
        if (txns.isEmpty()) {
            System.out.println("No transactions to display.");
            return;
        }
        for (Transaction t : txns) System.out.println(t);
    }

    // =====================================================================
    // INPUT HELPERS
    // =====================================================================

    private static String prompt(String label) {
        System.out.print(label);
        return sc.nextLine().trim();
    }

    private static int promptInt(String label) {
        while (true) {
            System.out.print(label);
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static void printResult(Library.OperationResult res) {
        System.out.println((res.success ? "[SUCCESS] " : "[FAILED] ") + res.message);
    }
}

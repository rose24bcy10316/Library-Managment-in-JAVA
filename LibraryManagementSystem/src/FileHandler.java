import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileHandler.java
 * Reads and writes the three CSV data files under the /data directory:
 *   data/books.csv        -> one line per Book
 *   data/members.csv      -> one line per Member
 *   data/transactions.csv -> one line per Transaction
 *
 * All files are created automatically (with a header row) the first
 * time the program runs, so a fresh checkout of the project works
 * out of the box with an empty library.
 */
public class FileHandler {
    private static final String DATA_DIR = "data";
    public static final String BOOKS_FILE = DATA_DIR + File.separator + "books.csv";
    public static final String MEMBERS_FILE = DATA_DIR + File.separator + "members.csv";
    public static final String TRANSACTIONS_FILE = DATA_DIR + File.separator + "transactions.csv";

    private static final String BOOKS_HEADER = "isbn,title,author,genre,publicationYear,totalCopies,availableCopies";
    private static final String MEMBERS_HEADER = "memberId,name,email,phone,booksBorrowedCount";
    private static final String TRANSACTIONS_HEADER = "transactionId,isbn,memberId,borrowDate,dueDate,returnDate,fineAmount,status";

    /** Ensures the data directory and all three CSV files (with headers) exist. */
    public static void ensureDataFiles() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            createIfMissing(BOOKS_FILE, BOOKS_HEADER);
            createIfMissing(MEMBERS_FILE, MEMBERS_HEADER);
            createIfMissing(TRANSACTIONS_FILE, TRANSACTIONS_HEADER);
        } catch (IOException e) {
            System.err.println("Warning: could not initialize data files: " + e.getMessage());
        }
    }

    private static void createIfMissing(String path, String header) throws IOException {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            Files.write(p, (header + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
        }
    }

    // ---------------- Generic line readers/writers ----------------

    private static List<String> readDataLines(String path) {
        List<String> lines = new ArrayList<>();
        File f = new File(path);
        if (!f.exists()) return lines;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading " + path + ": " + e.getMessage());
        }
        return lines;
    }

    private static void writeDataLines(String path, String header, List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path, false), StandardCharsets.UTF_8))) {
            bw.write(header);
            bw.newLine();
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing " + path + ": " + e.getMessage());
        }
    }

    // ---------------- Books ----------------

    public static List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        for (String line : readDataLines(BOOKS_FILE)) {
            try {
                books.add(Book.fromCsv(line));
            } catch (Exception e) {
                System.err.println("Skipping malformed book row: " + line);
            }
        }
        return books;
    }

    public static void saveBooks(List<Book> books) {
        List<String> lines = new ArrayList<>();
        for (Book b : books) lines.add(b.toCsv());
        writeDataLines(BOOKS_FILE, BOOKS_HEADER, lines);
    }

    // ---------------- Members ----------------

    public static List<Member> loadMembers() {
        List<Member> members = new ArrayList<>();
        for (String line : readDataLines(MEMBERS_FILE)) {
            try {
                members.add(Member.fromCsv(line));
            } catch (Exception e) {
                System.err.println("Skipping malformed member row: " + line);
            }
        }
        return members;
    }

    public static void saveMembers(List<Member> members) {
        List<String> lines = new ArrayList<>();
        for (Member m : members) lines.add(m.toCsv());
        writeDataLines(MEMBERS_FILE, MEMBERS_HEADER, lines);
    }

    // ---------------- Transactions ----------------

    public static List<Transaction> loadTransactions() {
        List<Transaction> txns = new ArrayList<>();
        for (String line : readDataLines(TRANSACTIONS_FILE)) {
            try {
                txns.add(Transaction.fromCsv(line));
            } catch (Exception e) {
                System.err.println("Skipping malformed transaction row: " + line);
            }
        }
        return txns;
    }

    public static void saveTransactions(List<Transaction> txns) {
        List<String> lines = new ArrayList<>();
        for (Transaction t : txns) lines.add(t.toCsv());
        writeDataLines(TRANSACTIONS_FILE, TRANSACTIONS_HEADER, lines);
    }
}

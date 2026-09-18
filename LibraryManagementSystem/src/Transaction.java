import java.time.LocalDate;

/**
 * Transaction.java
 * Represents a single borrow event, and (once completed) its return.
 */
public class Transaction {
    public enum Status { BORROWED, RETURNED }

    public static final int LOAN_PERIOD_DAYS = 14;
    public static final double FINE_PER_DAY = 5.0; // currency units per day overdue

    private String transactionId;
    private String isbn;
    private String memberId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate; // null until returned
    private double fineAmount;
    private Status status;

    public Transaction(String transactionId, String isbn, String memberId,
                        LocalDate borrowDate, LocalDate dueDate, LocalDate returnDate,
                        double fineAmount, Status status) {
        this.transactionId = transactionId;
        this.isbn = isbn;
        this.memberId = memberId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
        this.status = status;
    }

    /** Creates a brand-new BORROWED transaction starting today. */
    public static Transaction newBorrow(String transactionId, String isbn, String memberId) {
        LocalDate today = LocalDate.now();
        return new Transaction(transactionId, isbn, memberId, today,
                today.plusDays(LOAN_PERIOD_DAYS), null, 0.0, Status.BORROWED);
    }

    // ---------- Getters ----------
    public String getTransactionId() { return transactionId; }
    public String getIsbn() { return isbn; }
    public String getMemberId() { return memberId; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getFineAmount() { return fineAmount; }
    public Status getStatus() { return status; }

    public boolean isOverdue() {
        return status == Status.BORROWED && LocalDate.now().isAfter(dueDate);
    }

    public long daysOverdue() {
        if (!isOverdue()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    /** Marks this transaction as returned today and calculates any fine due. */
    public double closeWithReturn() {
        this.returnDate = LocalDate.now();
        long lateDays = java.time.temporal.ChronoUnit.DAYS.between(dueDate, returnDate);
        this.fineAmount = lateDays > 0 ? lateDays * FINE_PER_DAY : 0.0;
        this.status = Status.RETURNED;
        return this.fineAmount;
    }

    public String toCsv() {
        return String.join(",",
                CsvUtil.escape(transactionId), CsvUtil.escape(isbn), CsvUtil.escape(memberId),
                borrowDate.toString(), dueDate.toString(),
                returnDate == null ? "" : returnDate.toString(),
                String.valueOf(fineAmount), status.name());
    }

    public static Transaction fromCsv(String line) {
        String[] p = CsvUtil.splitCsvLine(line);
        LocalDate borrow = LocalDate.parse(p[3]);
        LocalDate due = LocalDate.parse(p[4]);
        LocalDate ret = p[5].isEmpty() ? null : LocalDate.parse(p[5]);
        double fine = Double.parseDouble(p[6]);
        Status status = Status.valueOf(p[7]);
        return new Transaction(p[0], p[1], p[2], borrow, due, ret, fine, status);
    }

    @Override
    public String toString() {
        String returnStr = returnDate == null ? "—" : returnDate.toString();
        String overdueTag = isOverdue() ? String.format(" [OVERDUE by %d day(s)]", daysOverdue()) : "";
        return String.format(
                "TxnID: %-8s | ISBN: %-12s | MemberID: %-8s | Borrowed: %s | Due: %s | Returned: %-10s | Status: %-9s | Fine: %.2f%s",
                transactionId, isbn, memberId, borrowDate, dueDate, returnStr, status, fineAmount, overdueTag);
    }
}

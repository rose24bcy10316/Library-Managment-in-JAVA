/**
 * Member.java
 * Represents a library member who can borrow books.
 */
public class Member {
    public static final int MAX_BOOKS_ALLOWED = 3;

    private String memberId;
    private String name;
    private String email;
    private String phone;
    private int booksBorrowedCount;

    public Member(String memberId, String name, String email, String phone, int booksBorrowedCount) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.booksBorrowedCount = booksBorrowedCount;
    }

    public Member(String memberId, String name, String email, String phone) {
        this(memberId, name, email, phone, 0);
    }

    // ---------- Getters ----------
    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public int getBooksBorrowedCount() { return booksBorrowedCount; }

    // ---------- Setters ----------
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean canBorrowMore() {
        return booksBorrowedCount < MAX_BOOKS_ALLOWED;
    }

    public void incrementBorrowedCount() { booksBorrowedCount++; }

    public void decrementBorrowedCount() {
        if (booksBorrowedCount > 0) booksBorrowedCount--;
    }

    public String toCsv() {
        return String.join(",",
                CsvUtil.escape(memberId), CsvUtil.escape(name),
                CsvUtil.escape(email), CsvUtil.escape(phone),
                String.valueOf(booksBorrowedCount));
    }

    public static Member fromCsv(String line) {
        String[] p = CsvUtil.splitCsvLine(line);
        return new Member(p[0], p[1], p[2], p[3], Integer.parseInt(p[4]));
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %-8s | %-25s | Email: %-25s | Phone: %-12s | Borrowed: %d/%d",
                memberId, name, email, phone, booksBorrowedCount, MAX_BOOKS_ALLOWED);
    }
}

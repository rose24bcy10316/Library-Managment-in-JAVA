import java.util.*;
import java.util.stream.Collectors;

/**
 * Library.java
 * Core service class holding all books, members and transactions in
 * memory, and providing every operation the menu (LibraryManagementSystem)
 * needs: add/remove/update/search books & members, borrow, return,
 * and reporting (overdue list, transaction history, etc).
 *
 * Data is loaded from CSV on startup and persisted back to CSV after
 * every mutating operation, so nothing is lost between runs.
 */
public class Library {
    private final Map<String, Book> booksByIsbn = new LinkedHashMap<>();
    private final Map<String, Member> membersById = new LinkedHashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();

    private int nextMemberSeq = 1;
    private int nextTxnSeq = 1;

    public Library() {
        FileHandler.ensureDataFiles();
        load();
    }

    private void load() {
        for (Book b : FileHandler.loadBooks()) {
            booksByIsbn.put(b.getIsbn(), b);
        }
        for (Member m : FileHandler.loadMembers()) {
            membersById.put(m.getMemberId(), m);
            int num = extractTrailingNumber(m.getMemberId());
            if (num >= nextMemberSeq) nextMemberSeq = num + 1;
        }
        transactions.addAll(FileHandler.loadTransactions());
        for (Transaction t : transactions) {
            int num = extractTrailingNumber(t.getTransactionId());
            if (num >= nextTxnSeq) nextTxnSeq = num + 1;
        }
    }

    private int extractTrailingNumber(String id) {
        String digits = id.replaceAll("\\D", "");
        if (digits.isEmpty()) return 0;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void persistBooks() { FileHandler.saveBooks(new ArrayList<>(booksByIsbn.values())); }
    private void persistMembers() { FileHandler.saveMembers(new ArrayList<>(membersById.values())); }
    private void persistTransactions() { FileHandler.saveTransactions(transactions); }

    // =====================================================================
    // BOOK MANAGEMENT
    // =====================================================================

    public static class OperationResult {
        public final boolean success;
        public final String message;
        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        public static OperationResult ok(String msg) { return new OperationResult(true, msg); }
        public static OperationResult fail(String msg) { return new OperationResult(false, msg); }
    }

    public OperationResult addBook(String isbn, String title, String author, String genre,
                                    int year, int copies) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return OperationResult.fail("ISBN cannot be empty.");
        }
        if (copies < 1) {
            return OperationResult.fail("Number of copies must be at least 1.");
        }
        if (booksByIsbn.containsKey(isbn)) {
            // Title already exists: treat this as "adding more copies" of the same book
            Book existing = booksByIsbn.get(isbn);
            existing.addCopies(copies);
            persistBooks();
            return OperationResult.ok("Book with this ISBN already existed. Added " + copies +
                    " more cop" + (copies == 1 ? "y" : "ies") + " to \"" + existing.getTitle() + "\".");
        }
        Book book = new Book(isbn.trim(), title.trim(), author.trim(), genre.trim(), year, copies);
        booksByIsbn.put(book.getIsbn(), book);
        persistBooks();
        return OperationResult.ok("Book \"" + title + "\" added successfully with " + copies + " copy/copies.");
    }

    public OperationResult removeBook(String isbn) {
        Book book = booksByIsbn.get(isbn);
        if (book == null) {
            return OperationResult.fail("No book found with ISBN " + isbn + ".");
        }
        boolean hasActiveLoan = transactions.stream()
                .anyMatch(t -> t.getIsbn().equals(isbn) && t.getStatus() == Transaction.Status.BORROWED);
        if (hasActiveLoan) {
            return OperationResult.fail("Cannot remove \"" + book.getTitle() +
                    "\" — one or more copies are currently on loan.");
        }
        booksByIsbn.remove(isbn);
        persistBooks();
        return OperationResult.ok("Book \"" + book.getTitle() + "\" removed successfully.");
    }

    public OperationResult updateBook(String isbn, String title, String author, String genre, Integer year) {
        Book book = booksByIsbn.get(isbn);
        if (book == null) {
            return OperationResult.fail("No book found with ISBN " + isbn + ".");
        }
        if (title != null && !title.trim().isEmpty()) book.setTitle(title.trim());
        if (author != null && !author.trim().isEmpty()) book.setAuthor(author.trim());
        if (genre != null && !genre.trim().isEmpty()) book.setGenre(genre.trim());
        if (year != null) book.setPublicationYear(year);
        persistBooks();
        return OperationResult.ok("Book updated successfully.");
    }

    public Optional<Book> findBookByIsbn(String isbn) {
        return Optional.ofNullable(booksByIsbn.get(isbn));
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(booksByIsbn.values());
    }

    /** Case-insensitive search across ISBN, title, author and genre. */
    public List<Book> searchBooks(String query) {
        String q = query.toLowerCase().trim();
        return booksByIsbn.values().stream()
                .filter(b -> b.getIsbn().toLowerCase().contains(q)
                        || b.getTitle().toLowerCase().contains(q)
                        || b.getAuthor().toLowerCase().contains(q)
                        || b.getGenre().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<Book> getAvailableBooks() {
        return booksByIsbn.values().stream().filter(Book::isAvailable).collect(Collectors.toList());
    }

    // =====================================================================
    // MEMBER MANAGEMENT
    // =====================================================================

    public Member addMember(String name, String email, String phone) {
        String id = "M" + String.format("%03d", nextMemberSeq++);
        Member member = new Member(id, name.trim(), email.trim(), phone.trim());
        membersById.put(id, member);
        persistMembers();
        return member;
    }

    public OperationResult removeMember(String memberId) {
        Member member = membersById.get(memberId);
        if (member == null) {
            return OperationResult.fail("No member found with ID " + memberId + ".");
        }
        if (member.getBooksBorrowedCount() > 0) {
            return OperationResult.fail("Cannot remove " + member.getName() +
                    " — they still have books borrowed.");
        }
        membersById.remove(memberId);
        persistMembers();
        return OperationResult.ok("Member " + member.getName() + " removed successfully.");
    }

    public OperationResult updateMember(String memberId, String name, String email, String phone) {
        Member member = membersById.get(memberId);
        if (member == null) {
            return OperationResult.fail("No member found with ID " + memberId + ".");
        }
        if (name != null && !name.trim().isEmpty()) member.setName(name.trim());
        if (email != null && !email.trim().isEmpty()) member.setEmail(email.trim());
        if (phone != null && !phone.trim().isEmpty()) member.setPhone(phone.trim());
        persistMembers();
        return OperationResult.ok("Member updated successfully.");
    }

    public Optional<Member> findMemberById(String memberId) {
        return Optional.ofNullable(membersById.get(memberId));
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(membersById.values());
    }

    public List<Member> searchMembers(String query) {
        String q = query.toLowerCase().trim();
        return membersById.values().stream()
                .filter(m -> m.getMemberId().toLowerCase().contains(q)
                        || m.getName().toLowerCase().contains(q)
                        || m.getEmail().toLowerCase().contains(q)
                        || m.getPhone().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    // =====================================================================
    // BORROW / RETURN
    // =====================================================================

    public OperationResult borrowBook(String isbn, String memberId) {
        Book book = booksByIsbn.get(isbn);
        if (book == null) {
            return OperationResult.fail("No book found with ISBN " + isbn + ".");
        }
        Member member = membersById.get(memberId);
        if (member == null) {
            return OperationResult.fail("No member found with ID " + memberId + ".");
        }
        if (!book.isAvailable()) {
            return OperationResult.fail("\"" + book.getTitle() + "\" has no available copies right now.");
        }
        if (!member.canBorrowMore()) {
            return OperationResult.fail(member.getName() + " has already reached the borrowing limit of " +
                    Member.MAX_BOOKS_ALLOWED + " books.");
        }
        boolean alreadyHasThisBook = transactions.stream().anyMatch(t ->
                t.getIsbn().equals(isbn) && t.getMemberId().equals(memberId)
                        && t.getStatus() == Transaction.Status.BORROWED);
        if (alreadyHasThisBook) {
            return OperationResult.fail(member.getName() + " already has a copy of \"" + book.getTitle() + "\" borrowed.");
        }

        book.lendOneCopy();
        member.incrementBorrowedCount();
        String txnId = "T" + String.format("%04d", nextTxnSeq++);
        Transaction txn = Transaction.newBorrow(txnId, isbn, memberId);
        transactions.add(txn);

        persistBooks();
        persistMembers();
        persistTransactions();

        return OperationResult.ok(String.format(
                "\"%s\" borrowed by %s. Transaction ID: %s. Due back on %s.",
                book.getTitle(), member.getName(), txnId, txn.getDueDate()));
    }

    public OperationResult returnBook(String transactionId) {
        Transaction txn = transactions.stream()
                .filter(t -> t.getTransactionId().equals(transactionId))
                .findFirst().orElse(null);
        if (txn == null) {
            return OperationResult.fail("No transaction found with ID " + transactionId + ".");
        }
        if (txn.getStatus() == Transaction.Status.RETURNED) {
            return OperationResult.fail("This transaction has already been marked as returned.");
        }

        double fine = txn.closeWithReturn();

        Book book = booksByIsbn.get(txn.getIsbn());
        if (book != null) book.returnOneCopy();

        Member member = membersById.get(txn.getMemberId());
        if (member != null) member.decrementBorrowedCount();

        persistBooks();
        persistMembers();
        persistTransactions();

        String bookTitle = book != null ? book.getTitle() : txn.getIsbn();
        if (fine > 0) {
            return OperationResult.ok(String.format(
                    "\"%s\" returned. Book was overdue — fine payable: %.2f", bookTitle, fine));
        }
        return OperationResult.ok("\"" + bookTitle + "\" returned on time. No fine due.");
    }

    /** Convenience overload: return a book by ISBN + memberId instead of transaction ID. */
    public OperationResult returnBookByIsbnAndMember(String isbn, String memberId) {
        Optional<Transaction> match = transactions.stream()
                .filter(t -> t.getIsbn().equals(isbn) && t.getMemberId().equals(memberId)
                        && t.getStatus() == Transaction.Status.BORROWED)
                .findFirst();
        if (!match.isPresent()) {
            return OperationResult.fail("No active loan found for that book and member combination.");
        }
        return returnBook(match.get().getTransactionId());
    }

    // =====================================================================
    // REPORTING
    // =====================================================================

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    public List<Transaction> getActiveLoans() {
        return transactions.stream()
                .filter(t -> t.getStatus() == Transaction.Status.BORROWED)
                .collect(Collectors.toList());
    }

    public List<Transaction> getOverdueLoans() {
        return transactions.stream()
                .filter(Transaction::isOverdue)
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionsForMember(String memberId) {
        return transactions.stream()
                .filter(t -> t.getMemberId().equals(memberId))
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionsForBook(String isbn) {
        return transactions.stream()
                .filter(t -> t.getIsbn().equals(isbn))
                .collect(Collectors.toList());
    }

    public int totalBookTitles() { return booksByIsbn.size(); }

    public int totalCopiesInLibrary() {
        return booksByIsbn.values().stream().mapToInt(Book::getTotalCopies).sum();
    }

    public int totalAvailableCopies() {
        return booksByIsbn.values().stream().mapToInt(Book::getAvailableCopies).sum();
    }

    public int totalMembers() { return membersById.size(); }
}

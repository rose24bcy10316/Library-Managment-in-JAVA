/**
 * Book.java
 * Represents a single book title held by the library.
 * A Book can have multiple physical copies (totalCopies), of which
 * some may currently be on loan (totalCopies - availableCopies).
 */
public class Book {
    private String isbn;
    private String title;
    private String author;
    private String genre;
    private int publicationYear;
    private int totalCopies;
    private int availableCopies;

    public Book(String isbn, String title, String author, String genre,
                int publicationYear, int totalCopies, int availableCopies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    // Convenience constructor used when first adding a brand-new title
    public Book(String isbn, String title, String author, String genre,
                int publicationYear, int totalCopies) {
        this(isbn, title, author, genre, publicationYear, totalCopies, totalCopies);
    }

    // ---------- Getters ----------
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public int getPublicationYear() { return publicationYear; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    // ---------- Setters ----------
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setPublicationYear(int publicationYear) { this.publicationYear = publicationYear; }

    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }

    /** Adds n new physical copies of this title to the library (n may be negative to remove). */
    public void addCopies(int n) {
        this.totalCopies += n;
        this.availableCopies += n;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    /** Called when a copy is lent out. Returns false if no copy was free. */
    public boolean lendOneCopy() {
        if (availableCopies <= 0) return false;
        availableCopies--;
        return true;
    }

    /** Called when a copy is returned. */
    public void returnOneCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    /** Serializes this book to a single CSV line. */
    public String toCsv() {
        return String.join(",",
                escape(isbn), escape(title), escape(author), escape(genre),
                String.valueOf(publicationYear), String.valueOf(totalCopies),
                String.valueOf(availableCopies));
    }

    /** Parses a CSV line (as produced by toCsv) back into a Book. */
    public static Book fromCsv(String line) {
        String[] p = CsvUtil.splitCsvLine(line);
        return new Book(
                p[0], p[1], p[2], p[3],
                Integer.parseInt(p[4]), Integer.parseInt(p[5]), Integer.parseInt(p[6])
        );
    }

    private String escape(String s) {
        return CsvUtil.escape(s);
    }

    @Override
    public String toString() {
        return String.format(
                "ISBN: %-15s | %-30s | Author: %-20s | Genre: %-12s | Year: %-4d | Copies: %d/%d available",
                isbn, title, author, genre, publicationYear, availableCopies, totalCopies);
    }
}

package biblioty.armbiblioty;

// Клас-модель для книг
public class Book {
    private final int id;
    private final String name;
    private final String author;
    private final String genre;
    private final int year;
    private final int quantity;
    private final String status;

    public Book(int id, String name, String author, String genre, int year, int quantity, String status) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.genre = genre;
        this.year = year;
        this.quantity = quantity;
        this.status = status;
    }

    // Гетери
    public int getId() { return id; }
    public String getName() { return name; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public int getYear() { return year; }
    public int getQuantity() { return quantity; }
    public String getStatus() { return status; }
}
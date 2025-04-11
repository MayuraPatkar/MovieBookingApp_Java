
import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MovieBookingApp extends JFrame {
    private final String username;
    private final String phoneNumber;

    // GUI Components
    private JComboBox<String> movieSelect, showtimeSelect;
    private JPanel seatSelectionPanel;
    private JTextArea movieDetailsArea;
    private JButton bookButton;
    private JLabel availableSeatsLabel;

    // Movie Data
    private Map<String, Movie> movies;
    private Movie currentMovie;
    private String selectedShowtime;
    private Map<String, List<JToggleButton>> seatButtons;
    private Map<String, Integer> seatCounts;

    // Payment Screen
    private JFrame paymentFrame;
    private JTextArea paymentDetailsArea;
    private JTextField cardNumberField, expiryDateField;
    private JPasswordField cvvField;
    private JButton payButton, backButton;

    public MovieBookingApp(String username, String phoneNumber) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        initializeData();
        initializeUI();
    }

    private void initializeData() {
        movies = new HashMap<>();
        movies.put("Shadow Realm", new Movie("Shadow Realm", "A dark fantasy adventure.",
                new String[] { "10:00 AM", "1:00 PM" }, "Phoenix Cinema", 14, 13, 11));
        movies.put("Mind Games", new Movie("Mind Games", "A gripping sci-fi mystery.",
                new String[] { "2:00 PM", "5:00 PM" }, "IMAX Arena", 16, 14, 12));
        movies.put("The Last Symphony", new Movie("The Last Symphony", "A heartfelt story of a musician.",
                new String[] { "11:30 AM", "3:30 PM" }, "Star Theater", 13, 11, 10));

    }

    private void initializeUI() {
        setTitle("CineEase - Movie Booking System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(255, 200, 100));

        // Components
        movieSelect = new JComboBox<>(movies.keySet().toArray(new String[0]));
        showtimeSelect = new JComboBox<>();
        movieDetailsArea = new JTextArea(8, 30);
        movieDetailsArea.setEditable(false);
        movieDetailsArea.setBackground(new Color(255, 255, 240));
        JScrollPane detailsScrollPane = new JScrollPane(movieDetailsArea);
        detailsScrollPane.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Movie Details"));

        availableSeatsLabel = new JLabel("Available Seats:");
        availableSeatsLabel.setFont(new Font("Arial", Font.BOLD, 14));

        seatSelectionPanel = new JPanel(new GridLayout(0, 10, 5, 5)); // Grid for seats
        JScrollPane seatScrollPane = new JScrollPane(seatSelectionPanel);
        seatScrollPane.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Seat Selection"));
        seatScrollPane.setPreferredSize(new Dimension(400, 200));

        bookButton = new JButton("Proceed to Payment");
        bookButton.setBackground(new Color(0, 128, 128)); // Teal button
        bookButton.setForeground(Color.WHITE);
        bookButton.setFont(new Font("Arial", Font.BOLD, 16));
        bookButton.setEnabled(false);

        movieSelect.addActionListener(e -> updateMovieDetails());
        showtimeSelect.addActionListener(e -> updateSeatSelection());
        bookButton.addActionListener(e -> showPaymentScreen());

        // Layout for Movie Selection
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        selectionPanel.setBackground(new Color(255, 255, 255));
        selectionPanel.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Movie and Showtime"));
        selectionPanel.add(new JLabel("Select Movie:"));
        selectionPanel.add(movieSelect);
        selectionPanel.add(new JLabel("Select Showtime:"));
        selectionPanel.add(showtimeSelect);

        // Main Layout
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(getContentPane().getBackground());
        mainPanel.add(selectionPanel, BorderLayout.NORTH);
        mainPanel.add(detailsScrollPane, BorderLayout.WEST);
        mainPanel.add(availableSeatsLabel, BorderLayout.SOUTH);
        mainPanel.add(seatScrollPane, BorderLayout.CENTER);
        mainPanel.add(bookButton, BorderLayout.SOUTH);

        add(mainPanel);

        updateMovieDetails();
        initializePaymentScreen();
    }

    private void updateMovieDetails() {
        String selected = (String) movieSelect.getSelectedItem();
        if (selected != null) {
            currentMovie = movies.get(selected);
            movieDetailsArea.setText("""
                    Movie Details
                    Title: %s
                    Description: %s
                    Theater: %s
                    Showtimes: %s
                    """.formatted(
                    currentMovie.name,
                    currentMovie.description,
                    currentMovie.theater,
                    String.join(", ", currentMovie.showtimes)));

            showtimeSelect.removeAllItems();
            Arrays.stream(currentMovie.showtimes).forEach(showtimeSelect::addItem);
            selectedShowtime = null;
            seatSelectionPanel.removeAll();
            seatCounts = new HashMap<>();
            seatButtons = new HashMap<>();
            bookButton.setEnabled(false);
            availableSeatsLabel.setText("Available Seats:");
            seatSelectionPanel.revalidate();
            seatSelectionPanel.repaint();
        }
    }

    private void updateSeatSelection() {
        selectedShowtime = (String) showtimeSelect.getSelectedItem();
        if (currentMovie != null && selectedShowtime != null) {
            seatSelectionPanel.removeAll();
            seatCounts = new HashMap<>();
            seatButtons = new HashMap<>();

            addSeatButtons("Silver", currentMovie.silverSeats);
            addSeatButtons("Gold", currentMovie.goldSeats);
            addSeatButtons("VIP", currentMovie.vipSeats);

            availableSeatsLabel.setText("Available Seats - Silver: " + currentMovie.silverSeats +
                    " | Gold: " + currentMovie.goldSeats +
                    " | VIP: " + currentMovie.vipSeats);
            bookButton.setEnabled(false); // Enable only when at least one seat is selected
            seatSelectionPanel.revalidate();
            seatSelectionPanel.repaint();
        }
    }

    private void addSeatButtons(String type, int count) {
        List<JToggleButton> buttons = new ArrayList<>();
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(new JLabel(type + ": "));
        seatSelectionPanel.add(typePanel);

        for (int i = 1; i <= count; i++) {
            JToggleButton seatButton = new JToggleButton(type.substring(0, 1) + i);
            seatButton.setBackground(new Color(220, 220, 220));
            seatButton.setForeground(Color.BLACK);
            seatButton.setPreferredSize(new Dimension(50, 30));
            seatButton.addActionListener(e -> {
                if (seatButton.isSelected()) {
                    seatCounts.put(seatButton.getText(), 1); // Mark as selected
                } else {
                    seatCounts.remove(seatButton.getText()); // Mark as deselected
                }
                bookButton.setEnabled(!seatCounts.isEmpty());
            });
            buttons.add(seatButton);
            seatSelectionPanel.add(seatButton);
        }
        seatButtons.put(type, buttons);
    }

    private void initializePaymentScreen() {
        paymentFrame = new JFrame("Payment Portal");
        paymentFrame.setSize(450, 400);
        paymentFrame.setLocationRelativeTo(this);
        paymentFrame.getContentPane().setBackground(new Color(240, 248, 255)); // Light blue

        paymentDetailsArea = new JTextArea(8, 35);
        paymentDetailsArea.setEditable(false);
        paymentDetailsArea.setBackground(new Color(255, 255, 240)); // Light yellow
        JScrollPane paymentDetailsScrollPane = new JScrollPane(paymentDetailsArea);
        paymentDetailsScrollPane.setBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Booking Details"));

        cardNumberField = new JTextField(16);
        expiryDateField = new JTextField(5);
        cvvField = new JPasswordField(3);

        payButton = new JButton("Pay Now");
        payButton.setBackground(new Color(0, 153, 76)); // Green
        payButton.setForeground(Color.WHITE);
        payButton.setFont(new Font("Arial", Font.BOLD, 14));

        backButton = new JButton("Cancel");
        backButton.setBackground(new Color(178, 34, 34)); // Firebrick
        backButton.setForeground(Color.WHITE);
        backButton.setFont(new Font("Arial", Font.BOLD, 14));

        payButton.addActionListener(e -> handlePayment());
        backButton.addActionListener(e -> paymentFrame.setVisible(false));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.add(new JLabel("Card Number:", SwingConstants.RIGHT));
        formPanel.add(cardNumberField);
        formPanel.add(new JLabel("Expiry (MM/YY):", SwingConstants.RIGHT));
        formPanel.add(expiryDateField);
        formPanel.add(new JLabel("CVV:", SwingConstants.RIGHT));
        formPanel.add(cvvField);
        formPanel.add(backButton);
        formPanel.add(payButton);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(paymentFrame.getContentPane().getBackground());

        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        container.setBackground(paymentFrame.getContentPane().getBackground());
        container.add(paymentDetailsScrollPane, BorderLayout.NORTH);
        container.add(formPanel, BorderLayout.CENTER);

        paymentFrame.add(container);
    }

    private void handlePayment() {
        String card = cardNumberField.getText().trim();
        String expiry = expiryDateField.getText().trim();
        String cvv = new String(cvvField.getPassword()).trim();

        if (card.matches("\\d{16}") && expiry.matches("\\d{2}/\\d{2}") && cvv.matches("\\d{3}")) {
            int totalSeats = seatCounts.size();
            if (totalSeats > 0) {
                String seatType = "";
                if (!seatCounts.isEmpty()) {
                    seatType = seatCounts.keySet().iterator().next().substring(0, 1);
                    if (seatType.equals("S"))
                        seatType = "Silver";
                    else if (seatType.equals("G"))
                        seatType = "Gold";
                    else if (seatType.equals("V"))
                        seatType = "VIP";
                }

                if (currentMovie.bookSeats(seatType, totalSeats)) {
                    try (FileWriter writer = new FileWriter("bookings.csv", true)) {
                        writer.write("%s,%s,%s,%s,%s,%d,%d\n".formatted(
                                username, phoneNumber, currentMovie.name, selectedShowtime,
                                seatType, totalSeats, totalSeats * 150));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Error saving booking.");
                    }
                    JOptionPane.showMessageDialog(this, "🎟 Booking Confirmed for " + totalSeats + " seats!");
                    paymentFrame.setVisible(false);
                    updateMovieDetails(); // Refresh available seats
                } else {
                    JOptionPane.showMessageDialog(this, "Insufficient seats available.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select seats before proceeding to payment.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid payment details.");
        }
    }

    private void showPaymentScreen() {
        if (currentMovie == null || selectedShowtime == null || seatCounts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a movie, showtime, and at least one seat.");
            return;
        }

        int count = seatCounts.size();
        String seatTypes = String.join(", ", seatCounts.keySet());
        int total = count * 150;

        paymentDetailsArea.setText("""
                Booking Summary:
                User: %s
                Phone: %s
                Movie: %s
                Showtime: %s
                Seats: %s (%d seats)
                Total Amount: Rs %d
                """.formatted(username, phoneNumber, currentMovie.name, selectedShowtime, seatTypes, count, total));

        paymentFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().createLoginWindow());
    }

    static class Movie {
        String name, description, theater;
        String[] showtimes;
        int silverSeats, goldSeats, vipSeats;

        public Movie(String name, String desc, String[] showtimes, String theater, int silver, int gold, int vip) {
            this.name = name;
            this.description = desc;
            this.showtimes = showtimes;
            this.theater = theater;
            this.silverSeats = silver;
            this.goldSeats = gold;
            this.vipSeats = vip;
        }

        public boolean bookSeats(String type, int count) {
            switch (type) {
                case "Silver":
                    if (silverSeats >= count) {
                        silverSeats -= count;
                        return true;
                    }
                    break;
                case "Gold":
                    if (goldSeats >= count) {
                        goldSeats -= count;
                        return true;
                    }
                    break;
                case "VIP":
                    if (vipSeats >= count) {
                        vipSeats -= count;
                        return true;
                    }
                    break;
            }
            return false;
        }
    }
}

// Login GUI
class LoginScreen {
    public void createLoginWindow() {
        JFrame frame = new JFrame("Welcome to CineEase");
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(new Color(255, 200, 100)); // Light cyan

        JTextField usernameField = new JTextField();
        JTextField phoneField = new JTextField();
        JCheckBox agree = new JCheckBox("Agree to terms & conditions");
        agree.setBackground(panel.getBackground());
        JButton enterButton = new JButton("Enter Portal");
        enterButton.setBackground(new Color(65, 105, 225)); // Royal blue
        enterButton.setForeground(Color.WHITE);
        enterButton.setFont(new Font("Arial", Font.BOLD, 14));

        enterButton.addActionListener(e -> {
            String name = usernameField.getText().trim();
            String phone = phoneField.getText().trim();

            if (!agree.isSelected()) {
                JOptionPane.showMessageDialog(frame, "Please agree to the terms.");
                return;
            }

            if (!name.isEmpty() && phone.matches("\\d{10}")) {
                frame.dispose();
                new MovieBookingApp(name, phone).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(frame, "Enter valid name and phone number.");
            }
        });

        panel.add(new JLabel("Name:", SwingConstants.LEFT));
        panel.add(usernameField);
        panel.add(new JLabel("Phone Number:", SwingConstants.LEFT));
        panel.add(phoneField);
        panel.add(agree);
        panel.add(enterButton);

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
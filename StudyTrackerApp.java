import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.sql.*;

public class StudyTrackerApp extends JFrame {

    private DefaultTableModel model;
    private JLabel totalLbl, goalLbl;
    private double totalHours = 0;
    private double dailyGoal = 0;
    private GraphPanel graphPanel;

    // 👤 Change this email if needed
    private final String USER_EMAIL = "neha@gmail.com";

    public StudyTrackerApp() {

        setTitle("Study Habit Tracker");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topPanel.add(new JLabel("Subject:"));
        JTextField subField = new JTextField();
        topPanel.add(subField);

        topPanel.add(new JLabel("Hours Studied:"));
        JTextField hrField = new JTextField();
        topPanel.add(hrField);

        topPanel.add(new JLabel("Date (YYYY-MM-DD or empty):"));
        JTextField dateField = new JTextField();
        topPanel.add(dateField);

        topPanel.add(new JLabel("Daily Goal (hrs):"));
        JTextField goalField = new JTextField("0");
        topPanel.add(goalField);

        add(topPanel, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Subject", "Hours", "Date"}, 0);
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        graphPanel = new GraphPanel();
        add(graphPanel, BorderLayout.EAST);

        JPanel botPanel = new JPanel();
        JButton addBtn = new JButton("Add Entry");
        totalLbl = new JLabel("Total: 0.0 hrs  |");
        goalLbl = new JLabel("Goal Progress: 0.0 / 0.0 hrs");

        botPanel.add(addBtn);
        botPanel.add(totalLbl);
        botPanel.add(goalLbl);
        add(botPanel, BorderLayout.SOUTH);

        loadData();

        addBtn.addActionListener(e -> {
            try {
                String sub = subField.getText().trim();
                double hrs = Double.parseDouble(hrField.getText().trim());

                if (sub.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Subject cannot be empty.");
                    return;
                }

                String dateStr = dateField.getText().trim();
                if (dateStr.isEmpty()) {
                    dateStr = LocalDate.now().toString();
                }

                model.addRow(new Object[]{sub, hrs, dateStr});

                saveData(sub, hrs, dateStr);

                dailyGoal = Double.parseDouble(goalField.getText().trim());
                updateStats();

                subField.setText("");
                hrField.setText("");
                dateField.setText("");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        });
    }

    // 👤 GET USER ID FROM EMAIL
    private int getUserId(String email) {
        try {
            Connection con = DBConnection.getConnection();

            String query = "SELECT id FROM users WHERE email=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 🔌 LOAD DATA FROM DB
    private void loadData() {
        try {
            Connection con = DBConnection.getConnection();

            int userId = getUserId(USER_EMAIL);

            String query = "SELECT subject, hours, date FROM study_logs WHERE user_id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String sub = rs.getString("subject");
                double hrs = rs.getDouble("hours");
                String date = rs.getString("date");

                model.addRow(new Object[]{sub, hrs, date});
                totalHours += hrs;
            }

            updateStats();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔌 SAVE DATA TO DB
    private void saveData(String sub, double hrs, String date) {
        try {
            System.out.println("Saving to DB...");

            Connection con = DBConnection.getConnection();

            int userId = getUserId(USER_EMAIL);

            if (userId == -1) {
                System.out.println("User not found ❌");
                return;
            }

            String query = "INSERT INTO study_logs (user_id, subject, hours, date) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, userId);
            ps.setString(2, sub);
            ps.setDouble(3, hrs);
            ps.setString(4, date);

            ps.executeUpdate();

            System.out.println("Inserted ✅");

            totalHours += hrs;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStats() {
        totalLbl.setText(String.format("Total: %.1f hrs  |", totalHours));
        goalLbl.setText(String.format("Goal Progress: %.1f / %.1f hrs", totalHours, dailyGoal));

        Map<String, Double> totals = new HashMap<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            String sub = model.getValueAt(i, 0).toString();
            double hrs = Double.parseDouble(model.getValueAt(i, 1).toString());
            totals.put(sub, totals.getOrDefault(sub, 0.0) + hrs);
        }

        graphPanel.updateData(totals);
    }

    class GraphPanel extends JPanel {
        private Map<String, Double> data = new HashMap<>();

        public GraphPanel() {
            setPreferredSize(new Dimension(200, 300));
            setBackground(Color.DARK_GRAY);
        }

        public void updateData(Map<String, Double> newData) {
            this.data = newData;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.isEmpty()) return;

            double maxHours = data.values().stream().max(Double::compareTo).orElse(1.0);
            int width = getWidth();
            int height = getHeight();
            int barWidth = width / data.size();

            int x = 0;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                int barHeight = (int) ((entry.getValue() / maxHours) * (height - 40));

                g.setColor(new Color(57, 255, 20));
                g.fillRect(x + 10, height - barHeight - 20, barWidth - 20, barHeight);

                g.setColor(Color.WHITE);
                g.drawString(entry.getKey(), x + 10, height - 5);
                g.drawString(String.format("%.1f", entry.getValue()), x + 10, height - barHeight - 25);

                x += barWidth;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudyTrackerApp().setVisible(true));
    }
}
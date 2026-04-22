import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

enum TaskStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED, OVERDUE
}

enum TaskPriority {
    LOW, MEDIUM, HIGH
}

class Task implements Comparable<Task> {
    private String taskId;
    private String title;
    private String subject;
    private LocalDate dueDate;
    private int progressPercent;
    private TaskStatus status;
    private TaskPriority priority;

    public Task(String title, String subject, LocalDate dueDate, TaskPriority priority) {
        this.taskId = UUID.randomUUID().toString();
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.priority = priority;
        this.progressPercent = 0;
        updateStatusBasedOnDateAndProgress();
    }

    private Task(String taskId, String title, String subject, LocalDate dueDate, int progressPercent, TaskStatus status, TaskPriority priority) {
        this.taskId = taskId;
        this.title = title;
        this.subject = subject;
        this.dueDate = dueDate;
        this.progressPercent = progressPercent;
        this.status = status;
        this.priority = priority;
        updateStatusBasedOnDateAndProgress(); 
    }

    public static Task fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length == 7) {
            return new Task(
                    parts[0],
                    parts[1],
                    parts[2],
                    LocalDate.parse(parts[3]),
                    Integer.parseInt(parts[4]),
                    TaskStatus.valueOf(parts[5]),
                    TaskPriority.valueOf(parts[6])
            );
        }
        return null;
    }

    public void updateProgress(int percent) {
        this.progressPercent = Math.max(0, Math.min(100, percent));
        if (this.progressPercent == 100) {
            this.status = TaskStatus.COMPLETED;
        } else if (this.progressPercent > 0) {
            this.status = TaskStatus.IN_PROGRESS;
        } else {
            this.status = TaskStatus.NOT_STARTED;
        }

        if (this.status != TaskStatus.COMPLETED && LocalDate.now().isAfter(this.dueDate)) {
            this.status = TaskStatus.OVERDUE;
        }
    }

    private void updateStatusBasedOnDateAndProgress() {
        if (this.status == TaskStatus.COMPLETED) return;
        
        if (LocalDate.now().isAfter(this.dueDate)) {
            this.status = TaskStatus.OVERDUE;
        } else if (this.progressPercent > 0) {
            this.status = TaskStatus.IN_PROGRESS;
        } else {
            this.status = TaskStatus.NOT_STARTED;
        }
    }

    @Override
    public int compareTo(Task other) {
        return this.dueDate.compareTo(other.dueDate);
    }

    public String toCSV() {
        return String.format("%s,%s,%s,%s,%d,%s,%s",
                taskId, title, subject, dueDate.toString(), progressPercent, status.name(), priority.name());
    }

    // Getters
    public String getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public LocalDate getDueDate() { return dueDate; }
    public int getProgressPercent() { return progressPercent; }
    public TaskStatus getStatus() { return status; }
    public TaskPriority getPriority() { return priority; }
}

public class StudyPlannerApp extends JFrame {
    private List<Task> tasks;
    private DefaultTableModel tableModel;
    private JTable taskTable;
    private JProgressBar averageProgressBar;
    private JLabel totalTasksLabel;
    private JLabel pendingTasksLabel;
    private JLabel completionRateLabel;

    private static final String CSV_FILE = "tasks.csv";
    
    // Electric Forest Theme Colors
    private static final Color DEEP_FOREST_GREEN = new Color(11, 26, 19);     // #0B1A13
    private static final Color EMERALD_GREEN = new Color(26, 67, 50);         // #1A4332
    private static final Color BRIGHT_MINT = new Color(46, 204, 113);         // #2ECC71
    private static final Color NEON_GREEN = new Color(57, 255, 20);           // #39FF14
    private static final Color TEXT_WHITE = Color.WHITE;
    private static final Color TEXT_BLACK = Color.BLACK;

    public StudyPlannerApp() {
        tasks = new ArrayList<>();

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Professional Study Planner");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(DEEP_FOREST_GREEN);

        // --- North Panel: Task Entry and Stats ---
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.setBackground(DEEP_FOREST_GREEN);

        JPanel taskEntryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        taskEntryPanel.setBackground(DEEP_FOREST_GREEN);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(NEON_GREEN), "Task Entry");
        titledBorder.setTitleColor(TEXT_WHITE);
        taskEntryPanel.setBorder(titledBorder);

        JLabel titleLbl = new JLabel("Title:");
        titleLbl.setForeground(TEXT_WHITE);
        JTextField titleField = new JTextField(10);

        JLabel subjectLbl = new JLabel("Subject:");
        subjectLbl.setForeground(TEXT_WHITE);
        JTextField subjectField = new JTextField(10);

        JLabel dateLbl = new JLabel("Due Date (YYYY-MM-DD):");
        dateLbl.setForeground(TEXT_WHITE);
        JTextField dueDateField = new JTextField(10);

        JLabel priorityLbl = new JLabel("Priority:");
        priorityLbl.setForeground(TEXT_WHITE);
        JComboBox<TaskPriority> priorityComboBox = new JComboBox<>(TaskPriority.values());

        JButton addButton = createStyledButton("Add Task", EMERALD_GREEN);

        taskEntryPanel.add(titleLbl);
        taskEntryPanel.add(titleField);
        taskEntryPanel.add(subjectLbl);
        taskEntryPanel.add(subjectField);
        taskEntryPanel.add(dateLbl);
        taskEntryPanel.add(dueDateField);
        taskEntryPanel.add(priorityLbl);
        taskEntryPanel.add(priorityComboBox);
        taskEntryPanel.add(addButton);

        // Dynamic Statistics Panel
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        statsPanel.setBackground(DEEP_FOREST_GREEN);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        totalTasksLabel = new JLabel("Total Tasks: 0");
        totalTasksLabel.setForeground(TEXT_WHITE);
        pendingTasksLabel = new JLabel("Tasks Pending: 0");
        pendingTasksLabel.setForeground(TEXT_WHITE);
        completionRateLabel = new JLabel("Completion Rate: 0%");
        completionRateLabel.setForeground(TEXT_WHITE);

        statsPanel.add(totalTasksLabel);
        statsPanel.add(pendingTasksLabel);
        statsPanel.add(completionRateLabel);

        northContainer.add(taskEntryPanel, BorderLayout.CENTER);
        northContainer.add(statsPanel, BorderLayout.EAST);

        add(northContainer, BorderLayout.NORTH);

        // --- Center Panel: Table ---
        String[] columns = {"Priority", "Task", "Subject", "Due Date", "Status", "% Done"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        taskTable = new JTable(tableModel);

        // Table Styling
        taskTable.setRowHeight(25);
        taskTable.setBackground(EMERALD_GREEN.darker());
        taskTable.setForeground(TEXT_WHITE);
        taskTable.setGridColor(DEEP_FOREST_GREEN);
        
        taskTable.setSelectionBackground(BRIGHT_MINT);
        taskTable.setSelectionForeground(TEXT_BLACK);

        taskTable.getTableHeader().setBackground(EMERALD_GREEN);
        taskTable.getTableHeader().setForeground(TEXT_WHITE);

        // Custom Center Alignment & Overdue Highlighting Renderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);

                int modelRow = table.convertRowIndexToModel(row);
                Task task = tasks.get(modelRow);

                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                } else {
                    c.setBackground(EMERALD_GREEN.darker());
                    c.setForeground(TEXT_WHITE);
                    // Bright Red for overdue tasks
                    if (task.getStatus() != TaskStatus.COMPLETED && LocalDate.now().isAfter(task.getDueDate())) {
                        c.setForeground(Color.RED);
                    }
                }
                return c;
            }
        };

        for (int i = 0; i < taskTable.getColumnCount(); i++) {
            taskTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        taskTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.getViewport().setBackground(DEEP_FOREST_GREEN);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // --- South Panel ---
        JPanel southPanel = new JPanel(new FlowLayout());
        southPanel.setBackground(DEEP_FOREST_GREEN);

        JLabel avgLbl = new JLabel("Average Progress:");
        avgLbl.setForeground(TEXT_WHITE);

        averageProgressBar = new JProgressBar(0, 100);
        averageProgressBar.setStringPainted(true);
        averageProgressBar.setForeground(NEON_GREEN);
        UIManager.put("ProgressBar.foreground", NEON_GREEN);
        UIManager.put("ProgressBar.selectionForeground", TEXT_BLACK);
        UIManager.put("ProgressBar.selectionBackground", NEON_GREEN);

        JButton deleteButton = createStyledButton("Delete", new Color(200, 50, 50)); 
        JButton mark100Button = createStyledButton("Mark 100%", EMERALD_GREEN);

        southPanel.add(avgLbl);
        southPanel.add(averageProgressBar);
        southPanel.add(Box.createHorizontalStrut(20));
        southPanel.add(deleteButton);
        southPanel.add(mark100Button);

        add(southPanel, BorderLayout.SOUTH);

        // Load persisted data on startup
        loadTasksFromFile();
        refreshTable();
        calculateGlobalMetrics();

        // --- Event Listeners ---
        addButton.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String subject = subjectField.getText().trim();
                LocalDate dueDate = LocalDate.parse(dueDateField.getText().trim());
                TaskPriority priority = (TaskPriority) priorityComboBox.getSelectedItem();

                if (title.isEmpty() || subject.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Title and Subject cannot be empty.");
                    return;
                }

                Task newTask = new Task(title, subject, dueDate, priority);
                tasks.add(newTask);
                
                refreshTable();
                calculateGlobalMetrics();
                saveTasksToFile();

                titleField.setText("");
                subjectField.setText("");
                dueDateField.setText("");
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Date Format. Please use YYYY-MM-DD.");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = taskTable.convertRowIndexToModel(selectedRow);
                tasks.remove(modelRow);
                
                refreshTable();
                calculateGlobalMetrics();
                saveTasksToFile();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to delete.");
            }
        });

        mark100Button.addActionListener(e -> {
            int selectedRow = taskTable.getSelectedRow();
            if (selectedRow >= 0) {
                int modelRow = taskTable.convertRowIndexToModel(selectedRow);
                Task task = tasks.get(modelRow);
                task.updateProgress(100);
                
                tableModel.setValueAt(task.getStatus(), modelRow, 4);
                tableModel.setValueAt(task.getProgressPercent(), modelRow, 5);
                
                calculateGlobalMetrics();
                saveTasksToFile();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to mark 100%.");
            }
        });

        // --- Popup Slider Logic ---
        JPopupMenu sliderPopup = new JPopupMenu();
        sliderPopup.setBackground(DEEP_FOREST_GREEN);
        JSlider progressSlider = new JSlider(0, 100);
        progressSlider.setBackground(DEEP_FOREST_GREEN);
        progressSlider.setForeground(NEON_GREEN);
        
        sliderPopup.add(progressSlider);

        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int viewRow = taskTable.rowAtPoint(e.getPoint());
                if (viewRow >= 0) {
                    taskTable.setRowSelectionInterval(viewRow, viewRow);
                    int modelRow = taskTable.convertRowIndexToModel(viewRow);
                    Task task = tasks.get(modelRow);

                    // Temporarily remove listener to avoid triggering changes during setup
                    for (ChangeListener cl : progressSlider.getChangeListeners()) {
                        progressSlider.removeChangeListener(cl);
                    }

                    progressSlider.setValue(task.getProgressPercent());

                    progressSlider.addChangeListener(ce -> {
                        int newValue = progressSlider.getValue();
                        task.updateProgress(newValue);
                        
                        // "Ensure the DefaultTableModel is updated via the setValueAt() method"
                        tableModel.setValueAt(task.getStatus(), modelRow, 4);
                        tableModel.setValueAt(task.getProgressPercent(), modelRow, 5);
                        
                        calculateGlobalMetrics();
                    });

                    sliderPopup.show(taskTable, e.getX(), e.getY());
                }
            }
        });

        sliderPopup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // Save data when slider popup closes
                saveTasksToFile();
            }
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
        btn.setFocusPainted(false);
        return btn;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Task task : tasks) {
            Object[] row = {
                    task.getPriority(),
                    task.getTitle(),
                    task.getSubject(),
                    task.getDueDate(),
                    task.getStatus(),
                    task.getProgressPercent()
            };
            tableModel.addRow(row);
        }
    }

    // 2. The Behind-the-Scenes Math
    private void calculateGlobalMetrics() {
        int totalProgress = 0;
        int completedCount = 0;
        int rowCount = tableModel.getRowCount();

        if (rowCount == 0) {
            totalTasksLabel.setText("Total Tasks: 0");
            pendingTasksLabel.setText("Tasks Pending: 0");
            completionRateLabel.setText("Completion Rate: 0%");
            averageProgressBar.setValue(0);
            return;
        }

        // Loop: for (int i = 0; i < tableModel.getRowCount(); i++)
        for (int i = 0; i < rowCount; i++) {
            // Extraction: Pull the integer from the "% Done" column
            int val = (int) tableModel.getValueAt(i, 5);
            
            // The Sum
            totalProgress += val;

            // Also calculate Completion Rate and Pending
            TaskStatus status = (TaskStatus) tableModel.getValueAt(i, 4);
            if (status == TaskStatus.COMPLETED) {
                completedCount++;
            }
        }

        // The Average
        int average = totalProgress / rowCount;
        
        // Weighted Completion
        int completionRate = (int) (((double) completedCount / rowCount) * 100);
        int pending = rowCount - completedCount;

        totalTasksLabel.setText("Total Tasks: " + rowCount);
        pendingTasksLabel.setText("Tasks Pending: " + pending);
        completionRateLabel.setText("Completion Rate: " + completionRate + "%");

        // The Update
        averageProgressBar.setValue(average);
    }

    private void saveTasksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE))) {
            for (Task task : tasks) {
                writer.write(task.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasksFromFile() {
        tasks.clear();
        File file = new File(CSV_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Task task = Task.fromCSV(line);
                    if (task != null) {
                        tasks.add(task);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudyPlannerApp().setVisible(true);
        });
    }
}

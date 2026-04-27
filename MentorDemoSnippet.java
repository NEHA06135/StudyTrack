import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;


public class MentorDemoSnippet {

    // Dummy references to simulate the UI components from the main app
    private JTextField sessionSubjectField, sessionHoursField;
    private JButton logSessionBtn, setGoalBtn;
    private JTextField dailyGoalField, weeklyGoalField;
    
    // Simulating the Controller/Manager
    private StudySessionManager sessionManager;
    private ChartPanel chartPanel;

    public void setupActionListeners() {
        
       
        logSessionBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // 1. EXTRACT DATA from Swing JTextFields (View)
                    String subject = sessionSubjectField.getText().trim();
                    double hours = Double.parseDouble(sessionHoursField.getText().trim());
                    
                    // 2. VALIDATE INPUT
                    if (subject.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Subject cannot be empty.");
                        return; // Stop execution if invalid
                    }
                    
                    // 3. UPDATE MODEL (Save data via Manager)
                    sessionManager.addSession(new StudySession(subject, hours, LocalDate.now()));
                    
                    // 4. UPDATE VIEW (Refresh Chart and Progress Bars dynamically)
                    ChartManager.updateChart(chartPanel, sessionManager.getHoursPerSubject());
                    updateGoalProgressBars();
                    
                    // 5. CLEAR UI for the next entry
                    sessionSubjectField.setText("");
                    sessionHoursField.setText("");
                    
                } catch (NumberFormatException ex) {
                    // 6. ERROR HANDLING: Catch invalid number formats gracefully
                    JOptionPane.showMessageDialog(null, "Please enter a valid number for hours.");
                }
            }
        });

        // ---------------------------------------------------------
        // 2. Action Listener for Setting Goals
        // Demonstrates: Modern Java Lambda syntax (e -> { ... })
        // ---------------------------------------------------------
        setGoalBtn.addActionListener(e -> {
            try {
                // Parse the user's daily and weekly goals
                double daily = Double.parseDouble(dailyGoalField.getText().trim());
                double weekly = Double.parseDouble(weeklyGoalField.getText().trim());
                
                // Update the Goal Model
                sessionManager.updateGoal(new Goal(daily, weekly));
                
                // Trigger a UI update to reflect the new goals immediately
                updateGoalProgressBars();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter valid numbers for goals.");
            }
        });
    }

    // ---------------------------------------------------------
    // DUMMY CONTEXT (To allow this file to compile standalone)
    // ---------------------------------------------------------
    private void updateGoalProgressBars() { /* UI Update Logic */ }
    
    class StudySessionManager {
        public void addSession(StudySession s) {}
        public void updateGoal(Goal g) {}
        public java.util.Map<String, Double> getHoursPerSubject() { return null; }
    }
    class StudySession { StudySession(String s, double h, LocalDate d){} }
    class Goal { Goal(double d, double w){} }
    static class ChartManager {
        static void updateChart(ChartPanel p, java.util.Map<String, Double> m) {}
    }
    class ChartPanel {}
}

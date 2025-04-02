import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Leaderboards {

    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> gameSelector;

    public Leaderboards() {
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        // Main frame setup
        frame = new JFrame("Leaderboards");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 700);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null); // Center the frame
    
        // Main panel with background image
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Load and draw background image
                Image bgImage = new ImageIcon("img/bg.jpg").getImage();
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setLayout(null);
    
        // Title label
        JLabel titleLabel = new JLabel("Leaderboards", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 30));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setBounds(50, 20, 350, 40); // Centered at the top
        panel.add(titleLabel);
    
        // Dropdown to select game
        String[] games = {"Word Matching", "Scrabble", "Fill In The Blank"};
        gameSelector = new JComboBox<>(games);
        gameSelector.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        gameSelector.setBounds(45, 70, 350, 30);

        // Apply custom styling
        gameSelector.setBackground(Color.YELLOW); // Set background color
        gameSelector.setOpaque(true);            // Ensure background is painted
        gameSelector.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1), // Black border
                BorderFactory.createEmptyBorder(5, 5, 5, 5))); // Padding for better spacing
        gameSelector.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() { // Add shadow
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.YELLOW); // Background color
                g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10); // Rounded corners
                super.paintCurrentValueBackground(g, bounds, hasFocus);
            }
        });

        gameSelector.addActionListener(_ -> updateLeaderboard((String) gameSelector.getSelectedItem()));
        panel.add(gameSelector);
        
        // Table setup
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
    
        // Add columns for player and points
        tableModel.addColumn("Player");
        tableModel.addColumn("Points");
    
        // Style the table
        table.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        table.setRowHeight(20);
        table.setGridColor(Color.BLACK); // Set the grid line color to black
        table.setShowGrid(true);        // Enable grid lines
        table.setOpaque(true);         // Ensure the table paints its background
        table.setBackground(Color.WHITE); // Set the background color to white
        table.setForeground(Color.BLACK); // Set text color to black
    
        // Center align the data in the table cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Player column
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Points column
    
        // Disable table editing and column reordering
        table.setEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);
    
        // Table header styling
        table.getTableHeader().setFont(new Font("Times New Roman", Font.BOLD, 20));
        table.getTableHeader().setForeground(Color.BLACK);
        table.getTableHeader().setBackground(new Color(255, 255, 204)); // Light yellow
    
        // Wrap the table in a scroll pane with a black border
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(17, 110, 400, 500);
        scrollPane.getViewport().setBackground(Color.WHITE); // Set the viewport background to white
        scrollPane.setOpaque(false);
        // scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        panel.add(scrollPane);                        
    
        // Home button (new one)
        JButton homeButton = createHomeButton();
        panel.add(homeButton);
    
        // Add the panel to the frame and make it visible
        frame.add(panel);
        frame.setVisible(true);
    
        // Load initial leaderboard data
        updateLeaderboard((String) gameSelector.getSelectedItem());
    }    

    private void updateLeaderboard(String gameName) {
        // Clear existing table data
        tableModel.setRowCount(0);

        // Load leaderboard data for the selected game
        List<String[]> leaderboardData = loadLeaderboardData(gameName);

        // Sort the leaderboard data by points in descending order
        leaderboardData.sort((entry1, entry2) -> {
            int points1 = Integer.parseInt(entry1[1]); // Parse points of the first entry
            int points2 = Integer.parseInt(entry2[1]); // Parse points of the second entry
            return Integer.compare(points2, points1);  // Compare in descending order
        });

        // Populate the table with leaderboard data
        for (String[] entry : leaderboardData) {
            tableModel.addRow(entry);
        }
    }

    private List<String[]> loadLeaderboardData(String gameName) {
        List<String[]> data = new ArrayList<>();
        File file = new File("Data/leaderboards.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" - ");
                if (parts.length == 3 && parts[2].equals(gameName)) {
                    data.add(new String[]{parts[0], parts[1]}); // Add player and points only
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    private JButton createHomeButton() {
        ImageIcon homeIcon = new ImageIcon("img/home_icon.png");
        Image scaledImage = homeIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        JButton homeButton = new JButton(new ImageIcon(scaledImage));
        homeButton.setBounds(10, 10, 50, 50);
        homeButton.setFocusPainted(false);
        homeButton.setContentAreaFilled(false);
        homeButton.setBorderPainted(false);
        homeButton.setOpaque(false);
        homeButton.addActionListener(_ -> {
            playSound("sound/select.wav");
            frame.dispose();
            new HomePage().createAndShowGUI();
        });
        return homeButton;
    }

    private void playSound(String soundFile) {
        try {
            File file = new File(soundFile);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Leaderboards::new);
    }
}

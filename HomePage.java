import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;

public class HomePage {

    private static int cloudXTop = -250; // Initial X position of the cloud at the top
    private static int cloudXBottom = 450; // Initial X position of the cloud at the bottom (starting from right)
    private static final int CLOUD_Y_TOP = 20; // Fixed Y position of the cloud at the top
    private static final int CLOUD_Y_BOTTOM = 140; // Fixed Y position of the cloud at the bottom (just below the first cloud)
    private static final int CLOUD_SPEED = 2; // Speed of cloud movement
    private static final int CLOUD_WIDTH = 250; // Cloud width (adjustable)
    private static final int CLOUD_HEIGHT = 120; // Cloud height (adjustable)

    public void createAndShowGUI() {
        // Create the main frame
        JFrame frame = new JFrame("Home Page");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 700); // Mobile-sized window
        frame.setLocationRelativeTo(null); // Center the frame

        // Create the main panel with a background image
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Load the background image
                Image bgImage = new ImageIcon("img/bg.jpg").getImage(); // Adjust path if necessary
                // Draw the background image
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);

                // Load the cloud image
                Image cloudImage = new ImageIcon("img/cloud.png").getImage();
                // Draw the cloud image at the top with specified width and height
                g.drawImage(cloudImage, cloudXTop, CLOUD_Y_TOP, CLOUD_WIDTH, CLOUD_HEIGHT, this);
                // Draw the cloud image at the bottom with specified width and height
                g.drawImage(cloudImage, cloudXBottom, CLOUD_Y_BOTTOM, CLOUD_WIDTH, CLOUD_HEIGHT, this);
            }
        };
        panel.setLayout(null); // Absolute positioning

        // Load and scale the leaderboard icon image
        ImageIcon leaderboardsIcon = new ImageIcon("img/leaderboards_icon.png"); // Replace with your image path
        Image scaledLeaderboardImage = leaderboardsIcon.getImage().getScaledInstance(50, 60, Image.SCALE_SMOOTH); // Default scale (40x40)
        ImageIcon scaledLeaderboardIcon = new ImageIcon(scaledLeaderboardImage);

        // Add "Leaderboards" button
        JButton leaderboardsButton = new JButton(scaledLeaderboardIcon);
        leaderboardsButton.setBounds(10, 0, 80, 80); // Button size
        leaderboardsButton.setFocusPainted(false); // Remove focus border
        leaderboardsButton.setContentAreaFilled(false); // Transparent background
        leaderboardsButton.setBorderPainted(false); // No border
        leaderboardsButton.setOpaque(false); // Ensure transparency

        // Add mouse listener for the popover effect (image scaling)
        leaderboardsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Enlarge the image on hover
                Image enlargedImage = leaderboardsIcon.getImage().getScaledInstance(55, 65, Image.SCALE_SMOOTH); // Enlarged scale
                leaderboardsButton.setIcon(new ImageIcon(enlargedImage)); // Update button icon
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                // Revert to original size
                leaderboardsButton.setIcon(scaledLeaderboardIcon); // Reset to the default icon
            }
        });

        // Add action listener for the button
        leaderboardsButton.addActionListener(_ -> {
            playSound("sound/select.wav"); // Play the sound when the button is clicked
            // Navigate to the Leaderboards screen
            frame.setVisible(false); // Hide the HomePage
            new Leaderboards(); // Launch the Leaderboards screen
        });

        // Add the button to the panel
        panel.add(leaderboardsButton);

        // Add a label at the top for game selection
        JLabel titleLabel = new JLabel("Select Game:");
        titleLabel.setBounds(50, 100, 350, 50);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 28));
        titleLabel.setForeground(Color.BLACK); // White text for better visibility
        panel.add(titleLabel);

        // Create game buttons
        JButton wordMatchingButton = createGameButton("Word Matching", 125, 200);
        JButton scrabbleButton = createGameButton("Scrabble", 125, 300);
        JButton fillInTheBlankButton = createGameButton("Fill in the Blank", 125, 400);

        wordMatchingButton.addActionListener(_ -> {
            playSound("sound/select.wav"); // Play the sound when the button is clicked
            frame.setVisible(false); // Hide HomePage
            new WordMatching(); // Launch Word Matching game
        });        
        
        scrabbleButton.addActionListener(_ -> {
            playSound("sound/select.wav"); // Play the sound when the button is clicked
            frame.setVisible(false); // Hide HomePage
            new Scrabble(); // Launch Scrabble game
        });
        
        fillInTheBlankButton.addActionListener(_ -> {
            playSound("sound/select.wav"); // Play the sound when the button is clicked
            frame.setVisible(false); // Hide HomePage
            new FillInTheBlank(); // Launch Fill in the Blank game
        });        

        // Add the game buttons to the panel
        panel.add(wordMatchingButton);
        panel.add(scrabbleButton);
        panel.add(fillInTheBlankButton);

        // Add a box on the top to display the last username entered, centered horizontally
        JPanel usernamePanel = new JPanel();
        usernamePanel.setBounds(0, 10, frame.getWidth(), 50); // Span the width of the frame and position near the top
        usernamePanel.setOpaque(false); // Make the panel completely transparent
        usernamePanel.setLayout(null); // Absolute positioning for the label

        // Get the last username from the file
        String lastUsername = getLastUsername();

        // Create and style the username label
        JLabel usernameLabel = new JLabel("Hello, " + lastUsername); // Display the greeting with the last username
        usernameLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 20)); // Styled font
        usernameLabel.setForeground(Color.BLACK); // Black text color
        usernameLabel.setBounds((frame.getWidth() - 200) / 2, 20, 200, 30); // Center the label horizontally within the panel
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER); // Align text to the center

        // Add the label to the panel
        usernamePanel.add(usernameLabel);
        panel.add(usernamePanel);

        // Add the panel to the frame
        frame.add(panel);

        // Timer for cloud animations (top and bottom)
        Timer cloudTimer = new Timer(20, _ -> {
            cloudXTop += CLOUD_SPEED; // Move the top cloud
            cloudXBottom -= CLOUD_SPEED; // Move the bottom cloud

            // Reset top cloud position when it moves off-screen
            if (cloudXTop > frame.getWidth()) {
                cloudXTop = -CLOUD_WIDTH;
            }

            // Reset bottom cloud position when it moves off-screen
            if (cloudXBottom < -CLOUD_WIDTH) {
                cloudXBottom = frame.getWidth();
            }

            panel.repaint(); // Repaint the panel to update the cloud's position
        });
        cloudTimer.start(); // Start the timer

        // Make the frame visible
        frame.setVisible(true);
    }

    private static String getLastUsername() {
        try (BufferedReader reader = new BufferedReader(new FileReader("Data/UserData.txt"))) {
            String line;
            String lastUsername = null;
            while ((line = reader.readLine()) != null) {
                lastUsername = line; // Keep updating to the last line (username)
            }
            return lastUsername; // Return the last username
        } catch (IOException e) {
            e.printStackTrace();
            return "No User"; // Default if no username found
        }
    }

    // Helper method to create styled buttons
    private JButton createGameButton(String text, int x, int y) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                g2.setColor(new Color(0, 0, 0, 100)); // Shadow color
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);

                // Draw button background
                g2.setColor(Color.CYAN); // Button color
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 20, 20);

                g2.dispose();

                // Draw text (default button behavior)
                super.paintComponent(g);
            }
        };

        button.setBounds(x, y, 200, 50);
        button.setFocusPainted(false); // Remove focus border
        button.setContentAreaFilled(false); // Remove default background
        button.setBorderPainted(false); // Remove default border
        button.setOpaque(false); // Ensure custom painting
        button.setForeground(Color.BLACK); // Text color
        button.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 18));

        return button;
    }

    public static void playSound(String filePath) {
        try {
            File soundFile = new File(filePath); // Specify the path to the sound file
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile); // Get the audio input stream
            Clip clip = AudioSystem.getClip(); // Get the clip to play the sound
            clip.open(audioStream); // Open the sound clip
            clip.start(); // Play the sound
        } catch (Exception e) {
            e.printStackTrace(); // Handle any exceptions
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HomePage().createAndShowGUI();
        });
    }
}

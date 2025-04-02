import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.sound.sampled.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class MobileApp {

    private static int cloudXTop = -250; // Initial X position of the cloud at the top
    private static int cloudXBottom = 450; // Initial X position of the cloud at the bottom (starting from right)
    private static final int CLOUD_Y_TOP = 20; // Fixed Y position of the cloud at the top
    private static final int CLOUD_Y_BOTTOM = 140; // Fixed Y position of the cloud at the bottom (just below the first cloud)
    private static final int CLOUD_SPEED = 2; // Speed of cloud movement
    private static final int CLOUD_WIDTH = 250; // Cloud width (adjustable)
    private static final int CLOUD_HEIGHT = 120; // Cloud height (adjustable)
    private static boolean isCooldown = false; // Track if cooldown is in effect
    private static boolean isSoundPlaying = false; // Flag to track if the sound is playing

    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Mobile App Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 700); // Set the size of the frame (similar to a mobile screen)

        // Center the frame on the screen
        frame.setLocationRelativeTo(null);

        // Create the main panel with a background image
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Load the background image
                Image bgImage = new ImageIcon("img/bg.jpg").getImage();
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
        panel.setLayout(null); // Use no layout for absolute positioning

        // Add a label with text
        JLabel label = new JLabel("English Literary Game", SwingConstants.CENTER);
        label.setHorizontalTextPosition(SwingConstants.CENTER); // Center text horizontally
        label.setVerticalTextPosition(SwingConstants.TOP); // Position the text above the image
        label.setBounds(50, 130, 350, 250); // Adjust size to accommodate the text and the image
        label.setForeground(Color.BLACK); // Set label text color to black
        label.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 28)); // Use a system font

        // Load and animate the sun icon
        ImageIcon sunIcon = new ImageIcon("img/sun_icon.png");
        Timer animationTimer = createSunAnimation(label, sunIcon);

        // Start the sun animation
        animationTimer.start();
        panel.add(label);

        // Create a button with custom appearance
        JButton startButton = new JButton("START") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                g2.setColor(new Color(0, 0, 0, 100)); // Shadow color with transparency
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);

                // Draw button background
                g2.setColor(Color.YELLOW);
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 20, 20);

                g2.dispose();

                // Draw text (default button behavior)
                super.paintComponent(g);
            }
        };
        startButton.setBounds(125, 400, 200, 50); // Center the button on the panel
        startButton.setFocusPainted(false); // Remove focus border
        startButton.setContentAreaFilled(false); // Remove default button background
        startButton.setBorderPainted(false); // Remove default border
        startButton.setOpaque(false); // Ensure custom painting is visible
        startButton.setForeground(Color.BLACK); // Set button text color
        startButton.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 18)); // Use a system font

        // Add hover effect (pop animation)
        startButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Enlarge the button slightly
                startButton.setBounds(startButton.getX() - 5, startButton.getY() - 5,
                        startButton.getWidth() + 10, startButton.getHeight() + 10);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Restore the button to its original size
                startButton.setBounds(125, 400, 200, 50);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Play the funny.mp3 sound when the button is clicked
                if (!isCooldown) {
                    // Start playing the sound only if it isn't already playing
                    if (!isSoundPlaying) {
                        playSound("music/funny.wav");
                        isSoundPlaying = true; // Mark the sound as playing
                    }

                    // Set cooldown flag to true
                    isCooldown = true;

                    // Start a cooldown timer (2 seconds)
                    new Timer(2000, _ -> isCooldown = false).start();

                    // Prompt for username input using a custom input dialog
                    String username = showCustomInputDialog(frame, "Enter Username:");
                    if (username != null && !username.isEmpty()) {
                        // Check if the username already exists in the file
                        if (isUsernameTaken(username)) {
                            JOptionPane.showMessageDialog(frame, "Username already taken. Please choose another.");
                        } else {
                            saveUsernameToFile(username); // Save the username to a file

                            // Hide the current frame (MobileApp)
                            frame.setVisible(false);

                            // Create and show the new HomePage frame
                            HomePage homePage = new HomePage();
                            homePage.createAndShowGUI();
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Username cannot be empty.");
                    }
                }
            }
        });

        panel.add(startButton);

        // Add panel to frame
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

    // Custom input dialog method with styled components
    private static String showCustomInputDialog(JFrame parentFrame, String message) {
        // Create a custom panel for the dialog
        JPanel customPanel = new JPanel();
        customPanel.setLayout(new BorderLayout());

        JLabel label = new JLabel(message);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(Color.BLACK);

        JTextField inputField = new JTextField(20);
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.setBackground(new Color(255, 255, 255));
        inputField.setForeground(new Color(0, 0, 0));

        customPanel.add(label, BorderLayout.NORTH);
        customPanel.add(inputField, BorderLayout.CENTER);

        int option = JOptionPane.showOptionDialog(parentFrame, customPanel, "Input Username",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

        if (option == JOptionPane.OK_OPTION) {
            return inputField.getText();
        }
        return null; // If Cancel is pressed or dialog is closed
    }

    private static Timer createSunAnimation(JLabel label, ImageIcon sunIcon) {
        int[] size = {150}; // Initial size of the sun icon
        boolean[] growing = {true}; // Indicates if the icon is growing or shrinking

        return new Timer(30, _ -> {
            if (growing[0]) {
                size[0] += 2; // Increase size
                if (size[0] >= 200) { // Maximum size
                    growing[0] = false;
                }
            } else {
                size[0] -= 2; // Decrease size
                if (size[0] <= 150) { // Minimum size
                    growing[0] = true;
                }
            }
            Image scaledSunImage = sunIcon.getImage().getScaledInstance(size[0], size[0], Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaledSunImage));
        });
    }

    public static void playSound(String filePath) {
        try {
            // Change the file extension to .wav if you converted it
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Loop the sound continuously
            clip.loop(Clip.LOOP_CONTINUOUSLY);

            // Start playing the sound
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveUsernameToFile(String username) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Data/UserData.txt", true))) {
            writer.write(username);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isUsernameTaken(String username) {
        try {
            List<String> existingUsernames = new ArrayList<>();
            // Using try-with-resources to ensure the BufferedReader is closed automatically
            try (BufferedReader reader = new BufferedReader(new FileReader("Data/UserData.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    existingUsernames.add(line);
                }
            }
            return existingUsernames.contains(username);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }    
}

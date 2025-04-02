import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class FillInTheBlank {

    private final ArrayList<String[]> sentences = new ArrayList<>();
    private int currentSentenceIndex = 0;
    private int score = 0;
    private JFrame frame;
    private JLabel sentenceLabel;
    private JTextField answerField;
    private JButton nextButton;
    private String username;

    // Cloud animation variables (same as in HomePage)
    private static int cloudXTop = -250;
    private static int cloudXBottom = 450;
    private static final int CLOUD_Y_TOP = 20;
    private static final int CLOUD_Y_BOTTOM = 140;
    private static final int CLOUD_SPEED = 2;
    private static final int CLOUD_WIDTH = 250;
    private static final int CLOUD_HEIGHT = 120;

    public FillInTheBlank() {

        // Load username from the file in the Data/UserData folder
        this.username = loadUsernameFromFile();

        // Nursery rhymes with a blank word and the correct word
        sentences.add(new String[]{"Twinkle, twinkle, little ___.", "star"});
        sentences.add(new String[]{"Jack and Jill went up the ___.", "hill"});
        sentences.add(new String[]{"Humpty Dumpty sat on a ___.", "wall"});
        sentences.add(new String[]{"Baa, baa, black sheep, have you any ___?", "wool"});
        sentences.add(new String[]{"Hickory Dickory ___, the mouse ran up the clock.", "dock"});
        sentences.add(new String[]{"London Bridge is falling ___.", "down"});
        sentences.add(new String[]{"Row, row, row your ___ gently down the stream.", "boat"});
        sentences.add(new String[]{"Old McDonald had a ___, E-I-E-I-O.", "farm"});
        sentences.add(new String[]{"The wheels on the bus go round and ___.", "round"});
        sentences.add(new String[]{"Ring-a-ring o' roses, a pocket full of ___.", "posies"});

        // Shuffle sentences to randomize
        Collections.shuffle(sentences);

        createAndShowGUI();
    }

    private String loadUsernameFromFile() {
        String username = "Guest"; // Default username if not found
        File file = new File("Data/UserData.txt");
    
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String lastLine = null;
    
            // Read through the file, line by line, keeping track of the last line
            while ((line = br.readLine()) != null) {
                lastLine = line; // Set the last line to the current line
            }
    
            // If a last line is found, set it as the username
            if (lastLine != null) {
                username = lastLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return username;
    }

    private void createAndShowGUI() {
        // Main frame setup
        frame = new JFrame("Fill in the Blank Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 700);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null); // Center the frame
    
        // Create main panel with background and cloud animation
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Load and draw background image
                Image bgImage = new ImageIcon("img/bg.jpg").getImage();
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
    
                // Load and draw the cloud image
                Image cloudImage = new ImageIcon("img/cloud.png").getImage();
                g.drawImage(cloudImage, cloudXTop, CLOUD_Y_TOP, CLOUD_WIDTH, CLOUD_HEIGHT, this);
                g.drawImage(cloudImage, cloudXBottom, CLOUD_Y_BOTTOM, CLOUD_WIDTH, CLOUD_HEIGHT, this);
            }
        };
        panel.setLayout(null);

        // Add username label at the top center
        JLabel usernameLabel = new JLabel("Hello, " + username);
        usernameLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 20));
        usernameLabel.setForeground(Color.BLACK);

        // Use the panel to get the FontMetrics
        FontMetrics metrics = panel.getFontMetrics(usernameLabel.getFont());
        int labelWidth = metrics.stringWidth("Hello, " + username);
        int xPosition = (frame.getWidth() - labelWidth) / 2;  // Center the label based on its width

        usernameLabel.setBounds(xPosition, 30, labelWidth, 30); // Set position and size dynamically
        panel.add(usernameLabel);
    
        // Load and scale the home icon image for the button
        ImageIcon homeIcon = new ImageIcon("img/home_icon.png");
        Image scaledImage = homeIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH); // Scale to fit
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
    
        // Add Home icon button at the top-left corner
        JButton homeButton = new JButton(scaledIcon);
        homeButton.setBounds(10, 10, 50, 50);
        homeButton.setFocusPainted(false); // Remove focus border
        homeButton.setContentAreaFilled(false); // Transparent background
        homeButton.setBorderPainted(false); // No border
        homeButton.setOpaque(false); // Ensure transparency
        homeButton.addActionListener(_ -> {
            playSound("sound/select.wav");  // Play sound on click
            frame.dispose(); // Close the current frame
            new HomePage().createAndShowGUI(); // Navigate back to HomePage
        });
        panel.add(homeButton);
    
        // Sentence label
        sentenceLabel = new JLabel("", SwingConstants.CENTER);
        sentenceLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 20));
        sentenceLabel.setForeground(Color.BLACK);
        sentenceLabel.setBounds(20, 120, 400, 100);
        panel.add(sentenceLabel);

        // Answer text field
        answerField = new JTextField();
        answerField.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        answerField.setBounds(50, 250, 350, 40);
        answerField.setHorizontalAlignment(SwingConstants.CENTER); // Center the text
        panel.add(answerField);
    
        // Next button
        nextButton = createStyledButton("Next");
        nextButton.setBounds(125, 400, 200, 50);
        nextButton.addActionListener(_ -> {
            playSound("sound/select.wav");
            processAnswer();
            if (currentSentenceIndex < sentences.size() - 1) {
                currentSentenceIndex++;
                updateSentence();
            } else {
                showScore(panel);
            }
        });
        panel.add(nextButton);
    
        // Add panel to frame
        frame.add(panel);
        frame.setVisible(true);
    
        // Timer for cloud animations
        Timer cloudTimer = new Timer(20, _ -> {
            cloudXTop += CLOUD_SPEED;
            cloudXBottom -= CLOUD_SPEED;
    
            if (cloudXTop > frame.getWidth()) {
                cloudXTop = -CLOUD_WIDTH;
            }
    
            if (cloudXBottom < -CLOUD_WIDTH) {
                cloudXBottom = frame.getWidth();
            }
    
            panel.repaint(); // Repaint the panel to update the cloud's position
        });
        cloudTimer.start();
    
        // Load the first sentence
        updateSentence();
    }

    private void updateSentence() {
        // Get the current sentence
        String[] currentSentence = sentences.get(currentSentenceIndex);
        String sentenceWithBlank = currentSentence[0];

        // Update sentence with blank
        sentenceLabel.setText("<html><div style='text-align: center;'>"
                + sentenceWithBlank.replace("___", "<u>___</u>") + "</div></html>");
    }

    private void processAnswer() {
        String correctAnswer = sentences.get(currentSentenceIndex)[1];
        String userAnswer = answerField.getText().trim().toLowerCase();

        if (userAnswer.equals(correctAnswer.toLowerCase())) {
            score++;
        }
        answerField.setText(""); // Clear the text field after each answer
    }

    private void showScore(JPanel panel) {
        // Save the score to the leaderboard file
        saveScoreToFile();

        // Clear panel and display score
        panel.removeAll();
        panel.repaint();

        // Total points label
        JLabel totalPointsLabel = new JLabel("Total Points", SwingConstants.CENTER);
        totalPointsLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 30));
        totalPointsLabel.setForeground(Color.BLACK);
        totalPointsLabel.setBounds(50, 200, 350, 40); // Adjusted position
        panel.add(totalPointsLabel);

        // Score label
        JLabel scoreLabel = new JLabel(score + "/" + sentences.size(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 60));
        scoreLabel.setForeground(Color.BLACK);
        scoreLabel.setBounds(50, 250, 350, 60); // Adjusted position
        panel.add(scoreLabel);

        // Back button
        JButton backButton = createStyledButton("Back to Home");
        backButton.setBounds(125, 400, 200, 50); // Adjusted position
        backButton.addActionListener(_ -> {
            playSound("sound/select.wav");
            frame.dispose(); // Close the current frame
            new HomePage().createAndShowGUI(); // Navigate back to HomePage
        });
        panel.add(backButton);

        // Add panel to frame
        frame.add(panel);
        frame.repaint();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);

                g2.setColor(Color.YELLOW);
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 20, 20);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 18));
        button.setForeground(Color.BLACK);
        return button;
    }  

    private void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveScoreToFile() {
        File file = new File("Data/Leaderboards.txt");
    
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            // Save the username, score, and game name in the desired format
            bw.write(username + " - " + score + " - Fill In The Blank");
            bw.newLine(); // Add a new line after each entry to separate them
        } catch (IOException e) {
            e.printStackTrace();
        }
    }     

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FillInTheBlank::new);
    }
}

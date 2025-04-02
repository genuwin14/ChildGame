import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class WordMatching {

    // Question data (use ArrayList for easy shuffling)
    private final ArrayList<String[]> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private JFrame frame;
    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup buttonGroup;
    private JButton nextButton;
    private String username;

    // Cloud animation variables
    private static int cloudXTop = -250; // Initial X position of the cloud at the top
    private static int cloudXBottom = 450; // Initial X position of the cloud at the bottom (starting from right)
    private static final int CLOUD_Y_TOP = 20; // Fixed Y position of the cloud at the top
    private static final int CLOUD_Y_BOTTOM = 140; // Fixed Y position of the cloud at the bottom (just below the first cloud)
    private static final int CLOUD_SPEED = 2; // Speed of cloud movement
    private static final int CLOUD_WIDTH = 250; // Cloud width (adjustable)
    private static final int CLOUD_HEIGHT = 120; // Cloud height (adjustable)

    public WordMatching() {

        // Load username from the file in the Data/UserData folder
        this.username = loadUsernameFromFile();

        // Initialize questions
        questions.add(new String[]{"Apple is to fruit as carrot is to?", "Vegetable", "Animal", "Mineral", "Liquid", "Vegetable"});
        questions.add(new String[]{"Dog is to bark as cat is to?", "Hiss", "Purr", "Meow", "Roar", "Meow"});
        questions.add(new String[]{"Fish is to swim as bird is to?", "Run", "Jump", "Fly", "Walk", "Fly"});
        questions.add(new String[]{"Car is to road as boat is to?", "Sky", "Water", "Rail", "Land", "Water"});
        questions.add(new String[]{"Pen is to write as eraser is to?", "Draw", "Erase", "Cut", "Paste", "Erase"});
        questions.add(new String[]{"Sun is to day as moon is to?", "Night", "Light", "Sky", "Star", "Night"});
        questions.add(new String[]{"Fire is to hot as ice is to?", "Cold", "Warm", "Solid", "Steam", "Cold"});
        questions.add(new String[]{"Chair is to sit as bed is to?", "Sleep", "Jump", "Stand", "Walk", "Sleep"});
        questions.add(new String[]{"Clock is to time as thermometer is to?", "Weather", "Heat", "Temperature", "Pressure", "Temperature"});
        questions.add(new String[]{"Eyes are to see as ears are to?", "Hear", "Smell", "Taste", "Feel", "Hear"});

        // Shuffle the questions
        Collections.shuffle(questions);

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

    private void saveScoreToLeaderBoard() {
        File file = new File("Data/Leaderboards.txt");
    
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            // Format: Username - Points - WordMatching
            String scoreEntry = username + " - " + score + " - Word Matching";
            bw.write(scoreEntry);
            bw.newLine();  // Add a new line for the next entry
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    

    private void createAndShowGUI() {
        // Main frame setup
        frame = new JFrame("Word Matching Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 700); // Same size as HomePage
        frame.setLayout(new BorderLayout());

        // Center the frame on the screen
        frame.setLocationRelativeTo(null);

        // Create main panel with background image
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Load and draw background image
                Image bgImage = new ImageIcon("img/bg.jpg").getImage();
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);

                // Load cloud image
                Image cloudImage = new ImageIcon("img/cloud.png").getImage();
                // Draw the cloud image at the top with specified width and height
                g.drawImage(cloudImage, cloudXTop, CLOUD_Y_TOP, CLOUD_WIDTH, CLOUD_HEIGHT, this);
                // Draw the cloud image at the bottom with specified width and height
                g.drawImage(cloudImage, cloudXBottom, CLOUD_Y_BOTTOM, CLOUD_WIDTH, CLOUD_HEIGHT, this);
            }
        };
        panel.setLayout(null); // Absolute positioning

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

        // Load and scale the home icon image to fit the button size
        ImageIcon homeIcon = new ImageIcon("img/home_icon.png"); // Replace with your image path
        Image scaledImage = homeIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH); // Scale image to 50x50
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        // Add Home icon button at the top-left corner
        JButton homeButton = new JButton(scaledIcon);
        homeButton.setBounds(10, 10, 50, 50); // Set button size and position
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

        // Question label
        questionLabel = new JLabel("", SwingConstants.CENTER);
        questionLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 20));
        questionLabel.setForeground(Color.BLACK); // Text color
        questionLabel.setBounds(20, 120, 400, 50); // Adjust position and size
        panel.add(questionLabel);

        // Options panel for radio buttons
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(4, 1, 10, 10));
        optionsPanel.setOpaque(false); // Make transparent
        optionsPanel.setBounds(50, 180, 350, 200);

        options = new JRadioButton[4];
        buttonGroup = new ButtonGroup();

        for (int i = 0; i < options.length; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 16));
            options[i].setOpaque(false); // Transparent background
            options[i].setForeground(Color.BLACK); // Text color
            buttonGroup.add(options[i]);
            optionsPanel.add(options[i]);
        }
        panel.add(optionsPanel);

        // Next/Submit button styled like HomePage
        nextButton = createStyledButton("Next");
        nextButton.setBounds(125, 400, 200, 50);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playSound("sound/select.wav");  // Play sound on click
                processAnswer();
                if (currentQuestionIndex < questions.size() - 1) {
                    currentQuestionIndex++;
                    updateQuestion();
                } else {
                    showScore(panel);
                }
            }
        });        
        panel.add(nextButton);

        // Add panel to frame
        frame.add(panel);
        frame.setVisible(true);

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

        // Load the first question
        updateQuestion();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);

                // Draw button background
                g2.setColor(Color.YELLOW);
                g2.fillRoundRect(0, 0, getWidth() - 10, getHeight() - 10, 20, 20);

                g2.dispose();

                // Draw text (default button behavior)
                super.paintComponent(g);
            }
        };
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 18));
        return button;
    }

    private void updateQuestion() {
        // Get the current question
        String[] currentQuestion = questions.get(currentQuestionIndex);

        // Shuffle options
        ArrayList<String> optionsList = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            optionsList.add(currentQuestion[i]);
        }
        Collections.shuffle(optionsList);

        // Update question and options
        questionLabel.setText("Q" + (currentQuestionIndex + 1) + ": " + currentQuestion[0]);
        for (int i = 0; i < options.length; i++) {
            options[i].setText(optionsList.get(i));
        }

        // Clear previous selection
        buttonGroup.clearSelection();

        // Update button text for the last question
        if (currentQuestionIndex == questions.size() - 1) {
            nextButton.setText("Submit");
        }
    }

    private void processAnswer() {
        // Loop through the options and check the selected answer
        for (int i = 0; i < options.length; i++) {
            if (options[i].isSelected()) {
                String selectedAnswer = options[i].getText();
                // Get the correct answer from the question array (index 5)
                String correctAnswer = questions.get(currentQuestionIndex)[5];
    
                // Debugging lines to check what's happening
                System.out.println("Selected Answer: " + selectedAnswer);
                System.out.println("Correct Answer: " + correctAnswer);
    
                // If the selected answer is correct, increment the score
                if (selectedAnswer.equals(correctAnswer)) {
                    score++;
                    System.out.println("Correct Answer! Score incremented.");
                } else {
                    System.out.println("Incorrect Answer.");
                }
                break;
            }
        }
    }            

    private void showScore(JPanel panel) {
        // Clear panel and display score
        panel.removeAll();
        panel.repaint();
    
        // Total points label
        JLabel totalPointsLabel = new JLabel("Total Points", SwingConstants.CENTER);
        totalPointsLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 30));
        totalPointsLabel.setForeground(Color.BLACK);
        totalPointsLabel.setBounds(50, 200, 350, 40);
        panel.add(totalPointsLabel);
    
        // Score label
        JLabel scoreLabel = new JLabel(score + "/" + questions.size(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 60));
        scoreLabel.setForeground(Color.BLACK);
        scoreLabel.setBounds(50, 250, 350, 60);
        panel.add(scoreLabel);
    
        // Save score to the leaderboard file
        saveScoreToLeaderBoard();
    
        // Back button
        JButton backButton = createStyledButton("Back");
        backButton.setBounds(125, 350, 200, 50);
        backButton.addActionListener(_ -> {
            playSound("sound/select.wav");  // Play sound on click
            frame.dispose();
            new HomePage().createAndShowGUI();
        });        
        panel.add(backButton);
    
        panel.revalidate();
        panel.repaint();
    }    

    public static void main(String[] args) {
        new WordMatching();
    }

    public static void playSound(String filePath) {
        try {
            File soundFile = new File(filePath); // Specify the path to the sound file
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile); // Get the audio input stream
            Clip clip = AudioSystem.getClip(); // Get the clip
            clip.open(audioStream); // Open the clip
            clip.start(); // Play the sound
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace(); // Print any errors to the console
        }
    }
}

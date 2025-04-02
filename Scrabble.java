import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class Scrabble {

    private final ArrayList<String> words = new ArrayList<>();
    private int currentWordIndex = 0;
    private int score = 0;
    private JFrame frame;
    private JLabel sentenceLabel;
    private JButton nextButton;
    private String username;
    private JPanel panel;
    private JLabel fruitImageLabel;

    // Cloud animation variables
    private static int cloudXTop = -250;
    private static int cloudXBottom = 450;
    private static final int CLOUD_Y_TOP = 20;
    private static final int CLOUD_Y_BOTTOM = 140;
    private static final int CLOUD_SPEED = 2;
    private static final int CLOUD_WIDTH = 250;
    private static final int CLOUD_HEIGHT = 120;
    private static final int FRUIT_IMAGE_WIDTH = 100;  // Fixed width for all images
    private static final int FRUIT_IMAGE_HEIGHT = 100;

    public Scrabble() {
        // Load username from the file in the Data/UserData folder
        this.username = loadUsernameFromFile();

        // Add words to the list
        words.add("apple");
        words.add("banana");
        words.add("grape");
        words.add("orange");
        words.add("cherry");
        words.add("watermelon");
        words.add("peach");
        words.add("mango");
        words.add("strawberry");
        words.add("blueberry");

        Collections.shuffle(words);
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
        frame = new JFrame("Scrabble Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 700);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        panel = new JPanel() { // Now panel is the class-level variable
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image bgImage = new ImageIcon("img/bg.jpg").getImage();
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
    
                Image cloudImage = new ImageIcon("img/cloud.png").getImage();
                g.drawImage(cloudImage, cloudXTop, CLOUD_Y_TOP, CLOUD_WIDTH, CLOUD_HEIGHT, this);
                g.drawImage(cloudImage, cloudXBottom, CLOUD_Y_BOTTOM, CLOUD_WIDTH, CLOUD_HEIGHT, this);
            }
        };
        panel.setLayout(null);

        JLabel usernameLabel = new JLabel("Hello, " + username);
        usernameLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 20));
        usernameLabel.setForeground(Color.BLACK);
        FontMetrics metrics = panel.getFontMetrics(usernameLabel.getFont());
        int labelWidth = metrics.stringWidth("Hello, " + username);
        int xPosition = (frame.getWidth() - labelWidth) / 2;
        usernameLabel.setBounds(xPosition, 30, labelWidth, 30);
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

        sentenceLabel = new JLabel("", SwingConstants.CENTER);
        sentenceLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 20));
        sentenceLabel.setForeground(Color.BLACK);
        sentenceLabel.setBounds(20, 195, 400, 50);
        panel.add(sentenceLabel);

        JTextField answerField = new JTextField();
        answerField.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        answerField.setBounds(50, 265, 350, 40);
        answerField.setHorizontalAlignment(JTextField.CENTER);
        panel.add(answerField);

        nextButton = createStyledButton("Next");
        nextButton.setBounds(125, 365, 200, 50);
        nextButton.addActionListener(_ -> {
            playSound("sound/select.wav");
            String userAnswer = answerField.getText().trim();
            processAnswer(userAnswer); // Process answer using the new input method
            if (currentWordIndex < words.size() - 1) {
                currentWordIndex++;
                answerField.setText(""); // Clear the input field
                updateSentence(); // Update sentence without options
            } else {
                saveToLeaderboard(score, "Scrabble");
                showScore(panel);
            }
        });
        panel.add(nextButton);

        frame.add(panel);
        frame.setVisible(true);

        Timer cloudTimer = new Timer(20, _ -> {
            cloudXTop += CLOUD_SPEED;
            cloudXBottom -= CLOUD_SPEED;

            if (cloudXTop > frame.getWidth()) {
                cloudXTop = -CLOUD_WIDTH;
            }
            if (cloudXBottom < -CLOUD_WIDTH) {
                cloudXBottom = frame.getWidth();
            }

            panel.repaint();
        });
        cloudTimer.start();

        updateSentence(); // Update sentence without options
    }

    private void updateSentence() {
        String word = words.get(currentWordIndex);
        String scrambledWord = scrambleWord(word);
        String sentence = "The fruit is " + scrambledWord + ".";
        sentenceLabel.setText(sentence);
    
        // Remove the previous fruit image if it exists
        if (fruitImageLabel != null) {
            panel.remove(fruitImageLabel);
        }
    
        // Add the new fruit image for the current word
        try {
            ImageIcon fruitImageIcon = new ImageIcon("img/" + word + ".png");
            Image scaledImage = fruitImageIcon.getImage()
                    .getScaledInstance(FRUIT_IMAGE_WIDTH, FRUIT_IMAGE_HEIGHT, Image.SCALE_SMOOTH);
            fruitImageIcon = new ImageIcon(scaledImage);
    
            fruitImageLabel = new JLabel(fruitImageIcon);
            fruitImageLabel.setBounds((panel.getWidth() - FRUIT_IMAGE_WIDTH) / 2, 100, FRUIT_IMAGE_WIDTH, FRUIT_IMAGE_HEIGHT);
            panel.add(fruitImageLabel);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error if the image cannot be loaded
        }
    
        panel.repaint();
        panel.revalidate();
    
        if (currentWordIndex == words.size() - 1) {
            nextButton.setText("Submit");
        }
    }

    private String scrambleWord(String word) {
        ArrayList<Character> letters = new ArrayList<>();
        for (char letter : word.toCharArray()) {
            letters.add(letter);
        }
        Collections.shuffle(letters);
        StringBuilder scrambled = new StringBuilder();
        for (char letter : letters) {
            scrambled.append(letter);
        }
        return scrambled.toString();
    }

    private void processAnswer(String userAnswer) {
        String correctWord = words.get(currentWordIndex);
        if (userAnswer.equalsIgnoreCase(correctWord)) {
            score++;
        }
    }

    private void showScore(JPanel panel) {
        panel.removeAll();
        panel.repaint();

        JLabel totalPointsLabel = new JLabel("Total Points", SwingConstants.CENTER);
        totalPointsLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 30));
        totalPointsLabel.setForeground(Color.BLACK);
        totalPointsLabel.setBounds(50, 200, 350, 40);
        panel.add(totalPointsLabel);

        JLabel scoreLabel = new JLabel(score + "/" + words.size(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Times New Roman", Font.ITALIC | Font.BOLD, 60));
        scoreLabel.setForeground(Color.BLACK);
        scoreLabel.setBounds(50, 250, 350, 60);
        panel.add(scoreLabel);

        JButton backButton = createStyledButton("Back");
        backButton.setBounds(125, 350, 200, 50);
        backButton.addActionListener(_ -> {
            playSound("sound/select.wav");
            frame.dispose();
            new HomePage().createAndShowGUI();
        });
        panel.add(backButton);

        panel.revalidate();
        panel.repaint();
    }

    private void saveToLeaderboard(int score, String gameName) {
        try {
            File dataFolder = new File("Data");
            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }
    
            File userFile = new File(dataFolder, "UserData.txt");
    
            // Read the last username from the file
            String playerName = "Unknown"; // Default name if there's an issue
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    playerName = line; // Update playerName with each line, ending with the last
                }
            } catch (IOException e) {
                e.printStackTrace(); // Handle file reading error
            }
    
            File leaderboardFile = new File(dataFolder, "Leaderboards.txt");
    
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(leaderboardFile, true))) {
                writer.write(playerName + " - " + score + " - " + gameName);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace(); // Handle file writing error
            }
        } catch (Exception e) {
            e.printStackTrace(); // General exception handling if any issues occur
        }
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

    private void playSound(String soundPath) {
        try {
            File soundFile = new File(soundPath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Scrabble::new);
    }
}

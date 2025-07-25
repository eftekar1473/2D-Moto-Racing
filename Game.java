// import statements remain unchanged
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class Game extends JPanel {
    public enum GameMode {
        SINGLE, MULTI
    }

    private final GameMode gameMode;
    int bikeX, bikeY, score;
    int bike2X, bike2Y, score2;
    float speedX, speedY, speed2X, speed2Y;
    boolean isFinished = false, isFinished2 = false;

    int crx, numOfenemys, highScore = 0;
    ImageIcon[] enemy;
    int[] enemyX, enemyY, enemySpeed;

    boolean gameStarted = false, enemyAdded = false, newHighScoreAchieved = false;
    boolean gameOverDialogShown = false;

    float speedModifier = 1.0f;
    int lastSpeedIncreaseLevel = 0;

    int userBikeHeight = 102, userBikeWidth = 153;
    int bikeCollisionOffsetX = 35, bikeCollisionOffsetY = 25;
    int carCollisionOffsetX = 15, carCollisionOffsetY = 8;
    String[] roadPattern = {"images/new.jpg", "images/st_road.png", "images/cross_road.png"};
    int roadWidth = 499;
    int totalPatternWidth = roadWidth * roadPattern.length;

    private String highScorerName = "High Score"; // Changed from "None"

    public Game(GameMode mode) {
        this.gameMode = mode;
        setLayout(null);
        setFocusable(true);
        requestFocusInWindow();

        loadHighScore();
        initializeGameVariables();

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!gameStarted && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    gameStarted = true;
                    speedX = 3;
                    if (gameMode == GameMode.MULTI) speed2X = 3;
                }

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> speedY = -2;
                    case KeyEvent.VK_DOWN -> speedY = 2;
                    case KeyEvent.VK_LEFT -> speedX = -2;
                    case KeyEvent.VK_RIGHT -> speedX = 3;
                }

                if (gameMode == GameMode.MULTI) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_W -> speed2Y = -2;
                        case KeyEvent.VK_S -> speed2Y = 2;
                        case KeyEvent.VK_A -> speed2X = -2;
                        case KeyEvent.VK_D -> speed2X = 3;
                    }
                }
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) speedY = 0;
                if (e.getKeyCode() == KeyEvent.VK_LEFT) speedX = 3;

                if (gameMode == GameMode.MULTI) {
                    if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_S) speed2Y = 0;
                    if (e.getKeyCode() == KeyEvent.VK_A) speed2X = 3;
                }
            }
        });

        javax.swing.Timer timer = new javax.swing.Timer(15, e -> {
            if (gameStarted) updateGame();
            repaint();
        });
        timer.start();
    }

    private void initializeGameVariables() {
        bikeX = 30; bikeY = 150; speedX = 0; speedY = 0; score = 0; isFinished = false;
        if (gameMode == GameMode.MULTI) {
            bike2X = 30; bike2Y = 300; speed2X = 0; speed2Y = 0; score2 = 0; isFinished2 = false;
        }
        enemy = new ImageIcon[20]; enemyX = new int[20]; enemyY = new int[20]; enemySpeed = new int[20];
        numOfenemys = 0; gameStarted = false; newHighScoreAchieved = false; speedModifier = 1.0f;
        lastSpeedIncreaseLevel = 0; gameOverDialogShown = false;
    }

    public void restartGame() {
        initializeGameVariables();
        SwingUtilities.invokeLater(() -> requestFocusInWindow());
        revalidate();
        repaint();
    }

    private void updateGame() {
        if (isGameOver()) {
            gameStarted = false;

            if (!gameOverDialogShown) {
                gameOverDialogShown = true;
                SwingUtilities.invokeLater(() -> {
                    int finalScore = (gameMode == GameMode.MULTI) ? Math.max(score, score2) : score;
                    String playerName = JOptionPane.showInputDialog(this, "Enter your name:");
                    String message = "Game Over!\nYour Score: " + finalScore;

                    if (gameMode == GameMode.MULTI) {
                        message += (score > score2) ? "\nPlayer 1 Wins!" :
                                   (score2 > score) ? "\nPlayer 2 Wins!" : "\nIt's a Tie!";
                    }

                    if (finalScore > highScore) {
                        newHighScoreAchieved = true;
                        highScore = finalScore;
                        highScorerName = (playerName == null || playerName.isEmpty()) ? "Unknown" : playerName;
                        saveHighScore(highScorerName);
                        message += "\nCongratulations, it's a high score!";
                    }

                    savePlayerScore(playerName, finalScore);

                    Object[] options = {"Restart", "Quit"};
                    int choice = JOptionPane.showOptionDialog(
                        this, message, "Game Over", JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE, null, options, options[0]
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        restartGame();
                    } else {
                        System.exit(0);
                    }
                });
            }
            return;
        }

        int currentLevel = (gameMode == GameMode.MULTI ? Math.max(score, score2) : score) / 500;
        if (currentLevel > lastSpeedIncreaseLevel) {
            speedModifier += 0.2f;
            lastSpeedIncreaseLevel = currentLevel;
        }

        float currentScrollSpeed = (isFinished) ? speed2X : speedX;
        crx -= currentScrollSpeed * speedModifier;
        for (int i = 0; i < numOfenemys; i++) enemyX[i] -= currentScrollSpeed * speedModifier;
        if (crx <= -totalPatternWidth) crx += totalPatternWidth;
        else if (crx > 0) crx -= totalPatternWidth;

        if (!isFinished && speedX > 0) score++;
        if (gameMode == GameMode.MULTI && !isFinished2 && speed2X > 0) score2++;

        if (!isFinished) {
            bikeY += speedY;
            if (bikeY < 0) bikeY = 0;
            if (bikeY > getHeight() - userBikeHeight) bikeY = getHeight() - userBikeHeight;
        }

        if (gameMode == GameMode.MULTI && !isFinished2) {
            bike2Y += speed2Y;
            if (bike2Y < 0) bike2Y = 0;
            if (bike2Y > getHeight() - userBikeHeight) bike2Y = getHeight() - userBikeHeight;
        }

        updateEnemies();
        checkCollisions();
    }

    private void updateEnemies() {
        for (int i = 0; i < numOfenemys; i++) {
            enemyX[i] -= (enemySpeed[i] * speedModifier);
            if (enemyX[i] < -200) {
                enemyX[i] = getWidth() + (int)(Math.random() * 500);
                enemyY[i] = switch ((int)(Math.random() * 4)) {
                    case 0 -> 250; case 1 -> 300; case 2 -> 185; default -> 130;
                };
            }
        }
        int triggerScore = (gameMode == GameMode.MULTI) ? Math.max(score, score2) : score;
        if (triggerScore > 0 && triggerScore % 150 == 0 && !enemyAdded && numOfenemys < 20) {
            addEnemy();
            enemyAdded = true;
        } else if (triggerScore % 150 != 0) enemyAdded = false;
    }

    private void checkCollisions() {
        if (!isFinished) {
            Rectangle playerRect = new Rectangle(bikeX + bikeCollisionOffsetX, bikeY + bikeCollisionOffsetY,
                userBikeWidth - (bikeCollisionOffsetX * 2), userBikeHeight - (bikeCollisionOffsetY * 2));
            for (int i = 0; i < numOfenemys; i++) {
                Rectangle enemyRect = new Rectangle(enemyX[i] + carCollisionOffsetX, enemyY[i] + carCollisionOffsetY,
                        enemy[i].getIconWidth() - (carCollisionOffsetX * 2), enemy[i].getIconHeight() - (carCollisionOffsetY * 2));
                if (playerRect.intersects(enemyRect)) {
                    isFinished = true;
                    break;
                }
            }
        }

        if (gameMode == GameMode.MULTI && !isFinished2) {
            Rectangle player2Rect = new Rectangle(bike2X + bikeCollisionOffsetX, bike2Y + bikeCollisionOffsetY,
                userBikeWidth - (bikeCollisionOffsetX * 2), userBikeHeight - (bikeCollisionOffsetY * 2));
            for (int i = 0; i < numOfenemys; i++) {
                Rectangle enemyRect = new Rectangle(enemyX[i] + carCollisionOffsetX, enemyY[i] + carCollisionOffsetY,
                        enemy[i].getIconWidth() - (carCollisionOffsetX * 2), enemy[i].getIconHeight() - (carCollisionOffsetY * 2));
                if (player2Rect.intersects(enemyRect)) {
                    isFinished2 = true;
                    break;
                }
            }
        }

        if (gameMode == GameMode.MULTI && !isFinished && !isFinished2) {
            Rectangle r1 = new Rectangle(bikeX + bikeCollisionOffsetX, bikeY + bikeCollisionOffsetY,
                    userBikeWidth - (bikeCollisionOffsetX * 2), userBikeHeight - (bikeCollisionOffsetY * 2));
            Rectangle r2 = new Rectangle(bike2X + bikeCollisionOffsetX, bike2Y + bikeCollisionOffsetY,
                    userBikeWidth - (bikeCollisionOffsetX * 2), userBikeHeight - (bikeCollisionOffsetY * 2));
            if (r1.intersects(r2)) {
                isFinished = true;
            }
        }
    }

    private boolean isGameOver() {
        return (gameMode == GameMode.SINGLE) ? isFinished : (isFinished && isFinished2);
    }

    private void addEnemy() {
        String path = "images/car_left_" + ((int)(Math.random() * 3 + 1)) + ".png";
        enemy[numOfenemys] = new ImageIcon(path);
        enemyX[numOfenemys] = 1000;
        enemyY[numOfenemys] = switch ((int)(Math.random() * 4)) {
            case 0 -> 250; case 1 -> 300; case 2 -> 185; default -> 130;
        };
        enemySpeed[numOfenemys] = (int)(Math.random() * 2) + 2;
        numOfenemys++;
    }

    private void loadHighScore() {
        try (Scanner scanner = new Scanner(new File("highscore.txt"))) {
            if (scanner.hasNextLine()) {
                String name = scanner.nextLine();
                if (name != null && !name.trim().isEmpty()) highScorerName = name;
            }
            if (scanner.hasNextInt()) highScore = scanner.nextInt();
        } catch (FileNotFoundException e) {
            highScore = 0;
            highScorerName = "High Score";
        }
    }

    private void saveHighScore(String name) {
        if (name == null || name.trim().isEmpty()) name = "Unknown";
        try (PrintWriter writer = new PrintWriter(new FileWriter("highscore.txt"))) {
            writer.println(name);
            writer.println(highScore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void savePlayerScore(String name, int score) {
        if (name == null || name.trim().isEmpty()) name = "Unknown";
        try (PrintWriter writer = new PrintWriter(new FileWriter("player_scores.txt", true))) {
            writer.println(name + " : " + score);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D obj = (Graphics2D) g;
        for (int i = 0; i < roadPattern.length; i++) {
            int xPos = i * roadWidth;
            obj.drawImage(getToolkit().getImage(roadPattern[i]), crx + xPos, 0, this);
            obj.drawImage(getToolkit().getImage(roadPattern[i]), crx + totalPatternWidth + xPos, 0, this);
        }

        obj.drawImage(getToolkit().getImage("images/self_bike_copy_153x102.png"), bikeX, bikeY, this);
        if (isFinished) obj.drawImage(getToolkit().getImage("images/boom.png"), bikeX - 30, bikeY - 30, this);

        if (gameMode == GameMode.MULTI) {
            obj.drawImage(getToolkit().getImage("images/self_bike_copy_153x102.png"), bike2X, bike2Y, this);
            if (isFinished2) obj.drawImage(getToolkit().getImage("images/boom.png"), bike2X - 30, bike2Y - 30, this);
        }

        for (int i = 0; i < numOfenemys; i++) obj.drawImage(enemy[i].getImage(), enemyX[i], enemyY[i], this);

        // Stylized score
        Font scoreFont = new Font("Arial", Font.BOLD, 22);
        obj.setFont(scoreFont);

        // Draw High Score with shadow
        obj.setColor(Color.BLACK);
        obj.drawString(highScorerName + " : " + highScore, 800, 57);
        obj.setColor(Color.YELLOW);
        obj.drawString(highScorerName + " : " + highScore, 800, 55);

        if (gameMode == GameMode.SINGLE) {
            obj.setColor(Color.BLACK);
            obj.drawString("Score: " + score, 852, 32);
            obj.setColor(Color.YELLOW);
            obj.drawString("Score: " + score, 850, 30);
        } else {
            obj.setColor(Color.BLACK);
            obj.drawString("P1 Score: " + score, 852, 32);
            obj.setColor(Color.CYAN);
            obj.drawString("P1 Score: " + score, 850, 30);

            obj.setColor(Color.BLACK);
            obj.drawString("P2 Score: " + score2, 852, 82);
            obj.setColor(Color.ORANGE);
            obj.drawString("P2 Score: " + score2, 850, 80);
        }

        if (!gameStarted && !isGameOver()) {
            obj.setColor(Color.WHITE);
            obj.setFont(new Font("Arial", Font.BOLD, 30));
            obj.drawString("Press SPACE to Start", 350, 50);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bike Racing Game");
        frame.setSize(1000, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JPanel menu = new JPanel();
        menu.setLayout(new GridLayout(3, 1, 10, 10));
        menu.setBackground(Color.BLACK);

        JLabel title = new JLabel("Welcome to the Thrill", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 50));
        title.setForeground(Color.WHITE);

        JButton single = new JButton("Single Player");
        single.setBackground(Color.WHITE);
        single.setFont(new Font("Arial", Font.BOLD, 50));
        single.setForeground(Color.GREEN);

        JButton multi = new JButton("Multiplayer");
        multi.setBackground(Color.WHITE);
        multi.setFont(new Font("Arial", Font.BOLD, 50));
        multi.setForeground(Color.ORANGE);

        menu.add(title);
        menu.add(single);
        menu.add(multi);
        frame.add(menu);
        frame.setVisible(true);

        ActionListener listener = e -> {
            GameMode selectedMode = (e.getSource() == single) ? GameMode.SINGLE : GameMode.MULTI;
            frame.remove(menu);
            Game game = new Game(selectedMode);
            frame.add(game);
            frame.revalidate();
            frame.repaint();
            SwingUtilities.invokeLater(game::requestFocusInWindow);
        };
        single.addActionListener(listener);
        multi.addActionListener(listener);
    }
}

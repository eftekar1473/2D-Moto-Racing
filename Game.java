import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Game extends JPanel {
    int bikeX, bikeY;
    int crx;
    float speedX, speedY;
    int numOfenemys;
    ImageIcon[] enemy;
    int[] enemyX, enemyY, enemySpeed;
    int score;
    int highScore = 0;
    boolean gameStarted = false;
    boolean isFinished = false;
    boolean enemyAdded = false;

    int userBikeHeight = 102, userBikeWidth = 153;

    String[] roadPattern = {
        "images/new.jpg",
        "images/st_road.png",
        "images/cross_road.png"
    };
    int roadWidth = 499;
    int totalPatternWidth = roadWidth * roadPattern.length;

    public Game() {
        setFocusable(true);
        requestFocusInWindow();

        bikeX = 30;
        bikeY = 150;
        crx = 0;

        speedX = 0;
        speedY = 0;

        enemy = new ImageIcon[20];
        enemyX = new int[20];
        enemyY = new int[20];
        enemySpeed = new int[20];

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE -> {
                        gameStarted = true;
                        speedX = 3;
                        speedY = 0;
                    }
                    case KeyEvent.VK_UP -> speedY = -2;
                    case KeyEvent.VK_DOWN -> speedY = 2;
                    case KeyEvent.VK_LEFT -> speedX = -2;
                    case KeyEvent.VK_RIGHT -> speedX = 2;
                }
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) speedY = 0;
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) speedX = 0;
            }
        });

        Timer timer = new Timer(15, e -> {
            if (gameStarted) {
                if (speedX > 0) {
                    crx -= speedX;

                    if (Math.abs(crx) >= totalPatternWidth) {
                        crx = 0;
                    }

                    for (int i = 0; i < numOfenemys; i++) {
                        enemyX[i] -= speedX;
                    }
                }

                bikeY += speedY;

                // Prevent bike from going off-screen
                if (bikeY < 0) bikeY = 0;
                if (bikeY > getHeight() - userBikeHeight) bikeY = getHeight() - userBikeHeight;

                for (int i = 0; i < numOfenemys; i++) {
                    enemyX[i] -= enemySpeed[i];
                }

                // Controlled enemy spawning
                if (score % 50 == 0 && !enemyAdded && numOfenemys < 20) {
                    addEnemy();
                    enemyAdded = true;
                } else if (score % 50 != 0) {
                    enemyAdded = false;
                }

                // Collision detection
                Rectangle playerRect = new Rectangle(bikeX, bikeY, userBikeWidth, userBikeHeight);
                for (int i = 0; i < numOfenemys; i++) {
                    int enemyW = enemy[i].getIconWidth();
                    int enemyH = enemy[i].getIconHeight();

                    Rectangle enemyRect = new Rectangle(enemyX[i], enemyY[i], enemyW, enemyH);
                    if (playerRect.intersects(enemyRect)) {
                        isFinished = true;
                        gameStarted = false;
                        if (score > highScore) highScore = score;

                        JOptionPane.showMessageDialog(this,
                                "Game Over!!!\nYour Score : " + score,
                                "Game Over", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                }

                score++;
            }
            repaint();
        });
        timer.start();
    }

    private void addEnemy() {
        String path = "images/car_left_" + ((int) (Math.random() * 3 + 1)) + ".png";
        enemy[numOfenemys] = new ImageIcon(path);
        enemyX[numOfenemys] = 1000;
        int p = (int) (Math.random() * 100) % 4;
        enemyY[numOfenemys] = switch (p) {
            case 0 -> 250;
            case 1 -> 300;
            case 2 -> 185;
            default -> 130;
        };
        enemySpeed[numOfenemys] = (int) (Math.random() * 2) + 2;
        numOfenemys++;
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D obj = (Graphics2D) g;
        obj.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try {
            // Fill background to prevent flicker/white screen
            obj.setColor(Color.GRAY);
            obj.fillRect(0, 0, getWidth(), getHeight());

            // Draw scrolling road
            for (int i = 0; i <= getWidth() / roadWidth + 2; i++) {
                int index = (i + (Math.abs(crx) / roadWidth)) % roadPattern.length;
                int xPos = crx + i * roadWidth;
                obj.drawImage(getToolkit().getImage(roadPattern[index]), xPos, 0, this);
            }

            // Draw player bike
            obj.drawImage(getToolkit().getImage("images/self_bike_copy_153x102.png"), bikeX, bikeY, this);

            // Draw explosion if game over
            if (isFinished) {
                obj.drawImage(getToolkit().getImage("images/boom.png"), bikeX - 30, bikeY - 30, this);
            }

            // Draw enemies
            for (int i = 0; i < numOfenemys; i++) {
                obj.drawImage(enemy[i].getImage(), enemyX[i], enemyY[i], this);
            }

            // Draw game start prompt
            if (!gameStarted && !isFinished) {
                obj.setColor(Color.WHITE);
                obj.setFont(new Font("Arial", Font.BOLD, 30));
                obj.drawString("Press SPACE to Start", 350, 50);
            }

            // Draw score
            obj.setColor(Color.YELLOW);
            obj.setFont(new Font("Arial", Font.BOLD, 20));
            obj.drawString("Score: " + score, 850, 30);
            obj.drawString("High Score: " + highScore, 850, 55);

            // Game over text
            if (isFinished) {
                obj.setColor(Color.RED);
                obj.setFont(new Font("Arial", Font.BOLD, 40));
                obj.drawString("GAME OVER", 400, 250);
            }
        } catch (Exception e) {
            System.out.println("Error in paint: " + e);
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
        multi.setEnabled(false);

        menu.add(title);
        menu.add(single);
        menu.add(multi);

        frame.add(menu);
        frame.setVisible(true);

        single.addActionListener(e -> {
            frame.remove(menu);
            Game game = new Game();
            frame.add(game);
            frame.revalidate();
            frame.repaint();
            game.requestFocusInWindow();
        });

        multi.addActionListener(e -> {
            // Multiplayer option (disabled)
        });
    }
}

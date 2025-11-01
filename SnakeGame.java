import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.io.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    // Configuration constants for the play area and snake size.
    private final int WIDTH = 500, HEIGHT = 400, DOT_SIZE = 10, ALL_DOTS = (WIDTH * HEIGHT) / (DOT_SIZE * DOT_SIZE);
    

    // Arrays holding the snake body coordinates. Each index represents one segment.
    // x[0], y[0] is the head.
    private int[] x = new int[ALL_DOTS];
    private int[] y = new int[ALL_DOTS];

    // Number of segments (length of the snake) and food coordinates.
    private int dots, foodX, foodY;
    private int score = 0;
    private int highScore = 0;
    private final String HIGH_SCORE_FILE = "highest_score.txt";
    private boolean left = false, right = true, up = false, down = false, inGame = true;
    private Timer timer;

    public SnakeGame() {
        // Initial UI and input setup
        setBackground(Color.BLUE);
        setFocusable(true);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addKeyListener(this);
        highScore = loadHighScore();
        initGame();
    }

    private void initGame() {
        // Initialize snake in a default position and start the main game timer.
        dots = 3;
        for (int i = 0; i < dots; i++) {
            x[i] = 50 - i * DOT_SIZE;
            y[i] = 50;
        }
        locateFood();
        startTimer(100);
    }

    private void locateFood() {
        Random rand = new Random();
        foodX = rand.nextInt(WIDTH / DOT_SIZE) * DOT_SIZE;
        foodY = rand.nextInt(HEIGHT / DOT_SIZE) * DOT_SIZE;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Use helper methods to draw game pieces and HUD. This improves readability
        // and isolates draw logic for easier future changes.
        if (inGame) {
            drawFood(g);
            drawSnake(g);
            drawHUD(g);
        } else {
            drawGameOver(g);
        }
    }

    // Draw the food item (single red square).
    private void drawFood(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(foodX, foodY, DOT_SIZE, DOT_SIZE);
    }

    // Draw the snake: head is green, body segments are white.
    private void drawSnake(Graphics g) {
        for (int i = 0; i < dots; i++) {
            g.setColor(i == 0 ? Color.GREEN : Color.WHITE);
            g.fillRect(x[i], y[i], DOT_SIZE, DOT_SIZE);
        }
    }

    // Draw heads-up display (current score) in the top-left.
    private void drawHUD(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);
    }

    // Draw the Game Over screen and helpful restart instructions.
    private void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.drawString("Game Over", WIDTH / 2 - 30, HEIGHT / 2);
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, WIDTH / 2 - 30, HEIGHT / 2 + 20);
        g.drawString("High Score: " + highScore, WIDTH / 2 - 30, HEIGHT / 2 + 40);
        g.drawString("Press 'R' to Try Again", WIDTH / 2 - 60, HEIGHT / 2 + 65);
    }

    private void move() {
        // Shift body segments to follow the head, then move the head one step.
        for (int i = dots - 1; i > 0; i--) {
            x[i] = x[i-1];
            y[i] = y[i-1];
        }
        if (left) x[0] -= DOT_SIZE;
        if (right) x[0] += DOT_SIZE;
        if (up) y[0] -= DOT_SIZE;
        if (down) y[0] += DOT_SIZE;
    }

    private void checkFood() {
        // Grow snake and increase score when head reaches the food location.
        if (x[0] == foodX && y[0] == foodY) {
            dots++;
            score++;
            locateFood();
        }
    }

    private void checkCollision() {
        // Check collision with self (any body segment) and walls.
        for (int i = dots - 1; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) { inGame = false; break; }
        }
        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) inGame = false;
        if (!inGame) {
            // On collision, stop the timer and persist high score if needed.
            stopTimer();
            if (score > highScore) {
                highScore = score;
                saveHighScore(highScore);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            checkFood();
            checkCollision();
            move();
        }
        repaint();
    }

    // Handle key events for snake movement
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        // If game over, allow 'R' to restart
        if (!inGame && key == KeyEvent.VK_R) {
            restartGame();
            return;
        }
        if ((key == KeyEvent.VK_LEFT) && !right) { left = true; up = false; down = false; right = false; }
        if ((key == KeyEvent.VK_RIGHT) && !left) { right = true; up = false; down = false; left = false; }
        if ((key == KeyEvent.VK_UP) && !down) { up = true; left = false; right = false; down = false; }
        if ((key == KeyEvent.VK_DOWN) && !up) { down = true; left = false; right = false; up = false; }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new SnakeGame());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Load high score from a file; return 0 on error or if file missing
    private int loadHighScore() {
        File f = new File(HIGH_SCORE_FILE);
        if (!f.exists()) return 0;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String s = br.readLine();
            if (s != null) return Integer.parseInt(s.trim());
        } catch (Exception ex) {
            // ignore and return 0
        }
        return 0;
    }

    private void saveHighScore(int value) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            bw.write(Integer.toString(value));
        } catch (IOException ex) {
            // ignoring write errors for this simple demo
        }
    }

    // Restart the game (Try Again)
    private void restartGame() {
        // Reset the game state back to defaults and start the timer again.
        dots = 3;
        for (int i = 0; i < dots; i++) {
            x[i] = 50 - i * DOT_SIZE;
            y[i] = 50;
        }
        left = false; right = true; up = false; down = false; inGame = true;
        score = 0;
        locateFood();
        startTimer(100);
        repaint();
    }

    // Helper to start the game timer with a given delay (ms).
    private void startTimer(int delayMs) {
        if (timer == null) {
            timer = new Timer(delayMs, this);
        }
        timer.setDelay(delayMs);
        timer.start();
    }

    // Helper to stop the current game timer.
    private void stopTimer() {
        if (timer != null && timer.isRunning()) timer.stop();
    }
}

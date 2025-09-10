import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // bird class
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // pipe class
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // game states
    enum GameState { HOME, PLAYING, GAME_OVER }
    GameState gameState = GameState.HOME;

    // game logic
    Bird bird;
    int velocityX = -4; // move pipes
    int velocityY = 0;  // bird speed
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    // music
    Clip bgMusic;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // load images
        backgroundImg = new ImageIcon(getClass().getResource("/flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("/flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/bottompipe.png")).getImage();

        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        placePipeTimer = new Timer(1500, e -> placePipes());
        gameLoop = new Timer(1000 / 60, this);
    }

    // music methods using resource
    void playMusic(String musicFile) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                    getClass().getResource("/" + musicFile)
            );
            bgMusic = AudioSystem.getClip();
            bgMusic.open(audioStream);
            bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
            bgMusic.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stopMusic() {
        if (bgMusic != null && bgMusic.isRunning()) {
            bgMusic.stop();
        }
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, this.boardWidth, this.boardHeight, null);

        if (gameState == GameState.HOME) {
            g.setColor(Color.black);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("Flappy Bird", 90, 200);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press SPACE to Start", 80, 300);

        } else if (gameState == GameState.PLAYING) {
            g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);
            for (Pipe pipe : pipes) {
                g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
            }
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.PLAIN, 32));
            g.drawString(String.valueOf((int) score), 10, 35);

        } else if (gameState == GameState.GAME_OVER) {
            g.setColor(Color.black);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("Game Over", 100, 200);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Score: " + (int) score, 120, 250);
            g.drawString("Press R to Restart", 80, 320);
        }
    }

    public void move() {
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            move();
            repaint();
            if (gameOver) {
                placePipeTimer.stop();
                gameLoop.stop();
                gameState = GameState.GAME_OVER;
                stopMusic(); // stop music on game over
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameState == GameState.HOME && e.getKeyCode() == KeyEvent.VK_SPACE) {
            startGame();
        } else if (gameState == GameState.PLAYING && e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;
        } else if (gameState == GameState.GAME_OVER && e.getKeyCode() == KeyEvent.VK_R) {
            startGame(); // restart directly
        }
    }

    void startGame() {
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameOver = false;
        gameState = GameState.PLAYING;
        placePipeTimer.start();
        gameLoop.start();
        playMusic("bgmusic.wav"); // play music from resources
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}

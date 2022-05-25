import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;

public class Snake extends JPanel implements Runnable {

private static final long serialVersionUID = 1L;

	enum Dir {
	    up(0, -1),
	    right(1, 0),
	    down(0, 1),
	    left(-1, 0);
	    
	    final int y;
	    
	    final int x;
	    
	    Dir(int x, int y) {
	      this.x = x;
	      this.y = y;
	    }
	}
  
  static final Random rand = new Random();
  
  static final int WALL = -1;
  
  static final int MAX_ENERGY = 1500;
  
  volatile boolean gameOver = true;
  
  Thread gameThread;
  
  int score;
  
  int hiScore;
  
  int nRows = 44;
  
  int nCols = 64;
  
  Dir dir;
  
  int energy;
  
  int[][] grid;
  
  List<Point> snake;
  
  List<Point> treats;
  
  Font smallFont;
  
  public Snake() {
    setPreferredSize(new Dimension(640, 440));
    setBackground(Color.white);
    setFont(new Font("SansSerif", 1, 48));
    setFocusable(true);
    this.smallFont = getFont().deriveFont(1, 18.0F);
    initGrid();
    addMouseListener(new MouseAdapter() {
          public void mousePressed(MouseEvent e) {
            if (Snake.this.gameOver) {
              Snake.this.startNewGame();
              Snake.this.repaint();
            } 
          }
        });
    addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
              case 38:
                if (Snake.this.dir != Snake.Dir.down)
                  Snake.this.dir = Snake.Dir.up; 
                break;
              case 37:
                if (Snake.this.dir != Snake.Dir.right)
                  Snake.this.dir = Snake.Dir.left; 
                break;
              case 39:
                if (Snake.this.dir != Snake.Dir.left)
                  Snake.this.dir = Snake.Dir.right; 
                break;
              case 40:
                if (Snake.this.dir != Snake.Dir.up)
                  Snake.this.dir = Snake.Dir.down; 
                break;
            } 
            Snake.this.repaint();
          }
        });
  }
  
  void startNewGame() {
    this.gameOver = false;
    stop();
    initGrid();
    this.treats = new LinkedList<>();
    this.dir = Dir.left;
    this.energy = 1500;
    if (this.score > this.hiScore)
      this.hiScore = this.score; 
    this.score = 0;
    this.snake = new ArrayList<>();
    for (int x = 0; x < 7; x++)
      this.snake.add(new Point(this.nCols / 2 + x, this.nRows / 2)); 
    do {
      addTreat();
    } while (this.treats.isEmpty());
    (this.gameThread = new Thread(this)).start();
  }
  
  void stop() {
    if (this.gameThread != null) {
      Thread tmp = this.gameThread;
      this.gameThread = null;
      tmp.interrupt();
    } 
  }
  
  void initGrid() {
    this.grid = new int[this.nRows][this.nCols];
    for (int r = 0; r < this.nRows; r++) {
      for (int c = 0; c < this.nCols; c++) {
        if (c == 0 || c == this.nCols - 1 || r == 0 || r == this.nRows - 1)
          this.grid[r][c] = -1; 
      } 
    } 
  }
  
  public void run() {
    while (Thread.currentThread() == this.gameThread) {
      try {
        Thread.sleep(Math.max(75 - this.score, 25));
      } catch (InterruptedException e) {
        return;
      } 
      if (energyUsed() || hitsWall() || hitsSnake()) {
        gameOver();
      } else {
        if (eatsTreat()) {
          this.score++;
          this.energy = 1500;
          growSnake();
        } 
        moveSnake();
        addTreat();
      } 
      repaint();
    } 
  }
  
  boolean energyUsed() {
    this.energy -= 10;
    return (this.energy <= 0);
  }
  
  boolean hitsWall() {
    Point head = this.snake.get(0);
    int nextCol = head.x + this.dir.x;
    int nextRow = head.y + this.dir.y;
    return (this.grid[nextRow][nextCol] == -1);
  }
  
  boolean hitsSnake() {
    Point head = this.snake.get(0);
    int nextCol = head.x + this.dir.x;
    int nextRow = head.y + this.dir.y;
    for (Point p : this.snake) {
      if (p.x == nextCol && p.y == nextRow)
        return true; 
    } 
    return false;
  }
  
  boolean eatsTreat() {
    Point head = this.snake.get(0);
    int nextCol = head.x + this.dir.x;
    int nextRow = head.y + this.dir.y;
    for (Point p : this.treats) {
      if (p.x == nextCol && p.y == nextRow)
        return this.treats.remove(p); 
    } 
    return false;
  }
  
  void gameOver() {
    this.gameOver = true;
    stop();
  }
  
  void moveSnake() {
    for (int i = this.snake.size() - 1; i > 0; i--) {
      Point p1 = this.snake.get(i - 1);
      Point p2 = this.snake.get(i);
      p2.x = p1.x;
      p2.y = p1.y;
    } 
    Point head = this.snake.get(0);
    head.x += this.dir.x;
    head.y += this.dir.y;
  }
  
  void growSnake() {
    Point tail = this.snake.get(this.snake.size() - 1);
    int x = tail.x + this.dir.x;
    int y = tail.y + this.dir.y;
    this.snake.add(new Point(x, y));
  }
  
  void addTreat() {
    if (this.treats.size() < 3)
      if (rand.nextInt(10) == 0)
        if (rand.nextInt(4) != 0) {
          Point p;
          while (true) {
            int x = rand.nextInt(this.nCols);
            int y = rand.nextInt(this.nRows);
            if (this.grid[y][x] != 0)
              continue; 
            p = new Point(x, y);
            if (this.snake.contains(p) || this.treats.contains(p))
              continue; 
            break;
          } 
          this.treats.add(p);
        } else if (this.treats.size() > 1) {
          this.treats.remove(0);
        }   
  }
  
  void drawGrid(Graphics2D g) {
    g.setColor(Color.lightGray);
    for (int r = 0; r < this.nRows; r++) {
      for (int c = 0; c < this.nCols; c++) {
        if (this.grid[r][c] == -1)
          g.fillRect(c * 10, r * 10, 10, 10); 
      } 
    } 
  }
  
  void drawSnake(Graphics2D g) {
    g.setColor(Color.blue);
    for (Point p : this.snake)
      g.fillRect(p.x * 10, p.y * 10, 10, 10); 
    g.setColor((this.energy < 500) ? Color.red : Color.orange);
    Point head = this.snake.get(0);
    g.fillRect(head.x * 10, head.y * 10, 10, 10);
  }
  
  void drawTreats(Graphics2D g) {
    g.setColor(Color.green);
    for (Point p : this.treats)
      g.fillRect(p.x * 10, p.y * 10, 10, 10); 
  }
  
  void drawStartScreen(Graphics2D g) {
    g.setColor(Color.blue);
    g.setFont(getFont());
    g.drawString("Snake", 240, 190);
    g.setColor(Color.orange);
    g.setFont(this.smallFont);
    g.drawString("(click to start)", 250, 240);
  }
  
  void drawScore(Graphics2D g) {
    int h = getHeight();
    g.setFont(this.smallFont);
    g.setColor(getForeground());
    String s = String.format("hiscore %d    score %d", new Object[] { Integer.valueOf(this.hiScore), Integer.valueOf(this.score) });
    g.drawString(s, 30, h - 30);
    g.drawString(String.format("energy %d", new Object[] { Integer.valueOf(this.energy) }), getWidth() - 150, h - 30);
  }
  
  public void paintComponent(Graphics gg) {
    super.paintComponent(gg);
    Graphics2D g = (Graphics2D)gg;
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
        RenderingHints.VALUE_ANTIALIAS_ON);
    drawGrid(g);
    if (this.gameOver) {
      drawStartScreen(g);
    } else {
      drawSnake(g);
      drawTreats(g);
      drawScore(g);
    } 
  }
}
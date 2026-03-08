import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Snake extends JFrame {
    public Snake() {
        setTitle("贪吃蛇游戏");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(new GamePanel());
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Snake());
    }
}

class GamePanel extends JPanel implements KeyListener {
    private static final int GRID_SIZE = 20;
    private static final int GRID_WIDTH = 30;
    private static final int GRID_HEIGHT = 20;
    private static final int GAME_SPEED = 100; // 毫秒

    private ArrayList<Point> snake;
    private Point food;
    private Direction direction;
    private Direction nextDirection;
    private boolean gameOver;
    private boolean autoMode;
    private boolean gamePaused;
    private int score;
    private Random random;
    private Timer gameTimer;

    enum Direction {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        final int dx;
        final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    public GamePanel() {
        setPreferredSize(new Dimension(GRID_WIDTH * GRID_SIZE, GRID_HEIGHT * GRID_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        random = new Random();
        initGame();

        gameTimer = new Timer(GAME_SPEED, e -> {
            if (!gameOver) {
                updateGame();
            }
            repaint();
        });
        gameTimer.start();
    }

    private void initGame() {
        snake = new ArrayList<>();
        // 初始蛇的位置 - 从中间开始
        snake.add(new Point(GRID_WIDTH / 2, GRID_HEIGHT / 2));
        snake.add(new Point(GRID_WIDTH / 2 - 1, GRID_HEIGHT / 2));
        snake.add(new Point(GRID_WIDTH / 2 - 2, GRID_HEIGHT / 2));

        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        gameOver = false;
        autoMode = false;
        gamePaused = false;
        score = 0;

        generateFood();
    }

    private void generateFood() {
        boolean validPosition;
        do {
            validPosition = true;
            food = new Point(random.nextInt(GRID_WIDTH), random.nextInt(GRID_HEIGHT));

            // 检查食物是否与蛇重叠
            for (Point p : snake) {
                if (p.equals(food)) {
                    validPosition = false;
                    break;
                }
            }
        } while (!validPosition);
    }

    // BFS算法寻找到食物的最短路径
    private Direction findPathToFood() {
        Point head = snake.get(0);
        if (head.equals(food)) {
            return direction;
        }

        Queue<Node> queue = new LinkedList<>();
        boolean[][] visited = new boolean[GRID_HEIGHT][GRID_WIDTH];
        
        queue.offer(new Node(head, null));
        visited[head.y][head.x] = true;

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
            // 尝试四个方向
            for (Direction dir : Direction.values()) {
                Point next = new Point(
                    (current.pos.x + dir.dx + GRID_WIDTH) % GRID_WIDTH,
                    (current.pos.y + dir.dy + GRID_HEIGHT) % GRID_HEIGHT
                );

                // 检查是否到达食物
                if (next.equals(food)) {
                    return current.direction != null ? current.direction : dir;
                }

                // 检查是否已访问或碰到蛇身
                if (!visited[next.y][next.x] && !isSnakeBody(next)) {
                    visited[next.y][next.x] = true;
                    Direction pathDirection = current.direction != null ? current.direction : dir;
                    queue.offer(new Node(next, pathDirection));
                }
            }
        }

        // 如果没有找到路径，尝试找一个不会自杀的方向来探索
        return findSafeDirection();
    }

    // 找一个安全的方向来避免困住，优先选择有较大空闲空间的方向
    private Direction findSafeDirection() {
        // 获取所有合法的方向
        ArrayList<Direction> legalDirections = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (isValidMove(dir)) {
                Point next = new Point(
                    (snake.get(0).x + dir.dx + GRID_WIDTH) % GRID_WIDTH,
                    (snake.get(0).y + dir.dy + GRID_HEIGHT) % GRID_HEIGHT
                );
                if (!isSnakeBody(next)) {
                    legalDirections.add(dir);
                }
            }
        }

        // 如果有合法方向，选择空闲空间最多的方向
        if (!legalDirections.isEmpty()) {
            Direction bestDir = legalDirections.get(0);
            int maxFreespace = -1;

            for (Direction dir : legalDirections) {
                int freespace = countFreespace(dir);
                if (freespace > maxFreespace) {
                    maxFreespace = freespace;
                    bestDir = dir;
                }
            }
            return bestDir;
        }

        // 如果没有任何合法方向，返回当前方向（虽然这不应该发生）
        return direction;
    }

    // 计算沿着某个方向移动3步内的空闲空间数量
    private int countFreespace(Direction dir) {
        int count = 0;
        Point current = new Point(snake.get(0).x, snake.get(0).y);

        for (int i = 0; i < 3; i++) {
            current = new Point(
                (current.x + dir.dx + GRID_WIDTH) % GRID_WIDTH,
                (current.y + dir.dy + GRID_HEIGHT) % GRID_HEIGHT
            );
            if (!isSnakeBody(current)) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    // 检查点是否是蛇的身体（除了头部）
    private boolean isSnakeBody(Point p) {
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(i).equals(p)) {
                return true;
            }
        }
        return false;
    }

    // 检查移动方向是否合法
    private boolean isValidMove(Direction dir) {
        // 不能与当前方向完全相反
        return !(direction == Direction.UP && dir == Direction.DOWN ||
                 direction == Direction.DOWN && dir == Direction.UP ||
                 direction == Direction.LEFT && dir == Direction.RIGHT ||
                 direction == Direction.RIGHT && dir == Direction.LEFT);
    }

    // BFS中使用的节点类
    private static class Node {
        Point pos;
        Direction direction;

        Node(Point pos, Direction direction) {
            this.pos = pos;
            this.direction = direction;
        }
    }

    // 根据两点计算方向
    private Direction getDirection(Point from, Point to) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;

        // 处理环绕
        if (dx > GRID_WIDTH / 2) dx -= GRID_WIDTH;
        if (dx < -GRID_WIDTH / 2) dx += GRID_WIDTH;
        if (dy > GRID_HEIGHT / 2) dy -= GRID_HEIGHT;
        if (dy < -GRID_HEIGHT / 2) dy += GRID_HEIGHT;

        if (dy < 0) return Direction.UP;
        if (dy > 0) return Direction.DOWN;
        if (dx < 0) return Direction.LEFT;
        return Direction.RIGHT;
    }

    // 根据蛇的身体位置计算颜色，从头到尾逐渐变暗
    private Color getSnakeSegmentColor(int index) {
        // 头部保持最亮（0, 255, 0）
        // 尾部最暗（0, 100, 0）
        int headBrightness = 255;
        int tailBrightness = 100;
        
        if (snake.size() <= 1) {
            return new Color(0, headBrightness, 0);
        }
        
        // 计算从头到尾的位置比例（0 = 头，1 = 尾）
        float ratio = (float) index / (snake.size() - 1);
        
        // 线性插值计算亮度
        int brightness = (int) (headBrightness - (headBrightness - tailBrightness) * ratio);
        
        return new Color(0, brightness, 0);
    }

    private void updateGame() {
        // 暂停时不更新游戏
        if (gamePaused) {
            return;
        }

        // 自动模式 - 计算寻路
        if (autoMode) {
            Direction pathDirection = findPathToFood();
            if (pathDirection != null && isValidMove(pathDirection)) {
                nextDirection = pathDirection;
            }
        }

        // 更新方向
        direction = nextDirection;

        // 计算新的头部位置
        Point head = snake.get(0);
        Point newHead = new Point(
            head.x + direction.dx,
            head.y + direction.dy
        );

        // 边界碰撞检测（环绕）
        if (newHead.x < 0) newHead.x = GRID_WIDTH - 1;
        if (newHead.x >= GRID_WIDTH) newHead.x = 0;
        if (newHead.y < 0) newHead.y = GRID_HEIGHT - 1;
        if (newHead.y >= GRID_HEIGHT) newHead.y = 0;

        // 自身碰撞检测
        for (Point p : snake) {
            if (p.equals(newHead)) {
                gameOver = true;
                return;
            }
        }

        // 添加新头部
        snake.add(0, newHead);

        // 检查是否吃到食物
        if (newHead.equals(food)) {
            score += 10;
            generateFood();
        } else {
            // 移除尾部（不吃食物时）
            snake.remove(snake.size() - 1);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 第一遍：绘制所有非转角的直线段（矩形）
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            int x = p.x * GRID_SIZE;
            int y = p.y * GRID_SIZE;
            int offset = 1;

            if (i == 0) {
                // 头部：绘制圆形
                g2d.setColor(getSnakeSegmentColor(i));
                g2d.fillOval(x + offset, y + offset, GRID_SIZE - 2 * offset, GRID_SIZE - 2 * offset);
            } else {
                // 身体段
                Point prev = snake.get(i - 1);
                Point next = i + 1 < snake.size() ? snake.get(i + 1) : null;
                
                Direction fromDir = getDirection(prev, p);
                Direction toDir = next != null ? getDirection(p, next) : fromDir;
                
                if (fromDir == toDir) {
                    // 直线段：绘制矩形
                    g2d.setColor(getSnakeSegmentColor(i));
                    g2d.fillRect(x + offset, y + offset, GRID_SIZE - 2 * offset, GRID_SIZE - 2 * offset);
                }
            }
        }
        
        // 第二遍：绘制所有转角处的四分之一圆
        for (int i = 1; i < snake.size() - 1; i++) {
            Point p = snake.get(i);
            Point prev = snake.get(i - 1);
            Point next = snake.get(i + 1);
            
            Direction fromDir = getDirection(prev, p);
            Direction toDir = getDirection(p, next);
            
            if (fromDir != toDir) {
                // 转角处：只绘制四分之一圆
                g2d.setColor(getSnakeSegmentColor(i));
                drawQuarterCircle(g2d, p.x * GRID_SIZE, p.y * GRID_SIZE, fromDir, toDir);
            }
        }

        // 绘制食物
        g2d.setColor(Color.RED);
        g2d.fillOval(food.x * GRID_SIZE + 2, food.y * GRID_SIZE + 2,
                     GRID_SIZE - 4, GRID_SIZE - 4);

        // 绘制分数和模式
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SimHei", Font.BOLD, 16));
        g2d.drawString("分数: " + score, 10, 20);
        
        String modeText = autoMode ? "【自动模式】" : "【手动模式】";
        if (gamePaused) {
            modeText += " [暂停]";
        }
        g2d.drawString(modeText, getWidth() - 200, 20);
        
        // 绘制模式切换提示
        g2d.setFont(new Font("SimHei", Font.PLAIN, 12));
        g2d.drawString("V切换自动 | 空格暂停", 10, getHeight() - 10);

        // 绘制游戏结束信息
        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SimHei", Font.BOLD, 40));
            String gameOverText = "游戏结束!";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(gameOverText)) / 2;
            int y = getHeight() / 2 - 50;
            g2d.drawString(gameOverText, x, y);

            g2d.setFont(new Font("SimHei", Font.BOLD, 24));
            String scoreText = "最终分数: " + score;
            fm = g2d.getFontMetrics();
            x = (getWidth() - fm.stringWidth(scoreText)) / 2;
            g2d.drawString(scoreText, x, y + 50);

            g2d.setFont(new Font("SimHei", Font.PLAIN, 16));
            String restartText = "按 R 重新开始";
            fm = g2d.getFontMetrics();
            x = (getWidth() - fm.stringWidth(restartText)) / 2;
            g2d.drawString(restartText, x, y + 100);
        }
    }

    // 绘制转角处的四分之一圆，半径等于GRID_SIZE，圆心在转角对应的正方形角上
    private void drawQuarterCircle(Graphics2D g2d, int x, int y, Direction from, Direction to) {
        // 根据蛇的进入和离开方向，确定圆心、外接矩形和填充象限
        
        if ((from == Direction.RIGHT && to == Direction.DOWN) || (from == Direction.UP && to == Direction.LEFT)) {
            // 从右进，向下出 && 从上进，向左出
            g2d.fillArc(x - GRID_SIZE, y , GRID_SIZE * 2, GRID_SIZE * 2, 0, 90);
        } else if ((from == Direction.RIGHT && to == Direction.UP) || (from == Direction.DOWN && to == Direction.LEFT)) {
            // 从右进，向上出 && 从下进，向左出
            g2d.fillArc(x - GRID_SIZE, y - GRID_SIZE, GRID_SIZE * 2, GRID_SIZE * 2, 270, 90);
        } else if ((from == Direction.LEFT && to == Direction.DOWN) || (from == Direction.UP && to == Direction.RIGHT)) {
            // 从左进，向下出 && 从上进，向右出
            g2d.fillArc(x , y , GRID_SIZE * 2, GRID_SIZE * 2, 90, 90);
        } else if ((from == Direction.LEFT && to == Direction.UP) || (from == Direction.DOWN && to == Direction.RIGHT)){
            // 从左进，向上出 && 从下进，向右出
            g2d.fillArc(x, y - GRID_SIZE, GRID_SIZE * 2, GRID_SIZE * 2, 180, 90);
        } 
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // 切换自动模式（V键）
        if (key == KeyEvent.VK_V) {
            autoMode = !autoMode;
        }

        // 暂停游戏（空格键）
        if (key == KeyEvent.VK_SPACE) {
            gamePaused = !gamePaused;
        }

        // WASD 控制（仅在非自动模式下且未暂停）
        if (!autoMode && !gamePaused) {
            if (key == KeyEvent.VK_W) {
                if (direction != Direction.DOWN) {
                    nextDirection = Direction.UP;
                }
            } else if (key == KeyEvent.VK_S) {
                if (direction != Direction.UP) {
                    nextDirection = Direction.DOWN;
                }
            } else if (key == KeyEvent.VK_A) {
                if (direction != Direction.RIGHT) {
                    nextDirection = Direction.LEFT;
                }
            } else if (key == KeyEvent.VK_D) {
                if (direction != Direction.LEFT) {
                    nextDirection = Direction.RIGHT;
                }
            }
        }

        // 重新开始（按R键）- 可以在暂停或游戏结束时重新开始
        if (key == KeyEvent.VK_R && (gameOver || gamePaused)) {
            gamePaused = false;
            initGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}

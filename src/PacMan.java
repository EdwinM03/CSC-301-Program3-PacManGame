import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    
    class Block {
        int x, y;
        int width, height;
        Image image;

        String ghostName = "";
        int startX, startY;

        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            this.direction = direction;
            updateVelocity();
        }

        // *** FIXED SPEED ***
        void updateVelocity() {
            int speed = tileSize / 4;

            switch (this.direction) {
                case 'U' -> { velocityX = 0; velocityY = -speed; }
                case 'D' -> { velocityX = 0; velocityY =  speed; }
                case 'L' -> { velocityX = -speed; velocityY = 0; }
                case 'R' -> { velocityX =  speed; velocityY = 0; }
            }
        }
        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;

    private int boardWidth  = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    Random random = new Random();

    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    class Node {
        int row, col;
        java.util.List<Node> neighbors = new java.util.ArrayList<>();

        Node(int r, int c) {
            row = r;
            col = c;
        }
    }
    Node[][] graph;

    PacMan() {
        initializeGame();
    }

    private void initializeGame() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        loadImages();
        loadMap();
        buildGraph();
        initializeGhosts();
        startGameLoop();
    }

    private void loadImages() {
        wallImage       = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage  = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage= new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage  = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage   = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage    = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage  = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage  = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();
    }

    public void loadMap() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                char tile = tileMap[r].charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                switch (tile) {
                    case 'X' -> walls.add(new Block(wallImage, x, y, tileSize, tileSize));

                    case 'r' -> {
                        Block bl = new Block(redGhostImage, x, y, tileSize, tileSize);
                        bl.ghostName = "blinky";
                        ghosts.add(bl);
                    }

                    case 'p' -> {
                        Block pk = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                        pk.ghostName = "pinky";
                        ghosts.add(pk);
                    }

                    case 'b' -> {
                        Block in = new Block(blueGhostImage, x, y, tileSize, tileSize);
                        in.ghostName = "inky";
                        ghosts.add(in);
                    }

                    case 'o' -> {
                        Block cl = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                        cl.ghostName = "clyde";
                        ghosts.add(cl);
                    }

                    case 'P' -> {
                        pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                    }

                    case ' ' -> foods.add(new Block(null, x+14, y+14, 4, 4));
                }
            }
        }
    }

    // ---------------------------------------------------------
    // GRAPH FOR BFS/A*
    // ---------------------------------------------------------
    private void buildGraph() {
        graph = new Node[rowCount][columnCount];

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                if (tileMap[r].charAt(c) != 'X') {
                    graph[r][c] = new Node(r, c);
                }
            }
        }

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                if (graph[r][c] == null) continue;

                if (r > 0 && graph[r-1][c] != null) graph[r][c].neighbors.add(graph[r-1][c]);
                if (r < rowCount-1 && graph[r+1][c] != null) graph[r][c].neighbors.add(graph[r+1][c]);
                if (c > 0 && graph[r][c-1] != null) graph[r][c].neighbors.add(graph[r][c-1]);
                if (c < columnCount-1 && graph[r][c+1] != null) graph[r][c].neighbors.add(graph[r][c+1]);
            }
        }
    }

    private void initializeGhosts() {
        char[] dirs = {'U','D','L','R'};
        for (Block ghost : ghosts) {
            ghost.updateDirection(dirs[random.nextInt(dirs.length)]);
        }
    }

    private void startGameLoop() {
        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    // ---------------------------------------------------------
    // BFS PATHFINDING
    // ---------------------------------------------------------
    private Node getNodeFor(Block b) {
        int r = b.y / tileSize;
        int c = b.x / tileSize;

        if (r < 0 || r >= rowCount || c < 0 || c >= columnCount) return null;
        return graph[r][c];
    }

    private java.util.List<Node> bfs(Node start, Node goal) {
        java.util.List<Node> path = new java.util.ArrayList<>();
        if (start == null || goal == null) return path;

        java.util.Queue<Node> queue = new java.util.ArrayDeque<>();
        java.util.HashMap<Node, Node> parent = new java.util.HashMap<>();
        java.util.HashSet<Node> visited = new java.util.HashSet<>();

        queue.add(start);
        visited.add(start);

        boolean found = false;

        while (!queue.isEmpty()) {
            Node current = queue.remove();

            if (current == goal) {
                found = true;
                break;
            }

            for (Node n : current.neighbors) {
                if (!visited.contains(n)) {
                    visited.add(n);
                    parent.put(n, current);
                    queue.add(n);
                }
            }
        }

        if (!found) return path;

        Node step = goal;
        while (step != null) {
            path.add(0, step);
            step = parent.get(step);
        }

        return path;
    }

    // ---------------------------------------------------------
    // A* FOR PINKY
    // ---------------------------------------------------------
    private int heuristic(Node a, Node b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    private java.util.List<Node> astar(Node start, Node goal) {
        java.util.List<Node> path = new java.util.ArrayList<>();
        if (start == null || goal == null) return path;

        java.util.PriorityQueue<Node> openSet =
                new java.util.PriorityQueue<>(
                    (n1, n2) -> Integer.compare(
                        fScore.getOrDefault(n1, Integer.MAX_VALUE),
                        fScore.getOrDefault(n2, Integer.MAX_VALUE)
                    )
                );

        java.util.HashMap<Node, Node> cameFrom = new java.util.HashMap<>();
        gScore.clear();
        fScore.clear();

        gScore.put(start, 0);
        fScore.put(start, heuristic(start, goal));

        openSet.add(start);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current == goal) {
                Node step = current;
                while (step != null) {
                    path.add(0, step);
                    step = cameFrom.get(step);
                }
                return path;
            }

            int currentG = gScore.getOrDefault(current, Integer.MAX_VALUE);

            for (Node n : current.neighbors) {
                int tentative = currentG + 1;
                if (tentative < gScore.getOrDefault(n, Integer.MAX_VALUE)) {
                    cameFrom.put(n, current);
                    gScore.put(n, tentative);
                    fScore.put(n, tentative + heuristic(n, goal));

                    if (!openSet.contains(n)) {
                        openSet.add(n);
                    }
                }
            }
        }

        return path;
    }

    java.util.HashMap<Node, Integer> gScore = new java.util.HashMap<>();
    java.util.HashMap<Node, Integer> fScore = new java.util.HashMap<>();

    // ---------------------------------------------------------
    // PINKY TARGET: 4 TILES AHEAD OF PACMAN
    // ---------------------------------------------------------
    private Node getPinkyTarget() {
        int r = pacman.y / tileSize;
        int c = pacman.x / tileSize;

        switch (pacman.direction) {
            case 'U' -> r -= 4;
            case 'D' -> r += 4;
            case 'L' -> c -= 4;
            case 'R' -> c += 4;
        }

        if (r < 0) r = 0;
        if (r >= rowCount) r = rowCount-1;
        if (c < 0) c = 0;
        if (c >= columnCount) c = columnCount-1;

        return graph[r][c];
    }

    // ---------------------------------------------------------
    // MOVEMENT HELPERS
    // ---------------------------------------------------------
    private void snap(Block b) {
        b.x = (b.x / tileSize) * tileSize;
        b.y = (b.y / tileSize) * tileSize;
    }

    private char directionFrom(Node a, Node b) {
        if (b.row == a.row && b.col == a.col + 1) return 'R';
        if (b.row == a.row && b.col == a.col - 1) return 'L';
        if (b.row == a.row - 1 && b.col == a.col) return 'U';
        if (b.row == a.row + 1 && b.col == a.col) return 'D';
        return 'U';
    }

    // ---------------------------------------------------------
    // MOVE PACMAN
    // ---------------------------------------------------------
    private void movePacman() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;
        checkWallCollision(pacman);
    }

    // ---------------------------------------------------------
    // MOVE GHOSTS
    // ---------------------------------------------------------
    private void moveGhosts() {
        for (Block g : ghosts) {
            if (collision(g, pacman)) {
                handleGhostCollision();
                return;
            }

            switch (g.ghostName) {
                case "blinky" -> moveBlinky(g);
                case "pinky"  -> movePinky(g);
                default       -> moveRandom(g);
            }
        }
    }

    // ---------------------------------------------------------
    // BLINKY = BFS DIRECT CHASE
    // ---------------------------------------------------------
    private void moveBlinky(Block bl) {
        if (bl.x % tileSize == 0 && bl.y % tileSize == 0) {
            snap(bl);

            Node gNode = getNodeFor(bl);
            Node pNode = getNodeFor(pacman);

            var path = bfs(gNode, pNode);

            if (path.size() > 1) {
                char d = directionFrom(gNode, path.get(1));
                bl.updateDirection(d);
            }
        }

        bl.x += bl.velocityX;
        bl.y += bl.velocityY;
        checkWallCollision(bl);
    }

    // ---------------------------------------------------------
    // PINKY = A* + LOOKAHEAD
    // ---------------------------------------------------------
    private void movePinky(Block pk) {
        if (pk.x % tileSize == 0 && pk.y % tileSize == 0) {
            snap(pk);

            Node gNode = getNodeFor(pk);
            Node target = getPinkyTarget();

            var path = astar(gNode, target);

            if (path.size() > 1) {
                char d = directionFrom(gNode, path.get(1));
                pk.updateDirection(d);
            }
        }

        pk.x += pk.velocityX;
        pk.y += pk.velocityY;
        checkWallCollision(pk);
    }

    private void moveRandom(Block g) {
        g.x += g.velocityX;
        g.y += g.velocityY;

        if (collisionWithWall(g)) {
            g.x -= g.velocityX;
            g.y -= g.velocityY;

            char[] dirs = {'U','D','L','R'};
            g.updateDirection(dirs[random.nextInt(4)]);
        }
    }

    private boolean collisionWithWall(Block g) {
        for (Block w : walls) {
            if (collision(g, w)) return true;
        }
        return false;
    }

    private void checkWallCollision(Block b) {
        for (Block wall : walls) {
            if (collision(b, wall)) {
                b.x -= b.velocityX;
                b.y -= b.velocityY;
                return;
            }
        }
    }

    private boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    private void handleGhostCollision() {
        lives--;
        if (lives == 0) {
            gameOver = true;
            return;
        }
        resetPositions();
    }

    private void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;

        char[] dirs = {'U','D','L','R'};
        for (Block g : ghosts) {
            g.reset();
            g.updateDirection(dirs[random.nextInt(dirs.length)]);
        }
    }

    private void checkFoodCollision() {
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        if (foodEaten != null) {
            foods.remove(foodEaten);
        }
    }

    public void updateGame() {
        movePacman();
        moveGhosts();
        checkFoodCollision();
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();

        if (gameOver) gameLoop.stop();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        drawPacman(g);
        drawGhosts(g);
        drawWalls(g);
        drawFoods(g);
        drawScore(g);
    }

    private void drawPacman(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
    }

    private void drawGhosts(Graphics g) {
        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }
    }

    private void drawWalls(Graphics g) {
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
    }

    private void drawFoods(Graphics g) {
        g.setColor(Color.WHITE);
        for (Block f : foods) {
            g.fillRect(f.x, f.y, f.width, f.height);
        }
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));

        if (gameOver) {
            g.drawString("Game Over: " + score, 20, 20);
        } else {
            g.drawString("x" + lives + " Score: " + score, 20, 20);
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            restartGame();
            return;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> pacman.updateDirection('U');
            case KeyEvent.VK_DOWN -> pacman.updateDirection('D');
            case KeyEvent.VK_LEFT -> pacman.updateDirection('L');
            case KeyEvent.VK_RIGHT -> pacman.updateDirection('R');
        }
        updatePacmanImage();
    }

    private void updatePacmanImage() {
        switch (pacman.direction) {
            case 'U' -> pacman.image = pacmanUpImage;
            case 'D' -> pacman.image = pacmanDownImage;
            case 'L' -> pacman.image = pacmanLeftImage;
            case 'R' -> pacman.image = pacmanRightImage;
        }
    }

    private void restartGame() {
        loadMap();
        buildGraph();
        resetPositions();
        score = 0;
        lives = 3;
        gameOver = false;
        gameLoop.start();
    }
}

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Simulation {
    private int width, height;
    private List<Agent> agents;
    private List<Obstacle> obstacles;
    private boolean[][] obstacleGrid;
    private int targetX, targetY;
    private double bI, bc;
    private int minAgentDist;
    private int maxSteps;
    private int currentStep;
    private boolean running;
    private SimulationPanel panel;

    public Simulation(int width, int height,
                      int numAgents,
                      int targetX, int targetY,
                      double bI, double bc,
                      int minAgentDist,
                      int maxSteps,
                      SimulationPanel panel) {
        this.width = width;
        this.height = height;
        this.targetX = targetX;
        this.targetY = targetY;
        this.bI = bI;
        this.bc = bc;
        this.minAgentDist = minAgentDist;
        this.maxSteps = maxSteps;
        this.panel = panel;
        this.currentStep = 0;
        this.running = true;

        agents = new ArrayList<>();
        obstacles = new ArrayList<>();
        obstacleGrid = new boolean[width][height];

        addObstacle(40, 35, 12, 12);
        addObstacle(55, 55, 15, 8);
        addObstacle(30, 50, 8, 12);
        addObstacle(35, 65, 10, 10);
        addObstacle(15, 10, 30, 6);
        addObstacle(67, 65, 10, 10);
        addObstacle(67, 25, 10, 10);
        addObstacle(80, 15, 10, 10);
        addObstacle(10, 80, 15, 10);
        addObstacle(67, 10, 10, 10);
        if (obstacleGrid[targetX][targetY]) {
            outer:
            for (int r = 1; r < Math.max(width, height); r++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dy = -r; dy <= r; dy++) {
                        int nx = targetX + dx;
                        int ny = targetY + dy;
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height &&
                                !obstacleGrid[nx][ny]) {
                            this.targetX = nx;
                            this.targetY = ny;
                            break outer;
                        }
                    }
                }
            }
        }

        initAgents(numAgents);
    }

    private void addObstacle(int x, int y, int w, int h) {
        Obstacle ob = new Obstacle(x, y, w, h);
        obstacles.add(ob);
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                if (i >= 0 && i < width && j >= 0 && j < height) {
                    obstacleGrid[i][j] = true;
                }
            }
        }
    }

    private void initAgents(int numAgents) {
        Random rand = new Random();
        int maxAttemptsPerAgent = 5000;

        for (int i = 0; i < numAgents; i++) {
            int x = -1, y = -1;
            boolean placed = false;

            for (int attempt = 0; attempt < maxAttemptsPerAgent; attempt++) {
                int cx = rand.nextInt(width);
                int cy = rand.nextInt(height);

                // 1) клетка не внутри препятствия
                if (obstacleGrid[cx][cy]) continue;

                // 2) достаточно далеко от уже поставленных агентов
                boolean farEnough = true;
                for (Agent a : agents) {
                    if (Math.hypot(a.x - cx, a.y - cy) < minAgentDist) {
                        farEnough = false;
                        break;
                    }
                }
                if (!farEnough) continue;

                x = cx; y = cy;
                placed = true;
                break;
            }

            // Запасной вариант: перебор всех клеток, ищем хоть одну подходящую
            if (!placed) {
                outer:
                for (int cx = 0; cx < width; cx++) {
                    for (int cy = 0; cy < height; cy++) {
                        if (obstacleGrid[cx][cy]) continue;
                        boolean ok = true;
                        for (Agent a : agents) {
                            if (Math.hypot(a.x - cx, a.y - cy) < minAgentDist) {
                                ok = false;
                                break;
                            }
                        }
                        if (ok) {
                            x = cx; y = cy;
                            placed = true;
                            break outer;
                        }
                    }
                }
            }

            if (!placed) {
                System.out.println("Не удалось разместить агента #" + i +
                        " (карта слишком плотная)");
                continue;
            }

            agents.add(new Agent(x, y));
            System.out.println("Агент #" + i + " стартует в клетке (" + x + ", " + y + ")");
        }
    }

    private int[] findNearestFreeCell(int cx, int cy) {
        for (int r = 1; r < Math.max(width, height); r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    int nx = cx + dx;
                    int ny = cy + dy;
                    if (nx >= 0 && nx < width && ny >= 0 && ny < height && !obstacleGrid[nx][ny]) {
                        return new int[]{nx, ny};
                    }
                }
            }
        }
        return new int[]{cx, cy};
    }

    private boolean isFreeCell(int x, int y, Set<String> occupied) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        if (obstacleGrid[x][y]) return false;
        return !occupied.contains(x + "," + y);
    }

    private boolean isFreeCell(int x, int y, Map<Agent, int[]> newPositions, Agent exclude) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        if (obstacleGrid[x][y]) return false;
        for (Agent a : agents) {
            if (a == exclude) continue;
            int[] pos = newPositions.get(a);
            if (pos != null) {
                if (pos[0] == x && pos[1] == y) return false;
            } else {
                if (a.x == x && a.y == y) return false;
            }
        }
        return true;
    }

    public void setPanel(SimulationPanel panel) { this.panel = panel; }

    public List<Agent> getAgents() { return agents; }
    public List<Obstacle> getObstacles() { return obstacles; }
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getCurrentStep() { return currentStep; }

    public int countDelivered() {
        int count = 0;
        for (Agent a : agents) if (a.isDelivered()) count++;
        return count;
    }

    public boolean step() {
        if (!running) return false;

        Map<Agent, int[]> newPositions = new HashMap<>();
        Map<Agent, double[]> newVelocities = new HashMap<>();

        // 8 направлений — 1 клетка за ход (по горизонтали, вертикали или диагонали)
        int[][] dirs = {
                {1,0},{-1,0},{0,1},{0,-1},
                {1,1},{1,-1},{-1,1},{-1,-1}
        };

        for (Agent agent : agents) {
            if (agent.isDelivered()) {
                newPositions.put(agent, new int[]{agent.x, agent.y});
                newVelocities.put(agent, new double[]{agent.dx, agent.dy});
                continue;
            }

            // --- 1. Ищем лучшего соседа ---
            Agent bestNeighbor = null;
            double bestDist = Double.MAX_VALUE;
            for (Agent other : agents) {
                if (other == agent || other.isDelivered()) continue;
                double dist = other.distanceTo(targetX, targetY);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestNeighbor = other;
                }
            }
            int neighborX = (bestNeighbor != null) ? bestNeighbor.x : agent.x;
            int neighborY = (bestNeighbor != null) ? bestNeighbor.y : agent.y;

            // --- 2. Вычисляем желаемую скорость по формуле PSO ---
            double[] vel = agent.computeVelocity(targetX, targetY, bI, bc, neighborX, neighborY);
            double newDx = vel[0];
            double newDy = vel[1];

            double mag = Math.hypot(newDx, newDy);

            // Если скорость ничтожна — стоим на месте
            if (mag < 0.001) {
                newPositions.put(agent, new int[]{agent.x, agent.y});
                newVelocities.put(agent, new double[]{newDx, newDy});
                continue;
            }

            double ux = newDx / mag;
            double uy = newDy / mag;

            // --- 3. Выбираем шаг (ровно 1 клетка) ---
            // Перебираем 8 направлений, оставляем только СВОБОДНЫЕ клетки,
            // среди них выбираем то, что лучше всего совпадает с желаемым (ux, uy).
            int bestStepX = 0, bestStepY = 0;
            double bestAlign = -2.0;
            boolean found = false;

            for (int[] d : dirs) {
                int nx = agent.x + d[0];
                int ny = agent.y + d[1];

                // границы поля
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                // препятствие
                if (obstacleGrid[nx][ny]) continue;
                // занято другим агентом (учитываем тех, кто уже перемещён в этом тике)
                if (!isFreeCell(nx, ny, newPositions, agent)) continue;

                double align = d[0] * ux + d[1] * uy; // косинус угла
                if (align > bestAlign) {
                    bestAlign = align;
                    bestStepX = d[0];
                    bestStepY = d[1];
                    found = true;
                }
            }

            if (found) {
                newPositions.put(agent, new int[]{agent.x + bestStepX, agent.y + bestStepY});
                newVelocities.put(agent, new double[]{newDx, newDy});
            } else {
                // со всех сторон заблокирован — стоим на месте,
                // но скорость сохраняем (инерция продолжит «давить» в нужную сторону)
                newPositions.put(agent, new int[]{agent.x, agent.y});
                newVelocities.put(agent, new double[]{newDx, newDy});
            }
        }

        // --- 4. Применяем результаты ---
        for (Agent agent : agents) {
            int[] pos = newPositions.get(agent);
            double[] v = newVelocities.get(agent);
            agent.applyNewPosition(pos[0], pos[1], v[0], v[1]);
            agent.updateBest(targetX, targetY);
            agent.tryDeliver(targetX, targetY);
        }

        resolveCollisions();

        currentStep++;

        boolean allDelivered = true;
        for (Agent a : agents) {
            if (!a.isDelivered()) { allDelivered = false; break; }
        }
        if (allDelivered || currentStep >= maxSteps) {
            running = false;
            return false;
        }
        return true;
    }

    private void resolveCollisions() {
        for (int i = 0; i < agents.size(); i++) {
            for (int j = i + 1; j < agents.size(); j++) {
                Agent a1 = agents.get(i);
                Agent a2 = agents.get(j);
                if (a1.x == a2.x && a1.y == a2.y) {
                    int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
                    boolean moved = false;
                    for (int[] d : dirs) {
                        int nx = a2.x + d[0];
                        int ny = a2.y + d[1];
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height &&
                                !obstacleGrid[nx][ny] && !isAgentAt(nx, ny, a2)) {
                            a2.x = nx; a2.y = ny;
                            moved = true;
                            break;
                        }
                    }
                    if (!moved) {
                        for (int[] d : dirs) {
                            int nx = a1.x + d[0];
                            int ny = a1.y + d[1];
                            if (nx >= 0 && nx < width && ny >= 0 && ny < height &&
                                    !obstacleGrid[nx][ny] && !isAgentAt(nx, ny, a1)) {
                                a1.x = nx; a1.y = ny;
                                moved = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isAgentAt(int x, int y, Agent exclude) {
        for (Agent a : agents) {
            if (a == exclude) continue;
            if (a.x == x && a.y == y) return true;
        }
        return false;
    }

    public void startSimulation(int delayMs) {
        new Thread(() -> {
            while (running) {
                boolean cont = step();
                if (!cont) break;
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    break;
                }
            }
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(panel, "Симуляция завершена!");
            });
        }).start();
    }

    public static void main(String[] args) {
        int width = 100;
        int height = 100;
        int numAgents = 30;

        int targetX = width - 2;
        int targetY = 1;

        double bI = 0.7;
        double bc = 1.5;
        int minAgentDist = 1;
        int maxSteps = 8000;   // путь длинный, с шагом 1 клетка нужно много шагов
        int delayMs = 100;     // медленная, спокойная анимация

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Рой агентов (сетка)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Simulation sim = new Simulation(width, height, numAgents, targetX, targetY,
                    bI, bc, minAgentDist, maxSteps, null);
            SimulationPanel panel = new SimulationPanel(sim);
            sim.setPanel(panel);

            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            System.out.println("Цель: (" + sim.getTargetX() + ", " + sim.getTargetY() + ")");

            sim.startSimulation(delayMs);
        });
    }
}
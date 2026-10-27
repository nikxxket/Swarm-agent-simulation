import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SimulationPanel extends JPanel {
    private Simulation sim;
    private static final int CELL_SIZE = 8;

    // Таймер плавной анимации
    private Timer animationTimer;
    private static final int FPS = 60;
    private static final double SMOOTHING = 0.25; // чем больше — тем быстрее догоняет

    public SimulationPanel(Simulation sim) {
        this.sim = sim;
        int width = sim.getWidth() * CELL_SIZE;
        int height = sim.getHeight() * CELL_SIZE;
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.WHITE);

        // Таймер обновляет визуальные позиции и перерисовывает панель
        animationTimer = new Timer(1000 / FPS, e -> {
            for (Agent a : sim.getAgents()) {
                a.updateVisual(SMOOTHING);
            }
            repaint();
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        // Включаем сглаживание линий/кругов — картинка станет аккуратнее
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = sim.getWidth();
        int h = sim.getHeight();

        // Сетка
        g2d.setColor(new Color(235, 235, 235));
        for (int i = 0; i <= w; i++) {
            g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, h * CELL_SIZE);
        }
        for (int i = 0; i <= h; i++) {
            g2d.drawLine(0, i * CELL_SIZE, w * CELL_SIZE, i * CELL_SIZE);
        }

        // Препятствия
        for (Obstacle ob : sim.getObstacles()) {
            int x = ob.x * CELL_SIZE;
            int y = ob.y * CELL_SIZE;
            int width = ob.width * CELL_SIZE;
            int height = ob.height * CELL_SIZE;
            g2d.setColor(Color.GRAY);
            g2d.fillRect(x, y, width, height);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRect(x, y, width, height);
        }

        // Цель
        int tx = sim.getTargetX() * CELL_SIZE + CELL_SIZE / 2;
        int ty = sim.getTargetY() * CELL_SIZE + CELL_SIZE / 2;
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        int size = CELL_SIZE / 2 + 3;
        g2d.drawLine(tx - size, ty - size, tx + size, ty + size);
        g2d.drawLine(tx + size, ty - size, tx - size, ty + size);
        g2d.setStroke(new BasicStroke(1));

        // Агенты — рисуем по ВИЗУАЛЬНЫМ координатам (плавно)
        for (Agent a : sim.getAgents()) {
            int ax = (int) Math.round(a.getVisualX() * CELL_SIZE + CELL_SIZE / 2);
            int ay = (int) Math.round(a.getVisualY() * CELL_SIZE + CELL_SIZE / 2);

            if (a.isDelivered()) {
                g2d.setColor(Color.GREEN);
            } else if (a.hasPart()) {
                g2d.setColor(new Color(30, 100, 220));
            } else {
                g2d.setColor(Color.ORANGE);
            }
            int radius = CELL_SIZE / 2 - 1;
            g2d.fillOval(ax - radius, ay - radius, 2 * radius, 2 * radius);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(ax - radius, ay - radius, 2 * radius, 2 * radius);
        }

        // Информация
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Доставлено: " + sim.countDelivered() + "/" + sim.getAgents().size(),
                5, 15);
        g2d.drawString("Шаг: " + sim.getCurrentStep(), 5, 30);
    }

    // Необязательно, но полезно: остановить таймер, когда окно закрывается
    public void stopAnimation() {
        if (animationTimer != null) animationTimer.stop();
    }
}
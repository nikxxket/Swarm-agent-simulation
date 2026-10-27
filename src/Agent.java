import java.util.Random;

public class Agent {
    public int x, y;
    public double visualX, visualY;
    public double dx, dy;
    public int bestX, bestY;
    public boolean hasPart;
    public boolean delivered;

    private static Random rand = new Random();

    public Agent(int x, int y) {
        this.x = x;
        this.y = y;
        this.visualX = x;
        this.visualY = y;
        this.dx = 0;
        this.dy = 0;
        this.bestX = x;
        this.bestY = y;
        this.hasPart = true;
        this.delivered = false;
    }

    public void updateVisual(double alpha) {
        visualX += (x - visualX) * alpha;
        visualY += (y - visualY) * alpha;
        if (Math.abs(visualX - x) < 0.01) visualX = x;
        if (Math.abs(visualY - y) < 0.01) visualY = y;
    }

    /**
     * Считает НОВУЮ СКОРОСТЬ (приращение), но не двигает агента.
     * Возвращает {newDx, newDy}.
     */
    public double[] computeVelocity(int targetX, int targetY, double bI, double bc,
                                    int bestNeighborX, int bestNeighborY) {
        double r1x = rand.nextDouble() * bc;
        double r1y = rand.nextDouble() * bc;
        double r2x = rand.nextDouble() * bc;
        double r2y = rand.nextDouble() * bc;

        double inertiaX = bI * dx;
        double inertiaY = bI * dy;

        double cognitiveX = r1x * (bestX - x);
        double cognitiveY = r1y * (bestY - y);

        double socialX = r2x * (bestNeighborX - x);
        double socialY = r2y * (bestNeighborY - y);

        double newDx = inertiaX + cognitiveX + socialX;
        double newDy = inertiaY + cognitiveY + socialY;

        return new double[] { newDx, newDy };
    }

    public void applyNewPosition(int newX, int newY, double newDx, double newDy) {
        this.x = newX;
        this.y = newY;
        this.dx = newDx;
        this.dy = newDy;
    }

    public void updateBest(int targetX, int targetY) {
        double currentDist = distanceTo(targetX, targetY);
        double bestDist = distanceTo(bestX, bestY, targetX, targetY);
        if (currentDist < bestDist) {
            bestX = x;
            bestY = y;
        }
    }

    public double distanceTo(int tx, int ty) {
        return Math.hypot(tx - x, ty - y);
    }

    public static double distanceTo(int x1, int y1, int x2, int y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    public boolean tryDeliver(int targetX, int targetY) {
        if (hasPart && !delivered && x == targetX && y == targetY) {
            delivered = true;
            hasPart = false;
            return true;
        }
        return false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public double getVisualX() { return visualX; }
    public double getVisualY() { return visualY; }
    public boolean isDelivered() { return delivered; }
    public boolean hasPart() { return hasPart; }
}
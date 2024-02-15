import java.util.concurrent.ThreadLocalRandom;

public class FlightController implements Runnable {
    private static Airport airport;

    public FlightController(Airport airport) {
        this.airport = airport;
    }

    @Override
    public void run() {
        while (true) {
            Plane nextPlane = airport.next();
            nextPlane.setHangar(ThreadLocalRandom.current().nextInt(1, 5));
            synchronized (nextPlane) {
                nextPlane.notify();
            }
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(50, 400));
            } catch (Exception e) {
                System.out.println("FlightController sleep hiba");
                e.printStackTrace();
            }
        }
    }
}

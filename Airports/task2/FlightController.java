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
            int hangarIndex = -1;
            while (hangarIndex == -1) {
                hangarIndex = airport.assignToHangar(nextPlane);
                if (hangarIndex == -1) {
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
                    } catch (Exception e) {
                        System.out.println("FlightController sleep hiba (assignToHangar)");
                        e.printStackTrace();
                    }
                }
            }
            nextPlane.setHangar(hangarIndex);
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

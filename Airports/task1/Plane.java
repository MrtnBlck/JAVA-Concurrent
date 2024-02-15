import java.util.concurrent.ThreadLocalRandom;

public class Plane implements Runnable {
    private static int passengerCount;
    private static Airport airport;
    private static int hangar = -1;

    public Plane(Airport airport) {
        passengerCount = ThreadLocalRandom.current().nextInt(50, 200);
        this.airport = airport;
    }

    @Override
    public void run() {
        airport.requestLanding(this);
        synchronized (this) {
            try {
                wait();
            } catch (Exception e) {
                System.out.println("plane wait() hiba");
                e.printStackTrace();
            }
        }
        System.out.println("Landing in " + hangar);

    }

    public synchronized void setHangar(int hangar) {
        Plane.hangar = hangar;
    }

}

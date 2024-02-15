import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Airport {
    private static BlockingQueue<Plane> planes;
    private static Plane[] hangars;

    public Airport(int N, int X) {
        planes = new ArrayBlockingQueue<>(N);
        hangars = new Plane[X];
    }

    public static void main(String[] args) {
        Airport airport = new Airport(100, 4);
        FlightController flightcontroller1 = new FlightController(airport);
        FlightController flightcontroller2 = new FlightController(airport);
        new Thread(flightcontroller1).start();
        new Thread(flightcontroller2).start();

        while (true) {
            try {
                Thread.sleep(750);
                Plane plane = new Plane(airport);
                new Thread(plane).start();
            } catch (Exception e) {
                System.out.println("main fv, vegtelen ciklus sleep hiba");
                e.printStackTrace();
            }

        }

    }

    public Plane next() {
        Plane firstPlane = null;
        try {
            firstPlane = planes.take();
        } catch (Exception e) {
            System.out.println("next() hiba");
            e.printStackTrace();
        }
        return firstPlane;
    }

    public void requestLanding(Plane plane) {
        try {
            planes.put(plane);
        } catch (Exception e) {
            System.out.println("requestLanding() hiba");
            e.printStackTrace();
        }
    }

    public synchronized int assignToHangar(Plane plane) {
        for (int i = 0; i < hangars.length; i++) {
            if (hangars[i] == null) {
                hangars[i] = plane;
                return i;
            }
        }
        return -1;
    }

    public void removePlaneFrom(Plane plane, int index) {
        if (plane == hangars[index]) {
            hangars[index] = null;
        } else {
            throw new IllegalArgumentException();
        }
    }
}

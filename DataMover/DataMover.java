import java.util.ArrayList;
import java.util.List;

public class DataMover {
    public static int[] data;
    public static List<Thread> movers = new ArrayList<>();

    public static void main(String[] args) {
        int waitTime;
        int threadCount;
        int[] threadWaitTimes;

        String[] noCLA = new String[] { "123", "111", "256", "404" };
        if (args.length < 1) {
            waitTime = Integer.parseInt(noCLA[0]);
            threadCount = noCLA.length - 1;
        } else {
            waitTime = Integer.parseInt(args[0]);
            threadCount = args.length - 1;
        }

        data = new int[threadCount];
        threadWaitTimes = new int[threadCount];
        Object[] locks = new Object[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threadWaitTimes[i] = Integer.parseInt(args.length > 1 ? args[i + 1] : noCLA[i + 1]);
            data[i] = i * 1000;
            locks[i] = new Object();
        }

        for (int i = 0; i < threadCount; i++) {

            int ind = i;
            int nextInd = (i + 1) % threadCount;

            Thread thread = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    try {
                        Thread.sleep(threadWaitTimes[ind]);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    synchronized (locks[ind > nextInd ? ind : nextInd]) {
                        data[ind] -= ind;
                        System.out.println("#" + ind + ": data " + ind + " == " + data[ind]);
                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        synchronized (locks[ind > nextInd ? nextInd : ind]) {
                            data[nextInd] += ind;
                            System.out.println("#" + ind + ": data " + (nextInd) + " -> " + data[nextInd]);
                        }
                    }
                }
            });

            movers.add(thread);
        }

        for (Thread t : movers) {
            t.start();
        }

        for (Thread t : movers) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.print("[");

        for (int i = 0; i < data.length; i++) {
            System.out.print(data[i] + (i == data.length - 1 ? "" : ", "));
        }

        System.out.println("]");

    }
}
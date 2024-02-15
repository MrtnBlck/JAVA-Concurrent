import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class DataMover2 {

  private static class DataMover2Result {
    public int count = 0, data = 0, forwarded = 0;
  }

  public static AtomicInteger arrivalCount = new AtomicInteger(0);
  public static AtomicInteger totalSent = new AtomicInteger(0);
  public static AtomicInteger totalArrived = new AtomicInteger(0);
  public static ExecutorService pool;
  public static List<BlockingQueue<Integer>> queues = new ArrayList<>();
  public static List<Future<DataMover2Result>> moverResults = new ArrayList<>();
  public static List<Integer> discards = new ArrayList<>();

  public static void main(String[] args) {

    int n;
    // check for command line arguments
    String[] noCLA = new String[] { "123", "111", "256", "404" };
    if (args.length < 1) {
      n = noCLA.length;
    } else {
      n = args.length;
    }
    // ----------------
    // set individual thread wait times
    int[] threadWaitTimes = new int[n];
    for (int i = 0; i < n; i++) {
      if (args.length < 1) {
        threadWaitTimes[i] = Integer.parseInt(noCLA[i]);
      } else {
        threadWaitTimes[i] = Integer.parseInt(args[i]);
      }
    }
    // ----------------
    // initialize queues
    for (int i = 0; i < n; i++) {
      queues.add(new LinkedBlockingQueue<>());
    }
    // ----------------
    // initialize thread pool
    pool = Executors.newFixedThreadPool(100);
    // ----------------
    // threads
    for (int i = 0; i < n; i++) {
      // calculate indexes
      int currentIndex = i;
      int nextIndex = (i + 1) % n;
      // ----------------
      // submit tasks
      moverResults.add(pool.submit(() -> {
        DataMover2Result result = new DataMover2Result();
        // calculations
        while (arrivalCount.get() < 5 * n) {
          try {
            // sends <x>
            // generate random X
            int x = (int) (Math.random() * 10001);
            // ----------------
            // put x in next queue
            queues.get(nextIndex).put(x);
            // ----------------
            // increment totalSent with x
            totalSent.getAndAdd(x);
            // ----------------
            System.out.printf("total   %d/%d | #%d sends %d %n", arrivalCount.get(), 5 * n, currentIndex, x);
            // ----------------
            Integer receivedValue;
            try {
              // try to get value from current queue with random timeout
              receivedValue = queues.get(currentIndex).poll(ThreadLocalRandom.current().nextInt(300, 1001),
                  TimeUnit.MILLISECONDS);
              // ----------------
            } catch (Exception e) {
              // got nothing...
              System.out.printf("total   %d/%d | #%d got nothing... %n", arrivalCount.get(), 5 * n, currentIndex);
              continue;
              // ----------------
            }
            if (receivedValue == null) {
              // got nothing...
              System.out.printf("total   %d/%d | #%d got nothing... %n", arrivalCount.get(), 5 * n, currentIndex);
              continue;
              // ----------------
            } else if (receivedValue % n == currentIndex) {
              // got <x>
              arrivalCount.getAndIncrement();
              result.count++;
              result.data += receivedValue;
              System.out.printf("total   %d/%d | #%d got %d %n", arrivalCount.get(), 5 * n, currentIndex,
                  receivedValue);
              // ----------------
            } else {
              // forwards <x>
              queues.get(nextIndex).put(receivedValue - 1);
              result.forwarded++;
              System.out.printf("total   %d/%d | #%d forwards %d [%d] %n", arrivalCount.get(), 5 * n, currentIndex,
                  x - 1, nextIndex);
              // ----------------
            }
            Thread.sleep(threadWaitTimes[currentIndex]);

          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        // ----------------

        return result;
      }));
    }
    pool.shutdown();
    try {
      pool.awaitTermination(30, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  
    for (Future<DataMover2Result> result : moverResults) {
      try {
        totalArrived.getAndAdd(result.get().data + result.get().forwarded);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    for (BlockingQueue<Integer> queue : queues) {
      int tmp = 0;
      while (!queue.isEmpty()) {
        tmp += queue.poll();
      }
      discards.add(tmp);
    }

    // discarded print
    int totalDiscarded = 0;
    System.out.print("discarded: [");
    for (int i = 0; i < discards.size(); i++) {
      System.out.print(discards.get(i));
      totalDiscarded += discards.get(i);
      if (i != discards.size() - 1) {
        System.out.print(", ");
      } else {
        System.out.printf("] = %d %n", totalDiscarded);
      }
    }
    // ----------------
    // final results print
    if (totalSent.get() == (totalArrived.get() + totalDiscarded)) {
      System.out.printf("sent %d === got %d = %d + discarded %d %n", totalSent.get(),
          (totalArrived.get() + totalDiscarded),
          totalArrived.get(), totalDiscarded);
    } else {
      System.out.printf("WRONG sent %d !== got %d = %d + discarded %d %n", totalSent.get(),
          (totalArrived.get() + totalDiscarded),
          totalArrived.get(), totalDiscarded);
    }

  }

}

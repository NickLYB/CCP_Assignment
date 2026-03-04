package concurrent.Threads;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author NICK
 */
public class RefuelingTruck implements Runnable {
    private static final int refuelTime = 2000;
    
    private final ReentrantLock truckLock = new ReentrantLock(true);
    
    // Hand-off variables
    private volatile Plane currentPlane = null;
    private volatile boolean isRefueling = false;
    private volatile boolean running = true;
    
    private int planesRefueled = 0;

    public RefuelingTruck(){}

    // Called by Plane Threads
    public void refuel(Plane plane) throws InterruptedException {
        truckLock.lockInterruptibly(); 
        try {
            // 1. Hand off the task
            this.currentPlane = plane;
            this.isRefueling = true;

            // 2. Wait for the Truck Thread to finish the work
            while (this.isRefueling) {
                Thread.sleep(50); 
            }
        } finally {
            truckLock.unlock(); 
        }
    }

    public void run() {
        System.out.println("RefuelingTruck: Online and waiting.");
        
        while (running) {
            if (isRefueling && currentPlane != null) {
                System.out.println("RefuelingTruck: Processing Plane-" + currentPlane.getPlaneId());
                
                try {
                    // Actual task duration
                    Thread.sleep(refuelTime); 
                } catch (InterruptedException e) {
                    System.out.println("RefuelingTruck: Interrupted.");
                }
                
                planesRefueled++;
                System.out.println("RefuelingTruck: Finished Plane-" + currentPlane.getPlaneId());
                
                // Reset for next plane
                currentPlane = null;
                isRefueling = false; 
            } else {
                try {
                    Thread.sleep(50); // Prevent CPU burning while idle
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    public void shutdown() {
        running = false;
        System.out.println("RefuelingTruck: Shutdown. Total planes: " + planesRefueled);
    }
}
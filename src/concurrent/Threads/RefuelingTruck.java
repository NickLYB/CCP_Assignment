/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.Threads;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author NICK
 */
public class RefuelingTruck implements Runnable{
    private static final int refuelTime = 2000;
    private final Queue<Plane> refuelQueue;
    private final Object truckLock = new Object();
    private volatile boolean running;
    
    //statistic
    private int planesRefueled;
        
    public RefuelingTruck(){
        this.refuelQueue = new LinkedList<>();
        this.running = true;
        this.planesRefueled = 0;
        
    }
public void run() {
        while (running) {
            Plane planeToRefuel = null;
            
            // 1. Lock only to safely check the queue and peek at the next plane
            synchronized (truckLock) {
                try {
                    while (refuelQueue.isEmpty() && running) {
                        truckLock.wait();
                    }
                    if (!running) break;
                    
                    if (!refuelQueue.isEmpty()) {
                        planeToRefuel = refuelQueue.peek(); // Just peek, keep it in queue so contains() works
                    }
                } catch (InterruptedException e) {
                    System.out.println("RefuelingTruck was interrupted.");
                    Thread.currentThread().interrupt(); // Restore interrupted status
                    break;
                }
            } // TRUCK LOCK IS RELEASED HERE!
            
            // 2. Perform the 2-second refueling OUTSIDE the lock
            // Now other planes can freely join the queue while this is happening!
            if (planeToRefuel != null) {
                try {
                    performRefueling(planeToRefuel);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                
                // 3. Re-lock briefly to remove the plane and notify waiting planes
                synchronized (truckLock) {
                    refuelQueue.poll(); 
                    truckLock.notifyAll(); // Wake up the specific plane waiting in refuel()
                }
            }
        }
    }
    
    public void refuel(Plane plane) throws InterruptedException {
        synchronized (truckLock) {
            refuelQueue.add(plane);
            System.out.println(Thread.currentThread().getName() + ": Added to refueling queue. Position: " + refuelQueue.size());
            truckLock.notifyAll(); // Wake up the truck if it's sleeping
            
            // Wait until the truck removes this plane from the queue
            while (refuelQueue.contains(plane)) {
                truckLock.wait();
            }
        }
    }
    
    private void performRefueling(Plane plane) throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + ": Refueling Plane-" + plane.getPlaneId());
        Thread.sleep(refuelTime);
        planesRefueled++;
        System.out.println(Thread.currentThread().getName() + ": Plane-" + plane.getPlaneId() + " refueling completed.");
    }
    
    public void shutdown() {
        synchronized (truckLock) {
            running = false;
            truckLock.notifyAll();
        }
        System.out.println("Thread-RefuelingTruck: Shutdown");
    }
    
}

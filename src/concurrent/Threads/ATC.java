/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.Threads;

import concurrent.Main;
import concurrent.SharedResources.*;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/**
 *
 * @author NICK
 * central monitor of airport
 * manages the flow of planes, assigns gates, and ensures runway safety
 */
public class ATC implements Runnable {
    
    //queueing list for landing and takeoff sequence
    private final LinkedList<Plane> landingQueue;
    private final LinkedList<Plane> takeoffQueue;
    
    //mutex lock for synchronize
    private final Object atcLock = new Object();
    //thread control flag
    private volatile boolean running;
    
    //remember who ATC already say denied to
    private int lastDeniedId = -1;
    
    //semaphore to limit the number of gates 
    private final Semaphore gateSlots = new Semaphore(3, true);
    
    //constructor
    public ATC(){
        this.landingQueue = new LinkedList<>();
        this.takeoffQueue = new LinkedList<>();
        this.running = true;
    }
    
    public void run() {
        while (running) {
            try {
                Plane landingPlane = null;
                Plane takeoffPlane = null;
                
                synchronized (atcLock) {
                    //wait until there is a plne to process or system shutdown
                    while (landingQueue.isEmpty() && takeoffQueue.isEmpty() && running) {
                        atcLock.wait(); 
                    }
                    if (!running) break;
                    
                    //landing queue
                    if (!landingQueue.isEmpty()) {
                        //priority logic
                        for (Plane p : landingQueue) {
                            //planes with fuel shortage has highest priority
                            if (p.hasFuelShortage()) {
                                landingPlane = p;
                                break;
                            }
                        }
                        if (landingPlane == null) {
                            landingPlane = landingQueue.getFirst();
                        }
                    }
                    //takeoff queue
                    if (!takeoffQueue.isEmpty()) {
                        takeoffPlane = takeoffQueue.getFirst();
                    }
                } 
                
                boolean workDone = false;
                
                //process landing
                if (landingPlane != null) {
                    //check all constraints
                    //airport capacity, gate capacity
                    if (!Main.airport.hasSpace() || gateSlots.availablePermits() == 0) {
                        if (landingPlane.getPlaneId() != lastDeniedId) {
                            System.out.println(Thread.currentThread().getName() + ": Landing Permission Denied for Plane-" + landingPlane.getPlaneId() + ". Airport/Gates Full.");
                            lastDeniedId = landingPlane.getPlaneId();
                        }
                    } 
                    // runway status
                    else if (!Main.runway.isAvailable()) {
                        if (landingPlane.getPlaneId() != lastDeniedId) {
                            System.out.println(Thread.currentThread().getName() + ": Landing Permission Denied for Plane-" + landingPlane.getPlaneId() + ". Runway Occupied.");
                            lastDeniedId = landingPlane.getPlaneId();
                        }
                    } 
                    //if both free, grant permission
                    else {
                        if (gateSlots.tryAcquire()) {
                            synchronized(atcLock) {
                                landingQueue.remove(landingPlane);
                            }
                            
                            Main.airport.planeEntered(); 
                            Gate gate = assignAvailableGate();
                            
                            if (gate != null) {
                                gate.reserve(landingPlane);
                                synchronized(landingPlane) {
                                    landingPlane.setAssignedGate(gate);
                                    System.out.println(Thread.currentThread().getName() + ": Landing Permission Granted for Plane-" + landingPlane.getPlaneId());
                                    landingPlane.notify(); //wake up plane thread
                                }
                                workDone = true;
                                lastDeniedId = -1;
                            } else {
                                //rollback if gate assignment fails
                                gateSlots.release();
                                Main.airport.planeLeft();
                            }
                        }
                    }
                }
                
                //process takeoff
                if (!workDone && takeoffPlane != null && Main.runway.isAvailable()) {
                    synchronized(atcLock) {
                        takeoffQueue.remove(takeoffPlane);
                    }
                    synchronized(takeoffPlane) {
                        takeoffPlane.setClearedForTakeoff(true);
                        System.out.println(Thread.currentThread().getName() + ": Takeoff Permission Granted for Plane-" + takeoffPlane.getPlaneId());
                        takeoffPlane.notify();
                    }
                    workDone = true;
                }
                
                //Idle Control
                if (!workDone) {
                    Thread.sleep(200); 
                } else {
                    Thread.sleep(50); 
                }
                
            } catch (InterruptedException e) {
                if (!running) break;
            }
        }
    }
    
    private Gate assignAvailableGate() {
        for (Gate gate : Main.gates) {
            if (gate.isAvailable()) {
                return gate;
            }
        }
        return null;
    } 
    
    //Plane call this and block until the ATC notifies them
    public Gate requestLandingPermission(Plane plane) throws InterruptedException {
        synchronized (atcLock) {
            landingQueue.add(plane);
            atcLock.notifyAll(); 
        } 
        synchronized (plane) {
            while (plane.getAssignedGate() == null) {
                plane.wait(); 
            }
            return plane.getAssignedGate();
        }
    }
    public void requestTakeOffPermission(Plane plane) throws InterruptedException {
        synchronized (atcLock) {
            takeoffQueue.add(plane);
            atcLock.notifyAll(); 
        } 
        synchronized (plane) {
            while (!plane.isClearedForTakeoff()) {
                plane.wait();
            }
        }
    }
    
    public void releaseGateSlot() {
        gateSlots.release(); 
    }
    
    public void shutdown() {
        running = false;
        synchronized(atcLock) {
            atcLock.notifyAll();
        }
        System.out.println("Thread-ATC: Shutdown");
    }
    
    //getter
    public int getWaitingQueueSize() {
        synchronized(atcLock) {
            return landingQueue.size();
        }
    }
    //condition check
    public boolean isRunning(){ 
        return running; 
    }
    

}

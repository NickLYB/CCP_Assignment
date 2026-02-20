/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.SharedResources;
import concurrent.Threads.Plane;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/**
 *
 * @author NICK
 */
public class Gate {
    private static int nextGateId = 1;
    private final int gateId;
    private Plane currentPlane;
    private boolean isReserved;
    private boolean isOccupied;
    
    // Cleaning handshakes
    private Plane planeToClean = null;
    private final Semaphore cleanSignal = new Semaphore(0);
    private final Semaphore cleanDoneSignal = new Semaphore(0);
    
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    
    public Gate(){
        this.gateId = nextGateId++;
        this.isReserved = false;
        this.isOccupied = false;
        this.currentPlane = null;
    }
    
    public void reserve(Plane plane){
        rwLock.writeLock().lock();
        try {
            this.currentPlane = plane;
            this.isReserved = true;
        } finally { rwLock.writeLock().unlock(); }
    }

    public void dock(Plane plane) {
        rwLock.writeLock().lock();
        try {
            isOccupied = true;
            isReserved = false;
            currentPlane = plane;
        } finally { rwLock.writeLock().unlock(); }
    }

    public void undock() {
        rwLock.writeLock().lock(); 
        try {
            isOccupied = false;
            currentPlane = null;
        } finally { 
            rwLock.writeLock().unlock(); 
        }
        concurrent.Main.atc.releaseGateSlot(); // Tell ATC the slot is open!
    }
    
    public void requestCleaning(Plane plane) {
        this.planeToClean = plane;
        cleanSignal.release(); // Wake up the cleaners
    }
    
    public void waitForCleaning() throws InterruptedException {
        cleanDoneSignal.acquire(); // Plane waits for cleaners to finish
    }

    public void awaitCleaning() throws InterruptedException {
        cleanSignal.acquire(); // Cleaners wait here for a plane
    }

    public void markCleaned() {
        this.planeToClean = null;
        cleanDoneSignal.release(); // Wake the plane back up
    }
    
    public Plane getPlaneToClean() { return planeToClean; }
    
    public void releaseForShutdown() {
        cleanSignal.release(); 
    }
    
    public boolean isAvailable(){
        rwLock.readLock().lock();
        try {
            return !isOccupied && !isReserved;
        } finally { rwLock.readLock().unlock(); }
    }

    public boolean isEmpty(){
        rwLock.readLock().lock();
        try {
            return !isOccupied && currentPlane == null;
        } finally { rwLock.readLock().unlock(); }
    }
    
    public int getGateId() { return gateId; }  
}
 
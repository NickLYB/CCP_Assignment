/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.SharedResources;
import concurrent.Threads.Plane;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
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
    
    //handshakes for cleaning operations
    private Plane planeToClean = null;
    private final Semaphore cleanSignal = new Semaphore(0);     //cleaner wait for plane
    private final Semaphore cleanDoneSignal = new Semaphore(0); //plane wait for cleaner
    
    //lock to ensures only one thread modifies gate status at a time
    private final ReentrantLock lock = new ReentrantLock(true);
    
    //constructor
    public Gate(){
        this.gateId = nextGateId++;
        this.isReserved = false;
        this.isOccupied = false;
        this.currentPlane = null;
    }
    
    public void reserve(Plane plane){
        lock.lock();
        try {
            this.currentPlane = plane;
            this.isReserved = true;
        } finally {
            lock.unlock();
        }
    }
    public void dock(Plane plane){
        lock.lock();
        try {
            isOccupied = true;
            isReserved = false;
            currentPlane = plane;
        } finally {
            lock.unlock();
        }
    }
    public void undock(){
        lock.lock(); 
        try {
            isOccupied = false;
            currentPlane = null;
        } finally { 
            lock.unlock(); 
        }
        concurrent.Main.atc.releaseGateSlot(); // Tell ATC the slot is open
    }
    
    //cleaning work
    public void requestCleaning(Plane plane){
        lock.lock();
        try {
            this.planeToClean = plane;
            cleanSignal.release(); //signal cleaner to start
        } finally {
            lock.unlock();
        }
    }
    public void waitForCleaning() throws InterruptedException{
        cleanDoneSignal.acquire(); //block plane until cleaned
    }
    public void awaitCleaning() throws InterruptedException{
        cleanSignal.acquire(); //cleaner wait here for a plane
    }
    public void markCleaned(){
        lock.lock();
        try {
            this.planeToClean = null;
            cleanDoneSignal.release();
        } finally {
            lock.unlock();
        }
    }
    
    public Plane getPlaneToClean(){ 
        lock.lock();
        try {
            return planeToClean;
        } finally {
            lock.unlock();
        }
    }
    
    public void releaseForShutdown(){
        cleanSignal.release(); //prevent cleaner from hanging on exit
    }
   
    //getter
    public int getGateId(){ 
        return gateId; 
    }     
    
    //condition checker
    public boolean isAvailable(){
        lock.lock();
        try {
            return !isOccupied && !isReserved;
        } finally { lock.unlock(); }
    }
    public boolean isEmpty(){
        lock.lock();
        try {
            return !isOccupied && currentPlane == null;
        } finally { lock.unlock(); }
    } 
    
}
 
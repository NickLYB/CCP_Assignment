/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.Threads;

import concurrent.Main;
import concurrent.SharedResources.*;
import java.util.LinkedList;

/**
 *
 * @author NICK
 */
public class ATC implements Runnable {
    private final LinkedList<Plane> landingQueue;
    private final LinkedList<Plane> takeoffQueue;
    
    private final Object atcLock = new Object();
    private volatile boolean running;
    
    private int lastDeniedId = -1;
    
    public ATC(){
        this.landingQueue = new LinkedList<>();
        this.takeoffQueue = new LinkedList<>();
        this.running = true;
    }
    
    public void run(){
        while(running){
            synchronized(atcLock){
                try{
                    while(landingQueue.isEmpty() && takeoffQueue.isEmpty()){
                        if (!running) return;
                        atcLock.wait();
                    }
                    
                    boolean workDone = false;

                    if(!landingQueue.isEmpty()){
                        Plane selectedPlane = null;
                        
                        // Search for emergency first
                        for(Plane p : landingQueue){
                            if(p.hasFuelShortage()){
                                selectedPlane = p;
                                break; 
                            }
                        }
                        
                        if(selectedPlane == null){
                            selectedPlane = landingQueue.peek();
                        }
                        
                        if(Main.airport.hasSpace() && !allGatesOccupied()&& Main.runway.isAvailable()) {
                            // ACT: Remove plane, assign gate
                            landingQueue.remove(selectedPlane);
                            Gate gate = assignAvailableGate();
                            
                            if(gate != null) {
                                Main.airport.planeEntered();
                                gate.reserve(selectedPlane);
                                
                                selectedPlane.setAssignedGate(gate);
                                
                                System.out.println(Thread.currentThread().getName() + ":Landing Permission GRANTED for Plane-" + selectedPlane.getPlaneId());
                                
                                atcLock.notifyAll(); 
                                workDone = true;
                                lastDeniedId = -1;
                            }
                        }
                    }
                    
                    if(!workDone && !takeoffQueue.isEmpty()) {
                        Plane candidate = takeoffQueue.getFirst();
                        
                        if(Main.runway.isAvailable()) {
                            takeoffQueue.removeFirst();
                            
                            // THE REPLY
                            candidate.setClearedForTakeoff(true);
                            
                            System.out.println(Thread.currentThread().getName() + ":Takeoff Permission GRANTED for Plane-" + candidate.getPlaneId());
                            
                            atcLock.notifyAll();
                            workDone = true;
                        }
                    }
                    if(!workDone) {
                        if(!landingQueue.isEmpty()){
                            Plane newest = landingQueue.getLast();

                            // Check why we failed
                            if (!Main.airport.hasSpace() || allGatesOccupied()) {
                                 if(newest.getPlaneId() != lastDeniedId){
                                     System.out.println(Thread.currentThread().getName() + ":Landing Permission Denied for Plane-" + newest.getPlaneId() + ". Airport Full.");
                                     lastDeniedId = newest.getPlaneId();
                                 }
                            } 
                            // FIX 3: Add explicit message for Landing Denied due to RUNWAY
                            else if (!Main.runway.isAvailable()) {
                                 if(newest.getPlaneId() != lastDeniedId){
                                     System.out.println(Thread.currentThread().getName() + ":Landing Permission Denied for Plane-" + newest.getPlaneId() + ". Runway Occupied.");
                                     lastDeniedId = newest.getPlaneId();
                                 }
                            }
                        }
                        else if(!takeoffQueue.isEmpty() && !Main.runway.isAvailable()) {
                             Plane newest = takeoffQueue.getLast();
                             System.out.println(Thread.currentThread().getName() + ":Takeoff Permission Denied for Plane-" + newest.getPlaneId() + ". Runway Occupied.");
                        }
                        atcLock.wait(1000);
                    }
                    else {
                        atcLock.wait(100); 
                    }
                } catch (InterruptedException e) {
                    if (!running) break;
                }
            }
        }
    }
    
    public Gate requestLandingPermission(Plane plane) throws InterruptedException{
        synchronized(atcLock){
            landingQueue.add(plane);
            atcLock.notifyAll(); 

            while(plane.getAssignedGate() == null){
                atcLock.wait();
            }
            return plane.getAssignedGate();
        }
        
    }
    
    public void requestTakeOffPermission(Plane plane) throws InterruptedException{
        synchronized(atcLock){
            takeoffQueue.add(plane);
            atcLock.notifyAll(); 

            while(!plane.isClearedForTakeoff()){
                atcLock.wait();
            }
        }
    }
    
    public void shutdown(){
        synchronized(atcLock){
            running = false;
            atcLock.notifyAll();
        }
        System.out.println("Thread-ATC: Shutdown");
    }
    
    private Gate assignAvailableGate(){
        for(Gate gate: Main.gates){
            if(gate.isAvailable()){
                return gate;
            }
        }
        return null;
    }
    private boolean allGatesOccupied() {
        for(Gate gate : Main.gates) {
            if(gate.isAvailable()) return false;
        }
        return true;
    }
    
    public int getWaitingQueueSize() {
        synchronized (atcLock) {
            return landingQueue.size();
        }
    }
    
    public boolean isRunning() {
        return running;
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.Threads;

import concurrent.Main;
import concurrent.SharedResources.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author NICK
 */
public class ATC implements Runnable {
    private final Queue<Plane> landingQueue;
    private final Queue<Plane> takeoffQueue;
    
    private final Object atcLock = new Object();
    private volatile boolean running;
    
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
                    
                    boolean actionTaken = false;

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
                        
                        if(Main.airport.hasSpace() && !allGatesOccupied()){
                             landingQueue.remove(selectedPlane);
                             
                             Gate gate = assignAvailableGate();
                             
                             if(gate != null){
                                 Main.airport.planeEntered();
                                 gate.reserve(selectedPlane);
                                 selectedPlane.setAssignedGate(gate);
                                 
                                 System.out.println(Thread.currentThread().getName() + ": Landing Permission GRANTED for Plane-" + selectedPlane.getPlaneId());
                                 atcLock.notifyAll(); 
                                 actionTaken = true;
                             }
                        } 
                    }
                    
                    if(!actionTaken && !takeoffQueue.isEmpty()){
                        Plane plane = takeoffQueue.peek();

                        if(Main.runway.isAvailable()){
                            takeoffQueue.poll();
                            plane.setClearedForTakeoff(true);
                            
                            System.out.println(Thread.currentThread().getName() + ": Takeoff Permission GRANTED for Plane-" + plane.getPlaneId());
                            atcLock.notifyAll(); 
                            actionTaken = true;
                        }
                    }

                    if(!actionTaken){
                        if(!landingQueue.isEmpty() && (!Main.airport.hasSpace() || allGatesOccupied())){
                             Plane p = landingQueue.peek(); 
                             System.out.println(Thread.currentThread().getName() + ": Landing Permission Denied for Plane-" + p.getPlaneId() + ", Airport Full");
                        }
                        else if(!takeoffQueue.isEmpty() && !Main.runway.isAvailable()){
                             Plane p = takeoffQueue.peek();
                             System.out.println(Thread.currentThread().getName() + ": Takeoff Permission Denied for Plane-" + p.getPlaneId() + ", Runway Occupied");
                        }
                        
                        atcLock.wait(1000);
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

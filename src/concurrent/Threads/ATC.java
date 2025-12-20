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
 * central monitor of airport
 * manages the flow of planes, assigns gates, and ensures runway safety
 */
public class ATC implements Runnable {
    
    //queueing list for landing and takeoff sequence
    private final LinkedList<Plane> landingQueue;
    private final LinkedList<Plane> takeoffQueue;
    
    //mutex lock
    private final Object atcLock = new Object();
    
    //thread control flag
    private volatile boolean running;
    
    //remember who ATC already say denied to
    private int lastDeniedId = -1;
    
    //constructor
    public ATC(){
        this.landingQueue = new LinkedList<>();
        this.takeoffQueue = new LinkedList<>();
        this.running = true;
    }
    
    public void run(){
        //keep running until shutdown() method is triggered
        while(running){
            synchronized(atcLock){
                try{
                    //1. wait if nothing is happening
                    while(landingQueue.isEmpty() && takeoffQueue.isEmpty()){
                        if (!running) return;
                        atcLock.wait(); //release lock and sleep until plane request something
                    }
                    
                    boolean workDone = false;
                    
                    //2. check landing
                    if(!landingQueue.isEmpty()){
                        Plane selectedPlane = null;
                        
                        // Search for emergency/fuel shortage plane first
                        for(Plane p : landingQueue){
                            if(p.hasFuelShortage()){
                                selectedPlane = p;
                                break; 
                            }
                        }
                        
                        //if no emergency plane, ge the first in queue
                        if(selectedPlane == null) selectedPlane = landingQueue.peek();
                        
                        //try lo land
                        //check if airport doesnt hit max capacity,
                        //have unoccupied gate,
                        //and runway was clear.
                        if(Main.airport.hasSpace() && !allGatesOccupied()&& Main.runway.isAvailable()) {
                            // remove the plane from queue and assign gate
                            landingQueue.remove(selectedPlane);
                            Gate gate = assignAvailableGate();
                            
                            //ensure gate is properlly assigned
                            if(gate != null) {
                                Main.airport.planeEntered(); //update airport capacity
                                gate.reserve(selectedPlane); //update gate current reserved plane
                                selectedPlane.setAssignedGate(gate); //tell the plane assigned gate
                                
                                //print log
                                System.out.println(Thread.currentThread().getName() + ":Landing Permission Granted for Plane-" + selectedPlane.getPlaneId());
                                
                                atcLock.notifyAll(); // wake up plane thread
                                workDone = true; // flag that landing permission gaved with properly assigned gate
                                lastDeniedId = -1; //reset memory because successfully did something
                            }
                        }
                    }
                    
                    //3. takeoff
                    if(!workDone && !takeoffQueue.isEmpty()) {
                        Plane selectedPlane = takeoffQueue.getFirst();
                        
                        //check runway
                        if(Main.runway.isAvailable()) {
                            takeoffQueue.removeFirst(); //remove plane from take off queue
                            selectedPlane.setClearedForTakeoff(true); //grant plane to take off
                            
                            //print log
                            System.out.println(Thread.currentThread().getName() + ":Takeoff Permission Granted for Plane-" + selectedPlane.getPlaneId());
                            
                            atcLock.notifyAll();//wake up plane thread
                            workDone = true; //flag that takeoff permission granted and properly execute
                        }
                    }
                    
                    //4. report denial, airport full
                    if(!workDone) {
                        //A. landing denial
                        if(!landingQueue.isEmpty()){
                            Plane newest = landingQueue.getLast();
                            //airport full
                            if (!Main.airport.hasSpace() || allGatesOccupied()) {
                                 if(newest.getPlaneId() != lastDeniedId){
                                     System.out.println(Thread.currentThread().getName() + ":Landing Permission Denied for Plane-" + newest.getPlaneId() + ". Airport Full.");
                                     lastDeniedId = newest.getPlaneId();
                                 }
                            }
                            //runway occupied
                            else if (!Main.runway.isAvailable()) {
                                 if(newest.getPlaneId() != lastDeniedId){
                                     System.out.println(Thread.currentThread().getName() + ":Landing Permission Denied for Plane-" + newest.getPlaneId() + ". Runway Occupied.");
                                     lastDeniedId = newest.getPlaneId();
                                 }
                            }
                        }
                        //B. take off denial
                        else if(!takeoffQueue.isEmpty() && !Main.runway.isAvailable()) {
                             Plane newest = takeoffQueue.getLast();
                             System.out.println(Thread.currentThread().getName() + ":Takeoff Permission Denied for Plane-" + newest.getPlaneId() + ". Runway Occupied.");
                        }
                        // sleep for 1 sec to avoid instant checking
                        atcLock.wait(1000);
                    }
                    else {
                        //small pauce to alow the granted plane to wake up and occupy resources
                        atcLock.wait(100); 
                    }
                } catch (InterruptedException e) {
                    if (!running) break;
                }
            }
        }
    }
    
    //helper
    private Gate assignAvailableGate(){
        for(Gate gate: Main.gates){
            if(gate.isAvailable()){
                return gate;
            }
        }
        return null;
    } //look for unoccupied gate
    private boolean allGatesOccupied() {
        for(Gate gate : Main.gates) {
            if(gate.isAvailable()) return false;
        }
        return true;
    } // state for gate
    
    //shared methods
    public Gate requestLandingPermission(Plane plane) throws InterruptedException{
        synchronized(atcLock){
            landingQueue.add(plane); //add plane to queue
            atcLock.notifyAll(); //

            while(plane.getAssignedGate() == null){
                atcLock.wait(); //
            }
            return plane.getAssignedGate();
        }
        
    } //plane use to request for landing permission, and is permission granted, return the asigned gate
    public void requestTakeOffPermission(Plane plane) throws InterruptedException{
        synchronized(atcLock){
            takeoffQueue.add(plane); //add plane to queue
            atcLock.notifyAll(); //

            while(!plane.isClearedForTakeoff()){
                atcLock.wait(); //
            }
        }
    } //plane use to request takeoff
    public void shutdown(){
        synchronized(atcLock){
            running = false; //
            atcLock.notifyAll(); //
        }
        System.out.println("Thread-ATC: Shutdown");
    } //terminate ATC thread
    
    //getter
    public int getWaitingQueueSize() {
        synchronized (atcLock) {
            return landingQueue.size();
        }
    }
    
    //flag
    public boolean isRunning() {
        return running;
    }
}

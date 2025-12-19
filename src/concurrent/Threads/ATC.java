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
    private final Queue<Plane> waitingQueue;
    private final Object atcLock = new Object();
    private volatile boolean running;
    
    public ATC(){
        this.waitingQueue = new LinkedList<>();
        this.running = true;
    }
    
    public void run(){
        while(running){
            synchronized(atcLock){
                try{
                    if (!waitingQueue.isEmpty() && Main.airport.hasSpace()) {
                        Plane plane = waitingQueue.peek();
                    }
                    atcLock.wait(100);
                } catch (InterruptedException e) {
                    if (!running) {
                        break;
                    }
                }
            }
        }
    }
    
    public Gate requestLandingPermission(Plane plane) throws InterruptedException{
        synchronized(atcLock){
            if(plane.hasFuelShortage()){
                System.out.println(Thread.currentThread().getName() + ": fuel shortage" + plane.getPlaneId());
                
            }
            
            while(!Main.airport.hasSpace()){
                System.out.println(Thread.currentThread().getName() + ":Airport Full.");
                
                if(!waitingQueue.contains(plane)){
                    waitingQueue.add(plane);
                }
                atcLock.wait();
                
                if(plane.hasFuelShortage() && Main.airport.hasSpace()){
                    waitingQueue.remove(plane);
                    break;
                }
                
                if(waitingQueue.peek() == plane && Main.airport.hasSpace()){
                    waitingQueue.poll();
                    break;
                }
            }
            waitingQueue.remove(plane);
            Main.airport.planeEntered();
            
            Gate assignedGate = assignAvailableGate();
            
            if(assignedGate != null){
                assignedGate.reserve(plane);
            }
            
            atcLock.notifyAll();
            
            return assignedGate;
        }
    }
    
    public void requestTakeOffPermission(Plane plane) throws InterruptedException{
        synchronized(atcLock){
            atcLock.notifyAll();
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
    
    public int getWaitingQueueSize() {
        synchronized (atcLock) {
            return waitingQueue.size();
        }
    }
    
    public boolean isRunning() {
        return running;
    }
}

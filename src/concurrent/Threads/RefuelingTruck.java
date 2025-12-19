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
    public void run(){
        
        while(running){
            synchronized(truckLock){
                try{
                    while(refuelQueue.isEmpty() && running){
                        truckLock.wait();
                    }
                    if(!running) break;
                    
                    if(!refuelQueue.isEmpty()){
                        Plane plane = refuelQueue.peek();
                        performRefueling(plane);
                        refuelQueue.poll();
                        truckLock.notifyAll();
                    }
                } catch(InterruptedException e){
                    
                }
            }
        }
    }
    
    public void refuel(Plane plane) throws InterruptedException{
        synchronized(truckLock){
            refuelQueue.add(plane);
            System.out.println(Thread.currentThread().getName() + ":Added to refuelling queue. Position:" + refuelQueue.size());
            truckLock.notifyAll();
            
            while(refuelQueue.contains(plane)){
                truckLock.wait();
            }
        }
    }
    private void performRefueling(Plane plane) throws InterruptedException{
        System.out.println(Thread.currentThread().getName() + ":Refueling Plane-" + plane.getPlaneId());
        Thread.sleep(refuelTime);
        planesRefueled++;
        System.out.println(Thread.currentThread().getName() + ":Plane-" + plane.getPlaneId() + " refueling completed.");
    }
    public void shutdown(){
        synchronized (truckLock) {
            running = false;
            truckLock.notifyAll();
        }
    }
    
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.SharedResources;
import concurrent.Threads.Plane;

/**
 *
 * @author NICK
 */
public class Gate {
    
    //gate id auto increment
    private static int nextGateId = 1;
    private final int gateId;
    
    private boolean isOccupied;
    private Plane currentPlane;
    
    private final Object gateLock = new Object();
    
    public Gate(){
        this.gateId = nextGateId++;
        this.isOccupied = false;
        this.currentPlane = null;
    }
    
    public void reserve(Plane plane){
        synchronized(gateLock){
            this.currentPlane = plane;
        }
    }
    
    public void dock(Plane plane) throws InterruptedException{
        synchronized(gateLock){
            while(isOccupied){
                gateLock.wait();
            }
            isOccupied = true;
            currentPlane = plane;
        }
    }
    
    public void undock(){
        synchronized(gateLock){
            isOccupied = false;
            currentPlane = null;
            gateLock.notifyAll();
        }
    }
    
    public boolean isAvailable(){
        synchronized(gateLock){
            return !isOccupied;
        }
    }
    
    public boolean isEmpty(){
        synchronized(gateLock){
            return !isOccupied && currentPlane == null;
        }
    }
    
    public int getGateId(){
        return gateId;
    }
    
    public Plane getCurrentPlane(){
        synchronized(gateLock){
            return currentPlane;
        }
    }
    
}
 
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
public class Runway {
    private boolean isOccupied;
    private Plane currentPlane;
    
    //mutex lock
    private final Object runwayLock = new Object();
    
    //constructor
    public Runway(){
        this.isOccupied = false;
        this.currentPlane = null;
    }
    
    //shared methods
    public void occupy(Plane plane) throws InterruptedException{
        synchronized(runwayLock){
            while (isOccupied){
                runwayLock.wait();
            }
            isOccupied = true;
            currentPlane = plane;
        }
    }
    public void release(){
        synchronized(runwayLock){
            isOccupied = false;
            currentPlane = null;
            runwayLock.notifyAll();
        }
    }
    
    //flag
    public boolean isAvailable(){
        synchronized(runwayLock){
            return !isOccupied;
        }
    }
    
    //getter
    public Plane getCurrentPlane(){
        synchronized(runwayLock){
            return currentPlane;
        }
    }
}

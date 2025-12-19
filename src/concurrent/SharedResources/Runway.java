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
    private final Object runwayLock = new Object();
    
    //initialize runway with no plane.
    public Runway(){
        this.isOccupied = false;
        this.currentPlane = null;
    }
    
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
    
    public boolean isAvailable(){
        synchronized(runwayLock){
            return !isOccupied;
        }
    }
    
    public Plane getCurrentPlane(){
        synchronized(runwayLock){
            return currentPlane;
        }
    }
}

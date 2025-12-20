/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.SharedResources;

/**
 *
 * @author NICK
 * not same with ATC
 * track the number of plane in airport only
 * Will trigger this class first before runway/gate
 */
public class Airport {
    
    private static final int maxCapacity = 3;
    private int currentOccupancy;
    
    //mutex lock
    private final Object airportLock = new Object();
    
    //constructor
    public Airport(){
        this.currentOccupancy = 0;
    }
    
    //shared methods
    public void planeEntered(){
        synchronized(airportLock){
            currentOccupancy++;
        }
    }
    public void planeLeft(){
        synchronized(airportLock){
            currentOccupancy--;
            airportLock.notifyAll(); //notify ATC/Plane that space has opened up
        }
    }
    
    //getter
    public int getCurrentOccupancy(){
        return currentOccupancy;
    }
    public int getMaxCapacity() {
        return maxCapacity;
    }
    
    //flag
    public boolean hasSpace(){
        synchronized(airportLock){
            return currentOccupancy < maxCapacity;
        }
    }
    public boolean isFull(){
        synchronized(airportLock){
            return currentOccupancy >= maxCapacity;
        }
    }
    public boolean isEmpty(){
        synchronized(airportLock) {
            return currentOccupancy == 0;
        }
    }
}

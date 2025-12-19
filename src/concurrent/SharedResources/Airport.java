/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.SharedResources;

/**
 *
 * @author NICK
 */
public class Airport {
    
    private static final int maxCapacity = 3;
    private int currentOccupancy;
    
    private final Object airportLock = new Object();
    
    public Airport(){
        this.currentOccupancy = 0;
    }
    
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
    
    public void planeEntered(){
        synchronized(airportLock){
            currentOccupancy++;
        }
    }
    
    public void planeLeft(){
        synchronized(airportLock){
            currentOccupancy--;
            airportLock.notifyAll();
        }
    }
    
    public int getCurrentOccupancy(){
        return currentOccupancy;
    }
    
    public void waitForSpace() throws InterruptedException{
        synchronized(airportLock){
            while(currentOccupancy >= maxCapacity){
                System.out.println("Airport: Full, waiting for space.");
                airportLock.wait();
            }
        }
    }
    public int getMaxCapacity() {
        return maxCapacity;
    }
}

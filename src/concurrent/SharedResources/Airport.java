/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.SharedResources;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author NICK
 * not same with ATC
 * track the number of plane in airport only
 * Will trigger this class first before runway/gate
 */

public class Airport {

    //maximum number of planes allowed in airport ground
    private static final int maxCapacity = 3;

    //semaphore controls real capacity
    private final Semaphore capacity = new Semaphore(maxCapacity, true);

    //thread safe counter to track the current plane count in airport
    private final AtomicInteger currentOccupancy = new AtomicInteger(0);

    // ATC calls this when granting landing clearance
    public void planeEntered() throws InterruptedException {
        capacity.acquire();              // block if full
        currentOccupancy.incrementAndGet();
    }
    // Plane calls this when fully leaving airport
    public void planeLeft() {
        currentOccupancy.decrementAndGet(); //release the permit to wake up waiting threads
        capacity.release();
    }

    //getter
    public int getCurrentOccupancy() {
        return currentOccupancy.get();
    }
    public int getMaxCapacity() {
        return maxCapacity;
    }

    //condition checker
    public boolean hasSpace() {
        return capacity.availablePermits() > 0;
    }
    public boolean isFull() {
        return capacity.availablePermits() == 0;
    }
}

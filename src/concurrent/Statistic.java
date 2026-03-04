package concurrent;

import concurrent.Threads.*;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author NICK
 */
public class Statistic {
    private final List<Long> waitingTimes;
    
    // Using AtomicInteger for thread-safe incrementing without locks!
    private final AtomicInteger totalPlanesServed;
    private final AtomicInteger totalPassengersBoarded;
    
    private final Object statsLock = new Object();
    
    public Statistic(){
        this.waitingTimes = new ArrayList<>();
        this.totalPlanesServed = new AtomicInteger(0);
        this.totalPassengersBoarded = new AtomicInteger(0);
    }
    
    public void recordPlane(Plane plane){
        // Only lock the list, AtomicIntegers handle themselves safely
        synchronized(statsLock){
            long waitTime = plane.getWaitingTime();
            waitingTimes.add(waitTime);
        }
        
        totalPlanesServed.incrementAndGet();
        totalPassengersBoarded.addAndGet(plane.getPassengerCount());
    }
    
    public void printStatistic(){
        synchronized(statsLock){
            System.out.println("SIMULATION STATISTICS");
            
            if(waitingTimes.isEmpty()){
                System.out.println("No data collected");
                return; 
            }
            
            long maxWaitTime = Collections.max(waitingTimes);
            long minWaitTime = Collections.min(waitingTimes);
            double avgWaitTime = waitingTimes.stream().mapToLong(Long::longValue).average().orElse(0);
            
            System.out.println("Waiting Time Statistic:");
            System.out.println("Max:" + maxWaitTime);
            System.out.println("Min:" + minWaitTime);
            System.out.println("Avg:" + String.format("%.1f", avgWaitTime));
            
            System.out.println("Service Statistics:");
            System.out.println("Total planes served: " + totalPlanesServed.get());
            System.out.println("Total passengers boarded: " + totalPassengersBoarded.get());
            
            if (totalPlanesServed.get() > 0) {
                double avgPassengers = (double) totalPassengersBoarded.get() / totalPlanesServed.get();
                System.out.println("Average passengers per plane: " + String.format("%.1f", avgPassengers));
            }
        }
    }
    
    public void reset() {
        synchronized (statsLock) {
            waitingTimes.clear();
        }
        totalPlanesServed.set(0);
        totalPassengersBoarded.set(0);
        System.out.println("Statistics: Reset to initial state.");
    }
    
    public int getTotalPlanesServed() { return totalPlanesServed.get(); }
    public int getTotalPassengersBoarded() { return totalPassengersBoarded.get(); }
    
    public long getMaxWaitingTime() {
        synchronized (statsLock) { return waitingTimes.isEmpty() ? 0 : Collections.max(waitingTimes); }
    }
    public long getMinWaitingTime() {
        synchronized (statsLock) { return waitingTimes.isEmpty() ? 0 : Collections.min(waitingTimes); }
    }
    public double getAverageWaitingTime() {
        synchronized (statsLock) { return waitingTimes.isEmpty() ? 0.0 : waitingTimes.stream().mapToLong(Long::longValue).average().orElse(0.0); }
    }
}
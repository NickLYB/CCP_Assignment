/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent;

import concurrent.Threads.*;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 *
 * @author NICK
 */
public class Statistic {
    private final List<Long> waitingTimes;
    private int totalPlanesServed;
    private int totalPassengersBoarded;
    private final Object statsLock = new Object();
    
    public Statistic(){
        this.waitingTimes = new ArrayList<>();
        this.totalPlanesServed = 0;
        this.totalPassengersBoarded = 0;
    }
    
    public void recordPlane(Plane plane){
        synchronized(statsLock){
            //waiting time
            long waitTime = plane.getWaitingTime();
            waitingTimes.add(waitTime);
            
            //passenger count
            totalPlanesServed++;
            totalPassengersBoarded += plane.getPassengerCount();
            
        }
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
            System.out.println("Total planes served: " + totalPlanesServed);
            System.out.println("Total passengers boarded: " + totalPassengersBoarded);
            
            if (totalPlanesServed > 0) {
                double avgPassengers = (double) totalPassengersBoarded / totalPlanesServed;
                System.out.println("Average passengers per plane: " + String.format("%.1f", avgPassengers));
            }
        }
    }
    
    public void reset() {
        synchronized (statsLock) {
            waitingTimes.clear();
            totalPlanesServed = 0;
            totalPassengersBoarded = 0;
            System.out.println("Statistics: Reset to initial state.");
        }
    }
    
    public int getTotalPlanesServed() {
        synchronized (statsLock) {
            return totalPlanesServed;
        }
    }
    
    public int getTotalPassengersBoarded() {
        synchronized (statsLock) {
            return totalPassengersBoarded;
        }
    }
    
    public long getMaxWaitingTime() {
        synchronized (statsLock) {
            if (waitingTimes.isEmpty()) {
                return 0;
            }
            return Collections.max(waitingTimes);
        }
    }
    public long getMinWaitingTime() {
        synchronized (statsLock) {
            if (waitingTimes.isEmpty()) {
                return 0;
            }
            return Collections.min(waitingTimes);
        }
    }
    
    public double getAverageWaitingTime() {
        synchronized (statsLock) {
            if (waitingTimes.isEmpty()) {
                return 0.0;
            }
            return waitingTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.Threads;
import concurrent.SharedResources.Gate;
/**
 *
 * @author NICK
 */
public class Cleaning implements Runnable {
    
    private final Gate assignedGate;
    private volatile boolean running;
    
    public Cleaning(Gate gate) {
        this.assignedGate = gate;
        this.running = true;
    }
    
    @Override
    public void run() {
        while (running) {
            try {
                //thread safely blocks here until the Plane calls requestCleaning()
                assignedGate.awaitCleaning();
                
                if (!running) break; 
                
                Plane targetPlane = assignedGate.getPlaneToClean();
                
                System.out.println("CleaningTeam-" + assignedGate.getGateId() + ": Started cleaning Plane-" + targetPlane.getPlaneId());
                Thread.sleep(2000); // Simulate cleaning time
                System.out.println("CleaningTeam-" + assignedGate.getGateId() + ": Finished cleaning Plane-" + targetPlane.getPlaneId());
                
                //tell the plane it is clean and ready for boarding
                assignedGate.markCleaned();
                
            } catch (InterruptedException e) {
                System.out.println("CleaningTeam-" + assignedGate.getGateId() + " was interrupted.");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public void shutdown() {
        running = false;
        assignedGate.releaseForShutdown(); 
        System.out.println("Thread-CleaningTeam-" + assignedGate.getGateId() + ": Shutdown");
    }
}

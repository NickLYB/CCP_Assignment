/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.Tester;
import concurrent.SharedResources.Gate;

/**
 *
 * @author NICK
 */
public class GateTest {
    public static void main(String[] args){
        Gate gate1 = new Gate();
        Gate gate2 = new Gate();
        Gate[] gates = {gate1, gate2};
        
        System.out.println("===== GATE TEST STARTING =====\n");
        
        // Create 4 test planes competing for 2 gates
        for (int i = 1; i <= 4; i++) {
            final int planeId = i;
            
            Thread testPlane = new Thread(() -> {
                try {
                    // Find an available gate
                    Gate assignedGate = null;
                    for (Gate gate : gates) {
                        if (gate.isAvailable()) {
                            assignedGate = gate;
                            gate.reserve(null); // Reserve it
                            break;
                        }
                    }
                    
                    if (assignedGate == null) {
                        // No gate available immediately, pick first one and wait
                        assignedGate = gate1;
                    }
                    
                    System.out.println("Plane-" + planeId + 
                                     ": Assigned to Gate-" + assignedGate.getGateId());
                    
                    // Try to dock at assigned gate
                    System.out.println("Plane-" + planeId + 
                                     ": Requesting to dock at Gate-" + 
                                     assignedGate.getGateId());
                    assignedGate.dock(null);  // Will wait if occupied
                    
                    System.out.println("Plane-" + planeId + 
                                     ": Docked at Gate-" + assignedGate.getGateId());
                    
                    // Simulate gate operations (3 seconds)
                    Thread.sleep(3000);
                    
                    System.out.println("Plane-" + planeId + 
                                     ": Leaving Gate-" + assignedGate.getGateId());
                    assignedGate.undock();
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Plane-" + i);
            
            testPlane.start();
            
            // Stagger plane arrivals
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Wait a bit, then check if gates are empty
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("\n===== SANITY CHECKS =====");
        System.out.println("Gate-1 is empty: " + gate1.isEmpty());
        System.out.println("Gate-2 is empty: " + gate2.isEmpty());
    }
}

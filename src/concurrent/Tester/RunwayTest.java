/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package concurrent.Tester;
import concurrent.SharedResources.Runway;

/**
 *
 * @author NICK
 */
public class RunwayTest {
    public static void main(String[] args) {
        Runway runway = new Runway();
        
        for(int i = 0; i <= 3; i++){
            final int planeId = i;
            
            Thread testPlane = new Thread(() -> {
                try {
                    System.out.println("Plane-" + planeId + ": Requesting runway");
                    
                    // Try to occupy runway
                    runway.occupy(null);  // Pass null for now (we don't have Plane class yet)
                    
                    System.out.println("Plane-" + planeId + ": Using runway");
                    //QThread.sleep(2000);  // Simulate using runway for 2 seconds
                    
                    System.out.println("Plane-" + planeId + ": Leaving runway");
                    runway.release();
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            
            testPlane.start();
        }
    }
}

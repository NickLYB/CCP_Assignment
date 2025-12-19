/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package concurrent;

import concurrent.SharedResources.*;
import concurrent.Threads.*;

/**
 *
 * @author NICK
 */



public class Main {
    /**
     * @param args the command line arguments
     */
    public static Runway runway;
    public static Airport airport;
    public static ATC atc;
    public static RefuelingTruck refuelingTruck;
    public static Gate[] gates;
    
    public static void main(String[] args) {
        // TODO code application logic here
        
        //setup shared resources
        runway = new Runway();
        airport = new Airport();
        gates = new Gate[3];
        for(int i = 0; i < 3; i++){
            gates[i] = new Gate();
        }
        
        
    }
    
}

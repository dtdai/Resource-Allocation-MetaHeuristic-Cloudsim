package section;

/**
 *
 * @author TrongDai
 */
public class Constants {

    public static int numHost = 4; // Amount Physical Machine - Host
    public static int numVm = 10; // Amount Virtual Machine - Task
    
    public static enum Type {
        ACO, PSO, SA, ACOSA, PSOSA
    }

    // <editor-fold desc="Algorithm Constants">
    
    public static int aco_numAnts = 100; // ACO - Amount ants generate
    public static double aco_alpha = 1.0; // ACO - Pheromone importance
    public static double aco_beta = 2.0; // ACO - Distance priority
    public static double aco_evRate = 0.5; // ACO - Pheromone evaporation rate
    public static int pso_numParticles = 50; // PSO - Amount Particles generate
    public static int pso_Iteration = 100; // PSO - Max PSO Iterator
    public static double pso_defaultW = 0.729844; // PSO Constants
    public static double pso_defaultC1 = 1.496185; // PSO Constants
    public static double pso_defaultC2 = 1.496185; // PSO Constants
    public static double sa_temperature = 100; // SA - Intialize temperature
    public static double sa_coolRate = 0.05; // SA - Cooling Rate
    public static int gm_k = 3; // Model - Nums resource (Include: Cpu(pes), ram, storage, bandwidth)
    public static int gm_alpha = 2; // Model - Coefficient

    // </editor-fold>
    // <editor-fold desc="Datacenter Constants">
    
    public static String arch = "x64"; // system architecture
    public static String os = "Linux"; // operating system
    public static String vmm = "Xen";  // virtual machine monitor
    public static double time_zone = 7.0; // time zone this resource located
    public static double cost = 3.0; // the cost of using processing in this resource
    public static double costPerMem = 0.05; // the cost of using memory in this resource
    public static double costPerStorage = 0.001; // the cost of using storage in this resource
    public static double costPerBw = 0.0; // the cost of using bw in this resource
    
    // </editor-fold>

}

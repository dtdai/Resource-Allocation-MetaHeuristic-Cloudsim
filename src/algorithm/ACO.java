package algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import api.HostManagement;
import section.Random;

/**
 *
 * @author TrongDai
 */
public class ACO {

    private final int numAnts;
    private final int numPM;
    private final int numVM;
    private final List<Host> pms;
    private final List<Vm> vms;
    private final ArrayList<ArrayList<Double>> trails;
    private ArrayList<Double> probabilities;
    private final double alpha;
    private final double beta;
    private final double evaporationRate;
    private final int Q = 1;
    private final double nguy = 0.5;
    private final int gm_k;
    private final int gm_alpha;
    private List<Host> bhost;
    private ArrayList<Integer> bestTour;
    private double bestValue = Double.MIN_EXPONENT;

    public ACO(List<Host> hosts, List<Vm> vms, int numAnts, double alpha, double beta, double evaporationRate, int gm_k, int gm_alpha) {
        this.pms = hosts;
        this.vms = vms;
        this.numAnts = numAnts;
        this.numVM = vms.size();
        this.numPM = pms.size();
        this.alpha = alpha;
        this.beta = beta;
        this.evaporationRate = evaporationRate;
        trails = new ArrayList<>();
        probabilities = new ArrayList<>();
        this.gm_k = gm_k;
        this.gm_alpha = gm_alpha;
    }

    public void solve() {
        ArrayList<Ants> A = new ArrayList<>(); // new int[numAnts][numVM];

        for (int i = 0; i < numVM; i++) {
            ArrayList<Double> trail = new ArrayList<>();
            for (int j = 0; j < numPM; j++) {
                trail.add(0.001);
            }
            trails.add(trail);
        }

        for (int index = 0; index < 100; index++) {
            for (int index2 = 0; index2 < numAnts; index2++) {
                A.add(generateAntTour());
            }
        }

//        System.out.println("Best Solution using ACO is:" + bestTour.toString());
//        System.out.println("Fairness-Utilization value is " + bestValue);
    }

    private Ants generateAntTour() {
        Ants a = new Ants();
        a.model = new GameModel(pms, vms, gm_k, gm_alpha);
        ArrayList<Integer> tour = new ArrayList<>();

        while (tour.size() != numVM) {
            tour = GenerateTour();
        }
        
        a.tour = tour;
        a.value = VirtualAllocation(a, tour);
        
        return a;
    }


    private ArrayList<Integer> GenerateTour() {
        ArrayList<Integer> tour = new ArrayList<>();
        ArrayList<Host> hosts = HostManagement.CloneHost(pms);

        int currentNode = 0;

        for (int i = 0; i < numVM; i++) {

            probabilities = calculateProbabilities(trails, hosts, currentNode);

            int nextNode = selectNext(currentNode, probabilities, hosts);

            if (nextNode == -1) {
                break;
            }

            hosts.get(nextNode).vmCreate(vms.get(i));
            tour.add(nextNode);
            currentNode = nextNode;
        }

        updateTrails(hosts, trails, numVM, numPM);
        return tour;
    }
    
    private double VirtualAllocation(Ants a, ArrayList<Integer> tour) {
        ArrayList<Host> hosts = HostManagement.CloneHost(pms);

        for (int i = 0; i < tour.size(); i++) {
            hosts.get(tour.get(i)).vmCreate(vms.get(i));
        }

        double value = a.model.FairnessUtilization(hosts);

        if (Double.compare(value, bestValue) > 0) {
            bhost = hosts;
            bestTour = tour;
            bestValue = value;
        }

        return value;
    }

    private int selectNext(int currentNode, ArrayList<Double> probabilities, ArrayList<Host> hosts) {
        int index = -1;
        double maxProb = 0.0;

        double r = Random.RandDouble(0.0, 1.0);
        if (Double.compare(r, 0.6) <= 0) {
            for (int i = 0; i < numPM; i++) {
                index = Random.RandInteger(0, numPM - 1);
                Host pm = hosts.get(index);
                if (pm.isSuitableForVm(vms.get(currentNode))) {
                    return index;
                }
            }

        } else {
            for (int i = 0; i < numPM; i++) {
                Host pm = hosts.get(i);
                if (Double.compare(maxProb, probabilities.get(i)) < 0 && pm.isSuitableForVm(vms.get(currentNode))) {
                    maxProb = probabilities.get(i);
                    index = i;
                }
            }
        }

        return index;
    }

    private void updateTrails(ArrayList<Host> hosts, ArrayList<ArrayList<Double>> trails, int numVM, int numPM) {
        double r[] = UpdatePheromone(hosts);
        double contribution = Q / (1 / (nguy * r[0] + (1 - nguy) * r[1]));
        for (int i = 0; i < numVM; i++) {
            for (int j = 0; j < numPM; j++) {
                trails.get(i).set(j, (1 - evaporationRate) * trails.get(i).get(j) + contribution);
            }
        }
    }

    private double[] UpdatePheromone(ArrayList<Host> hosts) {
        double[] uload = new double[numPM];
        double[] wload = new double[numPM];
        for (int i = 0; i < hosts.size(); i++) {
            uload[i] = Math.round((0.3 * (hosts.get(i).getNumberOfPes() - hosts.get(i).getNumberOfFreePes())
                    + 0.3 * hosts.get(i).getRamProvisioner().getUsedRam()
                    + 0.4 * hosts.get(i).getBwProvisioner().getUsedBw()) * 100) / 100;
            wload[i] = Math.sqrt(Math.pow(HostManagement.getPeUtilization(hosts.get(i)), 2)
                    + Math.pow(HostManagement.getRamUtilization(hosts.get(i)), 2)
                    + Math.pow(HostManagement.getBwUtilization(hosts.get(i)), 2));
        }
        double[] s = new double[2];
        double avgrload = avgrLoadPM(uload);

        double varLoad = 0.0;
        double wasteLoad = 0.0;
        for (int j = 0; j < numPM; j++) {
            varLoad += Math.pow(uload[j] - avgrload, 2);
            wasteLoad += wload[j];
        }
        s[0] = varLoad / (1.0 * (numPM - 1));
        s[1] = wasteLoad;
        return s;
    }

    private double avgrLoadPM(double[] aload) {
        double avgrload = 0.0;
        for (int i = 0; i < aload.length; i++) {
            avgrload += aload[i];
        }
        return avgrload / numPM;
    }

    private ArrayList<Double> calculateProbabilities(ArrayList<ArrayList<Double>> trails, ArrayList<Host> hosts, int currentNode) {
        ArrayList<Double> localProbabilities = new ArrayList<>();
        double pheromone = pheromones(trails, currentNode, hosts);
        for (int i = 0; i < numPM; i++) {
            double probability = Math.pow(trails.get(currentNode).get(i), alpha) * Math.pow(1.0 / performance(hosts.get(i)), beta);
            localProbabilities.add(probability / pheromone);
        }
        return localProbabilities;
    }

    private double pheromones(ArrayList<ArrayList<Double>> trails, int currentNode, ArrayList<Host> hosts) {
        double pheromone = 0.0;

        for (int i = 0; i < numPM; i++) {
            pheromone += Math.pow(trails.get(currentNode).get(i), alpha) * Math.pow(1.0 / performance(hosts.get(i)), beta);
        }
        return pheromone;
    }
    
    private double performance(Host host) {
        double a = HostManagement.getPeUtilization(host);
        double b = HostManagement.getRamUtilization(host);
        double c = HostManagement.getBwUtilization(host);
        return (a + b + c) / 3;
    }

    public List<Host> getBhost() {
        return bhost;
    }
    
    public Map<String, Host> getSolution() {
        Map<String, Host> solution = new HashMap<>();
        for (int i = 0; i < bestTour.size(); i++) {
            solution.put(vms.get(i).getUid(), pms.get(bestTour.get(i)));
        }
        return solution;
    }

    public double getSolutionValue() {
        return bestValue;
    }
    
    private class Ants {
        ArrayList<Integer> tour;
        Double value;
        GameModel model;
        
        Ants() {
            tour = new ArrayList<>();
            value = 0.0;
            model = null;
        }
    }
}

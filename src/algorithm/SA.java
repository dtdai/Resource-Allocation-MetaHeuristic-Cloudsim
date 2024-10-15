package algorithm;

import java.util.ArrayList;
import java.util.List;

import api.HostManagement;
import java.util.HashMap;
import java.util.Map;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import section.Random;

/**
 *
 * @author TrongDai
 */
public class SA {

    private final List<Host> pms;
    private final List<Vm> vms;
    private final int numPM;
    private final int numVM;
    private double temperature;
    private final double coolingRate;
    private final int gm_k;
    private final int gm_alpha;
    private List<Host> bhost;
    private ArrayList<Integer> besttour = new ArrayList<>();
    private double bestValue = Double.MIN_EXPONENT;

    public SA(List<Host> hostlist, List<Vm> vmlist, double temp, double coolRate, int gm_k, int gm_alpha) {
        this.pms = hostlist;
        this.vms = vmlist;
        this.numPM = pms.size();
        this.numVM = vms.size();
        this.temperature = temp;
        this.coolingRate = coolRate;
        this.gm_k = gm_k;
        this.gm_alpha = gm_alpha;
    }

    public void solve() {
        ArrayList<Annealing> annealings = new ArrayList<>();
        while (temperature > 20) {
            Annealing a = new Annealing();
            a.model = new GameModel(pms, vms, gm_k, gm_alpha);
            ArrayList<Integer> tour = new ArrayList<>();

            while (tour.size() != numVM) {
                tour = GenerateTour();
            }

            a.tour = tour;
            a.val = VirtualAllocation(a, tour);
            annealings.add(a);

            temperature = temperature * (1 - coolingRate);
        }

//        System.out.println("Best Solution using SA is: " + besttour.toString());
//        System.out.println("Fairness-Utilization value is " + bestValue);
    }

    private ArrayList<Integer> GenerateTour() {
        ArrayList<Integer> tour = new ArrayList<>();
        ArrayList<Host> hosts = HostManagement.CloneHost(pms);

        for (int i = 0; i < numVM; i++) {
            int randPM = Random.RandInteger(0, numPM - 1);
            randPM = CheckAvailable(hosts, randPM, i, 1);
            if (randPM == -1) {
                break;
            }
            tour.add(randPM);
            hosts.get(randPM).vmCreate(vms.get(i));
        }

        return tour;
    }

    private double VirtualAllocation(Annealing a, ArrayList<Integer> tour) {
        List<Host> hosts = HostManagement.CloneHost(pms);

        for (int i = 0; i < tour.size(); i++) {
            hosts.get(tour.get(i)).vmCreate(vms.get(i));
        }

        double value = a.model.FairnessUtilization(hosts);

        if (Double.compare(value, bestValue) > 0) {
            setBhost(hosts);
            besttour = tour;
            bestValue = value;
        }

        return value;
    }

    private int CheckAvailable(ArrayList<Host> host, int indexPM, int indexVM, int runtime) {
        while (!host.get(indexPM).isSuitableForVm(vms.get(indexVM))) {
            indexPM = Random.RandInteger(0, numPM - 1);
            if (runtime > numPM * 10) {
                return -1;
            }
            runtime++;
        }
        return indexPM;
    }

    public void setBhost(List<Host> bhost) {
        this.bhost = bhost;
    }

    public List<Host> getBhost() {
        return bhost;
    }

    public Map<String, Host> getSolution() {
        Map<String, Host> solution = new HashMap<>();
        for (int i = 0; i < besttour.size(); i++) {
            solution.put(vms.get(i).getUid(), bhost.get(besttour.get(i)));
        }
        return solution;
    }

    public double getSolutionValue() {
        return bestValue;
    }

    private class Annealing {

        ArrayList<Integer> tour;
        double val;
        GameModel model;

        Annealing() {
            tour = new ArrayList<>();
            val = 0.0;
            model = null;
        }
    }
}

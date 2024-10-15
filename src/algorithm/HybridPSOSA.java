package algorithm;

import api.HostManagement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import section.Random;

/**
 *
 * @author TrongDai
 */
public class HybridPSOSA {

    private final List<Host> pms;
    private final List<Vm> vms;
    private final int numPM;
    private final int numVM;
    private final int Particle;
    private final int Iteration;
    private final double W;
    private final double C1;
    private final double C2;
    private final double sa_temp;
    private final double sa_coolRate;
    private final int gm_k;
    private final int gm_alpha;
    private ArrayList<Integer> gbesttour = new ArrayList<>();
    private double gbest = Double.MIN_EXPONENT;

    public HybridPSOSA(List<Host> hostlist, List<Vm> vmlist, int numParticle, int Iteration, double W, double C1, double C2, double sa_temp, double sa_coolRate, int gm_k, int gm_alpha) {
        this.pms = hostlist;
        this.vms = vmlist;
        this.numPM = pms.size();
        this.numVM = vms.size();
        this.Particle = numParticle;
        this.Iteration = Iteration;
        this.W = W;
        this.C1 = C1;
        this.C2 = C2;
        this.sa_temp = sa_temp;
        this.sa_coolRate = sa_coolRate;
        this.gm_k = gm_k;
        this.gm_alpha = gm_alpha;
    }

    public void solve() {
        ArrayList<ParticleNode> particles = new ArrayList<>();
        for (int p = 0; p < 100; p++) {
            ParticleNode particle = new ParticleNode();
            IntialParticles(particle);
            for (int it = 0; it < Iteration; it++) {
                VelocityUpdate(particle);
                PositionUpdate(particle);
                LocalBestUpdate(particle);
                GlobalBestUpdate(particle);
            }
//            System.out.println(particle.pbest);
            particles.add(particle);
        }

//        System.out.println("Best Solution using Hybrid PSO-SA is: " + gbesttour.toString());
//        System.out.println("Fairness-Utilization value is " + gbest);
    }

    private void IntialParticles(ParticleNode particle) {
        particle.model = new GameModel(pms, vms, gm_k, gm_alpha);
        GenerateVelocity(particle); // 100 p, 100 vel
        GeneratePosition(particle); // 100 p, per p 100 pos

        particle.pbest = particle.pos.get(0);

        for (int p = 1; p < Particle; p++) {
            if (particle.pbest < particle.pos.get(p)) {
                particle.pbest = particle.pos.get(p);
                particle.ipbest = p;
            }
        }

        if (Double.compare(particle.pbest, gbest) > 0) {
            gbest = particle.pbest;
            gbesttour = particle.Atour.get(particle.ipbest);
        }
    }

    private void GenerateVelocity(ParticleNode particle) {
        for (int i = 0; i < Particle; i++) {
            Double v = Random.RandDouble(-2.0, 2.0);
            particle.vel.add(v);
        }
    }

    private void GeneratePosition(ParticleNode particle) {
        for (int p = 0; p < Particle; p++) {
            ArrayList<Integer> tour = new ArrayList<>();

            while (tour.size() != numVM) {
                tour = GenerateTour();
            }

            particle.Atour.add(tour);
            particle.pos.add(VirtualAllocation(particle, tour));
        }
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
            Allocation(hosts, randPM, i);
        }

        return tour;
    }

    private void generateSAtour(ParticleNode particle, double temporature, double coolRate) {
        ArrayList<Integer> intialtour = new ArrayList<>();
        ArrayList<Integer> bestroute = particle.Atour.get(particle.ipbest);
        ArrayList<Host> host = HostManagement.CloneHost(pms);

        int rand = Random.RandInteger(1, numVM);
        if (bestroute.size() == numVM && rand <= Math.round(numVM / 3)) {
            for (int index = 0; index < rand; index++) {
                try {
                    Allocation(host, bestroute.get(index), index);
                    intialtour.add(bestroute.get(index));
                } catch (Exception ex) {
                }
            }
        } else {
            rand = 0;
        }
//        -> host, intialtour

        while (temporature >= 20) {
            ArrayList<Integer> tour = new ArrayList<>();
            ArrayList<Host> mhost = null;

            while (tour.size() != numVM) {
                tour = new ArrayList<>();
                mhost = HostManagement.CloneHost(host);

                for (int i = 0; i < intialtour.size(); i++) {
                    tour.add(intialtour.get(i));
                }

                for (int i = rand; i < numVM; i++) {
                    int randPM = Random.RandInteger(0, numPM - 1);
                    randPM = CheckAvailable(mhost, randPM, i, 1);
                    if (randPM == -1) {
                        break;
                    }
                    tour.add(randPM);
                    Allocation(host, randPM, i);
                }
            }

            double bettervalue = particle.model.FairnessUtilization(mhost);

            if (Double.compare(bettervalue, particle.pbest) > 0) {
                particle.pbest = bettervalue;
                particle.Atour.set(particle.ipbest, tour);
            }

            temporature = temporature * (1 - coolRate);
        }
    }

    private double VirtualAllocation(ParticleNode particle, ArrayList<Integer> tour) {
        ArrayList<Host> hosts = HostManagement.CloneHost(pms);

        for (int i = 0; i < tour.size(); i++) {
            Allocation(hosts, tour.get(i), i);
        }

        return particle.model.FairnessUtilization(hosts);
    }

    private void VelocityUpdate(ParticleNode particle) {

        for (int g = 0; g < 100; g++) {
            double rand = Random.RandDouble(0, 1);
            if (rand <= 0.8) {
                generateSAtour(particle, sa_temp, sa_coolRate);
            }
        }

        for (int i = 0; i < Particle; i++) {
            double veloc = particle.vel.get(i);
            double pos = particle.pos.get(i);
            veloc = W * veloc + this.C1 * Random.RandDouble(0, 1) * (particle.pbest - pos) + this.C2 * Random.RandDouble(0, 1) * (gbest - pos);
            particle.vel.set(i, veloc);
        }
    }

    private void PositionUpdate(ParticleNode particle) {
        for (int i = 0; i < Particle; i++) {
            double pos = particle.pos.get(i);
            pos = pos + particle.vel.get(i);
            SufflePosition(particle, i, (int) Math.abs(pos));
        }
    }

    private void LocalBestUpdate(ParticleNode particle) {
        for (int i = 0; i < Particle; i++) {
            if (Double.compare(particle.pos.get(i), particle.pbest) > 0) {
                particle.ipbest = i;
                particle.pbest = particle.pos.get(i);
            }
        }
    }

    private void GlobalBestUpdate(ParticleNode particle) {
        if (Double.compare(particle.pbest, gbest) > 0) {
            gbest = particle.pbest;
            gbesttour = particle.Atour.get(particle.ipbest);
        }
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

    private void Allocation(ArrayList<Host> host, int indexPM, int indexVM) {
        host.get(indexPM).vmCreate(vms.get(indexVM));
    }

    private void SufflePosition(ParticleNode particle, int index, int value) {
        for (int it = 0; it < value; it++) {
            ArrayList<Integer> newtour = new ArrayList<>();

            while (newtour.size() != numVM) {
                newtour = GenerateTour();
            }

            double newpos = VirtualAllocation(particle, newtour);
            if (Double.compare(newpos, particle.pos.get(index)) > 0) {
                particle.Atour.set(index, newtour);
                particle.pos.set(index, newpos);
            }
        }
    }

    public Map<String, Host> getSolution() {
        Map<String, Host> solution = new HashMap<>();
        for (int i = 0; i < gbesttour.size(); i++) {
            solution.put(vms.get(i).getUid(), pms.get(gbesttour.get(i)));
        }
        return solution;
    }

    public double getSolutionValue() {
        return gbest;
    }

    private class ParticleNode {

        ArrayList<Double> vel;
        ArrayList<ArrayList<Integer>> Atour;
        GameModel model;
        ArrayList<Double> pos;
        int ipbest;
        double pbest;

        ParticleNode() {
            vel = new ArrayList<>();
            Atour = new ArrayList<>();
            pos = new ArrayList<>();
            model = null;
            pbest = 0.0;
            ipbest = 0;
        }
    }
}

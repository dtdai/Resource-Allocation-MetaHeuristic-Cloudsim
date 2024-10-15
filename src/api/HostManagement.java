package api;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 *
 * @author TrongDai
 */
public class HostManagement {

    /**
     * Host(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner,
     * long storage, List<? extends Pe> peList, VmScheduler vmScheduler);
     *
     * @param original host list
     * @return copy host list
     */
    public static ArrayList<Host> CloneHost(List<Host> original) {
        ArrayList<Host> copy = new ArrayList<>();
        for (Host host : original) {
            List<Pe> newPeList = new ArrayList<>();
            for (Pe p : host.getPeList()) {
                newPeList.add(new Pe(p.getId(), new PeProvisionerSimple(p.getMips())));
            }

            int id = host.getId();
            RamProvisionerSimple ram = new RamProvisionerSimple(host.getRam());
            long storage = host.getStorage();
            BwProvisionerSimple bw = new BwProvisionerSimple(host.getBw());
            VmSchedulerTimeShared scheduler = new VmSchedulerTimeShared(newPeList);

            copy.add(new Host(id, ram, bw, storage, newPeList, scheduler));
        }
        return copy;
    }

    public static double getUtilization(Host host) {
        double pe = 0.0, pp = 0.0;
        for (Pe p : host.getPeList()) {
            pe = pe + p.getPeProvisioner().getTotalAllocatedMips();
            pp = pp + p.getPeProvisioner().getMips();
        }
        pe = pe / pp;
        RamProvisioner ram = host.getRamProvisioner();
        double uram = ram.getUsedRam() / ram.getRam();
        BwProvisioner bw = host.getBwProvisioner();
        double ubw = bw.getUsedBw() / bw.getBw();
        return (pe + uram + ubw) / 3;
    }
    
    public static double getPeUtilization(Host host) {
        double pe = 0.0, pp = 0.0;
        for (Pe p : host.getPeList()) {
            pe = pe + p.getPeProvisioner().getAvailableMips();
            pp = pp + p.getPeProvisioner().getMips();
        }
        return pe / pp;
    }
    
    public static double getRamUtilization(Host host) {
        RamProvisioner p = host.getRamProvisioner();
        return p.getAvailableRam() / p.getRam();
    }
    
    public static double getBwUtilization(Host host) {
        BwProvisioner p = host.getBwProvisioner();
        return p.getAvailableBw()/ p.getBw();
    }
    
    public static void printResult(List<Vm> vm) {
        Log.printLine("Result of Simulation: ");
        for (Vm v : vm) {
            Log.printLine("Vm #" + v.getId() + " is allocated to Host #" + v.getHost().getId());
        }
    }
}

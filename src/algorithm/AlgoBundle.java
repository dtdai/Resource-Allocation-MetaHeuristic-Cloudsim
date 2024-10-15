package algorithm;

import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import static section.Constants.Type;
import static section.Constants.aco_alpha;
import static section.Constants.aco_beta;
import static section.Constants.aco_numAnts;
import static section.Constants.aco_evRate;
import static section.Constants.gm_alpha;
import static section.Constants.gm_k;
import static section.Constants.pso_Iteration;
import static section.Constants.pso_defaultC1;
import static section.Constants.pso_defaultC2;
import static section.Constants.pso_defaultW;
import static section.Constants.pso_numParticles;
import static section.Constants.sa_coolRate;
import static section.Constants.sa_temperature;

/**
 *
 * @author TrongDai
 */
public class AlgoBundle {
    
    private List<Host> hostlist;
    private List<Vm> vmlist;
    private Map<String, Host> vmtable;
    private double value;
    
    public AlgoBundle(List<Host> hosts, List<Vm> vms, Type algo) {
        setHostlist(hosts);
        setVmlist(vms);
        
        switch (algo) {
            case ACO -> {
                ACO aco = new ACO(hostlist, vmlist, aco_numAnts, aco_alpha, aco_beta, aco_evRate, gm_k, gm_alpha);
                aco.solve();
                setVmtable(aco.getSolution());
                setValue(aco.getSolutionValue());
            }
            case PSO -> {
                PSO pso = new PSO(hostlist, vmlist, pso_numParticles, pso_Iteration, pso_defaultW, pso_defaultC1, pso_defaultC2, gm_k, gm_alpha);
                pso.solve();
                setVmtable(pso.getSolution());
                setValue(pso.getSolutionValue());
            }
            case SA -> {
                SA sa = new SA(hostlist, vmlist, sa_temperature, sa_coolRate, gm_k, gm_alpha);
                sa.solve();
                setHostlist(sa.getBhost());
                setVmtable(sa.getSolution());
                setValue(sa.getSolutionValue());
            }

            case ACOSA -> {
                HybridACOSA hybridACOSA = new HybridACOSA(hostlist, vmlist, aco_numAnts, aco_alpha, aco_beta, aco_evRate, sa_temperature, sa_coolRate, gm_k, gm_alpha);
                hybridACOSA.solve();
                setVmtable(hybridACOSA.getSolution());
                setValue(hybridACOSA.getSolutionValue());
            }
            case PSOSA -> {
                HybridPSOSA hybridPSOSA = new HybridPSOSA(hostlist, vmlist, pso_numParticles, pso_Iteration, pso_defaultW, pso_defaultC1, pso_defaultC2, sa_temperature, sa_coolRate, gm_k, gm_alpha);
                hybridPSOSA.solve();
                setVmtable(hybridPSOSA.getSolution());
                setValue(hybridPSOSA.getSolutionValue());
            }
        }
    }

    public final void setHostlist(List<Host> hostlist) {
        this.hostlist = hostlist;
    }

    public final void setVmlist(List<Vm> vmlist) {
        this.vmlist = vmlist;
    }

    public final void setVmtable(Map<String, Host> vmtable) {
        this.vmtable = vmtable;
    }

    public final void setValue(double value) {
        this.value = value;
    }

    public List<Host> getHostlist() {
        return hostlist;
    }

    public Map<String, Host> getVmtable() {
        return vmtable;
    }

    public double getValue() {
        return value;
    }
}

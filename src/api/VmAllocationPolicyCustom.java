package api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import algorithm.AlgoBundle;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import section.Constants;

/**
 *
 * @author TrongDai
 */
public class VmAllocationPolicyCustom extends VmAllocationPolicy {

    private Map<String, Host> vmTable;
    private Map<String, Integer> usedPes;
    private List<Integer> freePes;
    private Constants.Type type;

    public VmAllocationPolicyCustom(List<? extends Host> list, Constants.Type type) {
        super(list);

        setFreePes(new ArrayList<>());
        for (Host host : getHostList()) {
            getFreePes().add(host.getNumberOfPes());
        }

        setVmTable(new HashMap<>());
        setUsedPes(new HashMap<>());
        // setVmTable(bundle.getVmtable());
        setType(type);
    }

    public void VmAllocationRun(List<Vm> vmlist) {
        AlgoBundle bundle = new AlgoBundle(getHostList(), vmlist, type);
        setHostList(bundle.getHostlist());
        setVmTable(bundle.getVmtable());
    }

    @Override
    public boolean allocateHostForVm(Vm vm) {
//        throw new UnsupportedOperationException("Not supported yet.");

        boolean result = true;

        if (!getVmTable().containsKey(vm.getUid())) {
            result = false;
        }

        return result;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        if (host.isSuitableForVm(vm)) {
            return host.vmCreate(vm);
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deallocateHostForVm(Vm vm) {
        Host host = getVmTable().remove(vm.getUid());
        if (host != null) {
            host.vmDestroy(vm);
        }
    }

    @Override
    public Host getHost(Vm vm) {
        return getVmTable().get(vm.getUid());
    }

    @Override
    public Host getHost(int vmId, int userId) {
        return getVmTable().get(Vm.getUid(userId, vmId));
    }

    public Map<String, Host> getVmTable() {
        return vmTable;
    }

    protected final void setVmTable(Map<String, Host> vmTable) {
        this.vmTable = vmTable;
    }

    protected final Map<String, Integer> getUsedPes() {
        return usedPes;
    }

    protected final void setUsedPes(Map<String, Integer> usedPes) {
        this.usedPes = usedPes;
    }

    protected final List<Integer> getFreePes() {
        return freePes;
    }

    protected final void setFreePes(List<Integer> freePes) {
        this.freePes = freePes;
    }

    public final void setType(Constants.Type type) {
        this.type = type;
    }

}

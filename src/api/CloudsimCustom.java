package api;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import section.Constants;

/**
 *
 * @author TrongDai
 */
public class CloudsimCustom {

    private static List<Cloudlet> cloudletList;
    private static List<Host> hostlist;
    private static List<Vm> vmlist;

    public static void main(String[] args) {

        Log.printLine("Starting CloudSim...");

        try {
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;

            CloudSim.init(num_user, calendar, trace_flag);

            Generate generate = new Generate();
            generate.GenerateHost(Constants.numHost);
            setHostlist(generate.getHostList());
            
//            Datacenter datacenter_1 = createDatacenter("Datacenter_1", Constants.Type.ACO);
//            Datacenter datacenter_2 = createDatacenter("Datacenter_2", Constants.Type.PSO);
            Datacenter datacenter_3 = createDatacenter("Datacenter_3", Constants.Type.SA);
//            Datacenter datacenter_4 = createDatacenter("Datacenter_4", Constants.Type.ACOSA);
//            Datacenter datacenter_5 = createDatacenter("Datacenter_5", Constants.Type.PSOSA);

            DatacenterBroker broker_1 = createBroker("Broker_1");
            int brokerId = broker_1.getId();
            
            generate.GenerateVm(Constants.numVm, brokerId);
            setVmlist(generate.getVmList());

            VmAllocationPolicyCustom policy = (VmAllocationPolicyCustom) datacenter_3.getVmAllocationPolicy();
            policy.VmAllocationRun(vmlist);
            broker_1.submitVmList(vmlist);
            
            setCloudletList(createCloudlet(brokerId, vmlist));
            broker_1.submitCloudletList(cloudletList);

            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            List<Cloudlet> newList = broker_1.getCloudletReceivedList();
            printCloudletList(newList);

            Log.printLine("CloudSim finished!");

        } catch (NullPointerException e) {
            Log.printLine("Unwanted errors happen. " + e.getMessage());
        }
    }

    private static Datacenter createDatacenter(String name, Constants.Type algo) {

        String arch = Constants.arch;
        String os = Constants.os;
        String vmm = Constants.vmm;
        double time_zone = Constants.time_zone;
        double cost = Constants.cost;
        double costPerMem = Constants.costPerMem;
        double costPerStorage = Constants.costPerStorage;
        double costPerBw = Constants.costPerBw;
        LinkedList<Storage> storageList = new LinkedList<>();
        ArrayList<Host> hosts = HostManagement.CloneHost(getHostlist());
        VmAllocationPolicy policy = new VmAllocationPolicyCustom(hosts, algo);

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hosts, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        Datacenter datacenter = null;

        // Datacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval);
        try {
            datacenter = new Datacenter(name, characteristics, policy, storageList, 0);

        } catch (Exception e) {
            Log.printLine("An error has occured with Datacenter. " + e);
        }

        return datacenter;
    }

    private static DatacenterBroker createBroker(String name) {
        DatacenterBroker broker = null;

        try {
            broker = new DatacenterBroker(name);

        } catch (Exception e) {
            Log.printLine("An error has occured with DatacenterBroker. " + e);
            return null;
        }
        return broker;
    }

    private static List<Cloudlet> createCloudlet(int userId, List<Vm> vm) {
        LinkedList<Cloudlet> list = new LinkedList<>();

        long length = 1000;
        long fileSize = 100;
        long outputSize = 100;
        
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet[] cloudlet = new Cloudlet[vm.size()];

        for (int i = 0; i < vm.size(); i++) {
            int pesNumber = vm.get(i).getNumberOfPes();
            cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet[i].setUserId(userId);
            list.add(cloudlet[i]);
        }

        return list;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        Log.printLine();
        Log.printLine("========== CLOUDLET OUTPUT ==========");
        Object[] headers = {"Cloudlet ID", "STATUS", "Datacenter ID", "VM ID", "Time", "Start Time", "Finish Time"};
        String hformat = "%-15s %-10s %-17s %-9s %-8s %-14s %-15s%n";
        String dformat = "%-4s %-10s %-15s %-14s %-8s %-11s %-14s %-15s%n";
        Log.format(hformat, headers);

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Object[] data = new Object[16];

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                data[0] = " ";
                data[1] = cloudlet.getCloudletId();
                data[2] = "SUCCESS";
                data[3] = cloudlet.getResourceId();
                data[4] = cloudlet.getVmId();
                data[5] = dft.format(cloudlet.getActualCPUTime());
                data[6] = dft.format(cloudlet.getExecStartTime());
                data[7] = dft.format(cloudlet.getFinishTime());

                Log.format(dformat, data);
            }
        }
    }

    public static List<Cloudlet> getCloudletList() {
        return cloudletList;
    }

    public static void setCloudletList(List<Cloudlet> cloudletList) {
        CloudsimCustom.cloudletList = cloudletList;
    }

    public static List<Host> getHostlist() {
        return hostlist;
    }

    public static void setHostlist(List<Host> hostlist) {
        CloudsimCustom.hostlist = hostlist;
    }

    public static List<Vm> getVmlist() {
        return vmlist;
    }

    public static void setVmlist(List<Vm> vmlist) {
        CloudsimCustom.vmlist = vmlist;
    }
}

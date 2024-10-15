package api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import section.Random;

/**
 *
 * @author TrongDai
 */
public class Generate {

    private ArrayList<Host> hostList;
    private ArrayList<Vm> vmList;
    private static int hostid;
    private static int vmid;

    public ArrayList<Host> getHostList() {
        return hostList;
    }

    public ArrayList<Vm> getVmList() {
        return vmList;
    }
    
    public Generate() {
        hostid = 0;
        vmid = 0;
        hostList = new ArrayList<>();
        vmList = new ArrayList<>();
    }

    public Generate(int numHost, int numVm) {
        // Host(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler);
        // Vm(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String vmm, CloudletScheduler cloudletScheduler);
        hostid = 0;
        vmid = 0;
        hostList = getHost(numHost);
        vmList = getVm(numVm, 0);
    }
    
    public void GenerateHost(int numHost) {
        hostList = getHost(numHost);
    }
    
    public void GenerateVm(int numVm, int userId) {
        vmList = getVm(numVm, userId);
    }

    private ArrayList<Host> getHost(int numHost) {
        ArrayList<Host> hosts = new ArrayList<>();
        try {
            ArrayList<String> HostFile = ImportFile("hostsample.txt");
            ArrayList<ArrayList<Integer>> hostParam = new ArrayList<>();

            for (int i = 0; i < numHost; i++) {
                String host = HostFile.get(i);
                String[] parts = host.split(" ");
                ArrayList<Integer> result = new ArrayList<>();
                for (String part : parts) {
                    try {
                        result.add(Integer.valueOf(part));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number: " + part);
                    }
                }
                hostParam.add(result);
            }

            int mips = 1000;

            for (ArrayList<Integer> host : hostParam) {
                List<Pe> pelist = new ArrayList<>();
                for (int i = 0; i < host.get(0); i++) {
                    pelist.add(new Pe(i, new PeProvisionerSimple(mips)));
                }
                int ram = host.get(1);
                int storage = host.get(2);
                int bw = host.get(3);
                hostid++;
                Host h = new Host(
                        hostid,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        pelist,
                        new VmSchedulerTimeShared(pelist)
                );
                hosts.add(h);
            }

        } catch (IOException ex) {
            System.err.println("Error from reading file: " + ex.getMessage());
        }
        return hosts;
    }

    private ArrayList<Vm> getVm(int numVm, int userid) {
        ArrayList<Vm> vms = new ArrayList<>();
        try {
            ArrayList<String> VmFile = ImportFile("vmsample.txt");
            ArrayList<ArrayList<Integer>> vmParam = new ArrayList<>();

            for (int i = 0; i < numVm; i++) {
                String vm = VmFile.get(i);
                String[] parts = vm.split(" ");
                ArrayList<Integer> result = new ArrayList<>();
                for (String part : parts) {
                    try {
                        result.add(Integer.valueOf(part));
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number: " + part);
                    }
                }
                vmParam.add(result);
            }

            int mips = 1000;

            for (ArrayList<Integer> vm : vmParam) {
                int userId = userid;
                int pesNumber = vm.get(0);
                int ram = vm.get(1);
                int size = vm.get(2);
                int bw = vm.get(3);
                String vmm = "Alex";
                vmid++;
                Vm v = new Vm(
                        vmid,
                        userId,
                        mips,
                        pesNumber,
                        ram,
                        bw,
                        size,
                        vmm,
                        new CloudletSchedulerTimeShared()
                );
                vms.add(v);
            }

        } catch (IOException ex) {
            System.err.println("Error from reading file: " + ex.getMessage());
        }
        return vms;
    }
    
    public static void main(String[] args) {
        GenerateSampleList(1000, 5000);
    }

    public static void GenerateSampleList(int numHost, int numVM) {
        try {
            ArrayList<String> hostsample = new ArrayList<>();
            ArrayList<String> vmsample = new ArrayList<>();
            List<Integer> pesample = new ArrayList<>(List.of(16, 32, 48, 64, 96));
            List<Integer> ramsample = new ArrayList<>(List.of(128, 256, 512, 1024));
            List<Integer> storagesample = new ArrayList<>(List.of(2048, 4096, 8192, 16384));
            int bandwidthsample = 20000;

            for (int i = 0; i < numHost; i++) {
                int pe = pesample.get(Random.RandInteger(0, pesample.size() - 1));
                int ram = ramsample.get(Random.RandInteger(0, ramsample.size() - 1));
                int storage = storagesample.get(Random.RandInteger(0, storagesample.size() - 1));
                hostsample.add(pe + " " + ram + " " + storage + " " + bandwidthsample);
            }
            WriteFile(hostsample, "hostsample.txt");

            ArrayList<String> VmParameter = ImportFile("ThongSoVm.txt");
            int vmlength = VmParameter.size() - 1;
            for (int i = 0; i < numVM; i++) {
                vmsample.add(VmParameter.get(Random.RandInteger(0, vmlength)));
            }
            WriteFile(vmsample, "vmsample.txt");

        } catch (IOException e) {
            System.err.println("An error occured while reading file: " + e.getMessage());
        }
    }

    private static ArrayList<String> ImportFile(String path) throws IOException {
        try {
            FileReader fileReader = new FileReader(path);

            ArrayList<String> lines;
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                lines = new ArrayList<>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
                }
            }

            return lines;

        } catch (IOException e) {
            System.err.println("An error occured while reading file: " + e.getMessage());
        }
        return null;
    }

    private static void WriteFile(ArrayList<String> array, String path) {
        try {
            FileWriter writer = new FileWriter(path);

            try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                for (String value : array) {
                    bufferedWriter.write(value);
                    bufferedWriter.newLine();
                }
            }

            System.out.println("Datas has been written to file successfully.");
        } catch (IOException e) {
            System.err.println("An error occured while writing to file: " + e.getMessage());
        }
    }
}

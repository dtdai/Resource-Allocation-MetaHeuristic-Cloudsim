package algorithm;


import api.HostManagement;
import java.util.List;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

/**
 *
 * @author TrongDai
 */
public class GameModel {

    private final List<Host> hosts;
    private final List<Vm> vms;
    private final int k; // Amount of Resource
    private final int u; // Amount of Task
    private final int[][] R;
    private final double[][] H;
    private final double[][] g;
    private final double d;
    private final int alpha;
    private double Omega = 0.0;
    private double Phil = 0.0;
    private double FairUtil = 0.0;

    public GameModel(List<Host> hosts, List<Vm> vms, int k, int alpha) {
        this.hosts = hosts;
        this.vms = vms;
        this.u = vms.size();
        this.k = k;
        this.alpha = alpha;

        R = RequirementMatrix(this.vms);
        H = NormalizedMatrix(R);
        g = NormalizedDemands(H);
        d = DominantShare(g);
        Omega = FairAllocation(d, g);
    }

    public double getOmega() {
        return Omega;
    }

    private int[][] RequirementMatrix(List<Vm> vms) {
        int[][] RM = new int[u][k];
        for (int i = 0; i < vms.size(); i++) {
            RM[i][0] = (int) vms.get(i).getNumberOfPes();
            RM[i][1] = (int) vms.get(i).getRam();
            RM[i][2] = (int) vms.get(i).getBw();
        }
        return RM;
    }

    private int[] SumResourceCalCulating() {
        int[] sumResPM = new int[k];
        for (int i = 0; i < hosts.size(); i++) {
            sumResPM[0] += hosts.get(i).getNumberOfPes();
            sumResPM[1] += hosts.get(i).getRam();
            sumResPM[2] += hosts.get(i).getBw();
        }
        return sumResPM;
    }

    private double[][] NormalizedMatrix(int[][] RM) {
        double[][] HM = new double[u][k];
        int[] sumResPM = SumResourceCalCulating();
        for (int i = 0; i < u; i++) {
            for (int j = 0; j < k; j++) {
                HM[i][j] = (1.0 * RM[i][j]) / (1.0 * sumResPM[j]);
            }
        }
        return HM;
    }

    private double[][] NormalizedDemands(double[][] HM) {
        double[][] GM = new double[u][k];
        double[] HMax = new double[u];
        for (int i = 0; i < u; i++) {
            HMax[i] = HM[i][0];
            for (int j = 1; j < k; j++) {
                if (HMax[i] < HM[i][j]) {
                    HMax[i] = HM[i][j];
                }
            }
        }
        for (int i = 0; i < u; i++) {
            for (int j = 0; j < k; j++) {
                GM[i][j] = HM[i][j] / HMax[i];
            }
        }
        return GM;
    }

    private double DominantShare(double[][] GM) {
        double Dmax = 0.0;
        for (int j = 0; j < k; j++) {
            double sumG = 0.0;
            for (int i = 0; i < u; i++) {
                sumG += GM[i][j];
            }
            if (Dmax < sumG) {
                Dmax = sumG;
            }
        }
        return Math.pow(Dmax, -1);
    }

    private double FairAllocation(double D, double[][] G) {
        double omega = 0.0;
        for (int i = 0; i < u; i++) {
            for (int j = 0; j < k; j++) {
                omega += Math.pow(Math.abs(H[i][j] - D * G[i][j]), alpha - 1);
            }
        }
        return Math.pow(omega, 1.0 / alpha);
    }

    public double ResourceUtilization(List<Host> hosts) {
        double res = 0.0;
        for (Host host : hosts) {
            double sum = 0.0;
            double util = HostManagement.getUtilization(host);
            if (util == 0) util = 1;
            sum = sum + Math.pow(HostManagement.getPeUtilization(host) / util - 1, 2);
            sum = sum + Math.pow(HostManagement.getRamUtilization(host) / util - 1, 2);
            sum = sum + Math.pow(HostManagement.getBwUtilization(host) / util - 1, 2);
            res += Math.sqrt(sum);
        }
        return res;
    }

    // F(A) = sgn(1 - alpha) * omega - phil
    public double FairnessUtilization(List<Host> pms) {
        this.Phil = ResourceUtilization(pms);
        this.FairUtil = Math.signum(1 - this.alpha) * this.Omega - this.Phil; // -> Negative value
        this.FairUtil = 1 / Math.abs(this.FairUtil);
//        this.FairUtil = 1 / (1 + Math.exp(this.FairUtil));
        return this.FairUtil;
    }
}

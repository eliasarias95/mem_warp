package rot;

import util.*;

import edu.mines.jtk.dsp.*;

import java.lang.Thread;
import java.lang.Exception;
import javax.swing.*;

/**
 * This class uses the smooth dynamic warping method to compute shifts between
 * seismic images and use those shifts to rotate one image to the other image.
 * @author Elias Arias, at his house, OG-CWP
 * @author Aaron Prunty, Colorado School of Mines, CWP
 */
public class RotateData {

  private DynamicWarpingK _dwk;
  private Sampling _s1, _s2;

  public RotateData(int k, Sampling s1, Sampling s2, double shift_max, 
      double sub_samp1, double sub_samp2, double strain1, double strain2) {
    _s1 = s1;
    _s2 = s2;
    _dwk = new DynamicWarpingK(k,-shift_max,shift_max,s1,s2);
    _dwk.setSmoothness(sub_samp1,sub_samp2);
    _dwk.setStrainLimits(-strain1,strain1,-strain2,strain2);
  }

  public float[][] computeShifts(float[][] ref, float[][] rot) {
    float[][] shifts = _dwk.findShifts(_s1,ref,_s1,rot);
    return shifts;
  }

  public float[][] rotateData(float[][] rot, float[][] shifts) {
    float[][] rotatedData = _dwk.applyShifts(_s1,rot,shifts);
    return rotatedData;
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {     
        // Data dimensions
        int n1 = 1800;
        double d1 = 0.0006;
        double f1 = 0.0;
        Sampling s1 = new Sampling(n1,d1,f1);

        int n2 = 601;
        double d2 = 0.005;
        double f2 = -1.5;
        Sampling s2 = new Sampling(n2,d2,f2);

        // Read data in
        float[][] data0 = Utility.readL(n1,n2,
          "/Users/earias/home/git/mem_warp/bench/data/eliasData0.rsf@");
        float[][] data12 = Utility.readL(n1,n2,
          "/Users/earias/home/git/mem_warp/bench/data/eliasData12.rsf@");

        // Create RotateData object to fins shifts and rotate the data
        int k = 5; // shift sampling interval 1/k
        double shift_max = 6.0; // max shift allowed
        double sub_samp1 = 10.0; // bigger = more smooth
        double sub_samp2 = 5.0; // smaller = less smooth
        double strain1 = 0.1; // strain in 1st dim
        double strain2 = 0.1; // strain in 2nd dim

        Sampling ss1 = new Sampling(n1);
        Sampling ss2 = new Sampling(n2);
        RotateData rd = new RotateData(k,ss1,ss2,shift_max,sub_samp1,sub_samp2,
            strain1,strain2);
        float[][] shifts = rd.computeShifts(data12,data0);
        float[][] rotatedData = rd.rotateData(data0,shifts);

        // Plotting
        float cmin = -1.5e-5f; // min value on colorbar
        float cmax =  1.5e-5f; // max value on colorbar
        Plot.plot(s1,s2,data0,"Data rotated 0 degrees","Amplitude",
          cmin,cmax,false);

        try {
          Thread.sleep(1000);
        } catch(Exception e) {
          System.out.println("Oops");
        }

        Plot.plot(s1,s2,data12,"Data rotated 12 degrees","Amplitude",
            cmin,cmax,false);
        Plot.plot(s1,s2,rotatedData,"Rotated Data","Amplitude",
            cmin,cmax,false);
        Plot.plot(ss1,ss2,shifts,"Computed shifts","Shifts (samples/sample)",
            -6.0f,6.0f,true);
      }
    });
  }
}

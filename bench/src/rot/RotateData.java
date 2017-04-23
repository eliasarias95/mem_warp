package rot;

import util.*;

import edu.mines.jtk.dsp.*;

import static edu.mines.jtk.util.ArrayMath.*;

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

  private DynamicWarpingK _dwkT, _dwkS;
  private Sampling _s1, _s2;

  public RotateData(int k, Sampling s1, Sampling s2, double shift_max, 
      double sub_samp1, double sub_samp2, double strain1, double strain2) {
    _s1 = s1;
    _s2 = s2;
    _dwkT = new DynamicWarpingK(k,-shift_max,shift_max,s1,s2);
    _dwkT.setSmoothness(sub_samp1,sub_samp2);
    _dwkT.setStrainLimits(-strain1,strain1,-strain2,strain2);
    _dwkS = new DynamicWarpingK(k,-shift_max,shift_max,s2,s1);
    _dwkS.setSmoothness(sub_samp2,sub_samp1);
    _dwkS.setStrainLimits(-strain2,strain2,-strain1,strain1);
  }

  public float[][] computeTimeShifts(float[][] ref, float[][] rot) {
    float[][] shifts = _dwkT.findShifts(_s1,ref,_s1,rot);
    return shifts;
  }

  public float[][] computeSpatialShifts(float[][] ref, float[][] rot) {
    float[][] refT = transpose(ref);
    float[][] rotT = transpose(rot);
    float[][] shifts = _dwkS.findShifts(_s2,refT,_s2,rotT);
    return shifts;
  }

  public float[][] warpData(float[][] rot, float[][] shifts) {
    float[][] rotatedData = _dwkT.applyShifts(_s1,rot,shifts);
    return rotatedData;
  }

  public static float[][] transpose(float[][] x) {
    int n1 = x[0].length;
    int n2 = x.length;
    float[][] y = new float[n1][n2];
    for (int i2=0; i2<n2; ++i2) {
      for (int i1=0; i1<n1; ++i1) {
        y[i1][i2] = x[i2][i1];
      }
    }
    return y;
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

        // Create RotateData object to fins shifts and rotate the data
        int k = 5; // shift sampling interval 1/k
        double shift_max = 5.0; // max shift allowed
        double sub_samp1 = 100.0; // bigger = more smooth
        double sub_samp2 = 30.0; // smaller = less smooth
        double strain1 = 0.4; // strain in 1st dim
        double strain2 = 0.7; // strain in 2nd dim

        Sampling ss1 = new Sampling(n1);
        Sampling ss2 = new Sampling(n2);
        RotateData rd = new RotateData(k,ss1,ss2,shift_max,sub_samp1,sub_samp2,
            strain1,strain2);

        // Read data in
        int i = 12; // values -12 to 12 for now
        float[][] reference = Utility.readL(n1,n2,
          System.getProperty("user.home") + 
            "/home/git/mem_warp/bench/data/eliasData0.rsf@");
        float[][] data = Utility.readL(n1,n2,
          System.getProperty("user.home") + 
            "/home/git/mem_warp/bench/data/eliasData"+i+".rsf@");
        float[][] timeShifts = rd.computeTimeShifts(reference,data);
        float[][] spatialShifts = rd.computeSpatialShifts(reference,data);
        float[][] spatialShiftsT = transpose(spatialShifts);

        timeShifts = mul((float)d1,timeShifts);
        spatialShiftsT = mul((float)d2,spatialShiftsT);

        // Plotting
        float cmin = -1.5e-5f; // min value on colorbar
        float cmax =  1.5e-5f; // max value on colorbar
        Plot.plot(s1,s2,reference,"Data rotated 0 degrees","Amplitude",
            cmin,cmax,false,false);

        try {
          Thread.sleep(1000);
        } catch(Exception e) {
          System.out.println("Oops");
        }

        Plot.plot(s1,s2,data,"Data rotated "+ i +" degrees","Amplitude",
            cmin,cmax,false,false);

        Plot.plot(s1,s2,spatialShiftsT,"Spatial shifts","Shifts (km)",
            -5.0f,5.0f,true,false);
        Plot.plot(s1,s2,timeShifts,"Time shifts","Shifts (s)",
            -5.0f,5.0f,true,false);
      }
    });
  }
}

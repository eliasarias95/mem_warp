package rot;

import util.*;

import edu.mines.jtk.dsp.*;
import slopes.*;

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

  public RotateData(
      int k, Sampling s1, Sampling s2, double shift_min, double shift_max, 
      double sub_samp1, double sub_samp2, double strain1min, double strain1max,
      double strain2min, double strain2max) {
    _s1 = s1;
    _s2 = s2;
    _dwkT = new DynamicWarpingK(k,shift_min,shift_max,s1,s2);
    _dwkT.setSmoothness(sub_samp1,sub_samp2);
    _dwkT.setStrainLimits(strain1min,strain1max,strain2min,strain2max);
    _dwkS = new DynamicWarpingK(k,shift_min,shift_max,s2,s1);
    _dwkS.setSmoothness(sub_samp2,sub_samp1);
    _dwkS.setStrainLimits(strain2min,strain2max,strain1min,strain1max);
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

  public float[][] warpDataT(float[][] data, float[][] shifts) {
    float[][] warpedData = _dwkT.applyShifts(_s1,data,shifts);
    return warpedData;
  }

  public float[][] warpDataS(float[][] data, float[][] shifts) {
    float[][] dataT = transpose(data);
    float[][] warpedData = _dwkS.applyShifts(_s2,dataT,shifts);
    return warpedData;
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
        double shift_min = -5.0; // max shift allowed
        double shift_max =  5.0; // max shift allowed

        double sub_samp1 = 10.0; // bigger = more smooth
        double sub_samp2 = 3.0; // smaller = less smooth

        double strain1min = -0.2; // min strain in 1st dim
        double strain2min = -0.3; // min strain in 2nd dim
        double strain1max = 0.2; // max strain in 1st dim
        double strain2max = 0.3; // max strain in 2nd dim

        Sampling ss1 = new Sampling(n1);
        Sampling ss2 = new Sampling(n2);
        RotateData rd = new RotateData(k,ss1,ss2,shift_min,shift_max,
            sub_samp1,sub_samp2,strain1min,strain1max,strain2min,strain2max);
        DynamicWarpingSlopes dws = new DynamicWarpingSlopes(k,shift_max,
            sub_samp1,sub_samp2,strain1max,strain2max,ss1,ss2);

        // Read data in
        int i = 12; // values -12 to 12 for now
        float[][] reference = Utility.readL(n1,n2,
            System.getProperty("user.home") + 
            "/home/git/mem_warp/bench/data/eliasData0.rsf@");
        float[][] data = Utility.readL(n1,n2,
            System.getProperty("user.home") + 
            "/home/git/mem_warp/bench/data/eliasData"+i+".rsf@");

        // slope estimation
        //float[][] slope_ref = Utility.readL(n1,n2,
        // System.getProperty("user.home") + 
        //   "/home/git/mem_warp/bench/data/eliasData0_slope.rsf@");
        //float[][] slope_data = Utility.readL(n1,n2,
        //  System.getProperty("user.home") + 
        //    "/home/git/mem_warp/bench/data/eliasData"+i+"_slope.rsf@");

        float[][] slope_ref = new float[n2][n1];
        float[][] slope_data = new float[n2][n1];
        dws.findSmoothSlopes(ss1,reference,slope_ref);
        dws.findSmoothSlopes(ss1,data,slope_data);
        Utility.writeL(slope_ref,System.getProperty("user.home") + 
            "/home/git/mem_warp/bench/data/eliasData0_slope.rsf@");
        Utility.writeL(slope_data,System.getProperty("user.home") + 
            "/home/git/mem_warp/bench/data/eliasData"+i+"_slope.rsf@");

        // getting proper units for plotting
        slope_ref = mul((float)(d1/d2),slope_ref);
        slope_data = mul((float)(d1/d2),slope_data);

        // compute time & spatial shifts
        float[][] timeShifts = rd.computeTimeShifts(reference,data);
        float[][] warpedDataT = rd.warpDataT(data,timeShifts);

        float[][] spatialShifts = rd.computeSpatialShifts(reference,data);
        float[][] spatialShiftsT = transpose(spatialShifts);
        float[][] warpedDataS = transpose(rd.warpDataS(data,spatialShifts));

        // getting proper units for plotting
        timeShifts = mul((float)(d1*1000.0),timeShifts);
        spatialShiftsT = mul((float)(d2*1000.0),spatialShiftsT);

        // Plotting
        float cmin = -1.5e-5f; // min value on colorbar
        float cmax =  1.5e-5f; // max value on colorbar
        boolean paint = false; // paint to png?
        boolean color = false;  // color or black&white?
        boolean showColorbar = false;
        Plot.plot(s1,s2,reference,"Data rotated 0 degrees","(colorbar label)",
            cmin,cmax,color,paint,showColorbar);
        Plot.plot(s1,s2,data,"Data rotated "+ i +" degrees","(colorbar label)",
            cmin,cmax,color,paint,showColorbar);

        // warping in time & space
        Plot.plot(s1,s2,warpedDataT,"Time warped data","(colorbar label)",
            cmin,cmax,color,paint,showColorbar);
        Plot.plot(s1,s2,warpedDataS,"Space warped data","(colorbar label)",
            cmin,cmax,color,paint,showColorbar);

        try {
          Thread.sleep(1000);
        } catch(Exception e) {
          System.out.println("Oops");
        }

        // computed time & space shifts
        Plot.plot(s1,s2,data,spatialShiftsT,"Spatial shifts","Shift (m)",
            (float)shift_min,(float)shift_max,paint);
        Plot.plot(s1,s2,data,timeShifts,"Time shifts","Shift (ms)",
            (float)shift_min,(float)shift_max,paint);

        // computed slopes
        Plot.plot(s1,s2,data,slope_data,"Angled data slopes",
            "slope (s/km)",(float)shift_min,(float)shift_max,paint);
        Plot.plot(s1,s2,reference,slope_ref,"Flat data slopes",
            "slope (s/km)",(float)shift_min,(float)shift_max,paint);

        // slope difference
        cmin = -4.0f;
        cmax = 4.0f;
        color = true;
        showColorbar = true;
        Plot.plot(s1,s2,sub(slope_data,slope_ref),"Difference between slopes",
            "slope (s/km)",cmin,cmax,color,paint,showColorbar);
      }
    });
  }
}

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

  /**
  public RotateData() {
    
  }

  public static float[][] computeShifts(float[][] ref, float[][] rot) {
    int n1 = ref[0].length;
    int n2 = ref.length;
    Sampling s1 = new Sampling(n1);
    Sampling s2 = new Sampling(n2);
    int k = 20; // shift sampling interval 1/k
    double pmax = 6.0; // max shift allowed
    double sub_samp1 = 10.0; // bigger = more smooth
    double sub_samp2 = 5.0; // smaller = less smooth
    double str1 = 0.2; // strain in 1st dim
    double str2 = 0.7; // strain in 2nd dim
    DynamicWarpingK dwk = new DynamicWarpingK(k,-pmax,pmax,s1,s2);
    dwk.setSmoothness(sub_samp1,sub_samp2);
    dwk.setStrainLimits(-str1,str1,-str2,str2);
    float[][] shifts = dwk.findShifts(s1,ref,s1,rot);
  }
  */

  public static float[][] rotateData(float[][] ref, float[][] rot) {
    int n1 = ref[0].length;
    int n2 = ref.length;
    Sampling s1 = new Sampling(n1);
    Sampling s2 = new Sampling(n2);
    int k = 10; // shift sampling interval 1/k
    double pmax = 6.0; // max shift allowed
    double sub_samp1 = 50.0; // bigger = more smooth
    double sub_samp2 = 20.0; // smaller = less smooth
    double str1 = 0.3; // strain in 1st dim
    double str2 = 0.7; // strain in 2nd dim
    DynamicWarpingK dwk = new DynamicWarpingK(k,-pmax,pmax,s1,s2);
    dwk.setSmoothness(sub_samp1,sub_samp2);
    dwk.setStrainLimits(-str1,str1,-str2,str2);
    float[][] shifts = dwk.findShifts(s1,ref,s1,rot);
    float[][] rotatedData = dwk.applyShifts(s1,rot,shifts);
    return rotatedData;
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {     
        int n1 = 1800;
        double d1 = 0.0006;
        double f1 = 0.0;

        int n2 = 601;
        double d2 = 0.005;
        double f2 = -1.5;
        //Sampling s1 = new Sampling(n1,d1,f1);
        //Sampling s2 = new Sampling(n2,d2,f2);
        Sampling s1 = new Sampling(n1);
        Sampling s2 = new Sampling(n2);
        float[][] data0 = Utility.readL(n1,n2,
          "/Users/earias/home/git/mem_warp/bench/data/eliasData0.rsf@");
        float[][] data12 = Utility.readL(n1,n2,
          "/Users/earias/home/git/mem_warp/bench/data/eliasData12.rsf@");

        float[][] rotatedData = rotateData(data12,data0);

        float cmin = -1.5e-5f;
        float cmax = 1.5e-5f;
        Plot.plot(s1,s2,data0,"Data rotated 0 degrees","Amplitude",
          cmin,cmax,false);

        try {
          Thread.sleep(1000);
        } catch(Exception e) {
          System.out.println("Oops");
        }

        Plot.plot(s1,s2,data12,"Data rotated 12 degrees","Amplitude",
            cmin,cmax,false);
        Plot.plot(s1,s2,rotatedData,"RotatedData","Amplitude",
            cmin,cmax,false);
      }
    });
  }
}

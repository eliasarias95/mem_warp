package util;

import edu.mines.jtk.mosaic.*;
import edu.mines.jtk.awt.ColorMap;
import edu.mines.jtk.dsp.*;
import edu.mines.jtk.sgl.*;

import static edu.mines.jtk.util.ArrayMath.*;

import java.awt.Color;
import javax.swing.*;

/**
 * Plotting class to hold various plot methods.
 * @author Elias Arias, Colorado School of Mines, CWP
 * @version 26.01.2015
 */
public class Plot {
  /**
   * Plots a 1D array of floats with specified title.
   * @param s1 the sampling in the 1st-dimension
   * @param f the 1D array of floats to be plotted.
   * @param title the title of the image generated.
   */
  public static void plot(Sampling s1, float[] f, String title,
      String cbl, float cmin, float cmax) {
    PlotPanel pp = new PlotPanel(1,1,PlotPanel.Orientation.X1DOWN_X2RIGHT);
    pp.addPoints(s1,f);
    //PlotPanel pp = new PlotPanel();
    //pp.setHLabel("Width");
    //pp.setVLabel("Height");
    //pp.addColorBar(cbl);
    //pp.setColorBarFormat("%1.5G");
    //pp.setColorBarWidthMinimum(150);
    pp.setTitle(title);
    PlotFrame pf = new PlotFrame(pp);
    pf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //pf.setSize(900,1200);
    pf.setVisible(true);
  }
  /**
   * Plots a 2D array of floats with specified title.
   * @param s1 the sampling in the 1st-dimension
   * @param s2 the sampling in the 2nd-dimension
   * @param f the 2D array of floats to be plotted.
   * @param title the title of the image generated.
   */
  public static void plot(Sampling s1, Sampling s2, float[][] f, String title,
      String cbl, float cmin, float cmax, boolean color) {
    PlotPanel pp = new PlotPanel(1,1,PlotPanel.Orientation.X1DOWN_X2RIGHT);
    PixelsView pv = pp.addPixels(s1,s2,f);
    pv.setOrientation(PixelsView.Orientation.X1DOWN_X2RIGHT);
    pv.setClips(cmin,cmax);
    if (color) pv.setColorModel(ColorMap.JET);    
    pp.addTiledView(pv);
    //pp.setHLabel("Width");
    //pp.setVLabel("Height");
    pp.addColorBar(cbl);
    pp.setColorBarFormat("%1.3G");
    pp.setColorBarWidthMinimum(120);
    pp.setTitle(title);
    PlotFrame pf = new PlotFrame(pp);
    pf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pf.setSize(800,1100);
    pf.setVisible(true);
  }
}

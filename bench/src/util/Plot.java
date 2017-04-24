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
 * @author Elias Arias, OG-CWP
 * @version 26.04.2017
 */
public class Plot {

  private static final String PATH = System.getProperty("user.home") +
    "/home/git/mem_warp/bench/data/";
    private static double _ratio = 16.0/9.0; // slide ratio (e.g., 4:3, 16:9)
    private static int _dpi = 720; // dots per inch

  /**
   * Plots a 2D array of floats with specified title.
   * @param s1 the sampling in the 1st-dimension
   * @param s2 the sampling in the 2nd-dimension
   * @param f the 2D array of floats to be plotted.
   * @param title the title of the image generated.
   */
  public static void plot(Sampling s1, Sampling s2, float[][] f, float[][] g, 
      String title, String cbl, float cmin, float cmax, boolean paint) {
    PlotPanel pp = new PlotPanel(1,1,PlotPanel.Orientation.X1DOWN_X2RIGHT);
    PixelsView pv1 = pp.addPixels(s1,s2,f);
    PixelsView pv2 = pp.addPixels(s1,s2,g);
    //pv1.setClips(-1000,1000);
    //pv2.setClips(cmin,cmax);
    pv2.setOrientation(PixelsView.Orientation.X1DOWN_X2RIGHT);
    pv2.setColorModel(ColorMap.setAlpha(ColorMap.JET,0.3));
    pv1.setInterpolation(PixelsView.Interpolation.NEAREST);
    pv2.setInterpolation(PixelsView.Interpolation.NEAREST);
    pp.addTiledView(pv1);
    pp.addTiledView(pv2);
    pp.setHLabel("Distance (km)");
    pp.setVLabel("Time (s)");
    pp.addColorBar(cbl);
    pp.setColorBarFormat("%1.3G");
    pp.setColorBarWidthMinimum(100);
    pp.setTitle(title);
    PlotFrame pf = new PlotFrame(pp);
    pf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pf.setSize(1000,800);
    pf.setVisible(true);
    pf.setFontSizeForSlide(1.2,1.2,_ratio);
    if (paint) {
      pf.paintToPng(_dpi,(1919f)/_dpi,PATH+title+".png");
    }
  }

  /**
   * Plots a 2D array of floats with specified title.
   * @param s1 the sampling in the 1st-dimension
   * @param s2 the sampling in the 2nd-dimension
   * @param f the 2D array of floats to be plotted.
   * @param title the title of the image generated.
   */
  public static void plot(Sampling s1, Sampling s2, float[][] f, String title,
      String cbl, float cmin, float cmax, boolean color, boolean paint,
      boolean showColorbar) {
    PlotPanel pp = new PlotPanel(1,1,PlotPanel.Orientation.X1DOWN_X2RIGHT);
    PixelsView pv = pp.addPixels(s1,s2,f);
    pv.setOrientation(PixelsView.Orientation.X1DOWN_X2RIGHT);
    //pv.setClips(cmin,cmax);
    if (color) pv.setColorModel(ColorMap.JET);    
    pp.addTiledView(pv);
    pp.setHLabel("Distance (km)");
    pp.setVLabel("Time (s)");
    if (showColorbar) {
      pp.addColorBar(cbl);
      pp.setColorBarFormat("%1.3G");
      pp.setColorBarWidthMinimum(100);
    }
    pp.setTitle(title);
    PlotFrame pf = new PlotFrame(pp);
    pf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pf.setSize(1000,800);
    pf.setFontSizeForSlide(1.2,1.2,_ratio);
    if (paint) pf.paintToPng(_dpi,(1919f)/_dpi,PATH+title+".png");
    pf.setVisible(true);
  }
}

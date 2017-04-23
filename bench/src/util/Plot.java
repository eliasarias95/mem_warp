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

  /**
   * Plots a 2D array of floats with specified title.
   * @param s1 the sampling in the 1st-dimension
   * @param s2 the sampling in the 2nd-dimension
   * @param f the 2D array of floats to be plotted.
   * @param title the title of the image generated.
   */
  public static void plot(Sampling s1, Sampling s2, float[][] f, String title,
      String cbl, float cmin, float cmax, boolean color, boolean paint) {
    PlotPanel pp = new PlotPanel(1,1,PlotPanel.Orientation.X1DOWN_X2RIGHT);
    PixelsView pv = pp.addPixels(s1,s2,f);
    pv.setOrientation(PixelsView.Orientation.X1DOWN_X2RIGHT);
    //pv.setClips(cmin,cmax);
    if (color) pv.setColorModel(ColorMap.JET);    
    pp.addTiledView(pv);
    pp.setHLabel("Distance (km)");
    pp.setVLabel("Time (s)");
    pp.addColorBar(cbl);
    pp.setColorBarFormat("%1.3G");
    pp.setColorBarWidthMinimum(120);
    pp.setTitle(title);
    PlotFrame pf = new PlotFrame(pp);
    pf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pf.setSize(1000,800);
    float fw = 0.8f; //fraction width for slide
    float fh = 0.8f; //fraction height for slide
    double ratio = 16.0/9.0; // slide ratio (e.g., 4:3, 16:9)
    int dpi = 720; // dots per inch
    pf.setFontSizeForSlide(fw,fh,ratio);
    if (paint) pf.paintToPng(dpi,(1920f*fw-1)/dpi,PATH+title+".png");
    pf.setVisible(true);
  }
}

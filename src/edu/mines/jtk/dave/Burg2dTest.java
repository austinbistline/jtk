/****************************************************************************
Copyright (c) 2005, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is 
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package edu.mines.jtk.dave;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import edu.mines.jtk.dsp.*;
import edu.mines.jtk.gui.*;
import edu.mines.jtk.mosaic.*;
import edu.mines.jtk.util.*;
import static edu.mines.jtk.util.MathPlus.*;
import static edu.mines.jtk.mosaic.Mosaic.*;

/**
 * Test Burg's algorithm for 2-D images.
 * @author Dave Hale, Colorado School of Mines
 * @version 2005.11.24
 */
public class Burg2dTest {

  public static void main(String[] args) {
    double theta = 0.0;
    int m = 2;
    if (args.length>0)
      theta = Double.valueOf(args[0]);
    if (args.length>1)
      m = Integer.valueOf(args[1]);
    int n1 = 32;
    int n2 = 32;
    float[][] x = makeImage(n1,n2,theta);
    float clip = max(abs(Array.min(x)),abs(Array.max(x)));
    System.out.println("xmin="+Array.min(x)+" xmax="+Array.max(x));
    plotImage(-clip,clip,x);
    float[][] f1 = burg2d1(m,x);
    System.out.println("f1min="+Array.min(f1)+" f1max="+Array.max(f1));
    plotImage(-clip,clip,f1);
    float[][] f4 = burg2d4(m,f1);
    System.out.println("f4min="+Array.min(f4)+" f4max="+Array.max(f4));
    plotImage(-clip,clip,f4);
  }

  private static float[][] burg2d1(int m, float[][] x) {
    int n2 = x.length;
    int n1 = x[0].length;
    float[][] f = Array.copy(x);
    float[][] b = Array.copy(x);
    for (int k=0; k<m; ++k) {
      int k1 = (k+2)/2;
      int k2 = (k+1)/2;
      int j1 = (k+1)%2;
      int j2 = (k+0)%2;
      float fb = 0.0f;
      float ff = 0.0f;
      float bb = 0.0f;
      for (int i2=k2; i2<n2; ++i2) {
        for (int i1=k1; i1<n1; ++i1) {
          float fk = f[i2][i1];
          float bk = b[i2-j2][i1-j1];
          fb += fk*bk;
          ff += fk*fk;
          bb += bk*bk;
        }
      }
      float cnum = 2.0f*fb;
      float cden = ff+bb;
      System.out.println("cden="+cden);
      float ck = (cden!=0.0f)?cnum/cden:0.0f;
      System.out.println("c["+(k+1)+"] = "+ck);
      for (int i2=n2-1; i2>=k2; --i2) {
        for (int i1=n1-1; i1>=k1; --i1) {
          float fk = f[i2][i1];
          float bk = b[i2-j2][i1-j1];
          f[i2][i1] = fk-ck*bk;
          b[i2][i1] = bk-ck*fk;
        }
      }
    }
    return f;
  }

  private static float[][] burg2d4(int m, float[][] x) {
    int n2 = x.length;
    int n1 = x[0].length;
    float[][] f = Array.copy(x);
    float[][] b = Array.copy(x);
    for (int k=0; k<m; ++k) {
      int k2 = (k+2)/2;
      int k1 = (k+1)/2;
      int j2 = (k+1)%2;
      int j1 = (k+0)%2;
      float fb = 0.0f;
      float ff = 0.0f;
      float bb = 0.0f;
      for (int i2=0; i2<n2-k2; ++i2) {
        for (int i1=k1; i1<n1; ++i1) {
          float fk = f[i2][i1];
          float bk = b[i2+j2][i1-j1];
          fb += fk*bk;
          ff += fk*fk;
          bb += bk*bk;
        }
      }
      float cnum = 2.0f*fb;
      float cden = ff+bb;
      System.out.println("cden="+cden);
      float ck = (cden!=0.0f)?cnum/cden:0.0f;
      System.out.println("c["+(k+1)+"] = "+ck);
      for (int i2=0; i2<n2-k2; ++i2) {
        for (int i1=n1-1; i1>=k1; --i1) {
          float fk = f[i2][i1];
          float bk = b[i2+j2][i1-j1];
          f[i2][i1] = fk-ck*bk;
          b[i2][i1] = bk-ck*fk;
        }
      }
    }
    return f;
  }

  private static float[][] makeImage(int n1, int n2, double theta) {
    int nk = 10;
    double dk = 0.4*DBL_PI/nk;
    double fk = dk;
    double ct = cos(theta*DBL_PI/180.);
    double st = sin(theta*DBL_PI/180.);
    float[][] f = new float[n2][n1];
    for (int i2=0; i2<n2; ++i2) {
      double x2 = i2-n2/2;
      for (int i1=0; i1<n1; ++i1) {
        double x1 = i1-n1/2;
        if (x2>0.0) x1 += 1.0;
        for (int ik=0; ik<nk; ++ik) {
          double k = fk+ik*dk;
          double fi = cos(k*(ct*x1+st*x2));
          f[i2][i1] += (float)fi;
        }
      }
    }
    return f;
  }

  private static void plotImage(float[][] f) {
    plotImage(Array.min(f),Array.max(f),f);
  }
  private static void plotImage(String filename, float[][] f) {
    plotImage(filename,Array.min(f),Array.max(f),f);
  }
  private static Mosaic plotImage(
    float fmin, float fmax, float[][] f) 
  {
    int n2 = f.length;
    int n1 = f[0].length;
    Set<Mosaic.AxesPlacement> axesPlacement = EnumSet.noneOf(
      Mosaic.AxesPlacement.class
    );
    Mosaic mosaic = new Mosaic(1,1,axesPlacement);
    mosaic.setBackground(Color.WHITE);
    mosaic.setFont(new Font("SansSerif",Font.PLAIN,18));
    mosaic.setPreferredSize(new Dimension(550,500));

    PixelsView pv = new PixelsView(f);
    pv.setOrientation(PixelsView.Orientation.X1DOWN_X2RIGHT);
    pv.setInterpolation(PixelsView.Interpolation.NEAREST);
    pv.setColorMap(PixelsView.ColorMap.GRAY);
    //pv.setClips(fmin,fmax);

    Tile tile = mosaic.getTile(0,0);
    tile.addTiledView(pv);

    ModeManager modeManager = mosaic.getModeManager();
    TileZoomMode zoomMode = new TileZoomMode(modeManager);
    zoomMode.setActive(true);

    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(mosaic,BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
    return mosaic;
  }
  private static void plotImage(
    String filename, float fmin, float fmax, float[][] f) 
  {
    Mosaic mosaic = plotImage(fmin,fmax,f);
    try {
      mosaic.paintToPng(300,6,filename+"Flat.png");
    } catch (IOException ioe) {
      System.out.println("Cannot write image to file: "+filename);
    }
  }
}

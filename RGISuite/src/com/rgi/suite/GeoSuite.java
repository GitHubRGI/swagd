/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.suite;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import utility.PropertiesAction;
import utility.TileStoreUtility;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.suite.uielements.windows.PackagerWindow;
import com.rgi.suite.uielements.windows.TileReadersOptionWindow;
import com.rgi.suite.uielements.windows.TilerWindow;
import com.rgi.view.MapViewWindow;

/**
 * Entry point for the program.
 *
 * @author Duff Means
 * @author Steven D. Lander
 */
public class GeoSuite
{
    /**
     * Code decision point for running in either GUI mode or headless (command-line) mode.
     *
     * @param args A string array of command line arguments.
     */
    public static void main(final String[] args)
    {
        if(args != null && args.length > 0)
        {
            GeoSuite.runHeadless(args);
        }
        else
        {
            @SuppressWarnings("unused")
            final GeoSuite geoSuite = new GeoSuite();
        }
    }

    private GeoSuite()
    {
        this.settings = new Settings(new File("settings.txt"));

        SwingUtilities.invokeLater(() -> this.startGui());
    }

    @SuppressWarnings("deprecation")
    private void startGui()
    {
        final JPanel contentPanel = new JPanel(new CardLayout());
        final JPanel navPanel     = new JPanel(new CardLayout());

        final JFrame suiteWindow = new JFrame();

        final Properties props = new Properties();

        try(InputStream inputStream = this.getClass().getResourceAsStream("geosuite.properties"))
        {
            props.load(inputStream);
        }
        catch(final IllegalArgumentException | IOException ex)
        {
            JOptionPane.showMessageDialog(null, "RGI Suite", "Unable to load properties", JOptionPane.OK_OPTION);
            ex.printStackTrace();
        }

        final Container c = suiteWindow.getContentPane();
        c.setLayout(new BorderLayout());

        c.add(contentPanel, BorderLayout.CENTER);
        c.add(navPanel,     BorderLayout.SOUTH);

        suiteWindow.setTitle("RGI Tiling and Packaging Suite");
        suiteWindow.setSize(540, 240);
        suiteWindow.setResizable(false);
        suiteWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // Show the window
        suiteWindow.addWindowListener(new WindowAdapter()
                                      {
                                          @Override
                                          public void windowClosing(final WindowEvent event)
                                          {
                                              final int option = JOptionPane.showConfirmDialog(suiteWindow, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
                                              if(option == JOptionPane.YES_OPTION)
                                              {
                                                  System.exit(0);
                                              }
                                          }
                                      });

        // main buttons
        final JPanel mainButtonPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        final JButton tileButton = new JButton(new PropertiesAction(props, "tile")
                                   {
                                       private static final long serialVersionUID = -3249428374853166484L;

                                       @Override
                                       public void actionPerformed(final ActionEvent event)
                                       {
                                           final JFrame frame = new TilerWindow(GeoSuite.this.settings);
                                           frame.setLocationRelativeTo(null);
                                           frame.setVisible(true);
                                       }
                                   });

        tileButton.setHideActionText(true);
        tileButton.setMargin(new Insets(0, 0, 0, 0));

        final JButton gpkgButton = new JButton(new PropertiesAction(props, "gpkg")
                                   {
                                       private static final long serialVersionUID = -1836754318915912580L;

                                       @Override
                                       public void actionPerformed(final ActionEvent event)
                                       {
                                           final JFrame frame = new PackagerWindow(GeoSuite.this.settings);
                                           frame.setLocationRelativeTo(null);
                                           frame.setVisible(true);
                                       }
                                   });

        gpkgButton.setHideActionText(true);
        gpkgButton.setMargin(new Insets(0, 0, 0, 0));

        final JButton viewButton = new JButton(new PropertiesAction(props, "view")
                                   {
                                       private static final long serialVersionUID = 1882624675173160883L;

                                       private static final String LastLocationSettingName = "ui.viewer.lastLocation";

                                       @Override
                                       public void actionPerformed(final ActionEvent event)
                                       {
                                           final String startDirectory = GeoSuite.this.settings.get(LastLocationSettingName, System.getProperty("user.home"));

                                           final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

                                           fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                                           fileChooser.setMultiSelectionEnabled(true);

                                           fileChooser.addActionListener(chooseEvent -> { if(JFileChooser.APPROVE_SELECTION.equals(chooseEvent.getActionCommand()))
                                                                                          {
                                                                                              final File[] files = fileChooser.getSelectedFiles();

                                                                                              if(files.length > 0)
                                                                                              {
                                                                                                  GeoSuite.this.settings.set(LastLocationSettingName, files[0].getParent());
                                                                                                  GeoSuite.this.settings.save();

                                                                                                  final TileReadersOptionWindow tileReadersOptionWindow = new TileReadersOptionWindow(TileStoreUtility.getTileStoreReaderAdapters(true, files),
                                                                                                                                                                                      readers -> { try
                                                                                                                                                                                                   {
                                                                                                                                                                                                       final JFrame viewWindow = new MapViewWindow(readers);
                                                                                                                                                                                                       viewWindow.setLocationRelativeTo(null);
                                                                                                                                                                                                       viewWindow.setVisible(true);
                                                                                                                                                                                                   }
                                                                                                                                                                                                   catch(final TileStoreException ex)
                                                                                                                                                                                                   {
                                                                                                                                                                                                       JOptionPane.showMessageDialog(null, "Map View", "Unable to view file selection: " + ex.getMessage(), JOptionPane.ERROR_MESSAGE);
                                                                                                                                                                                                       ex.printStackTrace();
                                                                                                                                                                                                   }
                                                                                                                                                                                                 });

                                                                                                  if(tileReadersOptionWindow.needsInput())
                                                                                                  {
                                                                                                      tileReadersOptionWindow.setLocationRelativeTo(null);
                                                                                                      tileReadersOptionWindow.setVisible(true);
                                                                                                  }
                                                                                                  else
                                                                                                  {
                                                                                                      tileReadersOptionWindow.execute();
                                                                                                  }
                                                                                              }
                                                                                          }
                                                                                        });

                                           fileChooser.showOpenDialog(suiteWindow);

                                       }
                                   });

        viewButton.setHideActionText(true);
        viewButton.setMargin(new Insets(0, 0, 0, 0));

        mainButtonPanel.add(tileButton, gbc);
        mainButtonPanel.add(gpkgButton, gbc);
        mainButtonPanel.add(viewButton, gbc);

        contentPanel.add(mainButtonPanel);

        suiteWindow.setLocationRelativeTo(null);
        suiteWindow.setVisible(true);
    }

    private static void runHeadless(@SuppressWarnings("unused") final String[] args)
    {
        // TODO
        System.out.println("Running headless is not yet supported.");
    }

    private final Settings settings;
}

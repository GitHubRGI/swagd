/*  Copyright (C) 2014 Reinventing Geospatial, Inc
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
 *  or write to the Free Software Foundation, Inc., 59 Temple Place -
 *  Suite 330, Boston, MA 02111-1307, USA.
 */

package com.rgi.suite2;

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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.rgi.suite.PropertiesAction;
import com.rgi.suite.Settings;

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
        suiteWindow.setSize(640, 480);
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

        // Settings panel / button
        final JPanel settingsNavPanel = new JPanel(new GridBagLayout());

        final JButton settingsButton = new JButton(new PropertiesAction(props, "pref")
                                             {
                                                 private static final long serialVersionUID = 5258278444574348376L;

                                                 @Override
                                                 public void actionPerformed(final ActionEvent event)
                                                 {
                                                     final JFrame frame = new SettingsWindow(GeoSuite.this.settings);
                                                     frame.pack();
                                                     frame.setVisible(true);
                                                 }
                                             });

        settingsButton.setHideActionText(true);
        settingsButton.setMargin(new Insets(0, 0, 0, 0));
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 10, 10, 10);

        settingsNavPanel.add(settingsButton, gbc);

        navPanel.add(settingsNavPanel);


        suiteWindow.setVisible(true);
    }

    private static void runHeadless(@SuppressWarnings("unused") final String[] args)
    {
        // TODO
        System.out.println("Running headless is not yet supported.");
    }

    private final Settings settings;
}

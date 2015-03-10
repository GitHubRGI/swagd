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

package com.rgi.suite;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class SettingsWindow extends JFrame
{
    private static final long serialVersionUID = 4509050309011262891L;

    private static String[] imageTypes = new String[] { "JPG", "PNG" }; // TODO enum?
    private static String[] crsTypes = new String[] {"EPSG:3857", "EPSG:3395", "EPSG:4326"}; // TODO enum?

    protected static final String OutputLocationSettingName     = "outputLocation";
    protected static final String TileWidthSettingName          = "tileWidth";
    protected static final String TileHeightSettingName         = "tileHeight";
    protected static final String OutputImageFormatSettingName  = "outputImageFormat";
    protected static final String OutputImageQualitySettingName = "outputImageQuality";
    protected static final String NoDataColorSettingName        = "noDataColor";
    protected static final String OutputCrsSettingName          = "outputCrs";

    protected static final String DefaultOutputLocation     = System.getProperty("user.home");
    protected static final int    DefaultTileWidth          = 256;
    protected static final int    DefaultTileHeight         = 256;
    protected static final String DefaultOutputImageFormat  = imageTypes[0];
    protected static final int    DefaultOutputImageQuality = 70;
    protected static final Color  DefaultNoDataColor        = new Color(0, 0, 0, 0);
    protected static final String DefaultOutputCrs          = crsTypes[0];

    private JTextField        tileOutputPathField;
    private JComboBox<String> outputCrs;
    private JSpinner          tileHeightSpinner;
    private JSpinner          tileWidthSpinner;
    private JSpinner          outputQualitySpinner;
    private JComboBox<String> outputImageType;
    private SwatchButton      nullDataColorButton;

    private final JPanel   contentPane;
    private final Settings settings;

    public SettingsWindow(final Settings settings)
    {
        this.setTitle("Settings");

        this.settings = settings;

        this.contentPane = new JPanel(new GridBagLayout());

        this.buildContentPane();

        this.add(this.contentPane);

        //this.load(this.context.getSettings());
    }

    protected void buildContentPane()
    {
        // Settings input
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;

        this.tileOutputPathField = new JTextField(this.settings.get(OutputLocationSettingName, DefaultOutputLocation));
        final JButton tileOutputPathButton = new JButton("\u2026");
        final Insets i = tileOutputPathButton.getMargin();

        if(i == null)
        {
            throw new RuntimeException("JButton::getMargin returned null");
        }

        tileOutputPathButton.setMargin(new Insets(i.top, 1, i.bottom, 1));
        gbc.gridy = 0;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Tiles Folder:"), gbc);
        gbc.weightx = 1;
        this.contentPane.add(this.tileOutputPathField, gbc);
        gbc.weightx = 0;
        this.contentPane.add(tileOutputPathButton, gbc);
        tileOutputPathButton.addActionListener(e -> { final JFileChooser fileChooser = new JFileChooser(new File(this.tileOutputPathField.getText()));

                                                      fileChooser.setMultiSelectionEnabled(false);
                                                      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                                                      final int option = fileChooser.showOpenDialog(this.contentPane);

                                                      if(option == JFileChooser.APPROVE_OPTION)
                                                      {
                                                          this.tileOutputPathField.setText(fileChooser.getSelectedFile().getPath());
                                                      }
                                                    });

        final int tileWidth = this.settings.get(TileWidthSettingName, Integer::parseInt, DefaultTileWidth);

        this.tileWidthSpinner = new JSpinner(new SpinnerNumberModel(tileWidth,
                                                                    128,
                                                                    2048,
                                                                    128));
        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Tile Width: "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.tileWidthSpinner, gbc);

        final int tileHeight = this.settings.get(TileHeightSettingName, Integer::parseInt, DefaultTileHeight);

        this.tileHeightSpinner = new JSpinner(new SpinnerNumberModel(tileHeight,
                                                                     128,
                                                                     2048,
                                                                     128));
        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Tile Height: "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.tileHeightSpinner, gbc);

        this.outputImageType = new JComboBox<>(new DefaultComboBoxModel<>(imageTypes));
        this.outputImageType.setSelectedItem(this.settings.get(OutputImageFormatSettingName, DefaultOutputImageFormat));
        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Output Type: "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.outputImageType, gbc);

        this.outputImageType.addItemListener(e -> { this.outputQualitySpinner.setEnabled("JPG".equals(this.outputImageType.getSelectedItem())); });

        final int imageQuality = this.settings.get(OutputImageQualitySettingName, Integer::parseInt, DefaultOutputImageQuality);

        this.outputQualitySpinner = new JSpinner(new SpinnerNumberModel(imageQuality,
                                                                        10,
                                                                        100,
                                                                        1));
        this.outputQualitySpinner.setEnabled(false);
        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Output Quality (JPG): "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.outputQualitySpinner, gbc);

        this.nullDataColorButton = new SwatchButton("...");
        this.nullDataColorButton.setColor(this.settings.get(NoDataColorSettingName,
                                                            SettingsWindow::colorFromString,
                                                            DefaultNoDataColor));
        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("No Data Color: "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.nullDataColorButton, gbc);
        this.nullDataColorButton.addActionListener(e -> { final Color c = JColorChooser.showDialog(this.contentPane,
                                                                                                   "Choose No Data color...",
                                                                                                   this.nullDataColorButton.getColor());

                                                          if(c != null)
                                                          {
                                                              this.nullDataColorButton.setColor(c);
                                                          }
                                                        });

        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.outputCrs = new JComboBox<>(new DefaultComboBoxModel<>(SettingsWindow.crsTypes));
        this.outputCrs.setSelectedItem(this.settings.get(OutputCrsSettingName, DefaultOutputCrs));
        this.contentPane.add(new JLabel("Output Profile: "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.outputCrs, gbc);

        // OK/Cancel buttons
        // save settings
        final JButton okButton = new JButton(new AbstractAction()
                                             {
                                                 private static final long serialVersionUID = -2265844182174484489L;

                                                 @Override
                                                 public void actionPerformed(final ActionEvent event)
                                                 {
                                                     SettingsWindow.this.apply();
                                                     SettingsWindow.this.settings.save();
                                                     SettingsWindow.this.closeFrame();
                                                 }
                                             });
        okButton.setText("OK");
        //okButton.setHideActionText(true);
        okButton.setMargin(new Insets(0, 0, 0, 0));

        // cancel settings editing
        final JButton cancelButton = new JButton(new AbstractAction()
                                                 {
                                                     private static final long serialVersionUID = -4389758606354266920L;

                                                     @Override
                                                     public void actionPerformed(final ActionEvent event)
                                                     {
                                                         SettingsWindow.this.closeFrame();
                                                     }
                                                 });

        cancelButton.setText("Cancel");
        //cancelButton.setHideActionText(true);
        cancelButton.setMargin(new Insets(0, 0, 0, 0));

        gbc.gridwidth = 2;
        gbc.weightx = 1;

        ++gbc.gridy;
        //gbc.insets = new Insets(10, 10, 10, 10);
        this.contentPane.add(cancelButton, gbc);
        //gbc.anchor = GridBagConstraints.EAST;
        ++gbc.gridy;
        this.contentPane.add(okButton, gbc);
    }

    public static Color colorFromString(final String string)
    {
        final Pattern colorPattern = Pattern.compile("(\\d+),(\\d+),(\\d+),(\\d+)");

        final Matcher colorMatcher = colorPattern.matcher(string);

        if(colorMatcher.matches())
        {
            try
            {
                return new Color(Integer.parseInt(colorMatcher.group(1)),
                                 Integer.parseInt(colorMatcher.group(2)),
                                 Integer.parseInt(colorMatcher.group(3)),
                                 Integer.parseInt(colorMatcher.group(4)));
            }
            catch(final IllegalArgumentException ex)
            {
                // Do nothing, fall through to return null
            }
        }

        return null;
    }

    public static String colorToString(final Color color)
    {
        return color == null ? null
                             : String.format("%d,%d,%d,%d",
                                             color.getRed(),
                                             color.getGreen(),
                                             color.getBlue(),
                                             color.getAlpha());
    }

    private void closeFrame()
    {
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void apply()
    {
        this.settings.set(OutputLocationSettingName,     this.tileOutputPathField.getText());
        this.settings.set(TileWidthSettingName,          this.tileWidthSpinner.getValue().toString());
        this.settings.set(TileHeightSettingName,         this.tileHeightSpinner.getValue().toString());
        this.settings.set(OutputImageFormatSettingName,  (String)this.outputImageType.getSelectedItem());
        this.settings.set(OutputImageQualitySettingName, this.outputQualitySpinner.getValue().toString());
        this.settings.set(NoDataColorSettingName,        this.nullDataColorButton.getColor(), SettingsWindow::colorToString);
        this.settings.set(OutputCrsSettingName,          (String)this.outputCrs.getSelectedItem());
    }
}

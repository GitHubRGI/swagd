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
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.rgi.common.task.Task;
import com.rgi.common.tile.TileOrigin;

public class SettingsWindow extends AbstractWindow
{
    private JTextField                  tileOutputPathField;
    private JComboBox<TileOrigin>       tileOriginChoice;
    private JComboBox<Settings.Profile> outputProfileChoice;
    private JSpinner                    tileHeightSpinner;
    private JSpinner                    tileWidthSpinner;
    private JSpinner                    outputQualitySpinner;
    private JComboBox<Settings.Type>    outputImageType;
    private SwatchButton                nullDataColorButton;

    public SettingsWindow(ApplicationContext context)
    {
        super(context);
    }

    @Override
    public void activate()
    {
        this.load(this.context.getSettings());
    }

    @Override
    protected void buildContentPane()
    {
        this.contentPane = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;

        this.tileOutputPathField = new JTextField();
        JButton tileOutputPathButton = new JButton("\u2026");
        Insets i = tileOutputPathButton.getMargin();

        if(i == null)
        {
            throw new RuntimeException("JButton::getMargin returned null");
        }

        Insets j = new Insets(i.top, 1, i.bottom, 1);
        tileOutputPathButton.setMargin(j);
        gbc.gridy = 0;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Tiles Folder:"), gbc);
        gbc.weightx = 1;
        this.contentPane.add(this.tileOutputPathField, gbc);
        gbc.weightx = 0;
        this.contentPane.add(tileOutputPathButton, gbc);
        tileOutputPathButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(false);
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fc.showOpenDialog(this.contentPane);
            if(option == JFileChooser.APPROVE_OPTION)
            {
                this.tileOutputPathField.setText(fc.getSelectedFile().getPath());
            }
        });

        this.tileOriginChoice = new JComboBox<>();
        this.tileOriginChoice.setModel(new DefaultComboBoxModel<>(TileOrigin.values()));
        ++gbc.gridy;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Tile Origin:"), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.tileOriginChoice, gbc);

        this.tileWidthSpinner = new JSpinner(new SpinnerNumberModel(256, 128, 2048, 128));
        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Tile Width: "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.tileWidthSpinner, gbc);

        this.tileHeightSpinner = new JSpinner(new SpinnerNumberModel(256, 128, 2048, 128));
        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Tile Height: "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.tileHeightSpinner, gbc);

        this.outputImageType = new JComboBox<>(new DefaultComboBoxModel<>(Settings.Type.values()));
        this.outputImageType.setSelectedItem(Type.PNG);
        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Output Type: "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.outputImageType, gbc);

        this.outputQualitySpinner = new JSpinner(new SpinnerNumberModel(70, 10, 100, 1));
        this.outputQualitySpinner.setEnabled(false);
        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Output Quality (JPG): "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.outputQualitySpinner, gbc);

        this.outputImageType.addItemListener(e -> {
            this.outputQualitySpinner.setEnabled(Type.JPG.equals(this.outputImageType.getSelectedItem()));
        });

        this.nullDataColorButton = new SwatchButton("...");
        this.nullDataColorButton.setColor((Color)Settings.Setting.NoDataColor.getDefaultValue());
        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("No Data Color: "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.nullDataColorButton, gbc);
        this.nullDataColorButton.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this.contentPane, "Choose No Data color...", this.nullDataColorButton.getColor());
            if(c != null)
            {
                this.nullDataColorButton.setColor(c);
            }
        });

        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.outputProfileChoice = new JComboBox<>(new DefaultComboBoxModel<>(Settings.Profile.values()));
        this.contentPane.add(new JLabel("Output Profile: "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.outputProfileChoice, gbc);
    }

    @Override
    protected void buildNavPane()
    {
        this.navPane = new JPanel(new GridBagLayout());

        Properties props = this.context.getProperties();

        // save settings
        JButton okButton = new JButton(new PropertiesAction(props, "ok")
        {
            /**
             * Generated serial
             */
            private static final long serialVersionUID = -2265844182174484489L;

            @Override
            public void actionPerformed(ActionEvent event)
            {
                Settings settings = SettingsWindow.this.context.getSettings();
                try
                {
                    SettingsWindow.this.apply(settings);
                    settings.save();
                }
                catch(Exception e)
                {
                    JOptionPane.showMessageDialog(SettingsWindow.this.contentPane, "Unable to save settings!");
                    e.printStackTrace();
                }
                SettingsWindow.this.closeSettings();
            }
        });
        okButton.setHideActionText(true);
        okButton.setMargin(new Insets(0, 0, 0, 0));

        // cancel settings editing
        JButton cancelButton = new JButton(new PropertiesAction(props, "cancel")
        {
            /**
             * Generated serial
             */
            private static final long serialVersionUID = -4389758606354266920L;

            @Override
            public void actionPerformed(ActionEvent event)
            {
                SettingsWindow.this.closeSettings();
            }
        });
        cancelButton.setHideActionText(true);
        cancelButton.setMargin(new Insets(0, 0, 0, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        this.navPane.add(cancelButton, gbc);
        gbc.anchor = GridBagConstraints.EAST;
        this.navPane.add(okButton, gbc);
    }

    private void closeSettings()
    {
        Task task = this.context.getActiveTask();
        if(task == null)
        {
            this.context.transitionTo(Window.MAIN);
        }
        else
        {
            this.context.transitionTo(Window.FILECHOOSER);
        }
    }

    public void load(Settings settings)
    {
        this.tileOutputPathField.setText(settings.get(Setting.TileFolder)); // where am I going to write files to
        this.nullDataColorButton.setColor(settings.getColor(Setting.NoDataColor));
        Integer quality = Integer.parseInt(settings.get(Setting.Quality));
        ((SpinnerNumberModel)this.outputQualitySpinner.getModel()).setValue(quality);
        int tileHeight = Integer.parseInt(settings.get(Setting.TileHeight));
        ((SpinnerNumberModel)this.tileHeightSpinner.getModel()).setValue(tileHeight);
        int tileWidth = Integer.parseInt(settings.get(Setting.TileWidth));
        ((SpinnerNumberModel)this.tileWidthSpinner.getModel()).setValue(tileWidth);
        try
        {
            this.tileOriginChoice.setSelectedItem(TileOrigin.valueOf(settings.get(Setting.TileOrigin)));
        }
        catch(Exception e)
        {
            this.tileOriginChoice.setSelectedItem(Setting.TileOrigin.getDefaultValue());
        }
        this.outputProfileChoice.setSelectedItem(Profile.valueOf(settings.get(Setting.CrsProfile)));
    }

    public void apply(Settings settings) throws IOException
    {
        settings.set(Setting.TileFolder, this.tileOutputPathField.getText());
        settings.set(Setting.NoDataColor, this.nullDataColorButton.getColor());
        settings.set(Setting.Quality, this.outputQualitySpinner.getValue().toString());
        settings.set(Setting.TileHeight, this.tileHeightSpinner.getValue().toString());
        settings.set(Setting.TileWidth, this.tileWidthSpinner.getValue().toString());
        settings.set(Setting.TileOrigin, ((TileOrigin)this.tileOriginChoice.getSelectedItem()).name());
        settings.set(Setting.CrsProfile, ((Profile)this.outputProfileChoice.getSelectedItem()).name());
    }
}

package com.rgi.suite;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.rgi.suite.ApplicationContext.Window;
import com.rgi.suite.Settings.Profile;
import com.rgi.suite.Settings.Setting;
import com.rgi.suite.Settings.Type;

public class PackageOutput extends AbstractWindow {

	private JTextField outputFileName;
	private JComboBox<Profile> outputProfileChoice;
	private JComboBox<Type> outputImageType;

	protected PackageOutput(ApplicationContext context) {
		super(context);
	}

	@Override
	protected void buildContentPane() {
		this.contentPane = new JPanel(new GridBagLayout());

		// Initial UI values
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;

        // Add the output geopackage name elements
        this.outputFileName = new JTextField();
        JButton outputFileNameButton = new JButton("\u2026");
        ++gbc.gridy;
        Insets i = outputFileNameButton.getMargin();
        Insets j = new Insets(i.top, 1, i.bottom, 1);
        outputFileNameButton.setMargin(j);
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Output File Name:"), gbc);
        gbc.weightx = 1;
        this.contentPane.add(this.outputFileName, gbc);
        gbc.weightx = 0;
        this.contentPane.add(outputFileNameButton, gbc);
        outputFileNameButton.addActionListener(e ->
                                                {
                                                    JFileChooser fc = new JFileChooser();
                                                    fc.setMultiSelectionEnabled(false);
                                                    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                                    fc.addChoosableFileFilter(new FileFilter()
                                                    {
                                                        @Override
                                                        public String getDescription()
                                                        {
                                                               return "GeoPackage Files (*.gpkg)";
                                                        }

                                                        @Override
                                                        public boolean accept(File pathname) {
                                                                return pathname.getName().toLowerCase().endsWith(".gpkg");
                                                        }
                                                    });
                                                    int option = fc.showOpenDialog(this.contentPane);
                                                    if (option == JFileChooser.APPROVE_OPTION) {
                                                        this.outputFileName.setText(fc.getSelectedFile().getPath());
                                                    }
                                                });

        // Add the input SRS combo box
        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.outputProfileChoice = new JComboBox<>(new DefaultComboBoxModel<>(
                Settings.Profile.values()));
        this.contentPane.add(new JLabel("Output Profile: "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.outputProfileChoice, gbc);

        // Add the output image type combo box
        this.outputImageType = new JComboBox<>(new DefaultComboBoxModel<>(Settings.Type.values()));
        this.outputImageType.setSelectedItem(Type.PNG);
        ++gbc.gridy;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Output Type: "), gbc);
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        this.contentPane.add(this.outputImageType, gbc);
	}

	@Override
	protected void buildNavPane() {
		// Navigation buttons go here.  These will either cancel and return
		// to the main window or continue on to build the geopackage.
		this.navPane = new JPanel(new GridBagLayout());
		Properties properties = this.context.getProperties();

		// move to next step
		JButton nextButton = new JButton(new PropertiesAction(properties, "next")
		{
			/**
			 * Generated serial
			 */
			private static final long serialVersionUID = -5059914828508260038L;

			@Override
			public void actionPerformed(ActionEvent event)
			{
				PackageOutput.this.executePackaging();
			}
		});
		nextButton.setHideActionText(true);

        // cancel packaging workflow
        JButton cancelButton = new JButton(new PropertiesAction(properties, "cancel")
        {
            /**
             * Generated serial
             */
            private static final long serialVersionUID = -4389758606354266920L;

            @Override
            public void actionPerformed(ActionEvent event)
            {
                PackageOutput.this.context.transitionTo(Window.MAIN);
            }
        });
        cancelButton.setHideActionText(true);
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        // Add buttons to pane
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);
        this.navPane.add(cancelButton, gbc);
        gbc.anchor = GridBagConstraints.EAST;
        this.navPane.add(nextButton, gbc);
	}

	private void executePackaging()
	{
		Settings settings = this.context.getSettings();
		settings.set(Setting.OutputFileName, this.outputFileName.getText());
		settings.set(Setting.TileType, ((Type) (this.outputImageType.getSelectedItem())).name());
		settings.set(Setting.CrsProfile, (Profile) (this.outputProfileChoice.getSelectedItem()));
		this.context.transitionTo(Window.PROGRESS);
		this.context.getActiveTask().execute(settings);
	}
}

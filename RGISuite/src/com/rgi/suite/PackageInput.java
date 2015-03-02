package com.rgi.suite;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rgi.suite.ApplicationContext.Window;
import com.rgi.suite.Settings.Setting;

/**
 * Gather additional information during Packaging workflow.
 * 
 * @author Steven D. Lander
 *
 */
public class PackageInput extends AbstractWindow {

	//private JTextField tileSetName;
	//private JTextField tileSetDescription;
	
	private JTextField tileSetName;
	private JTextField tileSetDescription;
	/**
	 * @param context The UI application context.
	 */
	protected PackageInput(ApplicationContext context) {
		super(context);
	}

	@Override
	protected void buildContentPane() {
		// build what the UI will look like
		this.contentPane = new JPanel(new GridBagLayout());
		
		// Initial UI values
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;

        // Add the tile set name elements
		this.tileSetName = new JTextField();
        gbc.gridy = 0;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Tile Set Name:"), gbc);
        gbc.weightx = 1;
        this.contentPane.add(this.tileSetName, gbc);

        // Add the tile set description elements
		this.tileSetDescription = new JTextField();
        ++gbc.gridy;
        gbc.weightx = 0;
        this.contentPane.add(new JLabel("Tile Set Description:"), gbc);
        gbc.weightx = 1;
        this.contentPane.add(this.tileSetDescription, gbc);
        
	}

	@Override
	protected void buildNavPane()
	{
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
				PackageInput.this.transitionToOutput();
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
                PackageInput.this.context.transitionTo(Window.MAIN);
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
	
	private void transitionToOutput()
	{
		Settings settings = this.context.getSettings();
		settings.set(Setting.TileSetName, this.tileSetName.getText());
		settings.set(Setting.TileSetDescription, this.tileSetDescription.getText());
		this.context.transitionTo(Window.PACKAGEOUTPUT);
	}
}

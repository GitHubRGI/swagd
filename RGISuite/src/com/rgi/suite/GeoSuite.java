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

import com.rgi.suite.cli.HeadlessOptions;
import com.rgi.suite.cli.HeadlessRunner;
import com.rgi.suite.uielements.windows.PackagerWindow;
import com.rgi.suite.uielements.windows.TileReadersOptionWindow;
import com.rgi.suite.uielements.windows.TilerWindow;
import com.rgi.view.MapViewWindow;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import utility.PropertiesAction;
import utility.TileStoreUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * Entry point for the program.
 *
 * @author Duff Means
 * @author Steven D. Lander
 */
@SuppressWarnings("OverlyComplexAnonymousInnerClass")
public final class GeoSuite
{
	private static final String SETTINGS_FILE_NAME = "swagd.prefs";
	private final Settings settings;

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
			new GeoSuite();
		}
	}

	private GeoSuite()
	{
		this.settings = new Settings(new File(Paths.get(System.getProperty("user.home"),
														SETTINGS_FILE_NAME).toString()));
		SwingUtilities.invokeLater(this::startGui);
	}

	@SuppressWarnings("deprecation")
	private void startGui()
	{
		final JPanel     contentPanel = new JPanel(new CardLayout());
		final JPanel     navPanel     = new JPanel(new CardLayout());
		final JFrame     suiteWindow  = new JFrame();
		final Properties props        = new Properties();
		try(InputStream inputStream = this.getClass().getResourceAsStream("geosuite.properties"))
		{
			props.load(inputStream);
		}
		catch(final IllegalArgumentException | IOException exception)
		{
			JOptionPane.showMessageDialog(null, "RGI Suite", "Unable to load properties", JOptionPane.OK_OPTION);
			System.out.println(exception.getMessage());
		}

		final Container c = suiteWindow.getContentPane();
		c.setLayout(new BorderLayout());

		c.add(contentPanel, BorderLayout.CENTER);
		c.add(navPanel, BorderLayout.SOUTH);

		suiteWindow.setTitle("RGI Tiling and Packaging Suite");
		suiteWindow.setSize(540, 240);
		suiteWindow.setResizable(false);
		suiteWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		suiteWindow.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent event)
			{
				//final int option = JOptionPane.showConfirmDialog(suiteWindow, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
				//if(option == JOptionPane.YES_OPTION)
				//{
				System.exit(0);
				//}
			}
		});

		// main buttons
		final JPanel             mainButtonPanel = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc             = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;

		final JButton tileButton = new JButton(new PropertiesAction(props, "tile")
		{
			private static final long serialVersionUID = -3249428374853166484L;

			@Override
			public void actionPerformed(final ActionEvent event)
			{
				final JFrame frame = new TilerWindow(GeoSuite.this.getSettings());
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
				final JFrame frame = new PackagerWindow(GeoSuite.this.getSettings());
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});

		gpkgButton.setHideActionText(true);
		gpkgButton.setMargin(new Insets(0, 0, 0, 0));

		final JButton viewButton = new JButton(new PropertiesAction(props, "view")
		{
			private static final long serialVersionUID = 1882624675173160883L;

			private static final String LAST_LOCATION_SETTING_NAME = "ui.viewer.lastLocation";

			@Override
			public void actionPerformed(final ActionEvent event)
			{
				final String startDirectory =
						GeoSuite.this.getSettings().get(LAST_LOCATION_SETTING_NAME,
														System.getProperty("user.home"));

				final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

				fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileChooser.setMultiSelectionEnabled(true);

				fileChooser.addActionListener(chooseEvent -> {
					if(JFileChooser.APPROVE_SELECTION.equals(chooseEvent.getActionCommand()))
					{
						final File[] files = fileChooser.getSelectedFiles();

						if(files.length > 0)
						{
							GeoSuite.this.getSettings().set(LAST_LOCATION_SETTING_NAME, files[0].getParent());
							GeoSuite.this.getSettings().save();

							final TileReadersOptionWindow tileReadersOptionWindow =
									new TileReadersOptionWindow(TileStoreUtility.getTileStoreReaderAdapters(true,
																											files),
																readers -> {
																	final JFrame viewWindow =
																			new MapViewWindow(readers);
																	viewWindow.setLocationRelativeTo(null);
																	viewWindow.setVisible(true);
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

	/**
	 * Parses command line arguments and launches the appropriate functionality
	 *
	 * @param args - cmd line argument string from main method.
	 */
	private static void runHeadless(@SuppressWarnings("unused") final String[] args)
	{
		final Logger logger = Logger.getLogger("RGISuite.logger");
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		final ConsoleHandler handler = new ConsoleHandler();
		final Formatter formatter = new Formatter()
		{
			@Override
			public String format(final LogRecord record)
			{
				return String.valueOf(new Date()) + ' ' + record.getLevel() + ' ' + record.getMessage() +
					   System.getProperty("line.separator");
			}
		};
		handler.setFormatter(formatter);
		logger.addHandler(handler);
		try
		{
			final HeadlessOptions opts = new HeadlessOptions(logger);
			final CmdLineParser parser = new CmdLineParser(opts);
			parser.parseArgument(args);
			if(opts.isValid())
			{
				final ExecutorService executor = Executors.newFixedThreadPool(1); //only 1 background thread supported
				final Runnable runner = new HeadlessRunner(opts, logger);
				logger.log(Level.INFO, "Begining tile generation:");
				executor.execute(runner);
			}
		}
		catch(final RuntimeException | CmdLineException error)
		{
			logger.log(Level.SEVERE, error.getMessage());
		}
	}

	public Settings getSettings()
	{
		return this.settings;
	}
}

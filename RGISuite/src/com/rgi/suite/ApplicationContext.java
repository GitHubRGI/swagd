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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.rgi.common.task.Settings;
import com.rgi.common.task.Task;

public class ApplicationContext extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = -4996528794673998019L;
	private Task task;

	public enum Window {
		MAIN, FILECHOOSER, PROGRESS, SETTINGS, ERROR, DONE;
	}

	private Settings settings;
	private Properties props;

	private JPanel contentPanel;
	private JPanel navPanel;
	private Exception error = null;

	private Map<Window, ApplicationWindow> windows = new HashMap<>();

	public ApplicationContext() {
		this.contentPanel = new JPanel(new CardLayout());
		this.navPanel = new JPanel(new CardLayout());

		this.props = new Properties();
		try(InputStream inputStream = this.getClass().getResourceAsStream("geosuite.properties")) {
			this.props.load(inputStream);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(null, "RGI Suite", "Unable to load properties", JOptionPane.OK_OPTION);
			ioe.printStackTrace();
			throw new RuntimeException(ioe.getMessage());
		}

		this.settings = new Settings();

		Container c = this.getContentPane();
		c.setLayout(new BorderLayout());
		c.add(this.contentPanel, BorderLayout.CENTER);
		c.add(this.navPanel, BorderLayout.SOUTH);

		this.setTitle("RGI Tiling and Geopackaging Suite");
		this.setSize(640, 480);
		this.setResizable(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		// show the window
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				int option = JOptionPane.showConfirmDialog(ApplicationContext.this, "Are you sure you want to exit?",
						"Confirm Exit", JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		});
	}

	public void go() {
		this.setVisible(true);
	}

	void addWindow(Window window, ApplicationWindow windowContent) {
		this.windows.put(window, windowContent);
		this.contentPanel.add(windowContent.getContentPane(), window.name());
		this.navPanel.add(windowContent.getNavigationPane(), window.name());
	}

	public Settings getSettings() {
		return this.settings;
	}

	public Properties getProperties() {
		return this.props;
	}

	public void setError(Exception error) {
	  this.error = error;
	}

	public Exception getError() {
	  return this.error;
	}

	public void transitionTo(Window window) {
		ApplicationWindow windowContent = this.windows.get(window);
		if (windowContent != null) {
			windowContent.activate();
			((CardLayout) this.contentPanel.getLayout()).show(this.contentPanel, window.name());
			((CardLayout) this.navPanel.getLayout()).show(this.navPanel, window.name());
		}
	}

	public void setActiveTask(Task task) {
		this.task = task;
	}

	public Task getActiveTask() {
		return this.task;
	}
}

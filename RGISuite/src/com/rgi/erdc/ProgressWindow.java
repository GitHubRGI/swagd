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

package com.rgi.erdc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.rgi.erdc.ApplicationContext.Window;
import com.rgi.erdc.task.MonitorableTask;
import com.rgi.erdc.task.Task;
import com.rgi.erdc.task.TaskMonitor;

public class ProgressWindow extends AbstractWindow implements TaskMonitor {
  private JProgressBar progressBar;
  
	public ProgressWindow(ApplicationContext context) {
		super(context);
	}

	@Override
	public void activate() {
	  progressBar.setIndeterminate(true);
	  Task task = context.getActiveTask();
	  if (task instanceof MonitorableTask) {
	    ((MonitorableTask)task).addMonitor(this);
	  }
	}
	
	@Override
	protected void buildContentPane() {
		this.contentPane = new JPanel(new GridBagLayout());
		progressBar = new JProgressBar(0, 100);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(20,20,20,20);
		contentPane.add(progressBar, gbc);
	}

	@Override
	protected void buildNavPane() {
		this.navPane = new JPanel(new GridBagLayout());
		Properties props = this.context.getProperties();
		JButton stopButton = new JButton(new PropertiesAction(props, "stop") {
			/**
			 * Generated serial
			 */
			private static final long serialVersionUID = -1678446487584371523L;

			@Override
			public void actionPerformed(ActionEvent event) {
				Task task = ProgressWindow.this.context.getActiveTask();
				if (task != null) {
					// TODO: task.cancel();
				}
			}
		});
		stopButton.setHideActionText(true);
		stopButton.setMargin(new Insets(0, 0, 0, 0));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(10, 10, 10, 10);
		this.navPane.add(stopButton, gbc);
	}
	
	@Override
	public void setMaximum(int max) {
    progressBar.setIndeterminate(false);
	  progressBar.setMaximum(max);
	}
	
	@Override
	public void setProgress(int value) {
	  System.out.println("Progress set to: "+value);
    progressBar.setIndeterminate(false);
    progressBar.setValue(value);
	}
	
	@Override
	public void cancelled() {
	  // TODO: switch to spinning "waiting" bar
	  progressBar.setIndeterminate(true);
	}
	
	@Override
	public void finished() {
	  context.transitionTo(Window.DONE);
	}
	@Override
	public void setError(Exception e) {
	  context.setError(e);
	  context.transitionTo(Window.ERROR);
	}
}

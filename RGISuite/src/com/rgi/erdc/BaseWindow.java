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

import com.rgi.erdc.ApplicationContext.Window;

public abstract class BaseWindow extends AbstractWindow {
	protected BaseWindow(ApplicationContext context) {
		super(context);
	}

	@Override
	protected void buildNavPane() {
		this.navPane = new JPanel(new GridBagLayout());
		Properties props = this.context.getProperties();
		JButton settingsButton = new JButton(new PropertiesAction(props, "pref") {
			/**
			 * Generated serial
			 */
			private static final long serialVersionUID = 5258278444574348376L;

			@Override
			public void actionPerformed(ActionEvent event) {
				BaseWindow.this.context.transitionTo(Window.SETTINGS);
			}
		});
		settingsButton.setHideActionText(true);
		settingsButton.setMargin(new Insets(0, 0, 0, 0));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(10, 10, 10, 10);
		this.navPane.add(settingsButton, gbc);
	}
}

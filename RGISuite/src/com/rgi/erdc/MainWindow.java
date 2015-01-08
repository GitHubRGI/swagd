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
import com.rgi.erdc.g2t.TilerFactory;
import com.rgi.erdc.gpkg.PackagerFactory;
import com.rgi.erdc.view.ViewerFactory;

public class MainWindow extends BaseWindow {
  public MainWindow(ApplicationContext context) {
    super(context);
  }
  
  @Override
protected void buildContentPane() {
    this.contentPane = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;

    Properties props = this.context.getProperties();
    
    JButton tileButton = new JButton(new PropertiesAction(props, "tile") {
      /**
  		 * 
  		 */
  		private static final long serialVersionUID = -3249428374853166484L;
    
    	@Override
    	public void actionPerformed(ActionEvent event) {
        MainWindow.this.context.setActiveTask(TilerFactory.getInstance().createTask());
        
        MainWindow.this.context.transitionTo(Window.FILECHOOSER);
      }
    });
    tileButton.setHideActionText(true);
    tileButton.setMargin(new Insets(0,0,0,0));
    JButton gpkgButton = new JButton(new PropertiesAction(props, "gpkg") {
      /**
  		 * 
  		 */
  		private static final long serialVersionUID = -1836754318915912580L;
  
    	@Override
    	public void actionPerformed(ActionEvent event) {
        MainWindow.this.context.setActiveTask(PackagerFactory.getInstance().createTask());
        MainWindow.this.context.transitionTo(Window.FILECHOOSER);
      }
    });
    gpkgButton.setHideActionText(true);
    gpkgButton.setMargin(new Insets(0,0,0,0));
    JButton viewButton = new JButton(new PropertiesAction(props, "view") {
      /**
  		 * 
  		 */
  		private static final long serialVersionUID = 1882624675173160883L;

    	@Override
    	public void actionPerformed(ActionEvent event) {
        MainWindow.this.context.setActiveTask(ViewerFactory.getInstance().createTask());
        MainWindow.this.context.transitionTo(Window.FILECHOOSER);
      }
    });
    viewButton.setHideActionText(true);
    viewButton.setMargin(new Insets(0,0,0,0));
  
    this.contentPane.add(tileButton, gbc);
    this.contentPane.add(gpkgButton, gbc);
    this.contentPane.add(viewButton, gbc);
  }
}

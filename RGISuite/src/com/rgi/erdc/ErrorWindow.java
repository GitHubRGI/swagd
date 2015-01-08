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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.rgi.erdc.ApplicationContext.Window;

public class ErrorWindow extends AbstractWindow {
  JTextArea errorPanel;
  
  public ErrorWindow(ApplicationContext context) {
    super(context);
  }
  
  @Override
  public void activate() {
    Exception e = context.getError();
    if (e != null) {
      // set editor pane content to stack trace of error
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      e.printStackTrace(new PrintStream(out));
      errorPanel.setText(new String(out.toByteArray()));
    }
  }
  
  @Override
  protected void buildContentPane() {
    contentPane = new JPanel(new GridBagLayout());
    errorPanel = new JTextArea();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    contentPane.add(errorPanel, gbc);
  }

  @Override
  protected void buildNavPane() {
    this.navPane = new JPanel(new GridBagLayout());
    Properties props = this.context.getProperties();
    JButton cancelButton = new JButton(new PropertiesAction(props, "cancel") {
      /**
       * Generated serial
       */
      private static final long serialVersionUID = -455467082471731446L;

      @Override
      public void actionPerformed(ActionEvent event) {
        context.transitionTo(Window.MAIN);
      }
    });
    cancelButton.setHideActionText(true);
    cancelButton.setMargin(new Insets(0, 0, 0, 0));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.EAST;
    gbc.weightx = 1.0;
    gbc.insets = new Insets(10, 10, 10, 10);
    this.navPane.add(cancelButton, gbc);
  }
}

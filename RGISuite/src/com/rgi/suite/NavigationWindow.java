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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public abstract class NavigationWindow extends JFrame
{
    protected final JPanel contentPanel = new JPanel();

    // Navigation stuff
    protected final JPanel  navigationPanel = new JPanel(new GridBagLayout());
    protected final JButton okButton        = new JButton("OK");
    protected final JButton cancelButton    = new JButton("Cancel");

    public NavigationWindow()
    {
        this.setLayout(new BorderLayout());

        this.add(this.contentPanel,    BorderLayout.CENTER);
        this.add(this.navigationPanel, BorderLayout.SOUTH);

        this.cancelButton.addActionListener(e -> { this.closeFrame(); });

        this.okButton.addActionListener(e -> { try
                                               {
                                                   if(this.execute())
                                                   {
                                                       this.closeFrame();
                                                   }
                                               }
                                               catch(final Exception ex)
                                               {
                                                   this.okButton.setEnabled(true);
                                                   ex.printStackTrace();
                                                   this.error("An error has occurred: " + ex.getMessage());
                                               }
                                             });

        // Add buttons to pane
        final Insets insets = new Insets(10, 10, 10, 10);
        final int    fill   = GridBagConstraints.NONE;

        this.navigationPanel.add(this.okButton,     new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, fill, insets, 0, 0));
        this.navigationPanel.add(this.cancelButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, fill, insets, 0, 0));
    }

    protected abstract boolean execute() throws Exception;

    protected abstract String processName();

    protected void warn(final String message)
    {
        JOptionPane.showMessageDialog(this,
                                      message,
                                      this.processName(),
                                      JOptionPane.WARNING_MESSAGE);
    }

    protected void error(final String message)
    {
        JOptionPane.showMessageDialog(this,
                                      message,
                                      this.processName(),
                                      JOptionPane.ERROR_MESSAGE);
    }

    protected void closeFrame()
    {
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
}

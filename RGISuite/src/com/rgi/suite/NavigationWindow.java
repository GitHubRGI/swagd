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

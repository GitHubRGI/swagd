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
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.rgi.common.task.Task;

/**
 * Extend JFrame functionality for UI elements of SWAGD.
 *
 * @author Duff Means
 * @author Steven D. Lander
 * @author Luke D. Lambert
 */
public class ApplicationContext extends JFrame
{
    private static final long serialVersionUID = -4996528794673998019L;
    private Task              task;

    private Settings   settings;
    private Properties props;

    private JPanel    contentPanel;
    private JPanel    navPanel;
    private Exception error;

    private List<ApplicationWindow> windows = new ArrayList<>();

    /**
     * Constructor.
     * @throws IOException
     */
    public ApplicationContext() throws IOException
    {
        this.contentPanel = new JPanel(new CardLayout());
        this.navPanel = new JPanel(new CardLayout());

        this.props = new Properties();
        try(InputStream inputStream = this.getClass().getResourceAsStream("geosuite.properties"))
        {
            this.props.load(inputStream);
        }
        catch(IOException ioe)
        {
            JOptionPane.showMessageDialog(null, "RGI Suite", "Unable to load properties", JOptionPane.OK_OPTION);
            ioe.printStackTrace();
            throw new RuntimeException(ioe.getMessage());
        }

        this.settings = new Settings(new File("settings.txt"));

        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());
        c.add(this.contentPanel, BorderLayout.CENTER);
        c.add(this.navPanel, BorderLayout.SOUTH);

        this.setTitle("RGI Tiling and Geopackaging Suite");
        this.setSize(640, 480);
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // show the window
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent event)
            {
                int option = JOptionPane.showConfirmDialog(ApplicationContext.this, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if(option == JOptionPane.YES_OPTION)
                {
                    System.exit(0);
                }
            }
        });
    }

    /**
     * Set this JFrame to visible.
     */
    public void go()
    {
        this.setVisible(true);
    }

    void addWindow(final ApplicationWindow windowContent)
    {
        this.windows.add(windowContent);

        this.contentPanel.add(windowContent.getContentPane(),    window.name());
        this.navPanel    .add(windowContent.getNavigationPane(), window.name());
    }

    /**
     * Return settings for this context.
     *
     * @return A list of settings relevant to this UI flow.
     */
    public Settings getSettings()
    {
        return this.settings;
    }

    /**
     * Return properties for this context.
     *
     * @return A Properties object containing info relevant to this current context.
     */
    public Properties getProperties()
    {
        return this.props;
    }

    /**
     * Set an error/exception for this context.
     *
     * @param error The error to be set for this context.
     */
    public void setError(Exception error)
    {
        this.error = error;
    }

    /**
     * Get the error set for this context.
     *
     * @return The current error set for this context.
     */
    public Exception getError()
    {
        return this.error;
    }

    /**
     * Transition the GUI to
     *
     * @param window The UI window that the GUI should transition to.
     */
    public void transitionTo(Window window)
    {
        ApplicationWindow windowContent = this.windows.get(window);
        if(windowContent != null)
        {
            windowContent.activate();
            ((CardLayout)this.contentPanel.getLayout()).show(this.contentPanel, window.name());
            ((CardLayout)this.navPanel    .getLayout()).show(this.navPanel,     window.name());
        }
    }

    public void setActiveTask(Task task)
    {
        this.task = task;
    }

    public Task getActiveTask()
    {
        return this.task;
    }
}

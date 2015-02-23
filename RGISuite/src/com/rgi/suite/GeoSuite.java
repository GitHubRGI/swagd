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

import javax.swing.SwingUtilities;

import com.rgi.suite.ApplicationContext.Window;

/**
 * Entry point for the program.
 *
 * @author Duff Means
 * @author Steven D. Lander
 */
public class GeoSuite
{
    private ApplicationContext context;

    private GeoSuite()
    {
        SwingUtilities.invokeLater(() -> { this.context = new ApplicationContext();

                                           this.context.addWindow(Window.MAIN,        new MainWindow       (this.context)); // This has to be first.
                                           this.context.addWindow(Window.DONE,        new DoneWindow       (this.context));
                                           this.context.addWindow(Window.FILECHOOSER, new FileChooserWindow(this.context));
                                           this.context.addWindow(Window.PACKAGEINPUT, new PackageInput	   (this.context));
                                           this.context.addWindow(Window.PACKAGEOUTPUT, new PackageOutput	   (this.context));
                                           this.context.addWindow(Window.PROGRESS,    new ProgressWindow   (this.context));
                                           this.context.addWindow(Window.SETTINGS,    new SettingsWindow   (this.context));
                                           this.context.addWindow(Window.WINDOWERROR, new ErrorWindow      (this.context));

                                           this.context.go();
                                         });
    }

    private static void runHeadless(@SuppressWarnings("unused")String[] args)
    {
        // TODO
    	System.out.println("Running headless is not yet supported.");
    }

    /**
     * Code decision point for running in either GUI mode or headless (command-line) mode.
     *
     * @param args A string array of command line arguments.
     */
    public static void main(String[] args)
    {
        if(args != null && args.length > 0)
        {
            GeoSuite.runHeadless(args);
        }
        else
        {
            @SuppressWarnings("unused")
            GeoSuite geoSuite = new GeoSuite();
        }
    }

}

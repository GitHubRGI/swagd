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

package com.rgi.view;

import java.io.File;

import javax.swing.JFrame;

/**
 * View a tile store or stores within a map viewer
 *
 * @author Steven D. Lander
 * @author Luke D. Lambert
 */
public class Viewer
{
    /**
     * Constructor
     *
     */
    public Viewer(final File[] files)
    {
        if(files == null)
        {
            throw new IllegalArgumentException("Array of files may not be null");
        }

        this.files = files;
    }

    public void execute()
    {
        try
        {
            final JFrame frame = new MapViewWindow(this.files);
            frame.pack();
            frame.setVisible(true);
        }
        catch(final Exception e)
        {
            e.printStackTrace();
        }
    }

    private final File[] files;
}

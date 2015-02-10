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

import javax.swing.JPanel;

public abstract class AbstractWindow implements ApplicationWindow
{
    protected ApplicationContext context;
    protected JPanel             contentPane;
    protected JPanel             navPane;

    protected AbstractWindow(ApplicationContext context)
    {
        this.context = context;
        this.buildContentPane();
        this.buildNavPane();
    }

    protected abstract void buildContentPane();

    protected abstract void buildNavPane();

    @Override
    public void activate()
    {
        // do nothing
    }

    @Override
    public JPanel getContentPane()
    {
        return this.contentPane;
    }

    @Override
    public JPanel getNavigationPane()
    {
        return this.navPane;
    }
}

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

package com.rgi.erdc.view;

import com.rgi.erdc.task.Task;
import com.rgi.erdc.task.TaskFactory;

public class ViewerFactory implements TaskFactory {
  private static final ViewerFactory instance = new ViewerFactory();
  public static final ViewerFactory getInstance() {
    return instance;
  }

  @Override
  public Task createTask() {
    return new Viewer(this);
  }

  @Override
  public boolean selectMultiple() {
    return true;
  }

  @Override
  public String getFilePrompt() {
	return "Select tile collection to be viewed...";
  }

  @Override
  public boolean selectFilesOnly() {
    return false;
  }

  @Override
  public boolean selectInput() {
    // TODO Auto-generated method stub
    return true;
  }
  
  @Override
  public boolean selectFoldersOnly() {
    return false;
  }
}

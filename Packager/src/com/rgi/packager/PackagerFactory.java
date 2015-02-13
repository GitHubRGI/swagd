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

package com.rgi.packager;

import com.rgi.common.task.Task;
import com.rgi.common.task.TaskFactory;

public class PackagerFactory implements TaskFactory {
  private static final PackagerFactory instance = new PackagerFactory();
  public static final PackagerFactory getInstance() {
    return instance;
  }

  @Override
  public Task createTask() {
    return new Packager(this);
  }

  @Override
  public boolean selectMultiple() {
    return false;
  }

  @Override
  public String getFilePrompt() {
    return "Enter output geopackage name...";
  }

  @Override
  public boolean selectInput() {
    return false;
  }

  @Override
  public boolean selectFilesOnly() {
    return false;
  }
  
  @Override
  public boolean selectFoldersOnly() {
    return true;
  }
}

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

import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public abstract class PropertiesAction extends AbstractAction {
  protected PropertiesAction(Properties p, String key) {
    setString(p, key+".name", Action.NAME);
    setKeyStroke(p, key+".accel", Action.ACCELERATOR_KEY);
    setString(p, key+".short", Action.SHORT_DESCRIPTION);
    setString(p, key+".long", Action.LONG_DESCRIPTION);
    setIcon(p, key+".small", Action.SMALL_ICON);
    setIcon(p, key+".large", Action.LARGE_ICON_KEY);
  }

  private void setString(Properties p, String property, String key) {
    String value = p.getProperty(property);
    if (value != null) {
      putValue(key, value);
    }
  }

  private void setKeyStroke(Properties p, String property, String key) {
    String value = p.getProperty(property);
    if (value != null) {
      putValue(key, KeyStroke.getKeyStroke(value));
    }
  }

  private void setIcon(Properties p, String property, String key) {
    String value = p.getProperty(property);
    if (value != null) {
      putValue(key, new ImageIcon(value));
    }
  }
}

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

package com.rgi.common.task;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.rgi.common.util.BinUtil;

public class Settings {
  public enum Setting {
    TileHeight(Integer.valueOf(TILESIZE)),
    TileWidth(Integer.valueOf(TILESIZE)),
    TileOrigin(com.rgi.common.tile.TileOrigin.LowerLeft),
    TileType(Type.PNG),
    Quality(Integer.valueOf(70)),
    TileFolder(System.getProperty("user.home")),
    FileSelection(null),
    NoDataColor(TRANSPARENT),
    InputSRS(null),
    CrsProfile(Profile.WebMercator);

    private Object defaultValue = null;

    private Setting(Object defaultValue) {
      this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
      return this.defaultValue;
    }
  }

  public enum Type {
    JPG,
    PNG;
  }

  public enum Profile {
    WebMercator("EPSG",3857),
    WorldMercator("EPSG",3395),
    ScaledWorldMercator("+proj=merc +datum=WGS84 +k_0=0.803798909747978",9004),
    SphericalMercator("EPSG",900913),
    Geodetic("EPSG",4326),
    Raster(null,0);

    private String auth;
    private int id;

    Profile(String auth, int id) {
      this.auth = auth;
      this.id = id;
    }

    public String getAuthority() {
      return this.auth;
    }

    public int getID() {
      return this.id;
    }
  }

  private static final int TILESIZE = 256;
  public static final Color TRANSPARENT = new Color(0,0,0,0);

  private Preferences prefs = Preferences.userNodeForPackage(Settings.class);

  public Settings() {
    try {
      this.prefs.sync();
    } catch (BackingStoreException bse) {
      System.err.println("Unable to load settings." + "\n" + bse.getMessage());
    }
  }

  public void save() throws Exception {
    try {
      this.prefs.flush();
    } catch (BackingStoreException bse) {
      System.err.println("Unable to save settings.");
      throw bse;
    }
  }

  /**
   *
   * @param setting
   * @return the setting value, or null
   */
  public String get(Setting setting) {
    if (setting.defaultValue instanceof String)
    {
        return this.prefs.get(setting.name(), (String)setting.defaultValue);
    }
    else if (setting.defaultValue != null)
    {
        return this.prefs.get(setting.name(), setting.defaultValue.toString());
    }
    return this.prefs.get(setting.name(), null);
  }

  public void set(Setting setting, String value) {
    this.prefs.put(setting.name(), value);
  }

  /**
   *
   * @param setting
   * @return the setting value, or -1 if the value is not present
   * @throws NumberFormatException if the value is present but is not an int
   */
  public Color getColor(Setting setting) {
    String colorStr = this.prefs.get(setting.name(), null);
    if (colorStr != null) {
      try {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(BinUtil.decode(colorStr)));
        return (Color)in.readObject();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (setting.defaultValue instanceof Color) {
      return (Color)setting.defaultValue;
    }
    return null;
  }

  public void set(Setting setting, Color value) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      new ObjectOutputStream(out).writeObject(value);
    } catch (Exception ioe) {
      ioe.printStackTrace();
    }
    this.prefs.put(setting.name(), BinUtil.encode(out.toByteArray()));
  }

  /**
   *
   * @param setting
   * @return the setting value, or -1 if the value is not present
   * @throws NumberFormatException if the value is present but is not an int
   */
  public int getInt(Setting setting) {
    if (setting.defaultValue instanceof Integer)
    {
        return this.prefs.getInt(setting.name(), (Integer)setting.defaultValue);
    }
    String value = this.prefs.get(setting.name(), null);
    if (value != null)
    {
        return Integer.parseInt(value);
    }
    return -1;
  }

  public void set(Setting setting, int value) {
    this.prefs.putInt(setting.name(), value);
  }

  /**
   *
   * @param setting
   * @return the setting value, or -1 if the value is not present
   * @throws NumberFormatException if the value is present but is not a long
   */
  public long getLong(Setting setting) {
    if (setting.defaultValue instanceof Long)
    {
        return this.prefs.getLong(setting.name(), (Long)setting.defaultValue);
    }
    String value = this.prefs.get(setting.name(), null);
    if (value != null)
    {
        return Long.parseLong(value);
    }
    return -1;
  }

  public void set(Setting setting, long value) {
    this.prefs.putLong(setting.name(), value);
  }

  /**
   *
   * @param setting
   * @return the setting value as a float, or NaN
   */
  public double getFloat(Setting setting) {
    if (setting.defaultValue instanceof Float)
    {
        return this.prefs.getFloat(setting.name(), (Float)setting.defaultValue);
    }
    String value = this.prefs.get(setting.name(), null);
    if (value != null)
    {
        return Float.parseFloat(value);
    }
    return Float.NaN;
  }

  public void set(Setting setting, float value) {
    this.prefs.putFloat(setting.name(), value);
  }

  /**
   *
   * @param setting
   * @return the setting value as a double, or NaN
   */
  public double getDouble(Setting setting) {
    if (setting.defaultValue instanceof Double)
    {
        return this.prefs.getDouble(setting.name(), (Double)setting.defaultValue);
    }
    String value = this.prefs.get(setting.name(), null);
    if (value != null)
    {
        return Double.parseDouble(value);
    }
    return Double.NaN;
  }

  public void set(Setting setting, double value) {
    this.prefs.putDouble(setting.name(), value);
  }

  /**
   *
   * @param setting
   * @return the setting value as an array of files, or null
   */
  public File[] getFiles(Setting setting) {
    String fileNames = this.get(setting);
    if (fileNames != null) {
      String[] paths = fileNames.split(";");
      List<File> files = new ArrayList<>();
      for (String path : paths) {
        files.add(new File(path));
      }
      return files.toArray(new File[files.size()]);
    }
    return null;
  }

  public void set(Setting setting, File file) {
    this.set(setting, file.getPath());
  }

  public void set(Setting setting, File[] files) {
    boolean first = true;
    StringBuffer fileNames = new StringBuffer();
    for (File file : files) {
      if (!first)
    {
        fileNames.append(";");
    }
      fileNames.append(file.getPath());
    }
    this.set(setting, fileNames.toString());
  }
}

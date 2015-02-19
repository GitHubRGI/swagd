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
import java.util.Base64;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Duff Means
 *
 */
public class Settings
{
    /**
     * @author Duff Means
     *
     */
    public enum Setting
    {
        /**
         * The height of a tile in pixels
         */
        TileHeight   (Integer.valueOf(TILESIZE)),
        /**
         * The width of a tile in pixels
         */
        TileWidth    (Integer.valueOf(TILESIZE)),
        /**
         * The origin of the tile (ex: LowerLeft, UpperRight)
         */
        TileOrigin   (com.rgi.common.tile.TileOrigin.LowerLeft),
        /**
         * Image type of the tiles (ex: Type.JPEG, Type.PNG)
         */
        TileType     (Type.PNG),
        /**
         * The name of the tile set (packaging).
         */
        TileSetName  (""),
        /**
         * The description of the tile set (packaging).
         */
        TileSetDescription (""),
        /**
         * The output file name (packaging).
         */
        OutputFileName (""),
        /**
         * The JPEG image quality.
         */
        Quality      (70),
        /**
         * The folder where the tiles should be output
         */
        TileFolder   (System.getProperty("user.home")),
        /**
         * The file selected by the user
         */
        FileSelection(null),
        /**
         * 
         */
        NoDataColor  (TRANSPARENT),
        /**
         * The Spatial Reference System the user indicates the input images are in
         */
        InputSRS     (null),
        /**
         * The Coordinate Reference System Profile Setting for the output tiles
         */
        CrsProfile   (Profile.WebMercator);

        private Object defaultValue = null;

        private Setting(final Object defaultValue)
        {
            this.defaultValue = defaultValue;
        }

        /**
         * 
         * @return the default value 
         */
        public Object getDefaultValue()
        {
            return this.defaultValue;
        }
    }

    /**
     * @author Duff Means
     *
     */
    public enum Type
    {
        /**
         * The image type jpeg
         */
        JPG, 
        /**
         * The image type PNG
         */
        PNG;
    }

    /**
     * The Coordinate Reference System Profile containing
     * the authority and version
     * @author Duff Means
     *
     */
    public enum Profile
    {
        /**
         *  Web Mercator (also known as Spherical Mercator) projection
         *  used in many popular web mapping applications (Google/Bing/OpenStreetMap/etc).
         *  Sometimes know nas EPSG:900913
         */
        WebMercator        ("EPSG", 3857),
        /**
         * World Mercator (also known as Ellipsoidal Mercator) projection to 
         * include the spheroid's flattening of the Earth in the calculation
         */
        WorldMercator      ("EPSG", 3395),
        /**
         * 
         */
        ScaledWorldMercator("+proj=merc +datum=WGS84 +k_0=0.803798909747978", 9004),
        /**
         *  Spherical Mercator (also known as Web Mercator) projection
         *  used in many popular web mapping applications (Google/Bing/OpenStreetMap/etc).
         *  Sometimes know nas EPSG:900913
         */
        SphericalMercator  ("EPSG", 3857),
        /**
         * World Geodetic System 1984 projection
         */
        Geodetic           ("EPSG", 4326),
        /**
         * 
         */
        Raster             (null, 0);

        private String auth;
        private int    id;

        Profile(final String auth, final int id)
        {
            this.auth = auth;
            this.id = id;
        }

        /**
         * Returns the authority of the Coordinate Reference System
         * @return authority the Coordinate Reference System authority name (typically "EPSG")
         */
        public String getAuthority()
        {
            return this.auth;
        }

        /**
         * Returns the version number of the Coordinate Reference System
         * 
         * @return identifier the version number of the authority
         */
        public int getID()
        {
            return this.id;
        }
    }

    private static final int  TILESIZE    = 256;
    /**
     * 
     */
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    private final Preferences       prefs       = Preferences.userNodeForPackage(Settings.class);

    /**
     * 
     */
    public Settings()
    {
        try
        {
            this.prefs.sync();
        }
        catch(final BackingStoreException bse)
        {
            System.err.println("Unable to load settings." + "\n" + bse.getMessage());
        }
    }

    /**
     * Saves the user's settings
     * 
     * @throws Exception
     *             an Exception is thrown when settings cannot be saved. If this
     *             saving operation cannot be completed due to a failure in the
     *             backing store, or inability to communicate with it.
     */
    public void save() throws Exception
    {
        try
        {
            this.prefs.flush();
        }
        catch(final BackingStoreException bse)
        {
            System.err.println("Unable to save settings.");
            throw bse;
        }
    }

    /**
     * Returns the settings of the user's preferences
     * 
     * @param setting
     *            the settings the user inputed
     * @return the setting value, or null
     */
    public String get(final Setting setting)
    {
        if(setting.defaultValue instanceof String)
        {
            return this.prefs.get(setting.name(), (String)setting.defaultValue);
        }
        else if(setting.defaultValue != null)
        {
            return this.prefs.get(setting.name(), setting.defaultValue.toString());
        }
        return this.prefs.get(setting.name(), null);
    }

    /**
     * Places the setting and it's value in into the Object Preferences
     * 
     * @param setting
     *            the type of setting (Ex:TileHeight)
     * @param value
     *            the value of the setting chosen
     */
    public void set(final Setting setting, final String value)
    {
        this.prefs.put(setting.name(), value);
    }

    /**
     * @param 
     * @return
     */
    public Color getColor(final Setting setting)
    {
        final String colorStr = this.prefs.get(setting.name(), null);
        if(colorStr != null)
        {
            final byte [] data = Base64.getDecoder().decode(colorStr);
            try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream))
            {
                return (Color)objectInputStream.readObject();
            }
            catch(final ClassNotFoundException | IOException ex)
            {
                return (Color)setting.defaultValue;
            }
        }

        if(setting.defaultValue instanceof Color)
        {
            return (Color)setting.defaultValue;
        }

        return null;
    }

    /**
     * @param setting
     * @param value
     * @throws IOException
     */
    public void set(final Setting setting, final Color value) throws IOException
    {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream))
        {
            objectOutputStream.writeObject(value);
            this.prefs.put(setting.name(), new String(Base64.getEncoder().encode(byteArrayOutputStream.toByteArray())));
        }
    }

    /**
     *
     * @param setting
     * @return the setting value, or -1 if the value is not present
     * @throws NumberFormatException
     *             if the value is present but is not an int
     */
    public int getInt(final Setting setting)
    {
        if(setting.defaultValue instanceof Integer)
        {
            return this.prefs.getInt(setting.name(), (Integer)setting.defaultValue);
        }
        final String value = this.prefs.get(setting.name(), null);
        if(value != null)
        {
            return Integer.parseInt(value);
        }
        return -1;
    }

    public void set(final Setting setting, final int value)
    {
        this.prefs.putInt(setting.name(), value);
    }

    /**
     *
     * @param setting
     * @return the setting value, or -1 if the value is not present
     * @throws NumberFormatException
     *             if the value is present but is not a long
     */
    public long getLong(final Setting setting)
    {
        if(setting.defaultValue instanceof Long)
        {
            return this.prefs.getLong(setting.name(), (Long)setting.defaultValue);
        }
        final String value = this.prefs.get(setting.name(), null);
        if(value != null)
        {
            return Long.parseLong(value);
        }
        return -1;
    }

    public void set(final Setting setting, final long value)
    {
        this.prefs.putLong(setting.name(), value);
    }

    /**
     *
     * @param setting
     * @return the setting value as a float, or NaN
     */
    public double getFloat(final Setting setting)
    {
        if(setting.defaultValue instanceof Float)
        {
            return this.prefs.getFloat(setting.name(), (Float)setting.defaultValue);
        }
        final String value = this.prefs.get(setting.name(), null);
        if(value != null)
        {
            return Float.parseFloat(value);
        }
        return Float.NaN;
    }

    public void set(final Setting setting, final float value)
    {
        this.prefs.putFloat(setting.name(), value);
    }

    /**
     *
     * @param setting
     * @return the setting value as a double, or NaN
     */
    public double getDouble(final Setting setting)
    {
        if(setting.defaultValue instanceof Double)
        {
            return this.prefs.getDouble(setting.name(), (Double)setting.defaultValue);
        }
        final String value = this.prefs.get(setting.name(), null);
        if(value != null)
        {
            return Double.parseDouble(value);
        }
        return Double.NaN;
    }

    public void set(final Setting setting, final double value)
    {
        this.prefs.putDouble(setting.name(), value);
    }

    /**
     *
     * @param setting
     * @return the setting value as an array of files, or null
     */
    public File[] getFiles(final Setting setting)
    {
        final String fileNames = this.get(setting);
        if(fileNames != null)
        {
            final String[] paths = fileNames.split(";");
            final List<File> files = new ArrayList<>();
            for(final String path : paths)
            {
                files.add(new File(path));
            }
            return files.toArray(new File[files.size()]);
        }
        return null;
    }

    public void set(final Setting setting, final File file)
    {
        this.set(setting, file.getPath());
    }

    public void set(final Setting setting, final File[] files)
    {
        final boolean first = true;
        final StringBuffer fileNames = new StringBuffer();
        for(final File file : files)
        {
            if(!first)
            {
                fileNames.append(";");
            }
            fileNames.append(file.getPath());
        }
        this.set(setting, fileNames.toString());
    }
}

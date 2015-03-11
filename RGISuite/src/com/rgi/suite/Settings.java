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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * @author Luke Lambert
 *
 */
public class Settings
{
    /**
     * Constructor
     *
     * @param file
     *             Location to read and write settings to
     */
    public Settings(final File file)
    {
        this.file     = file;
        this.settings = Settings.readSettings(file);
    }

    /**
     * Gets the value of a setting.
     *
     * @param settingName
     *             Name of the setting to get
     * @param defaultValue
     *             If there's no setting by the requested name, this value will
     *             be returned by default
     * @return Returns the setting value that corresponds to the requested
     *             setting name
     */
    public String get(final String settingName, final String defaultValue)
    {
        if(this.settings.containsKey(settingName))
        {
            return this.settings.get(settingName);
        }

        return defaultValue;
    }

    /**
     * Gets the value of a setting, and uses a mapping function to convert it
     * to a different type.
     *
     * @param settingName
     *             Name of the setting to get
     * @param mapper
     *             Function that maps the setting's string value to a different
     *             type
     * @param defaultValue
     *             If there's no setting by the requested name, this value will
     *             be returned by default
     * @return Returns the setting value that corresponds to the requested
     *             setting name
     */
    public <R> R get(final String settingName, final Function<String, R> mapper, final R defaultValue)
    {
        if(mapper == null)
        {
            throw new IllegalArgumentException("Mapping function may not be null");
        }

        if(this.settings.containsKey(settingName))
        {
            return mapper.apply(this.settings.get(settingName));
        }

        return defaultValue;
    }

    /**
     * Sets the value of a setting
     *
     * @param settingName
     *             Name of the setting to get
     * @param value
     *             value that corresponds to the supplied setting name
     */
    public void set(final String settingName, final String value)
    {
        if(settingName == null)
        {
            throw new IllegalArgumentException("Setting name may not be null");
        }

        this.settings.put(settingName, value);
    }

    /**
     * Sets the value of a setting
     *
     * @param settingName
     *             Name of the setting to get
     * @param value
     *             value that corresponds to the supplied setting name
     * @param mapper
     *             Function that maps an object to its appropriate string
     *             representation
     */
    public <T> void set(final String settingName, final T value, final Function<T, String> mapper)
    {
        if(settingName == null)
        {
            throw new IllegalArgumentException("Setting name may not be null");
        }

        if(mapper == null)
        {
            throw new IllegalArgumentException("Mapping function may not be null");
        }

        this.settings.put(settingName, mapper.apply(value));
    }

    /**
     * Writes settings to file
     *
     * @return Returns true if tile writing was successful
     */
    public boolean save()
    {
        try(FileOutputStream   fileOutputStream   = new FileOutputStream  (this.file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter     bufferedWriter     = new BufferedWriter    (outputStreamWriter))
        {
            for(final Entry<String, String> setting : this.settings.entrySet())
            {
                bufferedWriter.write(String.format("%s=%s",
                                                   setting.getKey(),
                                                   setting.getValue()));
                bufferedWriter.newLine();
            }

            return true;
        }
        catch(final IOException ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    private static Map<String, String> readSettings(final File file)
    {
        final Map<String, String> settings = new HashMap<>();

        if(file.exists())
        {
            try(final FileInputStream   fileInputStream   = new FileInputStream  (file);
                final InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                final BufferedReader    bufferedReader    = new BufferedReader   (inputStreamReader))
            {
                for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
                {
                    final String[] keyValuePair = line.split("=", 2);
                    if(keyValuePair.length == 2)
                    {
                        settings.put(keyValuePair[0], keyValuePair[1]);
                    }
                }
            }
            catch(final IOException ex)
            {
                ex.printStackTrace();
            }
        }
        else
        {
            try
            {
                file.createNewFile();
            }
            catch(final IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }

        return settings;
    }

    private final File                file;
    private final Map<String, String> settings;
}

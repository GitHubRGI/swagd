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

/**
 * @author Luke Lambert
 *
 */
public class Settings
{
    public Settings(final File file) throws IOException
    {
        this.file     = file;
        this.settings = Settings.readSettings(file);
    }

    public String get(final String setting, final String defaultValue)
    {
        if(this.settings.containsKey(setting))
        {
            return this.settings.get(setting);
        }

        return defaultValue;
    }

    public void set(final String setting, final String value) throws IOException
    {
        this.settings.put(setting, value);
        this.save();
    }

    private static Map<String, String> readSettings(final File file) throws IOException
    {
        Map<String, String> settings = new HashMap<>();

        try(FileInputStream   fileInputStream   = new FileInputStream  (file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader    bufferedReader    = new BufferedReader   (inputStreamReader))
        {
            for(String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
            {
                String[] keyValuePair = line.split("=", 2);
                if(keyValuePair.length == 2)
                {
                    settings.put(keyValuePair[0], keyValuePair[1]);
                }
            }
        }

        return settings;
    }

    private void save() throws IOException
    {
        try(FileOutputStream   fileOutputStream   = new FileOutputStream  (this.file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter     bufferedWriter     = new BufferedWriter    (outputStreamWriter))
        {
            for(Entry<String, String> setting : this.settings.entrySet())
            {
                bufferedWriter.write(String.format("%s=%s",
                                                   setting.getKey(),
                                                   setting.getValue()));
                bufferedWriter.newLine();
            }
        }
    }

    private final File                file;
    private final Map<String, String> settings;
}

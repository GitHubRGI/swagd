/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.rgi.suite;

import java.net.URL;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * @author Duff Means
 *
 */
@SuppressWarnings("serial")
@Deprecated
public abstract class PropertiesAction extends AbstractAction
{
    protected PropertiesAction(final Properties p, final String key)
    {
        this.setString   (p, key + ".name",  Action.NAME);
        this.setKeyStroke(p, key + ".accel", Action.ACCELERATOR_KEY);
        this.setString   (p, key + ".short", Action.SHORT_DESCRIPTION);
        this.setString   (p, key + ".long",  Action.LONG_DESCRIPTION);
        this.setIcon     (p, key + ".small", Action.SMALL_ICON);
        this.setIcon     (p, key + ".large", Action.LARGE_ICON_KEY);
    }

    private void setString(final Properties p, final String property, final String key)
    {
        final String value = p.getProperty(property);
        if(value != null)
        {
            this.putValue(key, value);
        }
    }

    private void setKeyStroke(final Properties p, final String property, final String key)
    {
        final String value = p.getProperty(property);
        if(value != null)
        {
            this.putValue(key, KeyStroke.getKeyStroke(value));
        }
    }

    private void setIcon(final Properties p, final String property, final String key)
    {
        final String value = p.getProperty(property);
        if(value != null)
        {
            final URL resource = this.getClass().getResource("/" + value);
            if(resource != null)
            {
                this.putValue(key, new ImageIcon(resource));
            }
        }
    }
}

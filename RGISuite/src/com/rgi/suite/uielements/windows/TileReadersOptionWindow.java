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
package com.rgi.suite.uielements.windows;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import utility.SimpleGridBagConstraints;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;

public class TileReadersOptionWindow extends NavigationWindow
{
    private final Collection<TileStoreReaderAdapter>    readerAdapters;
    private final Consumer<Collection<TileStoreReader>> readerConsumer;

    private boolean needsInput = false;

    public TileReadersOptionWindow(final Collection<TileStoreReaderAdapter> readerAdapters, final Consumer<Collection<TileStoreReader>> readerConsumer)
    {
        if(readerAdapters == null || readerAdapters.size() < 1)
        {
            throw new IllegalArgumentException("Adapter collection may not be null or empty");
        }

        this.setTitle("File Options");
        this.setResizable(false);

        this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.PAGE_AXIS));

        this.readerAdapters  = readerAdapters;
        this.readerConsumer = readerConsumer;

        for(final TileStoreReaderAdapter adapter : this.readerAdapters)
        {
            if(adapter != null)
            {
                this.needsInput |= adapter.needsInput();

                final JPanel readerPanel = new JPanel(new GridBagLayout());

                readerPanel.setBorder(BorderFactory.createTitledBorder(adapter.getFile().getName()));

                int rowCount = 0;

                for(final Collection<JComponent> row : adapter.getReaderParameterControls())
                {
                    int columnCount = 0;
                    for(final JComponent column : row)
                    {
                        final Dimension dimension = column.getPreferredSize();

                        if(columnCount == 1 &&
                           (dimension.getWidth()  < 1 ||
                            dimension.getHeight() < 1)) // TODO; This is a HACK
                        {

                            column.setPreferredSize(new Dimension(220, 25));
                        }

                        readerPanel.add(column, new SimpleGridBagConstraints(columnCount, rowCount, columnCount == 1)); // TODO; last parameter is similarly a hack for that second column

                        ++columnCount;
                    }

                    ++rowCount;
                }

                this.contentPanel.add(readerPanel);
            }
        }

        //this.contentPane.revalidate();
        this.pack();
    }

    public boolean needsInput()
    {
        return this.needsInput;
    }

    public Collection<TileStoreReader> getReaders()
    {
        final List<TileStoreReader> readers = new ArrayList<>();

        this.readerAdapters
            .stream()
            .forEach(adapter -> { try
                                  {
                                      readers.addAll(adapter.getTileStoreReaders());
                                  }
                                  catch(final TileStoreException ex)
                                  {
                                      this.error(String.format("There was an error opening %s: %s",
                                                               adapter.getFile().getName(),
                                                               ex.getMessage()));
                                  }
                                });

        return readers;
    }


    @Override
    protected String processName()
    {
        return "File Options";
    }

    @Override
    public boolean execute()
    {
        this.readerConsumer.accept(this.getReaders());
        return true;
    }
}

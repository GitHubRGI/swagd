package com.rgi.suite;

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

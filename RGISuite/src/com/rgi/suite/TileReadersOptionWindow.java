package com.rgi.suite;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;

public class TileReadersOptionWindow extends NavigationWindow
{
    private final Collection<TileStoreReaderAdapter>    readerAdapters;
    private final Consumer<Collection<TileStoreReader>> readerConsumer;

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
                final JPanel readerPanel = new JPanel(new GridBagLayout());

                readerPanel.setBorder(BorderFactory.createTitledBorder(adapter.getFile().getName()));

                int rowCount = 0;

                for(final Collection<JComponent> row : adapter.getReaderParameterControls())
                {
                    int columnCount = 0;
                    for(final JComponent column : row)
                    {
                        if(columnCount == 1) // TODO; This is a HACK
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

    public Collection<TileStoreReader> getReaders()
    {
        return this.readerAdapters.stream()
                                  .map(adapter -> { try
                                                    {
                                                        return adapter.getTileStoreReader();
                                                    }
                                                    catch(final TileStoreException ex)
                                                    {
                                                        JOptionPane.showMessageDialog(this,
                                                                                      String.format("There was an error opening %s: %s",
                                                                                                    adapter.getFile().getName(),
                                                                                                    ex.getMessage()),
                                                                                      "Open Failed",
                                                                                      JOptionPane.ERROR_MESSAGE);
                                                        return null;
                                                    }
                                                  })
                                  .filter(Objects::nonNull)
                                  .collect(Collectors.toList());
    }


    @Override
    protected void execute() throws Exception
    {
        this.readerConsumer.accept(this.getReaders());
    }
}

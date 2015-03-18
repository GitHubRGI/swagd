package com.rgi.suite;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.rgi.common.tile.store.TileStoreException;
import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;

public class TileReadersOptionWindow extends JFrame
{
    final JScrollPane contentPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    final Collection<TileStoreReaderAdapter> readerAdapters;

    public TileReadersOptionWindow(final Collection<TileStoreReaderAdapter> readerAdapters)
    {
        if(readerAdapters == null || readerAdapters.size() < 1)
        {
            throw new IllegalArgumentException("Adapter collection may not be null or empty");
        }

        this.setTitle("File");
        this.setLayout(new BorderLayout());
        this.setResizable(false);

        this.readerAdapters = readerAdapters;

        this.add(this.contentPane, BorderLayout.CENTER);

        this.contentPane.setMaximumSize(new Dimension(10000, 600));

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

                this.add(readerPanel);
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


}

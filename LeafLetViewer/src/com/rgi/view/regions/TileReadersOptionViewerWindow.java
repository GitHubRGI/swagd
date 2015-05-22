package com.rgi.view.regions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.rgi.store.tiles.TileStoreException;
import com.rgi.store.tiles.TileStoreReader;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;

public class TileReadersOptionViewerWindow extends Stage
{

    private final Collection<TileStoreReaderAdapter> readerAdapters;
    private final Consumer<Collection<TileStoreReader>> readerConsumer;
    private final VBox content = new VBox();
    private final boolean needsInput = false;

    public TileReadersOptionViewerWindow(final Collection<TileStoreReaderAdapter> readerAdapters, final Consumer<Collection<TileStoreReader>> readerConsumer)
    {
        if(readerAdapters == null || readerAdapters.size() < 1)
        {
            throw new IllegalArgumentException("Adapter collection may not be null or empty");
        }

        this.setTitle("File Options");
        this.setResizable(false);

      //  this.content.setLayout(new BoxLayout(this.content, BoxLayout.PAGE_AXIS));

        this.readerAdapters = readerAdapters;
        this.readerConsumer = readerConsumer;

//        for(final TileStoreReaderAdapter adapter : this.readerAdapters)
//        {
//            if(adapter != null)
//            {
//                this.needsInput  |= adapter.needsInput();
//
//                final GridPane readerPanel = new GridPane();
//                BorderedTitledPane pane = new BorderedTitledPane(adapter.getFile().getName(), readerPanel);
//                //readerPanel.setBorder(BorderFactory.createTitledBorder(adapter.getFile().getName()));
//
//                int rowCount = 0;
//
//                for(final Collection<JComponent> row : adapter.getReaderParameterControls())
//                {
//                    int columnCount = 0;
//                    for(final JComponent column : row)
//                    {
//                        final Dimension dimension = column.getPreferredSize();
//
//                        // This is a work-around to resize (and then stretch) the middle column to fit our input form layout
//                        if(dimension == null || (columnCount == 1 &&
//                           (dimension.getWidth()  < 1 ||
//                            dimension.getHeight() < 1)))
//                        {
//
//                            column.setPreferredSize(new Dimension(220, 25));
//                        }
//                        readerPanel.add(new SimpleGridBagConstraints(columnCount, rowCount, columnCount == 1), column, row);
//
//                        //readerPanel.add(column, new SimpleGridBagConstraints(columnCount, rowCount, columnCount == 1));
//
//                        ++columnCount;
//                    }
//
//                    ++rowCount;
//                }
//
//                this.content.getChildren().add(readerPanel);
//            }
//        }
//
//        this.sizeToScene();
    }

    public TileReadersOptionViewerWindow(final Collection<TileStoreReaderAdapter> tileStoreReaderAdapters)
    {
        this(tileStoreReaderAdapters, null);
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
//                                      this.error(String.format("There was an error opening %s: %s",
//                                                               adapter.getFile().getName(),
//                                                               ex.getMessage()));
                                  }
                                });

        return readers;
    }

    class BorderedTitledPane extends StackPane {
        BorderedTitledPane(final String titleString, final Node content) {
          Label title = new Label(" " + titleString + " ");
          title.getStyleClass().add("bordered-titled-title");
          StackPane.setAlignment(title, Pos.TOP_CENTER);

          StackPane contentPane = new StackPane();
          content.getStyleClass().add("bordered-titled-content");
          contentPane.getChildren().add(content);

          this.getStyleClass().add("bordered-titled-border");
          this.getChildren().addAll(title, contentPane);
        }
      }

}

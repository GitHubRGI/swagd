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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import utility.SimpleGridBagConstraints;
import utility.TileStoreUtility;

import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.packager.Packager;
import com.rgi.suite.Settings;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;
import com.rgi.suite.tilestoreadapter.TileStoreWriterAdapter;
import com.rgi.suite.tilestoreadapter.geopackage.GeoPackageTileStoreWriterAdapter;
import com.rgi.suite.tilestoreadapter.tms.TmsTileStoreWriterAdapter;
import com.rgi.suite.uielements.ProgressDialog;

/**
 * Gather additional information for packaging, and package
 *
 * @author Luke Lambert
 *
 */
public class PackagerWindow extends NavigationWindow
{
    private static final long serialVersionUID = -3488202344008846021L;

    private final Settings settings;

    private TileStoreReaderAdapter tileStoreReaderAdapter = null;
    private TileStoreWriterAdapter tileStoreWriterAdapter = null;

    // Input stuff
    private final JPanel     inputPanel          = new JPanel(new GridBagLayout());
    private final JTextField inputFileName       = new JTextField();
    private final JButton    inputFileNameButton = new JButton("\u2026");

    // Output stuff
    private final JPanel outputPanel = new JPanel(new GridBagLayout());
    private final JComboBox<TileStoreWriterAdapter> outputStoreType = new JComboBox<>();

    private static final String LastInputLocationSettingName = "package.lastInputLocation";

    /**
     * Constructor
     *
     * @param settings
     *             Settings used to hint user preferences
     */
    public PackagerWindow(final Settings settings)
    {
        this.setTitle(this.processName() + " Settings");

        this.setResizable(false);

        this.settings = settings;

        this.outputStoreType.addItem(new GeoPackageTileStoreWriterAdapter(settings));
        this.outputStoreType.addItem(new TmsTileStoreWriterAdapter       (settings));

        this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.PAGE_AXIS));

        // Input stuff
        this.inputFileName.setEditable(false);

        this.inputPanel .setBorder(BorderFactory.createTitledBorder("Input"));

        this.inputFileNameButton.addActionListener(e -> { final String startDirectory = this.settings.get(LastInputLocationSettingName, System.getProperty("user.home"));

                                                          final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

                                                          fileChooser.setMultiSelectionEnabled(false);
                                                          fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                                                          final int option = fileChooser.showOpenDialog(this);

                                                          if(option == JFileChooser.APPROVE_OPTION)
                                                          {
                                                              final File file = fileChooser.getSelectedFile();

                                                              try
                                                              {
                                                                  this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                                                                  final TileStoreReaderAdapter adapter = TileStoreUtility.getTileStoreReaderAdapter(false, file);

                                                                  if(adapter == null)
                                                                  {
                                                                      this.warn("Selected file doesn't contain a recognized tile store type.");
                                                                  }
                                                                  else
                                                                  {
                                                                      this.tileStoreReaderAdapter = adapter;
                                                                      this.inputFileName.setText(file.getAbsolutePath());

                                                                      this.settings.set(LastInputLocationSettingName, file.getParent());
                                                                      this.settings.save();

                                                                      this.buildInputContent();
                                                                      this.tileStoreWriterAdapter.hint(file);
                                                                  }
                                                              }
                                                              catch(final Exception ex)
                                                              {
                                                                  this.error(ex.getMessage());
                                                              }
                                                              finally
                                                              {
                                                                  this.setCursor(Cursor.getDefaultCursor());
                                                              }
                                                          }
                                                        });

        this.contentPanel.add(this.inputPanel);

        this.buildInputContent();

        // Output Stuff
        this.outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));

        this.outputStoreType.addActionListener(e -> { this.buildOutputContent();
                                                      try
                                                      {
                                                          this.tileStoreWriterAdapter.hint(new File(this.inputFileName.getText()));
                                                      }
                                                      catch(final Exception ex)
                                                      {
                                                          ex.printStackTrace();
                                                      }
                                                    });

        this.buildOutputContent();

        this.contentPanel.add(this.outputPanel);

        this.pack();
    }

    private void buildInputContent()
    {
        this.inputPanel.removeAll();

        this.inputPanel.add(new JLabel("File:"),      new SimpleGridBagConstraints(0, 0, false));
        this.inputPanel.add(this.inputFileName,       new SimpleGridBagConstraints(1, 0, true));
        this.inputPanel.add(this.inputFileNameButton, new SimpleGridBagConstraints(2, 0, false));

        if(this.tileStoreReaderAdapter != null)
        {
            int rowCount = 1;
            for(final Collection<JComponent> row : this.tileStoreReaderAdapter.getReaderParameterControls())
            {
                int columnCount = 0;
                for(final JComponent column : row)
                {
                    final Dimension dimension = column.getPreferredSize();

                    // This is a work-around to resize (and then stretch) the middle column to fit our input form layout
                    if(columnCount == 1 &&
                       (dimension.getWidth()  < 1 ||
                        dimension.getHeight() < 1))
                    {

                        column.setPreferredSize(new Dimension(220, 25));
                    }

                    this.inputPanel.add(column, new SimpleGridBagConstraints(columnCount, rowCount, columnCount == 1));

                    ++columnCount;
                }

                ++rowCount;
            }

            this.revalidate();
            this.outputPanel.repaint();
            this.pack();
        }
    }

    private void buildOutputContent()
    {
        this.outputPanel.removeAll();

        // Output tile store type
        this.outputPanel.add(new JLabel("Format:"), new SimpleGridBagConstraints(0, 0, false));
        this.outputPanel.add(this.outputStoreType,  new SimpleGridBagConstraints(1, 0, true));

        this.tileStoreWriterAdapter = (TileStoreWriterAdapter)this.outputStoreType.getSelectedItem();

        int rowCount = 1;
        for(final Collection<JComponent> row : this.tileStoreWriterAdapter.getWriterParameterControls())
        {
            int columnCount = 0;
            for(final JComponent column : row)
            {
                // This is a work-around to resize (and then stretch) the middle column to fit our input form layout
                if(columnCount == 1)
                {
                    column.setPreferredSize(new Dimension(220, 25));
                }

                this.outputPanel.add(column, new SimpleGridBagConstraints(columnCount, rowCount, columnCount == 1));

                ++columnCount;
            }

            ++rowCount;
        }

        this.revalidate();
        this.outputPanel.repaint();
        this.pack();
    }

    @Override
    protected String processName()
    {
        return "Packaging";
    }

    @Override
    protected boolean execute() throws Exception
    {
        if(this.tileStoreReaderAdapter == null)
        {
            this.warn("Please select an input file.");
            return false;
        }

        // This spawns a modal dialog and blocks this thread
        ProgressDialog.trackProgress(this,
                                     this.processName() + "...",
                                     taskMonitor -> { try(final TileStoreReader tileStoreReader = this.tileStoreReaderAdapter.getTileStoreReader())
                                                      {
                                                          try(final TileStoreWriter tileStoreWriter = this.tileStoreWriterAdapter.getTileStoreWriter(tileStoreReader))
                                                          {
                                                              (new Packager(taskMonitor,
                                                                            tileStoreReader,
                                                                            tileStoreWriter)).execute();
                                                              return null;
                                                          }
                                                      }
                                                    });

        return true;
    }
}

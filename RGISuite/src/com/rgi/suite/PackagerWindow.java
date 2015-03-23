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

import utility.TileStoreUtility;

import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.packager.Packager;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;
import com.rgi.suite.tilestoreadapter.TileStoreWriterAdapter;
import com.rgi.suite.tilestoreadapter.geopackage.GeoPackageTileStoreWriterAdapter;
import com.rgi.suite.tilestoreadapter.tms.TmsTileStoreWriterAdapter;



/**
 * Gather additional information for packaging, and package
 *
 * @author Luke D. Lambert
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

    private final String processName = "Packaging";

    private static final String LastInputLocationSettingName = "package.lastInputLocation";

    /**
     * Constructor
     *
     * @param settings
     *             Settings used to hint user preferences
     */
    public PackagerWindow(final Settings settings)
    {
        this.setTitle(this.processName + " Settings");

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
                                                                  final TileStoreReaderAdapter adapter = TileStoreUtility.getTileStoreReaderAdapter(false, file);

                                                                  if(adapter == null)
                                                                  {
                                                                      this.warn(this.processName, "Selected file doesn't contain a recognized tile store type.");
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
                                                                  this.error(this.processName, ex.getMessage());
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
                    if(columnCount == 1) // TODO; This is a HACK
                    {
                        column.setPreferredSize(new Dimension(220, 25));
                    }

                    this.inputPanel.add(column, new SimpleGridBagConstraints(columnCount, rowCount, columnCount == 1)); // TODO; last parameter is similarly a hack for that second column

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
                if(columnCount == 1) // TODO; This is a HACK
                {
                    column.setPreferredSize(new Dimension(220, 25));
                }

                this.outputPanel.add(column, new SimpleGridBagConstraints(columnCount, rowCount, columnCount == 1)); // TODO; last parameter is similarly a hack for that second column

                ++columnCount;
            }

            ++rowCount;
        }

        this.revalidate();
        this.outputPanel.repaint();
        this.pack();
    }

    @Override
    protected void execute() throws Exception
    {
        if(this.tileStoreReaderAdapter == null)
        {
            this.warn(this.processName, "Please select an input file.");
            return;
        }

        try(final TileStoreReader tileStoreReader = this.tileStoreReaderAdapter.getTileStoreReader())
        {
            try(final TileStoreWriter tileStoreWriter = this.tileStoreWriterAdapter.getTileStoreWriter(tileStoreReader))
            {
                final Packager packager = new Packager(tileStoreReader, tileStoreWriter);
                packager.execute();   // TODO monitor errors/progress
            }
        }
    }
}

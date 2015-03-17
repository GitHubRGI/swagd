package com.rgi.suite;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.rgi.common.tile.store.TileStoreReader;
import com.rgi.common.tile.store.TileStoreWriter;
import com.rgi.suite.tilestoreadapter.AdapterMismatchException;
import com.rgi.suite.tilestoreadapter.TileStoreReaderAdapter;
import com.rgi.suite.tilestoreadapter.TileStoreWriterAdapter;
import com.rgi.suite.tilestoreadapter.geopackage.GeoPackageTileStoreReaderAdapter;
import com.rgi.suite.tilestoreadapter.geopackage.GeoPackageTileStoreWriterAdapter;
import com.rgi.suite.tilestoreadapter.tms.TmsTileStoreReaderAdapter;
import com.rgi.suite.tilestoreadapter.tms.TmsTileStoreWriterAdapter;


/**
 * Gather additional information for packaging, and package
 *
 * @author Steven D. Lander
 * @author Luke D. Lambert
 *
 */
public abstract class TileStoreCreationWindow extends JFrame
{
    private static final long serialVersionUID = -3488202344008846021L;

    protected final Settings settings;

    protected TileStoreReaderAdapter tileStoreReaderAdapter = null;
    protected TileStoreWriterAdapter tileStoreWriterAdapter = null;

    protected final static Collection<Class<? extends TileStoreReaderAdapter>> KnownTileStoreReaderAdapters = Arrays.asList(TmsTileStoreReaderAdapter.class, GeoPackageTileStoreReaderAdapter.class);

    protected final JPanel contentPanel = new JPanel();

    // Input stuff
    protected final JPanel     inputPanel          = new JPanel(new GridBagLayout());
    protected final JTextField inputFileName       = new JTextField();
    protected final JButton    inputFileNameButton = new JButton("\u2026");

    // Output stuff
    protected final JPanel outputPanel = new JPanel(new GridBagLayout());
    protected final JComboBox<TileStoreWriterAdapter> outputStoreType = new JComboBox<>(new DefaultComboBoxModel<>());

    // Navigation stuff
    protected final JPanel  navigationPanel = new JPanel(new GridBagLayout());
    protected final JButton okButton        = new JButton("OK");
    protected final JButton cancelButton    = new JButton("Cancel");

    protected final String lastInputLocationSettingName;
    protected final String processName;

    /**
     * Constructor
     *
     * @param processName
     *             Concrete implementation's functional name (e.g. Packaging, Tiling, etc)
     * @param settings
     *             Settings used to hint user preferences
     * @param lastInputLocationSettingName
     *             The name of the setting to use to read/store the last input location
     */
    public TileStoreCreationWindow(final String processName, final Settings settings, final String lastInputLocationSettingName)
    {
        this.processName = processName;

        this.setTitle(processName + " Settings");
        this.setLayout(new BorderLayout());
        this.setResizable(false);

        this.settings = settings;
        this.lastInputLocationSettingName = lastInputLocationSettingName;

        this.outputStoreType.addItem(new GeoPackageTileStoreWriterAdapter(settings));
        this.outputStoreType.addItem(new TmsTileStoreWriterAdapter       (settings));

        this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.PAGE_AXIS));

        this.add(this.contentPanel,   BorderLayout.CENTER);
        this.add(this.navigationPanel,BorderLayout.SOUTH);

        // Input stuff
        this.inputFileName.setEditable(false);

        this.inputPanel .setBorder(BorderFactory.createTitledBorder("Input"));

        this.inputFileNameButton.addActionListener(e -> { final String startDirectory = TileStoreCreationWindow.this.settings.get(this.lastInputLocationSettingName, SettingsWindow.DefaultOutputLocation);

                                                          final JFileChooser fileChooser = new JFileChooser(new File(startDirectory));

                                                          fileChooser.setMultiSelectionEnabled(false);
                                                          fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                                                          final int option = fileChooser.showOpenDialog(TileStoreCreationWindow.this);

                                                          if(option == JFileChooser.APPROVE_OPTION)
                                                          {
                                                              final File file = fileChooser.getSelectedFile();

                                                              try
                                                              {
                                                                  this.tileStoreReaderAdapter = this.getReaderAdapter(file);

                                                                  this.inputFileName.setText(file.getAbsolutePath());

                                                                  this.settings.set(this.lastInputLocationSettingName, file.getParent());
                                                                  this.settings.save();

                                                                  this.buildInputContent();
                                                                  this.tileStoreWriterAdapter.hint(file);
                                                              }
                                                              catch(final Exception ex)
                                                              {
                                                                  this.error(ex.getMessage());
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

        this.buildNavigationPanel();
    }

    protected abstract void execute(final TileStoreReader tileStoreReader, final TileStoreWriter tileStoreWriter) throws Exception;

    protected String getLastInputLocation()
    {
        return TileStoreCreationWindow.this.settings.get(this.lastInputLocationSettingName, SettingsWindow.DefaultOutputLocation);
    }

    protected void closeFrame()
    {
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    @SuppressWarnings("serial")
    protected class SimpleGridBagConstraints extends GridBagConstraints
    {
        public SimpleGridBagConstraints(final int gridX, final int gridY, final boolean stretch)
        {
            super(gridX, gridY, 1, 1, stretch ? 1 : 0, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        }
    }

    protected void warn(final String message)
    {
        JOptionPane.showMessageDialog(this,
                                      message,
                                      this.processName,
                                      JOptionPane.WARNING_MESSAGE);
    }

    protected void error(final String message)
    {
        JOptionPane.showMessageDialog(this,
                                      message,
                                      this.processName,
                                      JOptionPane.ERROR_MESSAGE);
    }

    private void buildInputContent()
    {
        this.inputPanel.removeAll();

        this.inputPanel.add(new JLabel("File:"), new SimpleGridBagConstraints(0, 0, false));
        this.inputPanel.add(this.inputFileName,  new SimpleGridBagConstraints(1, 0, true));
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

    private TileStoreReaderAdapter getReaderAdapter(final File file)
    {
        for(final Class<? extends TileStoreReaderAdapter> readerClass : KnownTileStoreReaderAdapters)
        {
            try
            {
                return readerClass.getConstructor(File.class).newInstance(file);
            }
            catch(final NoSuchMethodException | SecurityException | IllegalAccessException | InstantiationException ex)
            {
                ex.printStackTrace();
            }
            catch(final InvocationTargetException ex)
            {
                if(ex.getTargetException() instanceof AdapterMismatchException)
                {
                    continue;
                }
            }
        }

        this.warn("Selected file doesn't contain a recognized tile store type.");
        return null;
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

    private void buildNavigationPanel()
    {
        this.cancelButton.addActionListener(e -> { TileStoreCreationWindow.this.closeFrame(); });

        this.okButton.addActionListener(e -> { try
                                               {
                                                   this.okButton.setEnabled(false);
                                                   this.execute();
                                                   this.closeFrame();
                                               }
                                               catch(final Exception ex)
                                               {
                                                   this.okButton.setEnabled(true);
                                                   ex.printStackTrace();
                                                   this.error("An error has occurred: " + ex.getMessage());
                                               }
                                             });

        // Add buttons to pane
        final Insets insets = new Insets(10, 10, 10, 10);
        final int    fill   = GridBagConstraints.NONE;

        this.navigationPanel.add(this.okButton,     new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST, fill, insets, 0, 0));
        this.navigationPanel.add(this.cancelButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, fill, insets, 0, 0));
    }

    private void execute() throws Exception
    {
        if(this.tileStoreReaderAdapter == null)
        {
            this.warn("Please select an input file.");
            return;
        }

        try(final TileStoreReader tileStoreReader = this.tileStoreReaderAdapter.getTileStoreReader())
        {
            try(final TileStoreWriter tileStoreWriter = this.tileStoreWriterAdapter.getTileStoreWriter(tileStoreReader))
            {
                this.execute(tileStoreReader, tileStoreWriter);
            }
        }
    }
}
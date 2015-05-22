package com.rgi.view.regions;

import java.io.File;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utility.TileStoreUtility;

import com.rgi.store.tiles.TileStoreException;


public class ViewerMenuBar extends MenuBar
{
    FileChooser fileChooser = new FileChooser();
    DirectoryChooser directoryChooser = new DirectoryChooser();
    public ViewerMenuBar(final Stage mainStage, final TreeRegion tree)
    {
        super();
        this.setFileChooser();
        Menu file = new Menu("File");
        MenuItem addGeoPackage = new MenuItem("Add GeoPackage");
        addGeoPackage.setOnAction(actionEvent -> {
                                                    List<File> files = this.fileChooser.showOpenMultipleDialog(mainStage);
                                                    if(files != null && files.size() > 0)
                                                    {

//                                                        final TileReadersOptionWindow tileReadersOptionWindow = new TileReadersOptionWindow(TileStoreUtility.getTileStoreReaderAdapters(true, files),
//                                                                                                                                            readers -> { final JFrame viewWindow = new MapViewWindow(readers);
//                                                                                                                                                         viewWindow.setLocationRelativeTo(null);
//                                                                                                                                                         viewWindow.setVisible(true);
//                                                                                                                                                       });
                                                         TileReadersOptionViewerWindow window = new TileReadersOptionViewerWindow(TileStoreUtility.getTileStoreReaderAdapters(true,  files.toArray(new File[files.size()])));
                                                         window.getReaders().forEach(reader-> { try
                                                                                                {
                                                                                                     tree.addTreeItem(reader);
                                                                                                }
                                                                                                catch(TileStoreException ex)
                                                                                                {
                                                                                                     throw new RuntimeException(ex.getMessage());
                                                                                                }
                                                                                              });
//                                                        if(tileReadersOptionWindow.needsInput())
//                                                        {
//                                                            tileReadersOptionWindow.setLocationRelativeTo(null);
//                                                            tileReadersOptionWindow.setVisible(true);
//                                                        }
//                                                        else
//                                                        {
//                                                            tileReadersOptionWindow.execute();
//                                                        }
                                                    }
                                                });
        MenuItem addTMS = new MenuItem("Add TMS");
        addTMS.setOnAction(actionEvent -> {
            File folder = this.directoryChooser.showDialog(mainStage);
            if(folder != null)
            {
                 TileReadersOptionViewerWindow window = new TileReadersOptionViewerWindow(TileStoreUtility.getTileStoreReaderAdapters(true, new File[]{folder}));
                 window.getReaders().forEach(reader-> { try
                                                        {
                                                             tree.addTreeItem(reader);
                                                        }
                                                        catch(TileStoreException ex)
                                                        {
                                                             throw new RuntimeException(ex.getMessage());
                                                        }
                                                      });
                 //TODO needs input!! tms!!!
//                if(tileReadersOptionWindow.needsInput())
//                {
//                    tileReadersOptionWindow.setLocationRelativeTo(null);
//                    tileReadersOptionWindow.setVisible(true);
//                }
            }
        });


        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(actionEvent -> Platform.exit());

        file.getItems().addAll(addGeoPackage, addTMS, exitMenuItem);

        this.getMenus().add(file);
    }
    private void setFileChooser()
    {
        this.fileChooser.setTitle("Open GeoPackage file or TMS folder");

    }
}

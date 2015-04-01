package com.rgi.verifiertool;

import java.io.File;

import javafx.application.Platform;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;

import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.GeoPackage.OpenMode;
import com.rgi.geopackage.verification.VerificationLevel;

public class FileVerificationPane extends TitledPane
{
    private final VBox content = new VBox();

    public FileVerificationPane(final File geoPackageFile)
    {
        if(geoPackageFile == null || !geoPackageFile.canRead())
        {
            throw new IllegalArgumentException("GeoPackage file may not be null, and must be a valid filename");
        }

        this.setText(geoPackageFile.getName());
        this.setContent(this.content);

        Platform.runLater(() -> { try(GeoPackage geoPackage = new GeoPackage(geoPackageFile, VerificationLevel.None, OpenMode.Open))
                                  {
                                      this.content.getChildren().add(new SubsystemVerificationPane("Core", () -> geoPackage.core().getVerificationIssues(geoPackage.getFile(), VerificationLevel.Full)));
                                  }
                                  catch(final Exception ex)
                                  {
                                      throw new RuntimeException(ex);
                                  }
                                });
    }
}

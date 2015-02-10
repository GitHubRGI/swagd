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

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.rgi.common.task.MonitorableTask;
import com.rgi.common.task.Settings;
import com.rgi.common.task.Settings.Setting;
import com.rgi.common.task.Task;
import com.rgi.common.task.TaskFactory;
import com.rgi.suite.ApplicationContext.Window;
import com.rgi.view.MapViewWindow;
import com.rgi.view.Viewer;

public class FileChooserWindow extends BaseWindow
{
    private JFileChooser fileChooser;

    public FileChooserWindow(ApplicationContext context)
    {
        super(context);
    }

    @Override
    public void activate()
    {
        Task task = this.context.getActiveTask();
        if(task != null)
        {
            TaskFactory factory = task.getFactory();
            this.fileChooser.setMultiSelectionEnabled(factory.selectMultiple());
            if(factory.selectFilesOnly())
            {
                this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            }
            else if(factory.selectFoldersOnly())
            {
                this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
            else
            {
                this.fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            }
            this.fileChooser.setToolTipText(factory.getFilePrompt());
        }
    }

    @Override
    protected void buildContentPane()
    {
        this.contentPane = new JPanel(new BorderLayout());
        this.fileChooser = new JFileChooser();
        this.contentPane.add(this.fileChooser, BorderLayout.CENTER);
        this.fileChooser.addActionListener(event -> { Task task = this.context.getActiveTask();

//                                                      if(task != null)
//                                                      {
//                                                          final File defaultFilePath = new File(task.getFactory().getDefaultFilePath());
//
//                                                          if(defaultFilePath.canRead())
//                                                          {
//                                                              this.fileChooser.setCurrentDirectory(defaultFilePath);
//                                                          }
//                                                      }

                                                      if(JFileChooser.APPROVE_SELECTION.equals(event.getActionCommand()))
                                                      {
                                                          Settings settings = this.context.getSettings();
                                                          if(this.fileChooser.isMultiSelectionEnabled())
                                                          {
                                                              settings.set(Setting.FileSelection, this.fileChooser.getSelectedFiles());
                                                          }
                                                          else
                                                          {
                                                              settings.set(Setting.FileSelection, this.fileChooser.getSelectedFile());
                                                          }
                                                          // File chosen is set, now do something based on the workflow
                                                          if(task != null && task instanceof MonitorableTask)
                                                          {
                                                              this.context.transitionTo(Window.PROGRESS);
                                                              task.execute(this.context.getSettings());
                                                              return;
                                                          }
                                                          else if(task != null && task instanceof Viewer)
                                                          {
                                                              // Probe the chosen file or files for the type of tile store
                                                              // Initialize a tile loader object on the file chosen
                                                              // Create a new JFrame window with a MapViewWindow, pointing
                                                              // to the appropriate loader
                                                              String selections = settings.get(Setting.FileSelection);
                                                              String[] fileSelections = selections.split(";");
                                                              if(fileSelections.length == 1)
                                                              {
                                                                  // Single selection
                                                                  File store = new File(fileSelections[0]);
                                                                  try
                                                                  {
                                                                      JFrame frame = new MapViewWindow(store);
                                                                      frame.pack();
                                                                      frame.setVisible(true);
                                                                  }
                                                                  catch(Exception e)
                                                                  {
                                                                      e.printStackTrace();
                                                                  }
                                                              }
                                                              else
                                                              {
                                                                  // Multi-selection
                                                              }
                                                          }
                                                      }
                                                      else if(JFileChooser.CANCEL_SELECTION.equals(event.getActionCommand()))
                                                      {
                                                          this.context.setActiveTask(null);
                                                      }
                                                      this.context.transitionTo(Window.MAIN);
                                                    });
    }
}

package com.rgi.suite;

import java.awt.Frame;
import java.util.function.Function;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import com.rgi.common.TaskMonitor;

public class ProgressDialog extends JDialog
{
    private final JProgressBar progressBar = new JProgressBar();

    public ProgressDialog(final Frame owner, final String title, final Function<TaskMonitor, Boolean> process)
    {
        super(owner, title, ModalityType.DOCUMENT_MODAL);

        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.progressBar.setStringPainted(true);

        this.progressBar.setMinimum(0);

        final SwingWorker<Boolean, Void> task = new SwingWorker<Boolean, Void>()
                                             {
                                                 @Override
                                                 protected Boolean doInBackground() throws Exception
                                                 {
                                                     return process.apply(new TaskMonitor()
                                                                          {
                                                                              @Override
                                                                              public void setProgress(final int value)
                                                                              {
                                                                                  ProgressDialog.this.progressBar.setValue(value);
                                                                              }

                                                                              @Override
                                                                              public void setMaximum(final int max)
                                                                              {
                                                                                  ProgressDialog.this.progressBar.setMaximum(max);
                                                                              }

                                                                              @Override
                                                                              public void setError(final Exception e)
                                                                              {
                                                                                  // Display error dialog
                                                                              }

                                                                              @Override
                                                                              public void finished()
                                                                              {
                                                                                  // TODO Auto-generated method stub
                                                                              }

                                                                              @Override
                                                                              public void cancelled()
                                                                              {
                                                                                  // TODO Auto-generated method stub
                                                                              }
                                                                          });
                                                 }
                                             };
    }
}

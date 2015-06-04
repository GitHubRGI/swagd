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

package com.rgi.suite.uielements;

import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import com.rgi.common.TaskMonitor;
import com.rgi.common.util.functional.ThrowingFunction;

/**
 * Convenience methods for creating a modal progress bar dialog
 *
 * @author Luke Lambert
 *
 */
public class ProgressDialog
{
    /**
     * Creates a modal progress bar dialog, and executes and monitors the progress of the supplied input {@code function}
     *
     * @param owner
     *             the {@link Window} from which the dialog is displayed or
     *             {@code null} if this dialog has no owner
     * @param title
     *             the {@link String} to display in the dialog's title bar or
     *             {@code null} if the dialog has no title
     * @param function
     *             Function that serves as a monitorable callback. Progress updates are reflected in the dialog's UI
     * @return Value returned by the {@code function}
     * @throws InterruptedException
     *             if the current thread was interrupted while waiting
     * @throws ExecutionException
     *             if the computation threw an exception
     */
    public static <R> R trackProgress(final Frame owner, final String title, final ThrowingFunction<TaskMonitor, R> function) throws InterruptedException, ExecutionException
    {
        final JDialog progressDialog = new JDialog(owner, title, ModalityType.DOCUMENT_MODAL);

        progressDialog.setResizable(false);
        progressDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        progressDialog.setLayout(new FlowLayout(FlowLayout.CENTER));

        final JProgressBar progressBar = new JProgressBar();
        final JButton cancelButton = new JButton("Cancel");

        progressBar.setSize(200, 15);
        progressBar.setStringPainted(true);
        progressBar.setMinimum(0);

        progressDialog.add(progressBar);
        progressDialog.add(cancelButton);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(owner);

        final SwingWorker<R, Void> task = new SwingWorker<R, Void>()
                                          {
                                              @Override
                                              protected R doInBackground() throws Exception
                                              {
                                                  return function.apply(new TaskMonitor()
                                                                        {
                                                                            @Override
                                                                            public void setProgress(final int value)
                                                                            {
                                                                                progressBar.setValue(value);
                                                                                progressBar.setString(String.format("%d/%d",
                                                                                                                    value,
                                                                                                                    progressBar.getMaximum()));
                                                                            }

                                                                            @Override
                                                                            public void setMaximum(final int max)
                                                                            {
                                                                                progressBar.setMaximum(max);
                                                                            }
                                                                        });
                                              }

                                              @Override
                                              protected void done()
                                              {
                                                  progressDialog.dispose();
                                              }
                                          };

        cancelButton.addActionListener(e -> task.cancel(true));
        task.execute();
        progressDialog.setVisible(true);
        return task.get();
    }
}

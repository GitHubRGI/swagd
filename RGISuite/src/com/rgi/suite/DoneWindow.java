///*  Copyright (C) 2014 Reinventing Geospatial, Inc
// *
// *  This program is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program.  If not, see <http://www.gnu.org/licenses/>,
// *  or write to the Free Software Foundation, Inc., 59 Temple Place -
// *  Suite 330, Boston, MA 02111-1307, USA.
// */
//
//package com.rgi.suite;
//
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.util.Properties;
//
//import javax.swing.JButton;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//
//import com.rgi.suite.ApplicationContext.Window;
//
//public class DoneWindow extends AbstractWindow {
//    public DoneWindow(ApplicationContext context) {
//        super(context);
//    }
//
//    @Override
//    protected void buildContentPane() {
//        this.contentPane = new JPanel(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.weightx = 1.0;
//        gbc.weighty = 1.0;
//
//        this.contentPane.add(new JLabel("Finished!"), gbc);
//    }
//
//    @Override
//    protected void buildNavPane() {
//        this.navPane = new JPanel(new GridBagLayout());
//        Properties props = this.context.getProperties();
//        JButton okButton = new JButton(new PropertiesAction(props, "done") {
//            /**
//             * Generated serial
//             */
//            private static final long serialVersionUID = -455467082471731446L;
//
//            @Override
//            public void actionPerformed(ActionEvent event) {
//                DoneWindow.this.context.transitionTo(Window.MAIN);
//            }
//        });
//        okButton.setHideActionText(true);
//        okButton.setMargin(new Insets(0, 0, 0, 0));
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.anchor = GridBagConstraints.EAST;
//        gbc.weightx = 1.0;
//        gbc.insets = new Insets(10, 10, 10, 10);
//        this.navPane.add(okButton, gbc);
//    }
//}

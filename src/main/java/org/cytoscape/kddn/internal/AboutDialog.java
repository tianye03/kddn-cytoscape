/*
 * Copyright (C) 2014 Ye Tian
 * Department of Electrical and Computer Engineering, Virginia Tech
 * 
 * This file is part of KDDN app for Cytoscape.
 *
 * KDDN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * KDDN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with KDDN. If not, see <http://www.gnu.org/licenses/>.
 */

package org.cytoscape.kddn.internal;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 8564703630585105888L;

	final public JFrame optionsFrame;

	public AboutDialog () {
		optionsFrame = new JFrame("About KDDN");
		optionsFrame.setResizable(false);
		optionsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		optionsFrame.setAlwaysOnTop(true);
		optionsFrame.setLocationRelativeTo(null);

		JEditorPane aboutPanel = anEditorPane();
		JPanel buttonPanel = aButton();

		optionsFrame.getContentPane().add(aboutPanel, BorderLayout.CENTER);
		optionsFrame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		optionsFrame.pack();
		optionsFrame.setVisible(true);
	}

	private JPanel aButton(){
		JPanel aPanel = new JPanel();
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				optionsFrame.dispose();
			}
		});
		closeButton.setAlignmentX(CENTER_ALIGNMENT);
		aPanel.add(closeButton);
		return aPanel;
	}

	private JEditorPane anEditorPane() {
		JEditorPane editorPane = new JEditorPane();
		editorPane.setMargin(new Insets(10, 10, 10, 10));
		editorPane.setEditable(false);
		editorPane.setOpaque(false);
		editorPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));

		String text = "<html><p align='center'>"
			+ "<b>K</b>nowledge-fused <b>D</b>ifferential <b>D</b>ependency <b>N</b>etwork</b><br>"
			+ "<small>Version 1.0.0</small><br /><br />"
			+ "<b>Main developers</b><br />"
			+ "Ye Tian (Virginia Tech)<br>"
			+ "Bai Zhang (Johns Hopkins University)"
			+ "<br /><br /><b>Contact</b><br />"
			+ "<a href=\"mailto:tianye@vt.edu\">tianye@vt.edu</a>"
			+ "<br><a href=\"mailto:baizhang@jhu.edu\">baizhang@jhu.edu</a>"
			+ "</p></html>";
		
		editorPane.setText(text);

		return editorPane;
	}
}


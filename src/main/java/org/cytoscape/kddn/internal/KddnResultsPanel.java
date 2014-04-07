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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;

/**
 * Results panel on the right side of Cytoscape
 * @author Ye Tian
 *
 */
public class KddnResultsPanel extends JPanel implements CytoPanelComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5850929400715900317L;
	// Constructor attributes
	private JTabbedPane resultsTabbedPanel;	
	protected CySwingApplication desktop;
	private CytoPanel cytoPanelEast;
	
	/**
	 * Default constructor
	 * creates an empty results panel
	 * @param desktop 
	 */
	public KddnResultsPanel(CySwingApplication desktop){
		this.desktop = desktop;
		this.cytoPanelEast = this.desktop.getCytoPanel(getCytoPanelName());
		this.setVisible(true);

		int width = (int)(0.2 * Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		this.setPreferredSize(new Dimension(width, 0));
		this.setLayout(new BorderLayout());
		this.resultsTabbedPanel = new JTabbedPane();
	
		this.add(resultsTabbedPanel, BorderLayout.CENTER);
	}
	
	
		
	/**
	 * Get the results panel component
	 */
	public Component getComponent() {
		return this;
	}
	
	/**
	 * Get the results panel name
	 */
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}
	
	/**
	 * Get the results panel title
	 */
	public String getTitle() {
		return "KDDN Results";
	}
	
	/**
	 * Get the results panel icon
	 */
	public Icon getIcon() {
		return null;
	}
	
	public JTabbedPane getResultsTabbedPanel() {
		return resultsTabbedPanel;
	}
	
	public void getPanel() {
		cytoPanelEast.setState(CytoPanelState.DOCK);
		int index = cytoPanelEast.indexOfComponent(CyActivator.kddnResultsPanel);
		if (index == -1) {
			return;
		}
		cytoPanelEast.setSelectedIndex(index);
	}
}

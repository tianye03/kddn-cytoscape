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

import java.awt.event.ActionEvent;

import javax.help.CSH.DisplayHelpFromSource;
import javax.help.HelpBroker;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;

/**
 * Creates KDDN menu item under Apps menu section.
 *
 */
public class KddnMenuAction extends AbstractCyAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7542159220010084516L;

	// Cytoscape swing service
	private final CySwingApplication desktopApp;
		
	// Cytoscape panels
	private final CytoPanel cyPanelWest;
	private final KddnConfigurePanel controlPanel;
		
	public KddnMenuAction(CyApplicationManager cyApplicationManager, final String menuTitle,
			CySwingApplication desktopApp,
			KddnConfigurePanel myControlPanel) {
		
		super(menuTitle);
		setPreferredMenu("Apps.KDDN");
		
		this.name = name;
		this.desktopApp = desktopApp;
		this.cyPanelWest = this.desktopApp.getCytoPanel(CytoPanelName.WEST);
		this.controlPanel = myControlPanel;
	}

	public void actionPerformed(ActionEvent e) {

		if (name == "About") {
			new AboutDialog();
		} else if (name == "Run analysis") {
			selectInputPanel();
		} else if (name == "Help") {
			HelpBroker helpBroker = CyActivator.cyHelpBroker.getHelpBroker();
			helpBroker.setCurrentID("index");
			DisplayHelpFromSource action = new DisplayHelpFromSource(helpBroker);
			action.actionPerformed(e);
		}
		
	}
	
	/**
	 * Select Cytoscape control panel
	 */
	public void selectInputPanel() {
		// If the state of the cytoPanelWest is HIDE, show it
		if (cyPanelWest.getState() == CytoPanelState.HIDE) {
			cyPanelWest.setState(CytoPanelState.DOCK);
		}
		// Select my panel
		int index = cyPanelWest.indexOfComponent(controlPanel);
		if (index == -1) {
			return;
		}
		cyPanelWest.setSelectedIndex(index);
	}
}

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
import java.awt.event.ActionListener;

public class ResultsTabCloseActionHandler implements ActionListener {

	private String tabName;
	private KddnResultsPanel kddnResultsPanel;
	
	public ResultsTabCloseActionHandler(String tabName,
			KddnResultsPanel kddnResultsPanel) {
		this.tabName = tabName;
		this.kddnResultsPanel = kddnResultsPanel;
	}

	public String getTabName() {
		return tabName;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		int index = kddnResultsPanel.getResultsTabbedPanel().indexOfTab(getTabName());
		if (index >= 0) {
			kddnResultsPanel.getResultsTabbedPanel().removeTabAt(index);
		}
	}

}

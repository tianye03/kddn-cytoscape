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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Tabbed panel in results panel
 * @author Ye Tian
 *
 */
public class KddnResultsTabbedPanel extends JPanel {
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = -4953680062811764653L;
	
	private KddnExperiment kddnExperiment;
	private KddnResults kddnResults;
		
	public KddnResultsTabbedPanel(KddnExperiment kddnExperiment, KddnResults kddnResults) {
		this.kddnExperiment = kddnExperiment;
		this.kddnResults = kddnResults;
		
    	JPanel main = new JPanel(new BorderLayout(2,2));
    	CyActivator.kddnResultsPanel.getResultsTabbedPanel().addTab("Tab "+CyActivator.totalNumberRun, main);

    	JPanel pnlTab = new JPanel(new GridBagLayout());
    	pnlTab.setOpaque(false);
    	JLabel lblTitle = new JLabel("KDDN network "+CyActivator.totalNumberRun);
    	JButton btnClose = new JButton("x");
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.weightx = 1;
    	pnlTab.add(lblTitle, gbc);
    	gbc.gridx++;
    	gbc.weightx = 0;
    	pnlTab.add(btnClose, gbc);
    	CyActivator.kddnResultsPanel.getResultsTabbedPanel().setTabComponentAt(CyActivator.kddnResultsPanel.getResultsTabbedPanel().getTabCount()-1, pnlTab);
    	btnClose.addActionListener(new ResultsTabCloseActionHandler("Tab "+CyActivator.totalNumberRun,
    			CyActivator.kddnResultsPanel));
    	   	
    	// add tables
    	JPanel subMain = new JPanel(new BorderLayout(2,12));
    	
    	// assemble parameter table
    	// single condition
		String tableName = "Experiment paramters:";
    	if(!kddnExperiment.twoCondition) {
    		// no knowledge
    		if(!kddnExperiment.useKnowledge) {
    			String[] header = {"lambda 1"};
    			Object[][] data = {{new Double(kddnExperiment.lambda1)}};
    			JPanel table = createTablePanel(data, header, tableName, 0, false);
    	    	subMain.add(table, BorderLayout.NORTH);
    		} else { // use knowledge
    			String[] header = {"lambda 1", "theta", "delta"};
    			Object[][] data = {{new Double(kddnExperiment.lambda1),
    				new Double(kddnExperiment.theta), new Double(kddnExperiment.delta)}};
    			JPanel table = createTablePanel(data, header, tableName, 0, false);
    	    	subMain.add(table, BorderLayout.NORTH);
    		}
    	} else { // two conditions
    		// no knowledge
    		if(!kddnExperiment.useKnowledge) {
    			String[] header = {"lambda 1", "lambda 2", "alpha"};
    			Object[][] data = {{new Double(kddnExperiment.lambda1),
    				new Double(kddnExperiment.lambda2), new Double(kddnExperiment.alpha)}};
    			JPanel table = createTablePanel(data, header, tableName, 0, false);
    	    	subMain.add(table, BorderLayout.NORTH);
    		} else { // use knowledge
    			String[] header = {"lambda 1", "lambda 2", "alpha", "theta", "delta"};
    			Object[][] data = {{new Double(kddnExperiment.lambda1),
    				new Double(kddnExperiment.lambda2), new Double(kddnExperiment.alpha),
    				new Double(kddnExperiment.theta), new Double(kddnExperiment.delta)}};
    			JPanel table = createTablePanel(data, header, tableName, 0, false);
    	    	subMain.add(table, BorderLayout.NORTH);
    		}
    	}
    	    	
    	// assemble node table
    	// single condition
    	if(!kddnExperiment.twoCondition) {
    		tableName = "Node connectivity degree:";
			String[] header = {"Node", "Degree"};
			Object[][] data = getNodeTableData(kddnResults, false);
			JPanel table = createTablePanel(data, header, tableName, 1, true);
	    	subMain.add(table, BorderLayout.CENTER);
    	} else { // two conditions
    		tableName = "Node differential connectivity degree:";
			String[] header = {"Node", "Degree"};
			Object[][] data = getNodeTableData(kddnResults, true);
			JPanel table = createTablePanel(data, header, tableName, 1, true);
	    	subMain.add(table, BorderLayout.CENTER);
    	}
    	
    	// assemble edge table
    	tableName = "Differential edges:";
    	if(kddnExperiment.twoCondition) {
    		// has pvalue
    		if(kddnExperiment.needPvalue) {
				String[] header = {"Node 1", "Node 2", "Condition", "P-value"};
				Object[][] data = getEdgeTableData(kddnResults, true);
				JPanel table = createTablePanel(data, header, tableName, 3, false);
		    	subMain.add(table, BorderLayout.SOUTH);
    		} else { // no pvalue
    			String[] header = {"Node 1", "Node 2", "Condition"};
				Object[][] data = getEdgeTableData(kddnResults, false);
				JPanel table = createTablePanel(data, header, tableName, 2, false);
		    	subMain.add(table, BorderLayout.SOUTH);
    		}
    	}
    	    	    	
    	JScrollPane mainScroll = new JScrollPane(subMain);
    	main.add(mainScroll, BorderLayout.CENTER);

    }

	/**
	 * create differential edge table data
	 * @param dn
	 * @param pvalue: whether pvalue is calculated
	 * @return
	 */
	private Object[][] getEdgeTableData(KddnResults kddn, boolean pvalue) {
		String[] var = kddn.varList;
		int p = kddn.pValue.length;
		double[][] pV = kddn.pValue;
		Object[][] tableData;
		
		if(pvalue) {
			tableData = new Object[p][4];
			for(int i=0; i<p; i++) {
				tableData[i][0] = var[(int) pV[i][0]];
				tableData[i][1] = var[(int) pV[i][1]];
				tableData[i][2] = (int) pV[i][3];
				tableData[i][3] = pV[i][2];
			}
		} else {
			tableData = new Object[p][3];
			for(int i=0; i<p; i++) {
				tableData[i][0] = var[(int) pV[i][0]];
				tableData[i][1] = var[(int) pV[i][1]];
				tableData[i][2] = (int) pV[i][3];
			}
		}
		return tableData;
	}

	/**
	 * Return table data for node table
	 * @param kddn
	 * @param twoCondition: if it's a two condition experiment
	 * @return
	 */
	private Object[][] getNodeTableData(KddnResults kddn, boolean twoCondition) {
		String[] var = kddn.varList;
		Object[][] tableData = new Object[var.length][2];
		for(int i=0; i<var.length; i++) {
			tableData[i][0] = var[i];
			if(twoCondition)
				tableData[i][1] = getDifferentialDegree(kddn.adjacentMatrix, i);
			else
				tableData[i][1] = getConserveDegree(kddn.adjacentMatrix, i);
		}
		return tableData;
	}
	
	/**
	 * Count degree of conserved edges of node i
	 * @param dn
	 * @param i
	 * @return
	 */
	private Object getConserveDegree(int[][] dn, int i) {
		int p = dn.length;
		int degree = 0;
		for(int j=0; j<p; j++) {
			if(dn[i][j] != 0)
				degree++;
		}
		return degree;
	}

	/**
	 * Count degree of differential edges of node i
	 * @param dn
	 * @param i
	 * @return
	 */
	private Object getDifferentialDegree(int[][] dn, int i) {
		int p = dn.length;
		int degree = 0;
		for(int j=0; j<p; j++) {
			if(dn[i][j] != dn[i][j+p])
				degree++;
		}
		return degree;
	}

	/**
	 * assemble table panel
	 * @param data
	 * @param columnNames
	 * @param tableName
	 * @param toggleTwice
	 * @param i: the column of default sorting
	 * @return
	 */
	private JPanel createTablePanel(Object[][] data, String[] columnNames,
			String tableName, int i, boolean toggleTwice) {
		
    	JLabel tableLabel = new JLabel(tableName);
    	JTable table = new JTable(data, columnNames);
    	JScrollPane scrollPane = new JScrollPane(table, 
    			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	Dimension d = table.getPreferredSize();
    	scrollPane.setPreferredSize(new Dimension(d.width,table.getRowHeight()*
    			((table.getRowCount()>10?10:table.getRowCount())+2)));
    	table.setFillsViewportHeight(false);
    	table.setAutoCreateRowSorter(true);
    	table.getRowSorter().toggleSortOrder(i);
    	if(toggleTwice)
    		table.getRowSorter().toggleSortOrder(i);
    	
    	JPanel panel = new JPanel(new BorderLayout(2,2));
    	panel.add(tableLabel, BorderLayout.NORTH);
    	panel.add(scrollPane, BorderLayout.CENTER);

    	return panel;

	}
}

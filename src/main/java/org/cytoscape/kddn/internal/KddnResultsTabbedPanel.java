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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Tabbed panel in results panel
 * @author Ye Tian
 *
 */
public class KddnResultsTabbedPanel extends JPanel implements ActionListener {
	 
    /**
	 * 
	 */
	private static final long serialVersionUID = -4953680062811764653L;
	
	private KddnExperiment kddnExperiment;
	private KddnResults kddnResults;
	
	public JButton nodeExportBtn = null;
	public JButton edgeExportBtn = null;
	public JButton betaExportBtn = null;
	private JFileChooser dataFileChooser= null;
		
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
    	JLabel pTableLabel = new JLabel("Experiment paramters:");
    	JPanel pTableHeader = new JPanel(new BorderLayout(0,0));
    	pTableHeader.add(pTableLabel, BorderLayout.NORTH);
    	
    	// single condition
		JPanel pTable = new JPanel(); 
    	if(!kddnExperiment.twoCondition) {
    		// no knowledge
    		if(!kddnExperiment.useKnowledge) {
    			String[] header = {"lambda 1"};
    			Object[][] data = {{new Double(kddnExperiment.lambda1)}};
    			pTable = createTablePanel(data, header, 0, false);
    		} else { // use knowledge
    			String[] header = {"lambda 1", "theta", "delta"};
    			Object[][] data = {{new Double(kddnExperiment.lambda1),
    				new Double(kddnExperiment.theta), new Double(kddnExperiment.delta)}};
    			pTable = createTablePanel(data, header, 0, false);
    		}
    	} else { // two conditions
    		// no knowledge
    		if(!kddnExperiment.useKnowledge) {
    			String[] header = {"lambda 1", "lambda 2", "alpha"};
    			Object[][] data = {{new Double(kddnExperiment.lambda1),
    				new Double(kddnExperiment.lambda2), new Double(kddnExperiment.alpha)}};
    			pTable = createTablePanel(data, header, 0, false);
    		} else { // use knowledge
    			String[] header = {"lambda 1", "lambda 2", "alpha", "theta", "delta"};
    			Object[][] data = {{new Double(kddnExperiment.lambda1),
    				new Double(kddnExperiment.lambda2), new Double(kddnExperiment.alpha),
    				new Double(kddnExperiment.theta), new Double(kddnExperiment.delta)}};
    			pTable = createTablePanel(data, header, 0, false);
    		}
    	}
    	    	
    	// assemble node table
    	JLabel nodeTableLabel = new JLabel();
    	if(kddnExperiment.twoCondition)
    		nodeTableLabel.setText("Node differential connectivity degree:");
    	else
    		nodeTableLabel.setText("Node connectivity degree:");
    	nodeExportBtn = new JButton("Export");
    	nodeExportBtn.addActionListener(this);
    	
    	JPanel nodeTableHeader = new JPanel(new GridBagLayout());
    	GridBagConstraints gbc1 = new GridBagConstraints();
    	gbc1.gridx = 0;
    	gbc1.gridy = 0;
    	gbc1.weightx = 1;
    	gbc1.anchor = GridBagConstraints.WEST;
    	nodeTableHeader.add(nodeTableLabel, gbc1);
    	gbc1.gridx++;
    	gbc1.weightx = 0;
    	nodeTableHeader.add(nodeExportBtn, gbc1);
    	
    	// single condition
    	JPanel nodeTable = new JPanel();
    	if(!kddnExperiment.twoCondition) {
			String[] header = {"Node", "Degree"};
			Object[][] data = getNodeTableData(kddnResults, false);
			nodeTable = createTablePanel(data, header, 1, true);
    	} else { // two conditions
			String[] header = {"Node", "Degree", "Fold Change", "P-value (t-test)"};
			Object[][] data = getNodeTableData(kddnResults, true);
			nodeTable = createTablePanel(data, header, 1, true);
    	}
    	
    	subMain.add(mergePanel(mergePanel(pTableHeader, pTable, 0, 0),
    			mergePanel(nodeTableHeader, nodeTable, 0, 0), 2, 12),
    			BorderLayout.NORTH);
    	
    	// assemble edge table
    	JLabel edgeTableLabel = new JLabel("Differential edges:");
    	JPanel edgeTableHeader = new JPanel(new GridBagLayout());    	
    	edgeExportBtn = new JButton("Export");
    	edgeExportBtn.addActionListener(this);
    	gbc1.gridx = 0;
    	gbc1.gridy = 0;
    	gbc1.weightx = 1;
    	gbc1.anchor = GridBagConstraints.WEST;
    	edgeTableHeader.add(edgeTableLabel, gbc1);
    	gbc1.gridx++;
    	gbc1.weightx = 0;
    	edgeTableHeader.add(edgeExportBtn, gbc1);
    	
    	JPanel table = new JPanel();
    	if(kddnExperiment.twoCondition) {
    		// has pvalue
    		if(kddnExperiment.needPvalue) {
				String[] header = {"Node 1", "Node 2", "Condition", "P-value"};
				Object[][] data = getEdgeTableData(kddnResults, true);
				table = createTablePanel(data, header, 3, false);
    		} else { // no pvalue
    			String[] header = {"Node 1", "Node 2", "Condition"};
				Object[][] data = getEdgeTableData(kddnResults, false);
				table = createTablePanel(data, header, 2, false);
    		}
        	subMain.add(mergePanel(edgeTableHeader, table, 0, 0), BorderLayout.CENTER);
    	}
    	
    	// assemble beta table
    	JLabel betaTableLabel = new JLabel("Network parameters:");
    	JPanel betaTableHeader = new JPanel(new GridBagLayout());
    	betaExportBtn = new JButton("Export");
    	betaExportBtn.addActionListener(this);
    	gbc1.gridx = 0;
    	gbc1.gridy = 0;
    	gbc1.weightx = 1;
    	gbc1.anchor = GridBagConstraints.WEST;
    	betaTableHeader.add(betaTableLabel, gbc1);
    	gbc1.gridx++;
    	gbc1.weightx = 0;
    	betaTableHeader.add(betaExportBtn, gbc1);
    	
     	JPanel betaTable = new JPanel();
    	if(kddnExperiment.twoCondition) {
			String[] header = {"Node 1", "Node 2", "beta (condition 1)", "beta (condition 1)"};
			Object[][] data = getBetaTableData(kddnResults, true);
			betaTable = createTablePanel(data, header, 0, false);
    	} else {
    		String[] header = {"Node 1", "Node 2", "beta"};
			Object[][] data = getBetaTableData(kddnResults, false);
			betaTable = createTablePanel(data, header, 0, false);
    	}
    	
    	if(kddnExperiment.twoCondition)
    		subMain.add(mergePanel(betaTableHeader, betaTable, 0, 0), BorderLayout.SOUTH);
    	else
    		subMain.add(mergePanel(betaTableHeader, betaTable, 0, 0), BorderLayout.CENTER);

    	JScrollPane mainScroll = new JScrollPane(subMain);
    	main.add(mainScroll, BorderLayout.CENTER);

    	dataFileChooser = new JFileChooser(System.getProperty("user.home"));
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
	 * create beta table data
	 * @param dn
	 * @return
	 */
	private Object[][] getBetaTableData(KddnResults kddn, boolean twoCond) {
		String[] var = kddn.varList;
		double[][] beta = kddn.beta;
		Object[][] tableData;	
		
		int rowId = 0;
		if(twoCond) {
			tableData = new Object[getEdgeCount(kddn)][4];
			for(int i=0; i<var.length-1; i++) {
				for(int j=i; j<var.length; j++) {
					if(kddn.adjacentMatrix[i][j] != 0 ||
							kddn.adjacentMatrix[i][j+var.length] != 0) {
						tableData[rowId][0] = var[i];
						tableData[rowId][1] = var[j];
						if(beta[i][j] != 0)
							tableData[rowId][2] = beta[i][j];
						else
							tableData[rowId][2] = beta[j][i];
						if(beta[i][j+var.length] != 0)
							tableData[rowId][3] = beta[i][j+var.length];
						else
							tableData[rowId][3] = beta[j][i+var.length];
						rowId++;
					}
				}
			}
		} else {
			tableData = new Object[getEdgeCount(kddn)][3];
			for(int i=0; i<var.length-1; i++) {
				for(int j=i; j<var.length; j++) {
					if(kddn.adjacentMatrix[i][j] != 0) {
						tableData[rowId][0] = var[i];
						tableData[rowId][1] = var[j];
						if(beta[i][j] != 0)
							tableData[rowId][2] = beta[i][j];
						else
							tableData[rowId][2] = beta[j][i];
						rowId++;
					}
				}
			}
		}
		return tableData;
	}

	private int getEdgeCount(KddnResults kddn) {
		int count = 0;
		
		for(int i=0; i<kddn.varList.length-1; i++)
			for(int j=i; j<kddn.varList.length; j++) {
				if(kddn.adjacentMatrix[i][j] != 0 ||
						kddn.adjacentMatrix[i][j+kddn.varList.length] != 0)
					count++;
			}
		return count;
	}

	/**
	 * Return table data for node table
	 * @param kddn
	 * @param twoCondition: if it's a two condition experiment
	 * @return
	 */
	private Object[][] getNodeTableData(KddnResults kddn, boolean twoCondition) {
		String[] var = kddn.varList;
		Object[][] tableData;
		if(twoCondition)
			tableData = new Object[var.length][4];
		else	
			tableData = new Object[var.length][2];
		for(int i=0; i<var.length; i++) {
			tableData[i][0] = var[i];
			if(twoCondition) {
				tableData[i][1] = getDifferentialDegree(kddn.adjacentMatrix, i);
				tableData[i][2] = kddn.foldChange.get(var[i]);
				tableData[i][3] = kddn.ttestP.get(var[i]);
			}
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
			int i, boolean toggleTwice) {
		
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
    	panel.add(scrollPane, BorderLayout.NORTH);

    	return panel;

	}
	
	private JPanel mergePanel(JPanel p1,
			JPanel p2, int x, int y) {
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(x,y));
		panel.add(p1, BorderLayout.NORTH);
		panel.add(p2, BorderLayout.CENTER);
		
		return panel;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == nodeExportBtn) {
			int returnVal = dataFileChooser.showSaveDialog(KddnResultsTabbedPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = dataFileChooser.getSelectedFile();
				String fileName = file.getParent() + '/' + file.getName();
	            if(!kddnExperiment.twoCondition) {
	        		String[] header = {"Node", "Degree"};
	        		Object[][] data = getNodeTableData(kddnResults, false);
		            writeCSV(fileName, data, header);
		        } else { // two conditions
        			String[] header = {"Node", "Degree", "Fold Change", "P-value (t-test)"};
        			Object[][] data = getNodeTableData(kddnResults, true);
		            writeCSV(fileName, data, header);
		        }
			}
		}
		
		if (e.getSource() == edgeExportBtn) {
			int returnVal = dataFileChooser.showSaveDialog(KddnResultsTabbedPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = dataFileChooser.getSelectedFile();
				String fileName = file.getParent() + '/' + file.getName();
				if(kddnExperiment.twoCondition) {
		    		// has pvalue
		    		if(kddnExperiment.needPvalue) {
						String[] header = {"Node 1", "Node 2", "Condition", "P-value"};
						Object[][] data = getEdgeTableData(kddnResults, true);
			            writeCSV(fileName, data, header);
		    		} else { // no pvalue
		    			String[] header = {"Node 1", "Node 2", "Condition"};
						Object[][] data = getEdgeTableData(kddnResults, false);
			            writeCSV(fileName, data, header);
		    		}
		    	}
			}
		}
		
		if (e.getSource() == betaExportBtn) {
			int returnVal = dataFileChooser.showSaveDialog(KddnResultsTabbedPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = dataFileChooser.getSelectedFile();
				String fileName = file.getParent() + '/' + file.getName();
				if(kddnExperiment.twoCondition) {
					String[] header = {"Node 1", "Node 2", "beta (condition 1)", "beta (condition 1)"};
					Object[][] data = getBetaTableData(kddnResults, true);
					writeCSV(fileName, data, header);
		    	} else {
		    		String[] header = {"Node 1", "Node 2", "beta"};
					Object[][] data = getBetaTableData(kddnResults, false);
					writeCSV(fileName, data, header);
		    	}
			}
		}
	}

	private void writeCSV(String fileName, Object[][] data, String[] header) {
		BufferedWriter outputStream = null;
        try {
            outputStream = new BufferedWriter(new FileWriter(fileName), 1*1024*1024);
        
   			for(int i=0; i<header.length; i++) {
   				outputStream.write(header[i]);
   				if(i < header.length - 1)
   					outputStream.write(",");
   			}
   			outputStream.newLine();

   			int row = data.length;
            int col = data[0].length;
            
            for(int i=0; i<row; i++) {
            	String content = "" + data[i][0];
            	for(int j=1; j<col-1; j++) {
            		content = content + "," + data[i][j];
            	}
            	content = content + "," + data[i][col-1];
        		outputStream.write(content);
                outputStream.newLine();
            }
        } catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
            if (outputStream != null) {
                try {
					outputStream.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        }
	}
}

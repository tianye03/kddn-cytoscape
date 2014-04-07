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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * Data input panel
 * @author Ye Tian
 *
 */
public class KddnDataPanel extends JPanel implements ActionListener {

	/**
	 * Fields
	 */
	private static final long serialVersionUID = -6869008874691451735L;
	
	// file chooser buttons
	public JButton geneButton = null;
	public JButton file1Button = null;
	public JButton file2Button = null;
	public JButton prinetButton = null;
	
	// cancel choice
	private JButton geneButtonCancel = null;
	private JButton file1ButtonCancel = null;
	public JButton file2ButtonCancel = null;
	private JButton prinetButtonCancel = null;
	public JLabel file2 = new JLabel("Data file 2: ");
	private JFileChooser dataFileChooser= null;
	
	// file pointers
	public File geneListFile = null;
	public File data1File = null;
	public File data2File = null;
	public File priNetFile = null;
	
	// indicator if files are chosen
	public boolean geneChosen = false;
	public boolean file1Chosen = false;
	public boolean file2Chosen = false;
	public boolean knowledgeChosen = false;
	
	// other panel reference
	private KddnConfigurePanel kddnConfigurePanel = null;
	private KddnParameterPanel parameterPanel = null; 
	
	/**
	 * Constructor
	 * @param parameterPanel
	 * @param kddnConfigurePanel
	 */
	public KddnDataPanel(KddnParameterPanel parameterPanel, 
			KddnConfigurePanel kddnConfigurePanel) {
		this.parameterPanel = parameterPanel;
		this.kddnConfigurePanel = kddnConfigurePanel;
		
		this.setLayout(new BorderLayout(0,0));
		
		// set layout	
		JPanel aPanel = new JPanel(new GridBagLayout());
		
		// step 1 panel
		Border border = BorderFactory.createEtchedBorder();
		String title = "Step 1: Input Data";
		TitledBorder titledBorder = BorderFactory.createTitledBorder(border, title,
				TitledBorder.LEFT, TitledBorder.TOP, new Font("titleFont", Font.BOLD, 12));
		aPanel.setBorder(titledBorder);
		aPanel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		
		int gridWidth = 1;
		// gene list file chooser
		JLabel geneLabel = new JLabel("Gene list: ");
		gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.weightx = 0.3;
    	gbc.anchor = GridBagConstraints.LINE_START;
    	gbc.gridwidth = gridWidth;
    	gbc.fill = GridBagConstraints.HORIZONTAL;
		aPanel.add(geneLabel, gbc);
		
		geneButton = new JButton("Choose a file");
		gbc.gridx = gridWidth;
    	gbc.gridy = 0;
    	gbc.weightx = 0.7;
		geneButton.addActionListener(this);
		aPanel.add(geneButton, gbc);
		
		geneButtonCancel = new JButton("x");
		geneButtonCancel.setPreferredSize(new Dimension(20, 20));
		gbc.gridx = 2*gridWidth;
    	gbc.gridy = 0;
    	gbc.weightx = 0;
    	gbc.gridwidth = 1;
		geneButtonCancel.addActionListener(this);
		aPanel.add(geneButtonCancel, gbc);
		
		// data file 1 chooser
		JLabel file1 = new JLabel("Data file 1: ");
		gbc.gridx = 0;
    	gbc.gridy = 1;
    	gbc.weightx = 0.3;
    	gbc.gridwidth = gridWidth;
		aPanel.add(file1, gbc);
		
		file1Button = new JButton("Choose a file");
		gbc.gridx = gridWidth;
    	gbc.gridy = 1;
    	gbc.weightx = 0.7;
		file1Button.addActionListener(this);
		aPanel.add(file1Button, gbc);
		
		file1ButtonCancel = new JButton("x");
		gbc.gridx = 2*gridWidth;
    	gbc.gridy = 1;
    	gbc.weightx = 0;
    	gbc.gridwidth = 1;
    	file1ButtonCancel.setPreferredSize(new Dimension(20, 20));
		file1ButtonCancel.addActionListener(this);
		aPanel.add(file1ButtonCancel, gbc);
		
		// data file 2 chooser
		gbc.gridx = 0;
    	gbc.gridy = 2;
    	gbc.weightx = 0.3;
    	gbc.gridwidth = gridWidth;
		aPanel.add(file2, gbc);
		
		file2Button = new JButton("Choose a file");
		gbc.gridx = gridWidth;
    	gbc.gridy = 2;
    	gbc.weightx = 0.7;
		file2Button.addActionListener(this);
		aPanel.add(file2Button, gbc);
		
		file2ButtonCancel = new JButton("x");
		gbc.gridx = 2*gridWidth;
    	gbc.gridy = 2;
    	gbc.weightx = 0;
    	gbc.gridwidth = 1;
		file2ButtonCancel.addActionListener(this);
    	file2ButtonCancel.setPreferredSize(new Dimension(20, 20));
		aPanel.add(file2ButtonCancel, gbc);
		
		// prior network chooser
		JLabel priNet = new JLabel("<html>Prior Knowledge<br>Network (optional):</html>");
		gbc.gridx = 0;
    	gbc.gridy = 3;
    	gbc.weightx = 0.3;
    	gbc.gridwidth = gridWidth;
		aPanel.add(priNet, gbc);
		
		prinetButton = new JButton("Choose a file");
		gbc.gridx = gridWidth;
    	gbc.gridy = 3;
    	gbc.weightx = 0.7;
		prinetButton.addActionListener(this);
		aPanel.add(prinetButton, gbc);
		
		prinetButtonCancel = new JButton("x");
		gbc.gridx = 2*gridWidth;
    	gbc.gridy = 3;
    	gbc.weightx = 0;
    	gbc.gridwidth = 1;
    	prinetButtonCancel.setPreferredSize(new Dimension(20, 20));
		prinetButtonCancel.addActionListener(this);
		aPanel.add(prinetButtonCancel, gbc);
		
		// tool tips
		geneLabel.setToolTipText("List of genes, one per line");
		file1.setToolTipText("Data of condition 1, one sample per line, "
				+ "columns order same with gene list, tab/comma delimited");
		file2.setToolTipText("Data of condition 2, one sample per line, "
				+ "columns order same with gene list, tab/comma delimited");
		priNet.setToolTipText("Prior knowledge network, "
				+ "one edge per line with two genes, tab/comma delimited; or xml file in KGML format from KEGG");
		this.add(aPanel, BorderLayout.NORTH);

		// file chooser default directory
		dataFileChooser = new JFileChooser(System.getProperty("user.home"));
	}

	/**
	 * Actions when choose a file
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == geneButton) {
			int returnVal = dataFileChooser.showOpenDialog(KddnDataPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// select file and change button text
	            geneListFile = dataFileChooser.getSelectedFile();
	            geneButton.setText(geneListFile.getName());
	            geneChosen = true;
	            
	            CyActivator.kddnConfigurePanel.demoGene = false;
			}
		}
		if (e.getSource() == file1Button) {
			int returnVal = dataFileChooser.showOpenDialog(KddnDataPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
	            data1File = dataFileChooser.getSelectedFile();
	            file1Button.setText(data1File.getName());
	            file1Chosen = true;
	            
	            CyActivator.kddnConfigurePanel.demoFile1 = false;
			}
		}
		if (e.getSource() == file2Button) {
			int returnVal = dataFileChooser.showOpenDialog(KddnDataPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
	            data2File = dataFileChooser.getSelectedFile();
	            file2Button.setText(data2File.getName());
	            file2Chosen = true;
	            
	            CyActivator.kddnConfigurePanel.demoFile2 = false;
			}
		}
		if (e.getSource() == prinetButton) {
			int returnVal = dataFileChooser.showOpenDialog(KddnDataPanel.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
	            priNetFile = dataFileChooser.getSelectedFile();
	            prinetButton.setText(priNetFile.getName());
	            knowledgeChosen = true;
	            
	            CyActivator.kddnConfigurePanel.demoPrior = false;
	            
	            parameterPanel.thetalabel.setForeground(SystemColor.textText);
	            parameterPanel.thetaBox.setEnabled(true);
	            if(parameterPanel.thetaBox.isSelected())
	            	parameterPanel.thetaDefault.setEnabled(true);
	            parameterPanel.deltalabel.setForeground(SystemColor.textText);
	            parameterPanel.deltaBox.setEnabled(true);
	            if(parameterPanel.deltaBox.isSelected())
	            	parameterPanel.deltaDefault.setEnabled(false);
	            else
	            	parameterPanel.deltaDefault.setEnabled(true);
			}
		}
		
		// reset file chooser
		if(e.getSource() == geneButtonCancel) {
			geneListFile = null;
            geneButton.setText("Choose a file");
            geneChosen = false;
            
            CyActivator.kddnConfigurePanel.demoGene = false;
		}
		if(e.getSource() == file1ButtonCancel) {
			data1File = null;
			file1Button.setText("Choose a file");
            file1Chosen = false;
            
            CyActivator.kddnConfigurePanel.demoFile1 = false;
		}
		if(e.getSource() == file2ButtonCancel) {
			data2File = null;
			file2Button.setText("Choose a file");
            file2Chosen = false;
            
            CyActivator.kddnConfigurePanel.demoFile2 = false;
		}
		if(e.getSource() == prinetButtonCancel) {
			priNetFile = null;
			prinetButton.setText("Choose a file");
            knowledgeChosen = false;
            
            CyActivator.kddnConfigurePanel.demoPrior = false;
            CyActivator.kddnConfigurePanel.runPanel.useKnowledge = false;
            
            parameterPanel.thetalabel.setForeground(SystemColor.textInactiveText);
            parameterPanel.thetaBox.setEnabled(false);
            parameterPanel.thetaDefault.setEnabled(false);
            parameterPanel.deltalabel.setForeground(SystemColor.textInactiveText);
            parameterPanel.deltaBox.setEnabled(false);
            parameterPanel.deltaDefault.setEnabled(false);
		}
		
		// if enough files are chosen, enable run button
		if(!kddnConfigurePanel.twoCondition && geneChosen && file1Chosen)
			CyActivator.kddnConfigurePanel.runPanel.runButton.setEnabled(true);
		else if(kddnConfigurePanel.twoCondition && geneChosen && file1Chosen && file2Chosen)
			CyActivator.kddnConfigurePanel.runPanel.runButton.setEnabled(true);
		else
			CyActivator.kddnConfigurePanel.runPanel.runButton.setEnabled(false);
		
	}
	
}

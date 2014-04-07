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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Main configure panel of KDDN
 * @author Ye Tian
 *
 */
public class KddnConfigurePanel extends JPanel implements CytoPanelComponent,
	ActionListener {
	
	/**
	 * Fields
	 */
	private static final long serialVersionUID = -8607484949790974850L;
	
	// experiment choice radio button
	protected static final JRadioButton twoConditionRadio = new JRadioButton("Two Conditions");
	protected static final JRadioButton oneConditionRadio = new JRadioButton("Single Condition");

	private JButton loadDemoButton = new JButton("Load Demo");
	private JButton resetButton = new JButton("Reset");
	
	// separate panels for data input, parameter setting, and run setting
	private KddnParameterPanel parameterPanel = null;
	private KddnDataPanel dataPanel = null;
	protected static KddnRunPanel runPanel;
	
	// network creation and visualization managers
	private CyNetworkManager cyNetworkManagerServiceRef = null;
	private CyNetworkNaming cyNetworkNamingServiceRef = null;
	private CyNetworkFactory cyNetworkFactoryServiceRef = null;
	private CyNetworkViewFactory networkViewFactory = null;
	private CyNetworkViewManager networkViewManager = null;
	private VisualMappingManager vmmServiceRef = null;
	private VisualStyleFactory vsfServiceRef = null;
	private VisualMappingFunctionFactory vmfFactoryC = null;
	private VisualMappingFunctionFactory vmfFactoryD = null;
	private VisualMappingFunctionFactory vmfFactoryP = null;
	
	// progress dialog
	private DialogTaskManager dialogTaskManager = null;
	
	// layout manager
	private CyLayoutAlgorithmManager clamRef = null;
	
	// indicator of experiment condition
	public boolean twoCondition = true;
			
	// demo data
	public boolean demoGene = false;
	public boolean demoFile1 = false;
	public boolean demoFile2 = false;
	public boolean demoPrior = false;
	
	/**
	 * Constructor
	 * @param cyNetworkManagerServiceRef
	 * @param cyNetworkNamingServiceRef
	 * @param cyNetworkFactoryServiceRef
	 * @param dialogTaskManager
	 * @param networkViewFactory
	 * @param networkViewManager
	 * @param vmmServiceRef
	 * @param vsfServiceRef
	 * @param vmfFactoryC
	 * @param vmfFactoryD
	 * @param vmfFactoryP
	 * @param clamRef
	 */
	public KddnConfigurePanel(CyNetworkManager cyNetworkManagerServiceRef, 
			CyNetworkNaming cyNetworkNamingServiceRef, 
			CyNetworkFactory cyNetworkFactoryServiceRef, 
			DialogTaskManager dialogTaskManager, 
			CyNetworkViewFactory networkViewFactory, 
			CyNetworkViewManager networkViewManager, 
			VisualMappingManager vmmServiceRef, 
			VisualStyleFactory vsfServiceRef, 
			VisualMappingFunctionFactory vmfFactoryC, 
			VisualMappingFunctionFactory vmfFactoryD, 
			VisualMappingFunctionFactory vmfFactoryP, CyLayoutAlgorithmManager clamRef) {

		this.cyNetworkFactoryServiceRef = cyNetworkFactoryServiceRef;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		this.cyNetworkNamingServiceRef = cyNetworkNamingServiceRef;
		this.dialogTaskManager = dialogTaskManager;
		this.networkViewFactory = networkViewFactory;
		this.networkViewManager = networkViewManager;
		this.vmmServiceRef = vmmServiceRef;
		this.vsfServiceRef = vsfServiceRef;
		this.vmfFactoryC = vmfFactoryC;
		this.vmfFactoryD = vmfFactoryD;
		this.vmfFactoryP = vmfFactoryP;
		this.clamRef = clamRef;
		
		// step 0 with radio buttons to choose experiment condition
		JPanel aPanel = new JPanel();
		aPanel.setLayout(new GridLayout(2,2));
		aPanel.add(twoConditionRadio);
		aPanel.add(loadDemoButton);
		aPanel.add(oneConditionRadio);
		aPanel.add(resetButton);

		// default if two condition experiment
		twoConditionRadio.setSelected(true);
		
		// group radio buttons
		ButtonGroup group = new ButtonGroup();
		group.add(twoConditionRadio);
		group.add(oneConditionRadio);

		// tool tips and action listener
		twoConditionRadio.setToolTipText("Differential analysis of two conditions");
		twoConditionRadio.addActionListener(this);

		oneConditionRadio.setToolTipText("Network analysis of single condition");
		oneConditionRadio.addActionListener(this);

		loadDemoButton.setToolTipText("Load a demo experiment");
		loadDemoButton.addActionListener(this);
		
		resetButton.addActionListener(this);
		
		// boder the step 0
		Border border = BorderFactory.createEtchedBorder();
		String title = "Step 0: Experiment Condition";
		TitledBorder titledBorder = BorderFactory.createTitledBorder(border, title,
				TitledBorder.LEFT, TitledBorder.TOP, new Font("titleFont", Font.BOLD, 12));
		aPanel.setBorder(titledBorder);

		// extra space at the bottom
		JPanel bottomPanel = new JPanel();
		
		// dimensions
		int width = (int)(0.25 * Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		this.setPreferredSize(new Dimension(width, 0));
		
		// step 2 parameter choice
		parameterPanel = new KddnParameterPanel();
		
		// step 1 data input
		dataPanel = new KddnDataPanel(parameterPanel, this);
		
		// step 3 run KDDN
		runPanel = new KddnRunPanel(this.cyNetworkManagerServiceRef,
				this.cyNetworkNamingServiceRef,
				this.cyNetworkFactoryServiceRef,
				this.dialogTaskManager,
				this.networkViewFactory,
				this.networkViewManager,
				this.vmmServiceRef, this.vsfServiceRef, this.vmfFactoryC,
				this.vmfFactoryD, this.vmfFactoryP, this.clamRef,
				parameterPanel, dataPanel, this);
		
		// layout the panels
		JPanel agentPanel = new JPanel(new BorderLayout(2,0));
		
		this.setLayout(new BorderLayout(2,0));
		agentPanel.add(aPanel, BorderLayout.NORTH);
		agentPanel.add(mergePanel(mergePanel(dataPanel, parameterPanel), runPanel), BorderLayout.CENTER);
		agentPanel.add(bottomPanel, BorderLayout.SOUTH);

		JScrollPane scrp = new JScrollPane(agentPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.add(scrp, BorderLayout.CENTER);
	}

	/**
	 * Layout subpanels
	 * @param p1
	 * @param p2
	 * @return
	 */
	private JPanel mergePanel(JPanel p1,
			JPanel p2) {
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(2,0));
		panel.add(p1, BorderLayout.NORTH);
		panel.add(p2, BorderLayout.CENTER);
		
		return panel;
	}


	public Component getComponent() {
		return this;
	}


	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}


	public String getTitle() {
		return "KDDN";
	}


	public Icon getIcon() {
		return null;
	}


	/**
	 * actions performed when altering the experiment condition radio buttons
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
	
		// two conditions are chosen, enable corresponding parameters 
		if (e.getSource() == twoConditionRadio) {
			
			// buttons for the 2nd file are enabled
			dataPanel.file2Button.setEnabled(true);
			dataPanel.file2.setEnabled(true);
			dataPanel.file2ButtonCancel.setEnabled(true);
			
			// lambda 2 for differential is enabled
			if(parameterPanel.l2Box.isSelected()) {
				parameterPanel.l2Default.setVisible(true);
				parameterPanel.l2Default.setEnabled(true);
			}
			parameterPanel.l2Box.setEnabled(true);
			
			// alpha is enabled
			if(!parameterPanel.l2Box.isSelected()) {
				if(!parameterPanel.alphaBox.isSelected())
					parameterPanel.alphaDefault.setEnabled(true);
				parameterPanel.alphaBox.setEnabled(true);
				parameterPanel.alphalabel.setForeground(SystemColor.textText);
			}
			parameterPanel.l2label.setForeground(SystemColor.textText);
			
			// permutation related settings are enabled
			runPanel.permBox.setEnabled(true);
			if(runPanel.permBox.isSelected()) {
				runPanel.numPerm.setEnabled(true);
				runPanel.numpermLabel.setEnabled(true);
				runPanel.needPvalue = true;
			}
			
			// run button is enabled if necessary files are provided
			if(!dataPanel.file2Chosen)
				CyActivator.kddnConfigurePanel.runPanel.runButton.setEnabled(false);
			
			twoCondition = true;
			
		} else if (e.getSource() == oneConditionRadio) {
			
			// file 2 buttons are disabled under single condition
			dataPanel.file2Button.setEnabled(false);
			dataPanel.file2.setEnabled(false);
			dataPanel.file2ButtonCancel.setEnabled(false);
			
			// lambda 2, alpha and permutation are disabled under single condition
			if(!parameterPanel.l2Box.isSelected())
				parameterPanel.l2Default.setVisible(false);
			else
				parameterPanel.l2Default.setEnabled(false);
			parameterPanel.l2Box.setEnabled(false);
			parameterPanel.alphaDefault.setEnabled(false);
			parameterPanel.alphaBox.setEnabled(false);
			parameterPanel.l2label.setForeground(SystemColor.textInactiveText);
			parameterPanel.alphalabel.setForeground(SystemColor.textInactiveText);
			runPanel.numPerm.setEnabled(false);
			runPanel.permBox.setEnabled(false);
			runPanel.numpermLabel.setEnabled(false);
			runPanel.needPvalue = false;
						
			// run button enabled if necessary files are provided 
			if(dataPanel.geneChosen && dataPanel.file1Chosen)
				CyActivator.kddnConfigurePanel.runPanel.runButton.setEnabled(true);
			
			twoCondition = false;
		}
		
		// reset all
		if(e.getSource() == resetButton) {
			// reset step 0
			twoConditionRadio.setSelected(true);
			
			// reset step 1
			dataPanel.geneListFile = null;
			dataPanel.geneButton.setText("Choose a file");
			dataPanel.geneChosen = false;
			dataPanel.data1File = null;
			dataPanel.file1Button.setText("Choose a file");
			dataPanel.file1Chosen = false;
			dataPanel.data2File = null;
			dataPanel.file2.setEnabled(true);
			dataPanel.file2Button.setEnabled(true);
			dataPanel.file2ButtonCancel.setEnabled(true);
			dataPanel.file2Button.setText("Choose a file");
			dataPanel.file2Chosen = false;
			dataPanel.priNetFile = null;
			dataPanel.prinetButton.setText("Choose a file");
			dataPanel.knowledgeChosen = false;
			
			runPanel.useKnowledge = false;
			demoGene = false;
			demoFile1 = false;
			demoFile2 = false;
			demoPrior = false;
			
			// reset step 2
			parameterPanel.l1Box.setSelected(false);
			parameterPanel.l1Default.setVisible(false);
			parameterPanel.l1label.setText("<html>&lambda;<sub>1</sub> (auto):</html>");
			parameterPanel.l1model.setValue(0.2);
			parameterPanel.l2Box.setSelected(false);
			parameterPanel.l2Default.setVisible(false);
			parameterPanel.l2label.setEnabled(true);
			parameterPanel.l2label.setText("<html>&lambda;<sub>2</sub> (auto):</html>");
			parameterPanel.l2Box.setEnabled(true);
			parameterPanel.l2model.setValue(0.05);
			parameterPanel.l2label.setForeground(SystemColor.textText);
			parameterPanel.alphaBox.setSelected(false);
			parameterPanel.alphaDefault.setValue(0.05);
			parameterPanel.alphaDefault.setEnabled(false);
			parameterPanel.alphalabel.setText("<html>&alpha; (set):</html>");
			parameterPanel.alphaBox.setEnabled(true);
			parameterPanel.alphaDefault.setEnabled(true);
			parameterPanel.alphalabel.setForeground(SystemColor.textText);
			parameterPanel.thetamodel.setValue(0.2);
			parameterPanel.thetaBox.setSelected(false);
			parameterPanel.thetaDefault.setVisible(false);
			parameterPanel.thetalabel.setText("<html>&theta; (auto):</html>");
			parameterPanel.thetalabel.setForeground(SystemColor.textInactiveText);
			parameterPanel.thetaBox.setEnabled(false);
			parameterPanel.deltalabel.setForeground(SystemColor.textInactiveText);
			parameterPanel.deltaBox.setSelected(false);
			parameterPanel.deltaBox.setEnabled(false);
			parameterPanel.deltaDefault.setValue(0.1);
			parameterPanel.deltaDefault.setEnabled(false);
			parameterPanel.deltalabel.setText("<html>&delta; (set):</html>");
			
			// reset step 3
			runPanel.permBox.setEnabled(true);
			runPanel.permBox.setSelected(false);
			runPanel.numpermLabel.setEnabled(false);
			runPanel.numPerm.setEnabled(false);
			runPanel.numPerm.setValue(1000);
			runPanel.runButton.setEnabled(false);
		}
		
		// load demo experiment
		if(e.getSource() == loadDemoButton) {
			// set a two condition experiment
			twoConditionRadio.setSelected(true);
			twoCondition = true;
			
			// direct files
			demoGene = true;
			dataPanel.geneButton.setText("demo list");
			dataPanel.geneChosen = true;
			
			demoFile1 = true;
			dataPanel.file1Button.setText("demo data 1");
			dataPanel.file1Chosen = true;
			
			demoFile2 = true;
			dataPanel.file2Button.setText("demo data 2");
			dataPanel.file2Chosen = true;
			dataPanel.file2Button.setEnabled(true);
			dataPanel.file2ButtonCancel.setEnabled(true);
			dataPanel.file2.setEnabled(true);
			
			demoPrior = true;
			dataPanel.prinetButton.setText("demo prior");
			dataPanel.knowledgeChosen = true;
			
			// set parameters
			parameterPanel.l1Box.setSelected(false);
			parameterPanel.l1Default.setVisible(false);
			parameterPanel.l1label.setText("<html>&lambda;<sub>1</sub> (auto):</html>");
			
			parameterPanel.l2Box.setSelected(false);
			parameterPanel.l2Default.setVisible(false);
			parameterPanel.l2label.setEnabled(true);
			parameterPanel.l2label.setText("<html>&lambda;<sub>2</sub> (auto):</html>");
			parameterPanel.l2Box.setEnabled(true);
			parameterPanel.l2label.setForeground(SystemColor.textText);
			
			parameterPanel.alphaBox.setSelected(true);
			parameterPanel.alphaDefault.setValue(0.05);
			parameterPanel.alphaDefault.setEnabled(false);
			parameterPanel.alphalabel.setText("<html>&alpha; (default):</html>");
			parameterPanel.alphaBox.setEnabled(true);
			parameterPanel.alphalabel.setForeground(SystemColor.textText);
			
			parameterPanel.thetaBox.setSelected(false);
			parameterPanel.thetaDefault.setVisible(false);
			parameterPanel.thetalabel.setText("<html>&theta; (auto):</html>");
			parameterPanel.thetalabel.setForeground(SystemColor.textText);
			parameterPanel.thetaBox.setEnabled(true);
			
			parameterPanel.deltalabel.setForeground(SystemColor.textText);
			parameterPanel.deltaDefault.setValue(0.1);
			parameterPanel.deltaDefault.setEnabled(false);
			parameterPanel.deltaBox.setSelected(true);
			parameterPanel.deltaBox.setEnabled(true);
			parameterPanel.deltalabel.setText("<html>&delta; (default):</html>");
			
			// set running
			runPanel.permBox.setEnabled(true);
			runPanel.permBox.setSelected(true);
			runPanel.numpermLabel.setEnabled(true);
			runPanel.numPerm.setEnabled(true);
			runPanel.numPerm.setValue(1000);
			runPanel.runButton.setEnabled(true);
		}

	}
}

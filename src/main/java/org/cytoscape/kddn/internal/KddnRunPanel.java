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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

public class KddnRunPanel extends JPanel implements 
			ItemListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6666660465449730881L;
	
	public JButton runButton = null;
	public JCheckBox permBox = null;
	public JSpinner numPerm = null;
	public JLabel numpermLabel = null;
	public SpinnerModel numSpin = new SpinnerNumberModel(1000, 1000, 10000, 1000);
	
	// drawing
	private CyNetworkManager cyNetworkManagerServiceRef = null;
	private CyNetworkNaming cyNetworkNamingServiceRef = null;
	private CyNetworkFactory cyNetworkFactoryServiceRef = null;
	private CyNetworkViewFactory networkViewFactory = null;
	private CyNetworkViewManager networkViewManager = null;
	
	private DialogTaskManager dialogTaskManager = null;
	
	private VisualMappingManager vmmServiceRef = null;
	private VisualStyleFactory vsfServiceRef = null;
	private VisualMappingFunctionFactory vmfFactoryC = null;
	private VisualMappingFunctionFactory vmfFactoryD = null;
	private VisualMappingFunctionFactory vmfFactoryP = null;
	private CyLayoutAlgorithmManager clamRef = null;
	
	// input data
    private String[] varList = null;
    private double[][] data1 = null;
    private double[][] data2 = null;
    private String[][] priorKnowledge = null;
    
    public boolean useKnowledge = false;
    
    // parameters
    public double lambda1 = 0.2;
    public double lambda2 = 0.0;
    public double alpha = 0.05;
    public double delta = 0.1;
    public double theta = 0;
    
    // permutation option
    public boolean needPvalue = false;
    public int numPermutation = 1000;

    // panel references
    private KddnParameterPanel parameterPanel = null;
    private KddnDataPanel dataPanel = null;
    private KddnConfigurePanel kddnConfigurePanel = null;

	public KddnRunPanel(CyNetworkManager cyNetworkManagerServiceRef, 
			CyNetworkNaming cyNetworkNamingServiceRef, 
			CyNetworkFactory cyNetworkFactoryServiceRef,
			DialogTaskManager dialogTaskManager, 
			CyNetworkViewFactory networkViewFactory,
			CyNetworkViewManager networkViewManager, 
			VisualMappingManager vmmServiceRef,
			VisualStyleFactory vsfServiceRef, 
			VisualMappingFunctionFactory vmfFactoryC, 
			VisualMappingFunctionFactory vmfFactoryD, 
			VisualMappingFunctionFactory vmfFactoryP,
			CyLayoutAlgorithmManager clamRef, 
			KddnParameterPanel parameterPanel, KddnDataPanel dataPanel, 
			KddnConfigurePanel kddnConfigurePanel) {
		
		this.cyNetworkFactoryServiceRef = cyNetworkFactoryServiceRef;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		this.cyNetworkNamingServiceRef = cyNetworkNamingServiceRef;
		this.networkViewFactory = networkViewFactory;
		this.dialogTaskManager = dialogTaskManager;
		this.networkViewManager = networkViewManager;
		this.vmmServiceRef = vmmServiceRef;
		this.vsfServiceRef = vsfServiceRef;
		this.vmfFactoryC = vmfFactoryC;
		this.vmfFactoryD = vmfFactoryD;
		this.vmfFactoryP = vmfFactoryP;
		this.clamRef = clamRef;
		this.parameterPanel = parameterPanel;
		this.dataPanel = dataPanel;
		this.kddnConfigurePanel = kddnConfigurePanel;
		
		this.setLayout(new BorderLayout(0,0));
		
		JPanel aPanel = new JPanel();
		aPanel.setLayout(new BorderLayout(0,0));
		
		// border for top panel
		Border border = BorderFactory.createEtchedBorder();
		String title = "Step 3: Running Options";
		TitledBorder titledBorder = BorderFactory.createTitledBorder(border, title,
				TitledBorder.LEFT, TitledBorder.TOP, new Font("titleFont", Font.BOLD, 12));
		aPanel.setBorder(titledBorder);
				
		permBox = new JCheckBox("Want differential edge p-value");
		aPanel.add(permBox, BorderLayout.NORTH);
		
		numpermLabel = new JLabel("Number of permutations:");
		numpermLabel.setEnabled(false);
		aPanel.add(numpermLabel, BorderLayout.WEST);
		
		numPerm = new JSpinner(numSpin);
		numPerm.setEnabled(false);
		aPanel.add(numPerm, BorderLayout.EAST);
		
		runButton = new JButton("Run KDDN");
		runButton.setHorizontalAlignment(SwingConstants.CENTER);
		runButton.setEnabled(false);
		aPanel.add(runButton, BorderLayout.SOUTH);
		runButton.addActionListener(this);
		
		permBox.addItemListener(this);
		
		this.add(aPanel, BorderLayout.NORTH);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if(source == permBox) {
			numPerm.setEnabled(true);
			numpermLabel.setEnabled(true);
			needPvalue = true;
		}
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			numPerm.setEnabled(false);
			numpermLabel.setEnabled(false);
			needPvalue = false;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(runButton == e.getSource()) {
			// check input format
			
			// check input source, demo or filechooser
			if(CyActivator.kddnConfigurePanel.demoGene) { // demo
				try {
					varList = FileInput.readGeneList(getClass().getResourceAsStream("/demo/geneList.txt"));
				} catch (IOException e2) {
					System.out.println("gotcha");
					e2.printStackTrace();
				}
			} else if(dataPanel.geneChosen) {
			    try {
					varList = FileInput.readGeneList(dataPanel.geneListFile.getPath());
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
			
			if(CyActivator.kddnConfigurePanel.demoFile1) { // demo
				try {
					data1 = FileInput.readData(getClass().getResourceAsStream("/demo/dataFile1.txt"));
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			} else if(dataPanel.file1Chosen) {
			    try {
					data1 = FileInput.readData(dataPanel.data1File.getPath());
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
			
			if(CyActivator.kddnConfigurePanel.demoFile2) { // demo
				try {
					data2 = FileInput.readData(getClass().getResourceAsStream("/demo/dataFile2.txt"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else if(dataPanel.file2Chosen && kddnConfigurePanel.twoCondition) {
			    try {
					data2 = FileInput.readData(dataPanel.data2File.getPath());
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
			
			if(CyActivator.kddnConfigurePanel.demoPrior) { // demo
				try {
					priorKnowledge = FileInput.readKnowledge(getClass().getResourceAsStream("/demo/priorNetwork.txt"));
					useKnowledge = true;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else if(dataPanel.knowledgeChosen) {
			    try {
					priorKnowledge = FileInput.readKnowledge(dataPanel.priNetFile.getPath());
					if(priorKnowledge != null && priorKnowledge[0].length != 3) {
						useKnowledge = false;
						JOptionPane.showMessageDialog(null, "Does the prior knowledge file follow\n" +
								"node1[tab|,]node2[tab|,]<condition> format?\n" +
								"Calculate without prior knowledge.");
					} else
						useKnowledge = true;
				} catch (IOException e2) {
					e2.printStackTrace();

				}
			}
			
			// return if input dimensions don't agree
			if(kddnConfigurePanel.twoCondition) {
				if(varList.length != data1[0].length || varList.length != data2[0].length) {
					String msg = "Please check input file dimensions.\n" +
						"There are " + varList.length + " genes, while there are " + data1[0].length +
						" rows of data in file 1 and " + data2[0].length +" rows of data in file 2.";
					JOptionPane.showMessageDialog(null, msg);
					
					return;
				}
			} else {
				if(varList.length != data1[0].length) {
					String msg = "Please check input file dimensions.\n" +
						"There are " + varList.length + " genes, while there are " + data1[0].length +
						" rows of data in file 1.";
					JOptionPane.showMessageDialog(null, msg);
					
					return;
				}
			}

			// parse parameter settings
		    // -1 means auto
		    if(!parameterPanel.l1Box.isSelected())
		    	lambda1 = -1;
		    else
		    	lambda1 = (Double) parameterPanel.l1Default.getValue();
		    
		    if(!parameterPanel.l2Box.isSelected())
		    	lambda2 = -1;
		    else
		    	lambda2 = (Double) parameterPanel.l2Default.getValue();
		    
			alpha = (Double) parameterPanel.alphaDefault.getValue();
			
			if(!parameterPanel.thetaBox.isSelected())
				theta = -1;
			else
				theta = (Double) parameterPanel.thetaDefault.getValue();
			
			delta = (Double) parameterPanel.deltaDefault.getValue();
						
			numPermutation = (Integer) numPerm.getValue();
			KddnExperiment kddnInstance = null;
			
			// run kddn task
			try {
				kddnInstance = new KddnExperiment(cyNetworkManagerServiceRef,
						cyNetworkNamingServiceRef,cyNetworkFactoryServiceRef,
						networkViewFactory, networkViewManager,
						vmmServiceRef, vsfServiceRef, vmfFactoryC,
						vmfFactoryD, vmfFactoryP, clamRef,
						varList, data1, data2, priorKnowledge, useKnowledge,
						lambda1, lambda2, alpha, theta, delta, 
						needPvalue, numPermutation, kddnConfigurePanel.twoCondition,
						dataPanel, parameterPanel);
				
				dialogTaskManager.execute(new TaskIterator(kddnInstance));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
				
		}
	}

}

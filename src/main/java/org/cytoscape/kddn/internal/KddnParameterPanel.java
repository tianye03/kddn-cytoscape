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
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * step 2 parameter setting panel
 * @author Ye Tian
 *
 */
public class KddnParameterPanel extends JPanel implements ItemListener {

	/**
	 * Fields
	 */
	private static final long serialVersionUID = -5292816967913823632L;

	// spinners for parameters
	public JSpinner l1Default = null;
	public JSpinner l2Default = null;
	public JSpinner alphaDefault = null;
	public JSpinner thetaDefault = null;
	public JSpinner deltaDefault = null;
	
	public SpinnerModel l1model = new SpinnerNumberModel(0.2, 0.1, 1, 0.01);
	public SpinnerModel l2model = new SpinnerNumberModel(0.05, 0, 1, 0.01);
	public SpinnerModel alphamodel = new SpinnerNumberModel(0.05, 0.01, 0.1, 0.01);
	public SpinnerModel thetamodel = new SpinnerNumberModel(0.2, 0, 0.5, 0.01);
	public SpinnerModel deltamodel = new SpinnerNumberModel(0.1, 0.05, 0.3, 0.01);

	// parameter labels
	public JLabel l1label = new JLabel("<html>&lambda;<sub>1</sub> (auto):</html>");
	public JLabel l2label = new JLabel("<html>&lambda;<sub>2</sub> (auto):</html>");
	public JLabel alphalabel = new JLabel("<html>&alpha; (set):</html>");
	public JLabel thetalabel = new JLabel("<html>&theta; (auto):</html>");
	public JLabel deltalabel = new JLabel("<html>&delta; (set):</html>");

	// parameter control checkbox
	public JCheckBox l1Box = null;
	public JCheckBox l2Box = null;
	public JCheckBox alphaBox = null;
	public JCheckBox thetaBox = null;
	public JCheckBox deltaBox = null;
	
	public KddnParameterPanel() {
		this.setLayout(new BorderLayout(0,0));

		JPanel aPanel = new JPanel();
		aPanel.setLayout(new GridLayout(5,3));
		
		// step 2
		Border border = BorderFactory.createEtchedBorder();
		String title = "Step 2: Set Model Parameters";
		TitledBorder titledBorder = BorderFactory.createTitledBorder(border, title,
				TitledBorder.LEFT, TitledBorder.TOP, new Font("titleFont", Font.BOLD, 12));
		aPanel.setBorder(titledBorder);
		
		aPanel.add(l1label);

		l1Box = new JCheckBox("Manual");
		aPanel.add(l1Box);
		l1Box.addItemListener(this);

		l1Default = new JSpinner(l1model);
		l1Default.setVisible(false);
		aPanel.add(l1Default);
		
		aPanel.add(l2label);

		l2Box = new JCheckBox("Manual");
		aPanel.add(l2Box);
		l2Box.addItemListener(this);

		l2Default = new JSpinner(l2model);
		l2Default.setVisible(false);
		aPanel.add(l2Default);
		
		aPanel.add(alphalabel);

		alphaBox = new JCheckBox("Default");
		aPanel.add(alphaBox);
		alphaBox.addItemListener(this);

		alphaDefault = new JSpinner(alphamodel);
		aPanel.add(alphaDefault);
		
		thetalabel.setForeground(SystemColor.textInactiveText);
		aPanel.add(thetalabel);

		thetaBox = new JCheckBox("Manual");
		aPanel.add(thetaBox);
		thetaBox.setEnabled(false);
		thetaBox.addItemListener(this);
		
		thetaDefault = new JSpinner(thetamodel);
		thetaDefault.setVisible(false);
		aPanel.add(thetaDefault);
		
		deltalabel.setForeground(SystemColor.textInactiveText);
		aPanel.add(deltalabel);

		deltaBox = new JCheckBox("Default");
		aPanel.add(deltaBox);
		deltaBox.setEnabled(false);
		deltaBox.addItemListener(this);
		
		deltaDefault = new JSpinner(deltamodel);
		deltaDefault.setEnabled(false);
		aPanel.add(deltaDefault);
		
		l1label.setToolTipText("Controls the sparsity of overall network");
		l2label.setToolTipText("Controls the sparsity of differential network");
		alphalabel.setToolTipText("Significance level of differential edges");
		thetalabel.setToolTipText("Degree of knowledge incorporation");
		deltalabel.setToolTipText("Largest tolerated deviation due to knowledge");
		
		this.add(aPanel, BorderLayout.NORTH);
	}

	/**
	 * Switch on/off spinners according to checkbox status
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if(source == l1Box) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				l1Default.setVisible(true);
				l1label.setText("<html>&lambda;<sub>1</sub> (manual):</html>");
			}
			else if(e.getStateChange() == ItemEvent.DESELECTED) {
				l1Default.setVisible(false);
				l1label.setText("<html>&lambda;<sub>1</sub> (auto):</html>");
			}
		}
		if(source == l2Box) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				l2Default.setVisible(true);
				l2label.setText("<html>&lambda;<sub>2</sub> (manual):</html>");
				
				alphaDefault.setEnabled(false);
				alphaBox.setEnabled(false);
				alphalabel.setForeground(SystemColor.textInactiveText);
			}
			else if(e.getStateChange() == ItemEvent.DESELECTED) {
				l2Default.setVisible(false);
				l2label.setText("<html>&lambda;<sub>2</sub> (auto):</html>");
				
				if(alphaBox.isSelected())
					alphaDefault.setEnabled(false);
				else
					alphaDefault.setEnabled(true);
				alphaBox.setEnabled(true);
				alphalabel.setForeground(SystemColor.textText);
			}
		}
		if(source == thetaBox) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				thetaDefault.setVisible(true);
				thetaDefault.setEnabled(true);
				thetalabel.setText("<html>&theta; (manual):</html>");
				
				deltaDefault.setEnabled(false);
				deltaBox.setEnabled(false);
				deltalabel.setForeground(SystemColor.textInactiveText);
			}
			else if(e.getStateChange() == ItemEvent.DESELECTED) {
				thetaDefault.setVisible(false);
				thetalabel.setText("<html>&theta; (auto):</html>");
				
				if(deltaBox.isSelected())
					deltaDefault.setEnabled(false);
				else
					deltaDefault.setEnabled(true);
				deltaBox.setEnabled(true);
				deltalabel.setForeground(SystemColor.textText);
			}
		}
		if(source == alphaBox) {
			if(e.getStateChange() == ItemEvent.DESELECTED) {
				alphaDefault.setEnabled(true);
				alphalabel.setText("<html>&alpha; (set):</html>");
			}
			else if(e.getStateChange() == ItemEvent.SELECTED) {
				alphaDefault.setValue(0.05);
				alphaDefault.setEnabled(false);
				alphalabel.setText("<html>&alpha; (default):</html>");
			}
		}
		if(source == deltaBox) {
			if(e.getStateChange() == ItemEvent.DESELECTED) {
				deltaDefault.setEnabled(true);
				deltalabel.setText("<html>&delta; (set):</html>");
			}
			else if(e.getStateChange() == ItemEvent.SELECTED) {
				deltaDefault.setValue(0.1);
				deltaDefault.setEnabled(false);
				deltalabel.setText("<html>&delta; (default):</html>");
			}
		}
	}

}

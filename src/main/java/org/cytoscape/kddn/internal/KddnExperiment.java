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

import java.awt.Color;
import java.awt.Paint;
import java.util.Set;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Perform an actual KDDN run
 * @author Ye Tian
 *
 */
public class KddnExperiment extends AbstractTask {

	/**
	 * Fields
	 */
	// run parameters
    public static double lambda1 = 0.2;
    public static double lambda2 = 0.0;
    public static double alpha = 0.05;
    public static double delta = 0.1;
    public static double pValueCutoff = 0.05;
    public static double theta = 0;
	private double firstStep = 0.3;
	private double secondStep = 0.3;
	
	// network creation managers
	private CyNetworkManager cyNetworkManagerServiceRef = null;
	private CyNetworkNaming cyNetworkNamingServiceRef = null;
	private CyNetworkFactory cyNetworkFactoryServiceRef = null;
	private CyNetworkViewFactory networkViewFactory = null;
	private CyNetworkViewManager networkViewManager = null;
	
	// network view managers
	private VisualMappingManager vmmServiceRef = null;
	private VisualStyleFactory vsfServiceRef = null;
	private VisualMappingFunctionFactory vmfFactoryC = null;
	private VisualMappingFunctionFactory vmfFactoryD = null;
	private VisualMappingFunctionFactory vmfFactoryP = null;
	private CyLayoutAlgorithmManager clamRef = null;
	
	// data and run settings
    private String[] varList = null;
    private double[][] data1 = null;
    private double[][] data2 = null;
    private String[][] priorKnowledge = null;
    public boolean useKnowledge = false;
    public boolean needPvalue = false;
    private int numPermutation = 1000;
    public boolean twoCondition = true;
    
    private KddnResults kddnDraw = null;
    
    // reference panels
    private KddnDataPanel dataPanel = null;
    private KddnParameterPanel parameterPanel = null;

    /**
     * Constructor
     * @param cyNetworkManagerServiceRef
     * @param cyNetworkNamingServiceRef
     * @param cyNetworkFactoryServiceRef
     * @param networkViewFactory
     * @param networkViewManager
     * @param vmmServiceRef
     * @param vsfServiceRef
     * @param vmfFactoryC
     * @param vmfFactoryD
     * @param vmfFactoryP
     * @param clamRef
     * @param varList
     * @param data1
     * @param data2
     * @param priorKnowledge
     * @param useKnowledge
     * @param lambda1
     * @param lambda2
     * @param alpha
     * @param theta
     * @param delta
     * @param needPvalue
     * @param numPermutation
     * @param twoCondition
     * @param dataPanel
     * @param parameterPanel
     */
	public KddnExperiment(CyNetworkManager cyNetworkManagerServiceRef,
			CyNetworkNaming cyNetworkNamingServiceRef, 
			CyNetworkFactory cyNetworkFactoryServiceRef, 
			CyNetworkViewFactory networkViewFactory, 
			CyNetworkViewManager networkViewManager,
			VisualMappingManager vmmServiceRef, 
			VisualStyleFactory vsfServiceRef, 
			VisualMappingFunctionFactory vmfFactoryC, 
			VisualMappingFunctionFactory vmfFactoryD, 
			VisualMappingFunctionFactory vmfFactoryP,
			CyLayoutAlgorithmManager clamRef, 
			String[] varList, double[][] data1, double[][] data2, 
			String[][] priorKnowledge, boolean useKnowledge, 
			double lambda1, double lambda2, 
			double alpha, double theta, double delta, boolean needPvalue,
			int numPermutation, boolean twoCondition, 
			KddnDataPanel dataPanel, KddnParameterPanel parameterPanel) {
		
		this.cyNetworkFactoryServiceRef = cyNetworkFactoryServiceRef;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		this.cyNetworkNamingServiceRef = cyNetworkNamingServiceRef;
		this.networkViewFactory = networkViewFactory;
		this.networkViewManager = networkViewManager;
		this.vmmServiceRef = vmmServiceRef;
		this.vsfServiceRef = vsfServiceRef;
		this.vmfFactoryC = vmfFactoryC;
		this.vmfFactoryD = vmfFactoryD;
		this.vmfFactoryP = vmfFactoryP;
		this.clamRef = clamRef;
		this.varList = varList;
		this.data1 = data1;	
		this.priorKnowledge = priorKnowledge;
		this.useKnowledge = useKnowledge;
		this.numPermutation = numPermutation;
		this.lambda1 = lambda1;
		this.lambda2 = lambda2;
		this.theta = theta;
		this.alpha = alpha;
		this.delta = delta;
		this.needPvalue = needPvalue;
		this.twoCondition = twoCondition;
		if(!twoCondition)
			this.data2 = data1;
		else
			this.data2 = data2;
		
		this.dataPanel = dataPanel;
		this.parameterPanel = parameterPanel;
	}

	// run task
	@Override
	public void run(TaskMonitor monitor) throws Exception {
		monitor.setTitle("KDDN experiment");
				
	    // variable selection according to t-test p-value
	    int[] varIndex = KddnMethods.variableSelection(varList, data1, data2);
		varList = KddnMethods.selectVariable(varList, varIndex);
		data1 = KddnMethods.selectData(data1, varIndex);
		data2 = KddnMethods.selectData(data2, varIndex);
		
		// initialize prior network W
		int[][] W = new int[varList.length][varList.length];
		for(int i=0; i<varList.length; i++)
			for(int j=0; j<varList.length; j++) {
				W[i][j] = 0;
			}
		
		int M = 0;
		if(useKnowledge) {
			// map knowledge network to matrix if prior network is provided
			KddnMethods.mapKnowledgeNetwork(varList, priorKnowledge, W);
			for(int i=0; i<varList.length-1; i++)
				for(int j=i+1; j<varList.length; j++) {
					M += W[i][j];
				}
		}
		
		monitor.setTitle("Determining parameters");
		// -1 means auto, find the lambda 1 automatically
		if(lambda1 == -1)
			lambda1 = KddnMethods.findLambda1(data1, data2, varList);
		
		// -1 means auto, find the lambda 2 automatically
		if(lambda2 == -1) {
			if(twoCondition) {
				monitor.setStatusMessage("<html>Finding &lambda;<sub>2</sub></html>");
				lambda2 = KddnMethods.findLambda2(data1, data2, lambda1, alpha, varList, monitor, firstStep);
			}
			else // set lambda 2 to 0 under single condition
				lambda2 = 0;
		}
       
		if(cancelled)
        	return;
        
		monitor.setProgress(firstStep);
		
		// if theta is auto and knowledge is provided, find it
		if(theta == -1) {
			if(useKnowledge && M > 0) {
				monitor.setStatusMessage("<html>Finding &theta;</html>");
				theta = KddnMethods.findTheta(data1, data2, lambda1, lambda2, varList, M, delta, 
						monitor, firstStep, secondStep);
			}
		}
		
        monitor.setProgress(firstStep + secondStep);
        if(cancelled)
        	return;
        
        // set up KDDN data environment with raw data
		KddnSettings rawKddn = new KddnSettings(lambda1, lambda2, pValueCutoff, theta, W, data1, data2, varList, alpha, delta);
	
		// standardize data
		KddnMethods.standardizeData(data1);
		KddnMethods.standardizeData(data2);
		// set up KDDN data environment with standardized data
		KddnSettings stdKddn = new KddnSettings(lambda1, lambda2, pValueCutoff, theta, W, data1, data2, varList, alpha, delta);
		
		// solve KDDN with standardized data
		KddnResults kddn = KddnMethods.solveDDN(stdKddn);

        if(cancelled)
        	return;
        
        monitor.setTitle("Calculating KDDN");
        
        // if p-value is asked, get it
        if(needPvalue) {
        	monitor.setStatusMessage("Calculating p-value");
        	KddnResults kddnWp = KddnMethods.calculatePvalue(kddn, rawKddn, 
        			numPermutation, monitor, firstStep, secondStep);
        	kddnDraw = kddnWp;
        } else
        	kddnDraw = kddn;
		
		monitor.setProgress(1);
		monitor.setTitle("Creating network");
		
		// increase universal network index 
		CyActivator.totalNumberRun++;
		
		// create network
		CreateNetwork cn = new CreateNetwork(cyNetworkManagerServiceRef,
				cyNetworkNamingServiceRef,cyNetworkFactoryServiceRef, 
				kddnDraw, this);
		
		cn.create();
		
		// create a new network view
		CyNetworkView kddnView = networkViewFactory.createNetworkView(cn.network);

		// create two conditions or single condition visual style
		VisualStyle vs = null;
		if(twoCondition)
			vs = createVisualStyleTwoCondition(vmmServiceRef, vsfServiceRef,
				vmfFactoryC, vmfFactoryD, vmfFactoryP);
		else
			vs = createVisualStyleSingle(vmmServiceRef, vsfServiceRef,
					vmfFactoryC, vmfFactoryD, vmfFactoryP);
		
		vs.apply(kddnView);
		// Add view to Cytoscape
		networkViewManager.addNetworkView(kddnView);
		
		// apply layout
		clamRef.getLayout("force-directed");
		CyLayoutAlgorithm layout = clamRef.getLayout("force-directed");
		String layoutAttribute = null;
		insertTasksAfterCurrentTask(layout.createTaskIterator(kddnView,
				layout.createLayoutContext(),
				CyLayoutAlgorithm.ALL_NODE_VIEWS,
				layoutAttribute));

		// add results tabbed pane
		new KddnResultsTabbedPanel(this, kddnDraw);
		CyActivator.kddnResultsPanel.getPanel();
		CyActivator.kddnResultsPanel.getResultsTabbedPanel().setSelectedIndex(CyActivator.kddnResultsPanel.getResultsTabbedPanel().getTabCount()-1);

		monitor.setStatusMessage("Done");
		monitor.setProgress(1);
		
	}

	/**
	 * Create visual style for single condition
	 * @param vmmServiceRef
	 * @param vsfServiceRef
	 * @param vmfFactoryC
	 * @param vmfFactoryD
	 * @param vmfFactoryP
	 * @return
	 */
	private VisualStyle createVisualStyleSingle(
			VisualMappingManager vmmServiceRef,
			VisualStyleFactory vsfServiceRef,
			VisualMappingFunctionFactory vmfFactoryC,
			VisualMappingFunctionFactory vmfFactoryD,
			VisualMappingFunctionFactory vmfFactoryP) {
		
		// retrieve visual style if already exist
		if(styleExist(vmmServiceRef, "KDDN visual style - single condition"))
			return getVSstyle(vmmServiceRef, "KDDN visual style - single condition");
		
		// node color setting
		Color NODE_COLOR = new Color(230, 191, 85);
		Color NODE_BORDER_COLOR = Color.WHITE;
		Color NODE_LABEL_COLOR = new Color(50, 50, 50);
		
		// To create a new VisualStyle object and set the mapping function
		VisualStyle vs= vsfServiceRef.createVisualStyle("KDDN visual style - single condition");

		// unlock node size
		Set<VisualPropertyDependency<?>> deps = vs.getAllVisualPropertyDependencies();
		for(VisualPropertyDependency<?> dep: deps) {
			dep.setDependency(false);
		}
		
		// set node related default
		vs.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, NODE_COLOR);
		vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, NODE_LABEL_COLOR);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, NODE_BORDER_COLOR);
		vs.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 220);
		vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 20);
		
		// map node names
		String nodeName = "name";
		PassthroughMapping nodeNameMapping = (PassthroughMapping) 
				vmfFactoryP.createVisualMappingFunction(nodeName, String.class, 
						BasicVisualLexicon.NODE_LABEL);
		vs.addVisualMappingFunction(nodeNameMapping);
				
		// map edge color
		String edgeType = "interaction";
		DiscreteMapping<String, Paint> edgeTypeMapping = (DiscreteMapping<String, Paint>) 
				vmfFactoryD.createVisualMappingFunction(edgeType, String.class, 
						BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		edgeTypeMapping.putMapValue("static edge", Color.DARK_GRAY);
		vs.addVisualMappingFunction(edgeTypeMapping);
		
		// add visual style if not added
		if(!styleExist(vmmServiceRef, "KDDN visual style - single condition"))
			vmmServiceRef.addVisualStyle(vs);
		
		return vs;
	}

	private VisualStyle getVSstyle(VisualMappingManager vmm,
			String name) {
		Set<VisualStyle> vss = vmm.getAllVisualStyles();
		for(final VisualStyle v : vss) {
			if(v.getTitle() == name)
				return v;
		}
		return null;
	}

	/**
	 * Create visual style for two conditions
	 * @param vmmServiceRef
	 * @param vsfServiceRef
	 * @param vmfFactoryC
	 * @param vmfFactoryD
	 * @param vmfFactoryP
	 * @return
	 */
	private VisualStyle createVisualStyleTwoCondition(VisualMappingManager vmmServiceRef,
			VisualStyleFactory vsfServiceRef,
			VisualMappingFunctionFactory vmfFactoryC,
			VisualMappingFunctionFactory vmfFactoryD,
			VisualMappingFunctionFactory vmfFactoryP) {
		
		// retrieve visual style if already exist
		if(styleExist(vmmServiceRef, "KDDN visual style - two conditions"))
			return getVSstyle(vmmServiceRef, "KDDN visual style - two conditions");
				
		// node related color
		Color NODE_COLOR = new Color(230, 191, 85);
		Color NODE_BORDER_COLOR = Color.WHITE;
		Color NODE_LABEL_COLOR = new Color(50, 50, 50);
		
		// To create a new VisualStyle object and set the mapping function
		VisualStyle vs= vsfServiceRef.createVisualStyle("KDDN visual style - two conditions");

		// unlock node size
		Set<VisualPropertyDependency<?>> deps = vs.getAllVisualPropertyDependencies();
		for(VisualPropertyDependency<?> dep: deps) {
			dep.setDependency(false);
		}
		
		// default node appearance
		vs.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		vs.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, NODE_COLOR);
		vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, NODE_LABEL_COLOR);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, NODE_BORDER_COLOR);
		vs.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 220);
		vs.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 20);
		
		// map node names
		String nodeName = "name";
		PassthroughMapping nodeNameMapping = (PassthroughMapping) 
				vmfFactoryP.createVisualMappingFunction(nodeName, String.class, 
						BasicVisualLexicon.NODE_LABEL);
		vs.addVisualMappingFunction(nodeNameMapping);
				
		// map edge color
		String edgeType = "interaction";
		DiscreteMapping<String, Paint> edgeTypeMapping = (DiscreteMapping<String, Paint>) 
				vmfFactoryD.createVisualMappingFunction(edgeType, String.class, 
						BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		edgeTypeMapping.putMapValue("condition 1", Color.RED);
		edgeTypeMapping.putMapValue("condition 2", new Color(0,196,26));
		vs.addVisualMappingFunction(edgeTypeMapping);
		
		// add visual style is not added
		if(!styleExist(vmmServiceRef, "KDDN visual style - two conditions"))
			vmmServiceRef.addVisualStyle(vs);
		
		return vs;
	}

	/**
	 * Check if a visual style already exsited
	 * @param vmm
	 * @param name
	 * @return
	 */
	private boolean styleExist(VisualMappingManager vmm,
			String name) {
		
		Set<VisualStyle> vss = vmm.getAllVisualStyles();
		for(final VisualStyle v : vss) {
			if(v.getTitle() == name)
				return true;
		}
		return false;
	}

}

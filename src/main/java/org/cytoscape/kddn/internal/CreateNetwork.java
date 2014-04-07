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

import java.util.HashMap;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.session.CyNetworkNaming;

/**
 * Create network and related managing tables
 * @author Ye Tian
 *
 */
public class CreateNetwork {

	/**
	 * Fields
	 */
	private final CyNetworkManager netMgr;
	private final CyNetworkFactory cnf;
	private final CyNetworkNaming namingUtil; 
	private final KddnResults kddn;
	public CyNetwork network = null;
	private boolean needPvalue = false;
	private boolean twoCondition = true;
	private KddnExperiment experiment = null;

	/**
	 * Constructor
	 * @param netMgr
	 * @param namingUtil
	 * @param cnf
	 * @param kddn
	 * @param experiment
	 */
	public CreateNetwork(final CyNetworkManager netMgr, 
			final CyNetworkNaming namingUtil, 
			final CyNetworkFactory cnf, KddnResults kddn,
			KddnExperiment experiment){
		this.netMgr = netMgr;
		this.cnf = cnf;
		this.namingUtil = namingUtil;
		this.kddn = kddn;
		this.experiment = experiment;
		this.needPvalue = experiment.needPvalue;
		this.twoCondition = experiment.twoCondition;
	}

	/**
	 * Network creation
	 */
	public void create() {
				
		// keep track of already added nodes
		HashMap<String, CyNode> nodeInNetwork = new HashMap<String, CyNode>();
		String[] varList = kddn.varList;
		
		// Create an empty network with unique title
		CyNetwork kddnNet = cnf.createNetwork();
		kddnNet.getRow(kddnNet).set(CyNetwork.NAME,
				      namingUtil.getSuggestedNetworkTitle("KDDN network " + CyActivator.totalNumberRun));
		
		// add lambda_1 column to network table
		kddnNet.getDefaultNetworkTable().createColumn("lambda_1", Double.class, true, 0.2d);
		kddnNet.getDefaultNetworkTable().getRow(kddnNet.getSUID()).set("lambda_1", experiment.lambda1);
		
		// add applicable columns to network table 
		if(twoCondition) {
			kddnNet.getDefaultNetworkTable().createColumn("lambda_2", Double.class, true, 0.05d);
			kddnNet.getDefaultNetworkTable().createColumn("alpha", Double.class, true, 0.05d);
			kddnNet.getDefaultNetworkTable().getRow(kddnNet.getSUID()).set("lambda_2", experiment.lambda2);
			kddnNet.getDefaultNetworkTable().getRow(kddnNet.getSUID()).set("alpha", experiment.alpha);
		}
		
		// add applicable columns to network table 
		if(experiment.useKnowledge) {
			kddnNet.getDefaultNetworkTable().createColumn("theta", Double.class, true, 0.2d);
			kddnNet.getDefaultNetworkTable().createColumn("delta", Double.class, true, 0.1d);
			kddnNet.getDefaultNetworkTable().getRow(kddnNet.getSUID()).set("theta", experiment.theta);
			kddnNet.getDefaultNetworkTable().getRow(kddnNet.getSUID()).set("delta", experiment.delta);
		}
		
		// add p-vale column to network table if it's calculated
		if(needPvalue && twoCondition)
			kddnNet.getDefaultEdgeTable().createColumn("pvalue", Double.class, true, 1d);
		
		// create network for two conditions experiment
		if(twoCondition) {
			for(int i=0; i<kddn.pValue.length; i++) {
				// pair of nodes
				String nd1 = varList[(int) kddn.pValue[i][0]];
				String nd2 = varList[(int) kddn.pValue[i][1]];
				
				CyNode node1 = null;
				CyNode node2 = null;
				// add node to node table if not in network
				if(!nodeInNetwork.containsKey(nd1)) {
					node1 = kddnNet.addNode();
					kddnNet.getDefaultNodeTable().getRow(node1.getSUID()).set("name", nd1);
					nodeInNetwork.put(nd1, node1);
				} else {
					node1 = nodeInNetwork.get(nd1);
				}
				if(!nodeInNetwork.containsKey(nd2)) {
					node2 = kddnNet.addNode();
					kddnNet.getDefaultNodeTable().getRow(node2.getSUID()).set("name", nd2);
					nodeInNetwork.put(nd2, node2);
				} else {
					node2 = nodeInNetwork.get(nd2);
				}
				
				// add differential edge
				String edgeType = "condition 1";
				if(kddn.pValue[i][3] == 2)
					edgeType = "condition 2";
				
				// assign attributes to edge table
				CyEdge edge = kddnNet.addEdge(node1, node2, false);
				kddnNet.getDefaultEdgeTable().getRow(edge.getSUID()).set("name", nd1+"<->"+nd2);
				kddnNet.getDefaultEdgeTable().getRow(edge.getSUID()).set("interaction", edgeType);
				
				if(needPvalue)
					kddnNet.getDefaultEdgeTable().getRow(edge.getSUID()).set("pvalue", kddn.pValue[i][2]);
			}
		} else { // create network for single condition experiment
			for(int i=0; i<kddn.varList.length-1; i++) {
				for(int j=i+1; j<kddn.varList.length; j++) {
					if(kddn.adjacentMatrix[i][j] != 0) {
						// pair of nodes
						String nd1 = varList[i];
						String nd2 = varList[j];
						
						CyNode node1 = null;
						CyNode node2 = null;
						// add node to network if not in network
						if(!nodeInNetwork.containsKey(nd1)) {
							node1 = kddnNet.addNode();
							kddnNet.getDefaultNodeTable().getRow(node1.getSUID()).set("name", nd1);
							nodeInNetwork.put(nd1, node1);
						} else {
							node1 = nodeInNetwork.get(nd1);
						}
						if(!nodeInNetwork.containsKey(nd2)) {
							node2 = kddnNet.addNode();
							kddnNet.getDefaultNodeTable().getRow(node2.getSUID()).set("name", nd2);
							nodeInNetwork.put(nd2, node2);
						} else {
							node2 = nodeInNetwork.get(nd2);
						}
						
						// add common edge
						String edgeType = "static edge";
						
						// assign edge table attributes
						CyEdge edge = kddnNet.addEdge(node1, node2, false);
						kddnNet.getDefaultEdgeTable().getRow(edge.getSUID()).set("name", nd1+"<->"+nd2);
						kddnNet.getDefaultEdgeTable().getRow(edge.getSUID()).set("interaction", edgeType);
					}
				}
			}
		}
		
		// add network to cytoscape
		netMgr.addNetwork(kddnNet);
		
		network = kddnNet;

	}

}

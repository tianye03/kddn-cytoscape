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

/**
 * KDDN results as adjacency matrix, beta matrix, pvalue
 * @author Ye Tian
 *
 */
public class KddnResults {
	
	public String[] varList = null;
	public int[][] adjacentMatrix = null; // p by 2p
	public double[][] beta = null;
	public double[][] pValue = null;
	public HashMap<String, Double> ttestP = new HashMap<String, Double>();
	public HashMap<String, Double> foldChange = new HashMap<String, Double>();
    
    public KddnResults(String[] varList, double[][] beta, int[][] adjacentMatrix) {

		this.varList = varList;
		this.beta = beta;
		this.adjacentMatrix = adjacentMatrix;
	}


	public KddnResults(String[] varList, int[][] adjacentMatrix,
			double[][] beta, double[][] pValue) {
		super();
		this.varList = varList;
		this.adjacentMatrix = adjacentMatrix;
		this.beta = beta;
		this.pValue = pValue;
	}


	/**
	 * Output adjacency matrix
	 */
	public void displayAdj() {
		int p = varList.length;
		for(int i=0; i<p; i++) {
			System.out.print("\t" + varList[i]);
		}
		System.out.println();
		
		for(int i=0; i<p; i++) {
			System.out.print(varList[i]);
			for(int j=0; j<p; j++) {
				if(adjacentMatrix[i][j]>0 || adjacentMatrix[j][i]>0)
					System.out.print("\t" + 1 + ",");
				else
					System.out.print("\t" + 0 + ",");
				
				if(adjacentMatrix[i][j+p]>0 || adjacentMatrix[j][i+p]>0)
					System.out.print(1);
				else
					System.out.print(0);
			}
			System.out.println();
		}
		
	}
	
	/**
	 * differential adjacency
	 * @return
	 */
	public int[][] getDifferentialNetwork() {
		int p = varList.length;
		int[][] difNet = new int[p][p];
		
		for(int i=0; i<p-1; i++)
			for(int j=i+1; j<p; j++) {
				if(adjacentMatrix[i][j] != adjacentMatrix[i][j+p]) {
					if(adjacentMatrix[i][j] == 0)
						// condition 2 edge
						difNet[i][j] = 2;
					else if(adjacentMatrix[i][j+p] == 0)
						// condition 1 edge
						difNet[i][j] = 1;
					else
						difNet[i][j] = 0;
				} else
					difNet[i][j] = 0;
			}
		
		return difNet;
	}

	public int[][] getDifferentialBeta() {
		int p = varList.length;
		int[][] difNet = new int[p][p];
		
		for(int i=0; i<p; i++)
			for(int j=0; j<p; j++) {
				if((beta[i][j] != 0 && beta[i][j+p] == 0) || 
						(beta[i][j] == 0 && beta[i][j+p] != 0) ||
						(beta[i][j] * beta[i][j+p] < 0))
					difNet[i][j] = 1;
				else
					difNet[i][j] = 0;
			}
		
		return difNet;
	}
	
	public void listPvalue() {
		int row = pValue.length;
		System.out.println("P-values of differential edges");
		System.out.println("node 1" + "\t" + "node 2" + "\t" + "p-value");
		for(int i=0; i<row; i++) {
			System.out.format("%s" + "\t" + "%s" + "\t" + "%.4f", varList[(int)pValue[i][0]], varList[(int)pValue[i][1]], pValue[i][2]);
			System.out.println();
		}
	}
}

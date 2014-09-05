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

/**
 * KDDN data, settings
 * @author Ye Tian
 *
 */
public class KddnSettings {

    /**
     * input setup variables
     */
    public double pValueCutoff = 0.05;
    public String[] varList = null;
    public double[][] data1 = null;
    public double[][] data2 = null;
    public double lambda1 = 0.2;
    public double lambda2 = 0.05;
    public double alpha = 0.05;
    public int p = 0; // # of variables
    public int N1 = 0; // # of samples
    public int N2 = 0; // # of samples
    
    public double theta = 0;
    public int[][] W = null;
    public double delta = 0.1;

    /**
     * Constructor of two condition kDDN, no prior knowledge, purely data
     * @param lambda1
     * @param lambda2
     * @param delta
     * @param pValueCutoff
     * @param data1
     * @param data2
     * @param varList
     */
    public KddnSettings(double lambda1, double lambda2,
			double pValueCutoff, double[][] data1, double[][] data2,
			String[] varList, double alpha) {

    	this.lambda1 = lambda1;
    	this.lambda2 = lambda2;
    	this.alpha = alpha;
    	this.data1 = data1;
    	this.data2 = data2;
    	this.varList = varList;
    	this.pValueCutoff = pValueCutoff;
    	
    	this.p = data1[0].length;
    	this.N1 = data1.length;
    	this.N2 = data2.length;

    	this.W = new int[this.p][2*this.p];
    	for(int i=0; i<this.p; i++)
    		for(int j=0; j<2*this.p; j++) {
    			this.W[i][j] = 0;
    		}
	}

    /**
     * Constructor with prior knowledge adjacency matrix provided
     * @param lambda1
     * @param lambda2
     * @param delta
     * @param pValueCutoff
     * @param theta
     * @param W
     * @param data1
     * @param data2
     * @param varList
     */
    public KddnSettings(double lambda1, double lambda2,
			double pValueCutoff, double theta, int[][] W, double[][] data1,
			double[][] data2, String[] varList, double alpha, double delta) {
		
    	this.lambda1 = lambda1;
    	this.lambda2 = lambda2;
    	this.alpha = alpha;
    	this.data1 = data1;
    	this.data2 = data2;
    	this.varList = varList;
    	this.pValueCutoff = pValueCutoff;
    	
    	this.p = data1[0].length;
    	this.N1 = data1.length;
    	this.N2 = data2.length;
	
    	this.theta = theta;
    	this.W = W;
    	this.delta = delta;
	}

}

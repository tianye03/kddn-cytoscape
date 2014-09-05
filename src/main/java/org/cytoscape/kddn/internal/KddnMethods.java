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
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.inference.TTest;
import org.cytoscape.work.TaskMonitor;

/**
 * kDDN related methods
 * @author Ye Tian
 *
 */
public class KddnMethods {
   
    public KddnMethods() {
		
	}

    /**
     * calculate DDN and p-value
     * @param network
     * @param paras
     * @param numPermutation 
     * @param monitor 
     * @param firstStep 
     * @param secondStep 
     * @return
     * @throws InterruptedException 
     */
    public static KddnResults calculatePvalue(KddnResults network, 
    		final KddnSettings paras, 
    		int numPermutation, TaskMonitor monitor, 
    		double firstStep, double secondStep) throws InterruptedException {

    	// copy settings
    	double pValueCutoff = paras.pValueCutoff;
        String[] varList = paras.varList;
        double[][] data1 = paras.data1;
        double[][] data2 = paras.data2;
        double lambda1 = paras.lambda1;
        double lambda2 = paras.lambda2;
        double alpha = paras.alpha;
        int p = paras.p;
        int N1 = paras.N1;
        int N2 = paras.N2;
        double theta = paras.theta;
        int[][] W = paras.W;
        double delta = paras.delta;
        
        int[][] difNet = network.getDifferentialNetwork();
        int[][] permNet = new int[p][p];
        int numDif = 0;
        for(int i=0; i<p; i++)
        	for(int j=0; j<p; j++) {
        		permNet[i][j] = 0;
        		if(difNet[i][j] != 0)
        			numDif++;
        	}
                
        double thirdStep = 1 - firstStep - secondStep;
        double progress = firstStep + secondStep;
        
        for(int i=0; i<numPermutation; i++) {
        	int[] permId = permutation(N1+N2);
        	double[][] pd1 = permute(data1, data2, permId, 0, N1-1);
        	double[][] pd2 = permute(data1, data2, permId, N1, N1+N2-1);
        	
        	standardizeData(pd1);
        	standardizeData(pd2);
        	
        	KddnSettings aRun = new KddnSettings(lambda1, lambda2, pValueCutoff, theta, W, pd1, pd2, varList, alpha, delta);
        	KddnResults aResult = solveDDN(aRun);
        	permNet = addMatrix(permNet, aResult.getDifferentialBeta());

        	progress = firstStep + secondStep + thirdStep * (i+1) / numPermutation;
    		monitor.setProgress(progress);
        }
        
        double[][] pValue = new double[numDif][4];
        int rowId = 0;
        for(int i=0; i<p-1; i++)
        	for(int j=i+1; j<p; j++) {
        		if(difNet[i][j] != 0) {
        			pValue[rowId][0] = i;
        			pValue[rowId][1] = j;
        			double p1 = (double) permNet[i][j] / numPermutation;
        			double p2 = (double) permNet[j][i] / numPermutation;
        			pValue[rowId][2] = Math.min(p1, p2);
        			if(difNet[i][j] == 1)
        				pValue[rowId][3] = 1;
        			else
        				pValue[rowId][3] = 2;
        			
        			rowId++;
        		}
        	}
        
    	return new KddnResults(paras.varList, network.adjacentMatrix, network.beta, pValue);
    }
    
    /**
     * Return permuted data matrix
     * @param a
     * @param b
     * @param id
     * @param start
     * @param end
     * @return
     */
    private static double[][] permute(double[][] a, double[][] b,
			int[] id, int start, int end) {
		double result[][] = new double[end-start+1][a[0].length];
		for(int i=start; i<=end; i++) {
			int row = id[i];
			if(row < a.length)
				System.arraycopy(a[row], 0, result[i-start], 0, a[0].length);
			else
				System.arraycopy(b[row-a.length], 0, result[i-start], 0, a[0].length);
		}
		return result;
	}

    /**
     * Add two matrices
     * @param a
     * @param b
     * @return
     */
	private static int[][] addMatrix(int[][] a,
			int[][] b) {
		int p = a.length;
		for(int i=0; i<p; i++)
			for(int j=0; j<p; j++)
				a[i][j] += b[i][j];
		return a;
	}

	/**
     * get permutation of array
     * @param N
     * @return
     */
    public static int[] permutation(int N) {
        int[] a = new int[N];

        for (int i = 0; i < N; i++)
            a[i] = i;

        for (int i = 0; i < N; i++) {
            int r = (int) (Math.random() * (i+1));
            int swap = a[r];
            a[r] = a[i];
            a[i] = swap;
        }

        return a;
    }
    
	/**
     * Do an actual calculation of ddn
     * @return 
     */
	public static KddnResults solveDDN(final KddnSettings kddn) throws InterruptedException {
	 // loop through all variables to calculate beta
		final int[] idx = new int[kddn.p];
		for(int i=0; i<kddn.p; i++)
			idx[i] = i;

		final double[][] beta = new double[kddn.p][2*kddn.p]; // beta results in rows
	    final int[][] adjacentMatrix = new int[kddn.p][2*kddn.p];

		ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		try {
			for(final Integer i : idx) {
				exec.submit(new Runnable() {
					@Override
		            public void run() {
		            	double[] y1 = getColumn(kddn.data1, i);
						double[] y2 = getColumn(kddn.data2, i);
						double[][] X1 = removeColumn(kddn.data1, i);
						double[][] X2 = removeColumn(kddn.data2, i);
						
						double[] l1 = new double[2*(kddn.p-1)];
						double[] l1a = new double[2*kddn.p];
						for(int j=0; j<2*kddn.p; j++)
							l1a[j] = (1 - kddn.theta*kddn.W[i][j]) * kddn.lambda1;
						
						if(i==0) {
							System.arraycopy(l1a, 1, l1, 0, kddn.p-1);
							System.arraycopy(l1a, kddn.p+1, l1, kddn.p-1, kddn.p-1);
						} else if(i==kddn.p-1){
							System.arraycopy(l1a, 0, l1, 0, kddn.p-1);
							System.arraycopy(l1a, kddn.p, l1, kddn.p-1, kddn.p-1);
						} else {
							System.arraycopy(l1a, 0, l1, 0, i);
							System.arraycopy(l1a, kddn.p, l1, kddn.p-1, i);
							System.arraycopy(l1a, i+1, l1, i, kddn.p-i-1);
							System.arraycopy(l1a, kddn.p+i+1, l1, kddn.p+i-1, kddn.p-i-1);
						}
						
						BCD oneNode = new BCD(y1, y2, X1, X2, l1, kddn.lambda2);

						if(oneNode.solve()) {
							if(i>0 && i<kddn.p-1) {
								System.arraycopy(oneNode.getBeta(), 0, beta[i], 0, i);
								System.arraycopy(oneNode.getBeta(), kddn.p-1, beta[i], kddn.p, i);
								System.arraycopy(oneNode.getBeta(), i, beta[i], i+1, kddn.p-1-i);
								System.arraycopy(oneNode.getBeta(), kddn.p-1+i, beta[i], kddn.p+i+1, kddn.p-1-i);
								System.arraycopy(oneNode.getAdj(), 0, adjacentMatrix[i], 0, i);
								System.arraycopy(oneNode.getAdj(), kddn.p-1, adjacentMatrix[i], kddn.p, i);
								System.arraycopy(oneNode.getAdj(), i, adjacentMatrix[i], i+1, kddn.p-1-i);
								System.arraycopy(oneNode.getAdj(), kddn.p-1+i, adjacentMatrix[i], kddn.p+i+1, kddn.p-1-i);
							} else if(i==0) {
								System.arraycopy(oneNode.getBeta(), 0, beta[i], 1, kddn.p-1);
								System.arraycopy(oneNode.getBeta(), kddn.p-1, beta[i], kddn.p+1, kddn.p-1);
								System.arraycopy(oneNode.getAdj(), 0, adjacentMatrix[i], 1, kddn.p-1);
								System.arraycopy(oneNode.getAdj(), kddn.p-1, adjacentMatrix[i], kddn.p+1, kddn.p-1);
							} else if(i==kddn.p-1) {
								System.arraycopy(oneNode.getBeta(), 0, beta[i], 0, kddn.p-1);
								System.arraycopy(oneNode.getBeta(), kddn.p-1, beta[i], kddn.p, kddn.p-1);
								System.arraycopy(oneNode.getAdj(), 0, adjacentMatrix[i], 0, kddn.p-1);
								System.arraycopy(oneNode.getAdj(), kddn.p-1, adjacentMatrix[i], kddn.p, kddn.p-1);
							}
						} else
							System.err.println("BCD error!");
						
		            }
		        });
				
			}
		} finally {
		    exec.shutdown();
		    exec.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		}

		// symetrify adjacent matrix, requires sign consistency
		for(int i=0; i<kddn.p-1; i++)
			for(int j=i+1; j<kddn.p; j++) {
				if((adjacentMatrix[i][j] + adjacentMatrix[j][i] > 0) &&
						(adjacentMatrix[i][j] * adjacentMatrix[j][i] >= 0)) {
					adjacentMatrix[i][j] = 1;
					adjacentMatrix[j][i] = 1;
				} else if((adjacentMatrix[i][j] + adjacentMatrix[j][i] < 0) &&
						(adjacentMatrix[i][j] * adjacentMatrix[j][i] >= 0)) {
					adjacentMatrix[i][j] = -1;
					adjacentMatrix[j][i] = -1;
				} else if(adjacentMatrix[i][j] * adjacentMatrix[j][i] < 0) {
					adjacentMatrix[i][j] = 0;
					adjacentMatrix[j][i] = 0;
				}
				
				if((adjacentMatrix[i][j+kddn.p] + adjacentMatrix[j][i+kddn.p] > 0) &&
						(adjacentMatrix[i][j+kddn.p] * adjacentMatrix[j][i+kddn.p] >= 0)) {
					adjacentMatrix[i][j+kddn.p] = 1;
					adjacentMatrix[j][i+kddn.p] = 1;
				} else if((adjacentMatrix[i][j+kddn.p] + adjacentMatrix[j][i+kddn.p] < 0) &&
						(adjacentMatrix[i][j+kddn.p] * adjacentMatrix[j][i+kddn.p] >= 0)) {
					adjacentMatrix[i][j+kddn.p] = -1;
					adjacentMatrix[j][i+kddn.p] = -1;
				} else if(adjacentMatrix[i][j+kddn.p] * adjacentMatrix[j][i+kddn.p] < 0) {
					adjacentMatrix[i][j+kddn.p] = 0;
					adjacentMatrix[j][i+kddn.p] = 0;
				}
			}
		
		KddnResults woPvalue = new KddnResults(kddn.varList, beta, adjacentMatrix);
		
		int[][] difNet = woPvalue.getDifferentialNetwork();
        int numDif = 0;
        for(int i=0; i<kddn.p-1; i++)
        	for(int j=i+1; j<kddn.p; j++) {
        		if(difNet[i][j] != 0)
        			numDif++;
        	}
        
        double[][] pValue = new double[numDif][4];
        int rowId = 0;
        for(int i=0; i<kddn.p-1; i++)
        	for(int j=i+1; j<kddn.p; j++) {
        		if(difNet[i][j] != 0) {
        			pValue[rowId][0] = i;
        			pValue[rowId][1] = j;
        			pValue[rowId][2] = -1;
        			if(difNet[i][j] == 1)
        				pValue[rowId][3] = 1;
        			else
        				pValue[rowId][3] = 2;
        			
        			rowId++;
        		}
        	}
        
    	return new KddnResults(kddn.varList, adjacentMatrix, beta, pValue); 
    }
	
    /**
     * Get a column from matrix
     * @param data data matrix
     * @param i the column to get
     * @return vector
     */
    static double[] getColumn(double[][] data, int i) {
		
    	double[] v = new double[data.length];
    	for(int m=0; m<v.length; m++)
    		v[m] = data[m][i];
		return v;
	}

    /**
     * Replace column
     * @param data
     * @param i
     * @param d
     */
    private static void fillColumn(double[][] data, int id, double[] d) {
		
    	for(int i=0; i<data.length; i++)
    		data[i][id] = d[i];

	}

	/**
     * Remove a column from matrix
     * @param data the input data matrix
     * @param i the column will be removed
     * @return data matrix with a column removed
     */
	 private static double[][] removeColumn(double[][] data, int i) {
		
		double[][] result = new double[data.length][data[0].length-1];
		for(int m=0; m<data.length; m++) {
			int len1 = i;
			int len2 = data[0].length-1-i;
			
			if(len1>0)
				System.arraycopy(data[m], 0, result[m], 0, len1);
			if(len2>0)
				System.arraycopy(data[m], len1+1, result[m], len1, len2);
		}
		
		return result;
	}

    /**
     * Standardize data to zero mean and unit variance
     * @param data
     */
	public static void standardizeData(double[][] data) {
		
		double[] miu = new double[data[0].length];
		for(int i=0; i<miu.length; i++)
			miu[i] = 0;
		
		// calculate mean of variables
		for(int i=0; i<miu.length; i++) {
			for(int j=0; j<data.length; j++) {
				miu[i] += data[j][i];
			}
			miu[i] /= data.length;
		}
		
		// substract mean from data
		for(int i=0; i<miu.length; i++) {
			for(int j=0; j<data.length; j++) {
				data[j][i] -= miu[i];
			}
		}
		
		// calculate sqrt of sum of squares
		double[] s2 = new double[data[0].length];
		for(int i=0; i<s2.length; i++)
			s2[i] = 0;
		
		for(int i=0; i<s2.length; i++) {
			for(int j=0; j<data.length; j++) {
				s2[i] += data[j][i]*data[j][i];
			}
			s2[i] = Math.sqrt(s2[i]);
		}
		
		// divide normalization factor
		for(int i=0; i<s2.length; i++) {
			for(int j=0; j<data.length; j++) {
				data[j][i] /= s2[i];
			}
		}
	}
  
    /**
     * variable selection based on t-test
     * @param var
     * @param d1
     * @param d2
     * @return
     */
	public static int[] variableSelection(String[] var,
			double[][] d1, double[][] d2) {
		
		LinkedHashMap<String, Integer> selectedVar = new LinkedHashMap<String, Integer>();
		LinkedHashMap<String, Double> varPvalue = new LinkedHashMap<String, Double>();

		TTest aTest = new TTest();

		for(int i=0; i<var.length; i++) {
			double pval = 0;
			
			// equal variance
			//pval = aTest.homoscedasticTTest(getColumn(d1,i), getColumn(d2,i));
			// unequal variance
			pval = aTest.tTest(getColumn(d1,i), getColumn(d2,i));
	
			if(selectedVar.containsKey(var[i])) {
				if(varPvalue.get(var[i]) > pval) {
					selectedVar.put(var[i], i);
					varPvalue.put(var[i], pval);
				}
			} else {
				selectedVar.put(var[i], i);
				varPvalue.put(var[i], pval);
			}
		}
		
		int[] idx = new int[selectedVar.size()];
		int i = 0;
		for(String s : selectedVar.keySet()) {
			idx[i] = selectedVar.get(s);
			i++;
		}
		
		return idx;
	}

	/**
	 * select subset of variables
	 * @param s
	 * @param id
	 * @return
	 */
	public static String[] selectVariable(String[] s, int[] id) {
		
		String[] r = new String[id.length];
		for(int i=0; i<id.length; i++)
			r[i] = s[id[i]];
		
		return r;
	}

	/**
	 * slice matrix to subset of columns
	 * @param d
	 * @param id
	 * @return
	 */
	public static double[][] selectData(double[][] d, int[] id) {
		
		double[][] v = new double[d.length][id.length];
		for(int i=0; i<id.length; i++)
			fillColumn(v, i, getColumn(d, id[i]));
	
		return v;
	}

	/**
	 * qnorm
	 * @param p
	 * @param upper
	 * @return
	 */
	public static double qnorm(double p,boolean upper) {
	    /* Reference:
	       J. D. Beasley and S. G. Springer 
	       Algorithm AS 111: "The Percentage Points of the Normal Distribution"
	       Applied Statistics
	    */
	    if(p<0 || p>1)
	      throw new IllegalArgumentException("Illegal argument "+p+" for qnorm(p).");
	    double split=0.42,
	           a0=  2.50662823884,
	           a1=-18.61500062529,
	           a2= 41.39119773534,
	           a3=-25.44106049637,
	           b1= -8.47351093090,
	           b2= 23.08336743743,
	           b3=-21.06224101826,
	           b4=  3.13082909833,
	           c0= -2.78718931138,
	           c1= -2.29796479134,
	           c2=  4.85014127135,
	           c3=  2.32121276858,
	           d1=  3.54388924762,
	           d2=  1.63706781897,
	           q=p-0.5;
	    double r,ppnd;
	    if(Math.abs(q)<=split) {
	      r=q*q;
	      ppnd=q*(((a3*r+a2)*r+a1)*r+a0)/((((b4*r+b3)*r+b2)*r+b1)*r+1);
	    }
	    else {
	      r=p;
	      if(q>0) r=1-p;
	      if(r>0) {
	        r=Math.sqrt(-Math.log(r));
	        ppnd=(((c3*r+c2)*r+c1)*r+c0)/((d2*r+d1)*r+1);
	        if(q<0) ppnd=-ppnd;
	      }
	      else {
	        ppnd=0;
	      }
	    }
	    if(upper) ppnd=1-ppnd;
	    return(ppnd);
	  }
	
	/**
	 * Calculate lambda 1
	 * @param d1
	 * @param d2
	 * @param varList
	 * @return
	 * @throws InterruptedException
	 */
	public static double findLambda1(double[][] d1, double[][] d2, String[] varList) throws InterruptedException {

		int n1 = d1.length;
		int n2 = d2.length;
		int p = varList.length;
		double[][] ld1 = new double[d1.length][d1[0].length];
    	double[][] ld2 = new double[d2.length][d2[0].length];
    	for(int i=0; i<d1.length; i++)
    		ld1[i] = d1[i].clone();
    	for(int i=0; i<d2.length; i++)
    		ld2[i] = d2[i].clone();
    	
		double m = (double) (n1 + n2) / 2;
		double l1 = 2 / m * qnorm(1 - 0.05 / 2 / p / (m*m), false);
		
		standardizeData(ld1);
    	standardizeData(ld2);
		KddnSettings aRun = new KddnSettings(l1, 0, 0.05, ld1, ld2, varList, 0);
    	KddnResults aResult = solveDDN(aRun);
    	int size = getNetworkSize(aResult);

    	while(size == 0) {
    		l1 = l1 / 4;
    		aRun = new KddnSettings(l1, 0, 0.05, ld1, ld2, varList, 0);
        	aResult = solveDDN(aRun);
        	size = getNetworkSize(aResult);
    	}
    	
    	return l1;
	}

	/**
	 * Calculate lambda 2 according to alpha
	 * @param d1
	 * @param d2
	 * @param l1
	 * @param alpha
	 * @param varList
	 * @param monitor
	 * @param firstStep
	 * @return
	 * @throws InterruptedException
	 */
	public static double findLambda2(double[][] d1, double[][] d2,
			double l1, double alpha, String[] varList, 
			TaskMonitor monitor, double firstStep) throws InterruptedException {
		
		int N1 = d1.length;
		int N2 = d2.length;
    	
		double l2 = 0;
		double step = 0.035;
		double pNull = 0;
		double high = 0;
		double low = 0;
		double mid = 0;
		double lambda2 = 0;
		
		int B = 100;
		double progress = 0;
        for(int i=0; i<B; i++) {
        	int[] permId = permutation(N1+N2);
        	double[][] pd1 = permute(d1, d2, permId, 0, N1-1);
        	double[][] pd2 = permute(d1, d2, permId, N1, N1+N2-1);
        	
        	standardizeData(pd1);
        	standardizeData(pd2);

        	l2 = mid;

        	KddnSettings aRun = new KddnSettings(l1, l2, 0.05, pd1, pd2, varList, alpha);
        	KddnResults aResult = solveDDN(aRun);

        	pNull = getPnull(aResult);
        	
        	if(pNull > alpha) { 	
	        	while(pNull > alpha) {
	        		low = l2;
	        		l2 += step;
	        		high = l2;
	        		aRun = new KddnSettings(l1, l2, 0.05, pd1, pd2, varList, alpha);
	            	aResult = solveDDN(aRun);
	            	pNull = getPnull(aResult);
	        	}
        	} else {
        		while(pNull < alpha) {
        			high = l2;
        			l2 -= step;
        			low = l2;
        			aRun = new KddnSettings(l1, l2, 0.05, pd1, pd2, varList, alpha);
	            	aResult = solveDDN(aRun);
	            	pNull = getPnull(aResult);
        		}
        	}
        	
        	mid = (high-low)/2 + low;
        	aRun = new KddnSettings(l1, mid, 0.05, pd1, pd2, varList, alpha);
        	aResult = solveDDN(aRun);
        	pNull = getPnull(aResult);
        	
        	while(Math.abs(pNull - alpha) > 0.001 && high - low > 0.01) {
        		if(pNull > alpha) {
        			low = mid;
        			mid = (high-low)/2 + low;
        			aRun = new KddnSettings(l1, mid, 0.05, pd1, pd2, varList, alpha);
                	aResult = solveDDN(aRun);
                	pNull = getPnull(aResult);
        		} else {
        			high = mid;
        			mid = (high-low)/2 + low;
        			aRun = new KddnSettings(l1, mid, 0.05, pd1, pd2, varList, alpha);
                	aResult = solveDDN(aRun);
                	pNull = getPnull(aResult);
        		}
        	}
        	
        	progress = firstStep * (i+1) / B;
    		monitor.setProgress(progress);

        	lambda2 += mid;
        }
        
		return lambda2 / B;
	}

	/**
	 * calculate power under null
	 * @param r
	 * @return
	 */
	private static double getPnull(KddnResults r) {
		
		int netSize = 0;
		int difSize = 0;
		int p = r.beta.length;
		for(int i=0; i<p; i++) {
			for(int j=0; j<p; j++) {
				if(r.beta[i][j] != 0) {
					netSize++;
					if(r.beta[i][j+p] == 0)
						difSize++;
				}
				if(r.beta[i][j+p] != 0) {
					netSize++;
					if(r.beta[i][j] == 0)
						difSize++;
				}
				if(r.beta[i][j] * r.beta[i][j+p] < 0)
					difSize++;
			}
		}
		
		return (double) difSize / netSize * 2;
	}
	
	/**
	 * network size in number of total edges, sum of both networks
	 * @param r
	 * @return
	 */
	private static int getNetworkSize(KddnResults r) {
		
		int netSize = 0;
		int p = r.beta.length;
		for(int i=0; i<p-1; i++) {
			for(int j=i+1; j<p; j++) {
				if(r.adjacentMatrix[i][j] != 0) {
					netSize++;
				}
				if(r.adjacentMatrix[i][j+p] != 0) {
					netSize++;
				}
			}
		}
		
		return netSize;
	}

	/**
	 * Calculate theta corresponding to theta
	 * @param d1
	 * @param d2
	 * @param l1
	 * @param l2
	 * @param varList
	 * @param M
	 * @param delta
	 * @param monitor 
	 * @param secondStep 
	 * @param firstStep 
	 * @return
	 * @throws InterruptedException
	 */
	public static double findTheta(double[][] d1, double[][] d2,
			double l1, double l2, String[] varList, int M,
			double delta, TaskMonitor monitor, double firstStep, double secondStep) throws InterruptedException {
		   	
		double portion = 0.2;
		double remaining = secondStep;
		
		standardizeData(d1);
    	standardizeData(d2);
    	
    	KddnSettings dataSetting = new KddnSettings(l1, l2, 0.05, d1, d2, varList, 0.05);
    	KddnResults dataResult = solveDDN(dataSetting);

    	double high = 0.5;
    	double low = 0.02;
    	double mid = (high-low) / 2 + low;

    	double deviation = thetaError(d1, d2, l1, l2, varList, M, mid, dataResult);
    	
    	while(high - low > 0.01) {
    		if(deviation > delta) {
    			high = mid;
    			mid = (high-low) / 2 + low;
    			deviation = thetaError(d1, d2, l1, l2, varList, M, mid, dataResult);
    		} else {
    			low = mid;
    			mid = (high-low) / 2 + low;
    			deviation = thetaError(d1, d2, l1, l2, varList, M, mid, dataResult);
    		}
    		
    		double progress = remaining * portion;
    		remaining -= progress;
    		portion += 0.05;
    		
    		monitor.setProgress(firstStep + secondStep - remaining);
    	}
    	
		return mid;
	}

	/**
	 * Calculate deviation caused by theta
	 * @param d1
	 * @param d2
	 * @param l1
	 * @param l2
	 * @param varList
	 * @param M
	 * @param theta
	 * @param dataResult
	 * @return
	 * @throws InterruptedException
	 */
	private static double thetaError(double[][] d1, double[][] d2, double l1,
			double l2, String[] varList, int M, double theta,
			KddnResults dataResult) throws InterruptedException {
		
		int B = 100;
		int p = d1[0].length;
		int error = 0;
		
		for(int i=0; i<B; i++) {
			int[][] W = randomMatrix(p, M);
			KddnSettings aRun = new KddnSettings(l1, l2, 0.05, theta, W, d1, d2, varList, 0.05, 0.1);
        	KddnResults aResult = solveDDN(aRun);
        	
        	for(int s=0; s<p-1; s++)
        		for(int t=s+1; t<p; t++) {
        			if(aResult.adjacentMatrix[s][t] != dataResult.adjacentMatrix[s][t])
        				error++;
        			if(aResult.adjacentMatrix[s][t+p] != dataResult.adjacentMatrix[s][t+p])
        				error++;
        		}
		}
		
		return (double) error / B / getNetworkSize(dataResult);
	}

	/**
	 * Generate a random matrix with M non zero element
	 */
	private static int[][] randomMatrix(int p, int M) {
		
		int[] idx = new int[M];
		System.arraycopy(permutation(p*(p-1)/2), 0, idx, 0, M);
		
		int[][] W = new int[p][2*p];
		for(int i=0; i<p; i++)
    		for(int j=0; j<2*p; j++) {
    			W[i][j] = 0;
    		}
		
		for(int i=0; i<M; i++) {
			int row = 0;
	        int col = 0;
	        int ln = idx[i]+1;
	        for(int j=p-1; j>0; j--) {
	            ln = ln-j;
	            if (ln > 0)
	              row++;
	            else {
	                col = ln + j + row;
	                W[row][col] = 1;
	                W[col][row] = 1;
	                W[row][col+p] = 1;
	                W[col][row+p] = 1;
	                break;
	            }
	        }
	    }
		return W;
	}

	/**
	 * Map pair nodes knowledge network into matrix
	 * @param varList
	 * @param net
	 * @param W
	 */
	public static void mapKnowledgeNetwork(String[] varList,
			String[][] net, int[][] W) {
		
		// if prior net is read from valid file
		if(net != null) {
			HashMap<String, Integer> index = new HashMap<String, Integer>();
			for(int i=0; i<varList.length; i++) {
				index.put(varList[i].toLowerCase(), i);
			}
			
			int row = 0;
			int col = 0;
			for(int i=0; i<net.length; i++) {
				if(index.containsKey(net[i][0].toLowerCase()) && index.containsKey(net[i][1].toLowerCase())) {
					row = index.get(net[i][0].toLowerCase());
					col = index.get(net[i][1].toLowerCase());
					if(net[i][2] == "0") {  // both conditions
						W[row][col] = 1;
						W[col][row] = 1;
						W[row][col+varList.length] = 1;
						W[col][row+varList.length] = 1;
					} else if(net[i][2] == "1") {  // condition 1
						W[row][col] = 1;
						W[col][row] = 1;
					} else {  // condition 2
						W[row][col+varList.length] = 1;
						W[col][row+varList.length] = 1;
					}
				}
			}
		}
		
	}

	public static double getVecorMean(double[] d) {
		double total = 0;
		int l = d.length;
		for(int i=0; i<l; i++)
			total += d[i];
		
		return total/l;
	}

}

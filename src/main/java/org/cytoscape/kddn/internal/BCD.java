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

import java.util.Arrays;
import java.lang.Math;

/**
 * <code>BCD</code> implements block-wise coordinate descent algorithm to solve
 * two-conditioned regression problem.
 *
 * @version      2014.0215
 */

public class BCD {

    private double[] y1;
    private double[] y2;
    private double[][] X1;
    private double[][] X2;
    private double []lambda1;
    private double lambda2;
    private double[] beta1;
    private double[] beta2;
    private double[] beta;
    private int[] adj;
    private int p;
    private int n1;
    private int n2;

    
    /**
     * A constructor for the class <code>BCD</code>.
     * 
     * @param y1        response variable under condition 1
     * @param y2        response variable under condition 2
     * @param X1        data matrix (n1 X p) under condtion 1
     * @param X2        data matrix (n2 X p) under condtion 2
     * @param lambda1   penalty parameter that promotes sparsity in the learned 
     *                  structure
     * @param lambda2   penalty parameter that promotes sparse changes between 
     *                  two conditions
     */
    public BCD(double[] y1, double[] y2, double[][] X1, double[][] X2, double []lambda1, double lambda2) {
		this.y1 = Arrays.copyOf(y1, y1.length);
		this.y2 = Arrays.copyOf(y2, y2.length);
		this.lambda1 = Arrays.copyOf(lambda1, lambda1.length);
		this.lambda2 = lambda2;
		this.n1 = X1.length;
		this.n2 = X2.length;
		this.p = X1[0].length;
		if (X1[0].length != X2[0].length) {
		    System.out.println("The column number of X1 does NOT equal the column number of X2.");
		}
		this.X1 = new double[n1][p];
		this.X2 = new double[n2][p];
		for (int i = 0; i < n1; i ++) {
		    for (int j = 0; j < p; j ++) {
			this.X1[i][j] = X1[i][j];
		    }
		}
		for (int i = 0; i < n2; i ++) {
		    for (int j = 0; j < p; j ++) {
			this.X2[i][j] = X2[i][j];
		    }
		}
		this.beta1 = new double[p];
		this.beta2 = new double[p];
		this.beta = new double[p*2];
		this.adj = new int[2*p];
    }

    /**
     * A constructor for the class <code>BCD</code> when initial beta is given.
     * 
     * @param y1        response variable under condition 1
     * @param y2        response variable under condition 2
     * @param X1        data matrix (n1 X p) under condtion 1
     * @param X2        data matrix (n2 X p) under condtion 2
     * @param lambda1   penalty parameter that promotes sparsity in the learned 
     *                  structure
     * @param lambda1   penalty parameter that promotes sparse changes between 
     *                  two conditions
     * @param beta1     initial value of beta1 under condition 1
     * @param beta2     initial value of beta2 under condition 2
     */
    public BCD(double[] y1, double[] y2, double[][] X1, double[][] X2, double []lambda1, double lambda2, double[] beta1, double[] beta2) {

		this(y1, y2, X1, X2, lambda1, lambda2);
		System.arraycopy(beta1, 0, this.beta1, 0, p);
		System.arraycopy(beta2, 0, this.beta2, 0, p);
		System.arraycopy(beta1, 0, this.beta, 0, p);
		System.arraycopy(beta2, 0, this.beta, p, p);

    }


    /**
     * Solves the 2-d sub-problem of the coordinate block.
     */
    private double[] solve2d(double[] z1, double[] z2, double[] x1, double[] x2, int k) {
		double rho1 = 0;
		double rho2 = 0;
		double b1 = 0;
		double b2 = 0;
	
		for (int i = 0; i < n1; i ++) {
		    rho1 = rho1 + z1[i] * x1[i];
		}
		for (int i = 0; i < n2; i ++) {
		    rho2 = rho2 + z2[i] * x2[i];
		}
	
		b1 = 0;
		b2 = 0;
		int ai = 0;
		if (rho2 <= (rho1 - (lambda1[k] - lambda1[k+p]) + 2*lambda2) 
			&& rho2 >= (rho1 -(lambda1[k] - lambda1[k+p]) - 2*lambda2)
		    && rho2 >= (lambda1[k] + lambda1[k+p] - rho1)) {
		    b1 = (rho1 + rho2)/2 - (lambda1[k] + lambda1[k+p]) / 2;
		    b2 = (rho1 + rho2)/2 - (lambda1[k] + lambda1[k+p]) / 2;
			ai = 1;	
		}
	
		if (rho2 > (rho1 - (lambda1[k] - lambda1[k+p]) + 2*lambda2) 
			&& rho1 >= (lambda1[k] - lambda2)) {
		    b1 = rho1 - lambda1[k] + lambda2;
		    b2 = rho2 - lambda1[k+p] - lambda2;
		    ai = 2;
		}
	
		if (rho1 < (lambda1[k] - lambda2) && rho1 >= -(lambda1[k] + lambda2)
		    && rho2 >= (lambda1[k+p] + lambda2)) {
		    b1 = 0;
		    b2 = rho2 - lambda1[k+p] - lambda2;
		    ai = 3;
		}
	
		if (rho1 < -(lambda1[k] + lambda2) && rho2 >= (lambda1[k+p] + lambda2)) {
		    b1 = rho1 + lambda1[k] + lambda2;
		    b2 = rho2 - lambda1[k+p] - lambda2;
		    ai = 4;
		}
	
		if (rho1 < -(lambda1[k] + lambda2) && rho2 < (lambda1[k+p] + lambda2)
		    && rho2 >= -(lambda1[k+p] - lambda2)) {
		    b1 = rho1 + lambda1[k] + lambda2;
		    b2 = 0;
		    ai = 5;
		}
	
		if (rho2 < -(lambda1[k+p] - lambda2)
			&& rho2 >= (rho1 + (lambda1[k] - lambda1[k+p]) + 2*lambda2)) {
		    b1 = rho1 + lambda1[k] + lambda2;
		    b2 = rho2 + lambda1[k+p] - lambda2;
		    ai = 6;
		}
	
		if (rho2 >= (rho1 + (lambda1[k] - lambda1[k+p]) - 2*lambda2) 
			&& rho2 < (rho1 + (lambda1[k] - lambda1[k+p]) + 2*lambda2)
		    && rho2 <= (-(lambda1[k] + lambda1[k+p]) - rho1)) {
		    b1 = (rho1 + rho2)/2 + (lambda1[k] + lambda1[k+p]) / 2;
		    b2 = (rho1 + rho2)/2 + (lambda1[k] + lambda1[k+p]) / 2;
		    ai = 7;
		}
	
		if (rho2 < (rho1 + (lambda1[k] - lambda1[k+p]) - 2*lambda2) 
			&& rho1 <= -(lambda1[k] - lambda2)) {
		    b1 = rho1 + lambda1[k] - lambda2;
		    b2 = rho2 + lambda1[k+p] + lambda2;
		    ai = 8;
		}
	
		if (rho1 <= (lambda1[k] + lambda2) && rho1 >= -(lambda1[k] - lambda2)
		    && rho2 <= -(lambda1[k+p] + lambda2)) {
		    b1 = 0;
		    b2 = rho2 + lambda1[k+p] + lambda2;
		    ai = 9;
		}
	
		if (rho1 > (lambda1[k] + lambda2) && rho2 <= -(lambda1[k+p] + lambda2)) {
		    b1 = rho1 - lambda1[k] - lambda2;
		    b2 = rho2 + lambda1[k+p] + lambda2;
		    ai = 10;
		}
	
		if (rho2 > -(lambda1[k+p] + lambda2) && rho2 <= (lambda1[k+p] - lambda2)
		    && rho1 >= (lambda1[k] + lambda2)) {
		    b1 = rho1 - lambda1[k] - lambda2;
		    b2 = 0;
		    ai = 11;
		}
	
		if (rho2 > (lambda1[k+p] - lambda2) 
			&& rho2 < (rho1 - (lambda1[k] - lambda1[k+p]) - 2*lambda2)) {
		    b1 = rho1 - lambda1[k] - lambda2;
		    b2 = rho2 - lambda1[k+p] + lambda2;
		    ai = 12;
		}
	
		double[] b = {b1, b2};
		return b;
    }

    /**
     * Solves the optimization problem using block-wise coordinate descent algorithm.
     * 
     * @return     <code>true</code>, if an optimal solution is successfully obtained;
     *             <code>false</code>, otherwise. 
     */
    public boolean solve() {

		boolean isStop = false;
		int r = 0;
		double[] beta1_old = new double[p];
		double[] beta2_old = new double[p];
		double[] x1 = new double[n1];
		double[] x2 = new double[n2];
		double[] z1 = new double[n1];
		double[] z2 = new double[n2];
	
		if (p == 1) {
	
		    for (int m = 0; m < n1; m ++) {
			x1[m] = X1[m][0];
		    }
		    for (int m = 0; m < n2; m ++) {
			x2[m] = X2[m][0];
		    }
		    beta = solve2d(y1, y2, x1, x2, 1);
	
		}else {
	
		    while (!isStop) {
	
			for (int i = 0; i < p; i ++ ) {
			    beta1_old[i] = beta1[i];
			    beta2_old[i] = beta2[i];
			}
			for (int i = 0; i < p; i ++) {
			    r = r + 1;
			    int k = r % p;
			    
			    for (int m = 0; m < n1; m ++) {
				x1[m] = X1[m][k];
			    }
			    for (int m = 0; m < n2; m ++) {
				x2[m] = X2[m][k];
			    }
			    
			    //	double y_residual = y - X[, c(-k, -p-k)] %*% matrix(beta[c(-k, -p-k)], nrow=2*p-2, ncol=1);
			    for (int m = 0; m < n1; m ++) {
				z1[m] = y1[m];
				for (int n = 0; n < p; n ++) {
				    if (n != k) {
					z1[m] = z1[m] - X1[m][n] * beta1[n];
				    }
				}
				
			    }
	
			    for (int m = 0; m < n2; m ++) {
				z2[m] = y2[m];
				for (int n = 0; n < p; n ++) {
				    if (n != k) {
					z2[m] = z2[m] - X2[m][n] * beta2[n];
				    }
				}
				
			    }
	
			    double[] beta2d = solve2d(z1, z2, x1, x2, k);
			    beta1[k] = beta2d[0];
			    beta2[k] = beta2d[1];
			}
	
	
			double changeInBeta = 0;
			for (int m = 0; m < p; m ++ ) {
			    changeInBeta += Math.abs(beta1[m] - beta1_old[m]) + Math.abs(beta2[m] - beta2_old[m]);
			}
			if (changeInBeta < 0.00001*p*2) {
			    isStop = true;
			}
		    }
	
		} 
	
		return true;
    }

    /**
     * Returns the value of beta, where beta = [beta1, beta2].
     * 
     * @return     <code>beta</code>.
     */
    public double[] getBeta() {
		for (int i = 0; i < p; i ++) {
		    beta[i] = beta1[i];
		    beta[p+i] = beta2[i];
		}
		return beta;
    }

    /**
     * Returns adjacency
     * @return
     */
	public Object getAdj() {
		for(int i=0; i<p; i++) {
			if(beta1[i] > 0)
				adj[i] = 1;
			else if(beta1[i] < 0)
				adj[i] = -1;
			else
				adj[i] = 0;
			
			if(beta2[i] > 0)
				adj[i+p] = 1;
			else if(beta2[i] < 0)
				adj[i+p] = -1;
			else
				adj[i+p] = 0;
		}
		return adj;
	}

}
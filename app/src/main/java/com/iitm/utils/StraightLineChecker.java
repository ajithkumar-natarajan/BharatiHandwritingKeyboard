package com.iitm.utils;

import android.util.Log;

import com.iitm.stroke.Stroke;

public class StraightLineChecker {
	
	//Tested and working fine
	private static String LOG = "StraightLineChecker";
	
	public static boolean isLine(Stroke stroke){
		
		double[][] m = new double[stroke.size()-1][2];

		for(int i=0;i<(stroke.size()-1);i++){
			m[i][0] = stroke.getX(i+1) - stroke.getX(0);
			m[i][1] = stroke.getY(i+1) - stroke.getY(0);
		}
		
		return isLine(m);
				
	}
	
	public static boolean isLine(double[][] m){

		int rank = 0;
		double[] singularValues = sVD(m);

		for(int svIndex=0;svIndex<2;svIndex++){//because only 2 dimensional pts
			if(singularValues[svIndex]>0.05)//Was 0.075 at pre-android code//0.05)//0.16)//0.14)//0.125)//0.05
				rank++;
		}

		//Log.i(LOG,"Singular Values "+singularValues[0]+" "+singularValues[1]);
		Log.i(LOG,"isLine returning "+(rank==1));
		
		return (rank==1) ;
	}

	public static double pythag(double a, double b)
	{
		double at = Math.abs(a), bt = Math.abs(b), ct, result;

		if (at > bt)       { ct = bt / at; result = at * Math.sqrt(1.0 + ct * ct); }
		else if (bt > 0.0) { ct = at / bt; result = bt * Math.sqrt(1.0 + ct * ct); }
		else result = 0.0;
		return result;
	}

	public static double[] decompose(double[][] a, int m, int n, double[]  w)
	{
		int flag, i, its, j, jj, k, l, nm=0;
		double c, f, h, s, x, y, z;
		double anorm = 0.0, g = 0.0, scale = 0.0;
		double[] rv1;

		//Log.i(LOG,"SV decompose : m "+m+" n "+n+" a.length "+a.length);
		
		if (m < n)
		{
			//Log.i(LOG,"Error in SVD : #rows must be > #cols \n");
			return w;
		}

		rv1 = new double[n];

		// Householder reduction to bidiagonal form
		for (i = 0; i < n; i++)
		{
			// left-hand reduction
			l = i + 1;
			rv1[i] = scale * g;
			g = s = scale = 0.0;
			if (i < m)
			{
				for (k = i; k < m; k++)
					scale += Math.abs((double)a[k][i]);
				if (scale != 0)
				{
					for (k = i; k < m; k++)
					{
						a[k][i] =  (double)((double)a[k][i]/scale);
						s += ((double)a[k][i] * (double)a[k][i]);
					}
					f = (double)a[i][i];
					g = -copysign(Math.sqrt(s), f);
					h = f * g - s;
					a[i][i] = (double)(f - g);
					if (i != (n - 1))
					{
						for (j = l; j < n; j++)
						{
							for (s = 0.0, k = i; k < m; k++)
								s += ((double)a[k][i] * (double)a[k][j]);
							f = s / h;
							for (k = i; k < m; k++)
								a[k][j] += (double)(f * (double)a[k][i]);
						}
					}
					for (k = i; k < m; k++)
						a[k][i] = (double)((double)a[k][i]*scale);
				}
			}
			w[i] = (double)(scale * g);

			// right-hand reduction
			g = s = scale = 0.0;
			if (i < m && i != n - 1)
			{
				for (k = l; k < n; k++)
					scale += Math.abs((double)a[i][k]);
				if (scale!=0)
				{
					for (k = l; k < n; k++)
					{
						a[i][k] = (double)((double)a[i][k]/scale);
						s += ((double)a[i][k] * (double)a[i][k]);
					}
					f = (double)a[i][l];
					g = -copysign(Math.sqrt(s), f);
					h = f * g - s;
					a[i][l] = (double)(f - g);
					for (k = l; k < n; k++)
						rv1[k] = (double)a[i][k] / h;
					if (i != m - 1)
					{
						for (j = l; j < m; j++)
						{
							for (s = 0.0, k = l; k < n; k++)
								s += ((double)a[j][k] * (double)a[i][k]);
							for (k = l; k < n; k++)
								a[j][k] += (double)(s * rv1[k]);
						}
					}
					for (k = l; k < n; k++)
						a[i][k] =  (double)((double)a[i][k]*scale);
				}
			}
			anorm = Math.max(anorm, (Math.abs((double)w[i]) + Math.abs(rv1[i])));
		}

		// accumulate the right-hand transformation
		for (i = n - 1; i >= 0; i--)
		{
			g = rv1[i];
			l = i;
		}

		// accumulate the left-hand transformation
		for (i = n - 1; i >= 0; i--)
		{
			l = i + 1;
			g = (double)w[i];
			if (i < n - 1)
				for (j = l; j < n; j++)
					a[i][j] = 0.0;
			if (g!=0)
			{
				g = 1.0 / g;
				if (i != n - 1)
				{
					for (j = l; j < n; j++)
					{
						for (s = 0.0, k = l; k < m; k++)
							s += ((double)a[k][i] * (double)a[k][j]);
						f = (s / (double)a[i][i]) * g;
						for (k = i; k < m; k++)
							a[k][j] =  a[k][j] + (double)(f * (double)a[k][i]);
					}
				}
				for (j = i; j < m; j++)
					a[j][i] = (double)((double)a[j][i]*g);
			}
			else
			{
				for (j = i; j < m; j++)
					a[j][i] = 0.0;
			}
			a[i][i]++;
		}

		// diagonalize the bidiagonal form
		for (k = n - 1; k >= 0; k--)
		{                             // loop over singular values
			for (its = 0; its < 30; its++)
			{                         // loop over allowed iterations
				flag = 1;
				for (l = k; l >= 0; l--)
				{                     // test for splitting
					nm = l - 1;
					if ((Math.abs(rv1[l]) + anorm) == anorm)
					{
						flag = 0;
						break;
					}
					if ((Math.abs((double)w[nm]) + anorm) == anorm)
						break;
				}
				if (flag!=0)
				{
					c = 0.0;
					s = 1.0;
					for (i = l; i <= k; i++)
					{
						f = s * rv1[i];
						if ((Math.abs(f) + anorm) != anorm)
						{
							g = (double)w[i];
							h = pythag(f, g);
							w[i] = (double)h;
							h = 1.0 / h;
							c = g * h;
							s = (- f * h);
							for (j = 0; j < m; j++)
							{
								y = (double)a[j][nm];
								z = (double)a[j][i];
								a[j][nm] = (double)(y * c + z * s);
								a[j][i] = (double)(z * c - y * s);
							}
						}
					}
				}
				z = (double)w[k];
				if (l == k)
				{
					if (z < 0.0)
					{
						w[k] = (double)(-z);
						//						for (j = 0; j < n; j++)
						//							j;
						j = n;
						//v[j][k] = (-v[j][k]);
					}
					break;
				}
				if (its >= 30) {
					rv1=null;
					//Log.i(LOG,"Error : No convergence after 30,000! iterations");
					return w;
				}

				/* shift from bottom 2 x 2 minor */
				x = (double)w[l];
				nm = k - 1;
				y = (double)w[nm];
				g = rv1[nm];
				h = rv1[k];
				f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2.0 * h * y);
				g = pythag(f, 1.0);
				f = ((x - z) * (x + z) + h * ((y / (f + copysign(g, f))) - h)) / x;

				/* next QR transformation */
				c = s = 1.0;
				for (j = l; j <= nm; j++)
				{
					i = j + 1;
					g = rv1[i];
					y = (double)w[i];
					h = s * g;
					g = c * g;
					z = pythag(f, h);
					rv1[j] = z;
					c = f / z;
					s = h / z;
					f = x * c + g * s;
					g = g * c - x * s;
					h = y * s;
					y = y * c;
					for (jj = 0; jj < n; jj++)
					{
						//x = (double)v[jj][j];
						//z = (double)v[jj][i];
						//v[jj][j] = (double)(x * c + z * s);
						//v[jj][i] = (double)(z * c - x * s);
					}
					z = pythag(f, h);
					w[j] = (double)z;
					if (z!=0)
					{
						z = 1.0 / z;
						c = f * z;
						s = h * z;
					}
					f = (c * g) + (s * y);
					x = (c * y) - (s * g);
					for (jj = 0; jj < m; jj++)
					{
						y = (double)a[jj][j];
						z = (double)a[jj][i];
						a[jj][j] =(double)(y * c + z * s);
						a[jj][i] = (double)(z * c - y * s);
					}
				}
				rv1[l] = 0.0;
				rv1[k] = f;
				w[k] = (double)x;
			}
		}
		rv1 = null;
		return w;
	}

	public static double copysign(double a,double b) {
		return ((b >= 0.0) ? Math.abs(a) : -Math.abs(a));
	}

	public static double[] sVD(double[][] m)
	{
		double[] w = new double[2];//no. of cols is 2
		w = decompose(m, m.length, 2, w);
		//sort w
		return w;
	}

	private StraightLineChecker(){ //uninstantiable class
		
	}
	
	//Code for testing
	public static void main(String[] args){
		
		Stroke s= new Stroke();
		s.addPoint(1,2);
		s.addPoint(3,4);
		s.addPoint(5,6);
		s.addPoint(1,1);
		s.addPoint(1,1);
		s.addPoint(1,2);
		
		isLine(s);
		
	}

	
}

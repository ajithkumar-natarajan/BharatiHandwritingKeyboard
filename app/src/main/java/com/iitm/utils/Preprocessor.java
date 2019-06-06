package com.iitm.utils;

import java.util.ArrayList;
import java.util.List;

import com.iitm.stroke.Stroke;

import android.util.Log;;

public class Preprocessor {

	//tested and working perfect
	
	/** PreProcessing Functions for strokes
	 *  Modified only the vector and Matrix classes
	 *  Rest are verbatim translated from CPP version
	 *  
	 *  Interpolate -> Curve_Norm -> Smooth -> Interpolate to feature vec -> Unit Square
	 */

	private static String LOG = "Preprocessor";
	
	private int initialInterpolationSize = 200;

	private double[] xCoordinates;
	private double[] yCoordinates;

	private double xMin,yMin,xMax,yMax;
	private double width,height;

	@Deprecated
	public Preprocessor(){
		//default constructor disabled
	}

	public Preprocessor(Stroke stroke){

		xCoordinates = new double[stroke.size()];
		yCoordinates = new double[stroke.size()];

		for(int i=0;i<stroke.size();i++){
			xCoordinates[i] = stroke.getX(i);
			yCoordinates[i] = stroke.getY(i);
		}

		this.xMin = stroke.xMin;
		this.yMin = stroke.yMin;
		this.xMax = stroke.xMax;
		this.yMax = stroke.yMax;
		this.width = stroke.width;
		this.height = stroke.height;

	}

	public List<Double> getFeatureVector(int feature_half_length){

		Log.i(LOG,"Getting Feature Vector");

		resampleCurve(initialInterpolationSize);
		curveLengthBasedNormalization();
		smoothen();
		resampleCurve(feature_half_length);
		unitSquareNormalize();

		List<Double> featureVector = new ArrayList<Double>(2*feature_half_length);

		for(int i=0;i<feature_half_length;i++){
			featureVector.add(xCoordinates[i]);
		}	

		for(int i=0;i<feature_half_length;i++){
			featureVector.add(yCoordinates[i]);
		}

		return featureVector;

	}

	private void unitSquareNormalize()
	{
		/*
		Stroke (strokex,strokey) is translated and zoomed to fit inside the unit square
		 */

		recomputeBounds();

		if(width==0)
			width = 1;

		if(height==0)
			height = 1;


		for(int i=0;i<xCoordinates.length;i++)
		{
			xCoordinates[i] = (xCoordinates[i] - xMin)/width;
			yCoordinates[i]= (yCoordinates[i] - yMin)/height;
		}

	}

	private void resampleCurve(int size)
	{
		/*
		Interpolates the matrix y and returns a new matrix with number_of_rows=size
		 */

		try{

			double[] x = new double[((xCoordinates.length))];
			double step = (double)(size - 1) / (double)(xCoordinates.length-1);

			for(int i=0;i<(xCoordinates.length);i++){
				x[i] = (1 + (i*step));
				//cout<<x(i+1)<<" ";
			}
			//cout<<endl;
			double[] xi = new double[size];
			for(int i=0;i<(size);i++){
				xi[i] = i+1;
			}

			//cout<<x(1,1)<<" "<<xi(1,1)<<" "<<x(x.nrows,1)<<" "<<xi(xi.nrows,1)<<endl;
			interpolateX(x,xi);


			double[] y = new double[((yCoordinates.length))];
			step = (double)(size - 1) / (double)(yCoordinates.length-1);

			for(int i=0;i<(yCoordinates.length);i++){
				y[i] = (1 + (i*step));
				//cout<<x(i+1)<<" ";
			}

			double[] yi = new double[size];
			for(int i=0;i<(size);i++){
				yi[i] = i+1;
			}

			interpolateY(y,yi);


		}
		catch(Exception e)
		{
			Log.e(LOG,"Exception in interpolateForSVM : "+e);
		}
	}

	private void interpolateX(double[] x, double[] xi){

		/*
		Interpolation of x to (xCoords,yCoords) on xi scale
		 */

		List<Double> yi_dummy = new ArrayList<Double>();

		int j,l=1,m;
		double a,b,slp,cons;
		int prev_j=1;
		//cout<<size(1)<<"   " <<size1(1)<<endl;
		try{

			for(m=1;m<=xi.length;m++) //xi size
			{
				for(j=1;j<x.length;j++) //x size
				{

					if((xi[m-1]>=x[j-1]) && (xi[m-1]<=x[j+l-1]))
					{
						//cout<<"there"<<endl;
						a=x[j-1];
						b=x[j+l-1];

						//y=slp*x+cons between a and b
						slp = (xCoordinates[j+1-1]-xCoordinates[j-1] ) / (b-a);
						cons = xCoordinates[j-1] - (slp * a);

						yi_dummy.add((slp*xi[m-1])+cons);

						prev_j = j;
						//cout<<c1*y(j)+c2*y(j+l)<<" ";

						break;

					}


				}

				if(yi_dummy.size()!=m){
					//cout<<"Concern!"<<endl;
					//cout<<m<<" "<<j<<" "<<" "<<(size1(1)-1)<<" "<<(xi(m)>=x(size1(1)-1))<<" "<<(xi(m)<=x(size1(1)))<<" "<<xi(m)<<" "<<(x(size1(1)-1))<<" "<<(x(size1(1)));
					//cout<<endl;
					j=prev_j;
					yi_dummy.add(xCoordinates[j+1-1]);
				}
			}



			xCoordinates = new double[(yi_dummy.size())];

			for(int i = 0;i<yi_dummy.size();i++)
			{
				xCoordinates[i] = yi_dummy.get(i);
			}

		}
		catch(Exception e)
		{
			Log.e(LOG,"Exception in interpolateForSVM : "+e);
		}


	}

	private void interpolateY(double[] x, double[] xi){

		/*
		Interpolation of x to (xCoords,yCoords) on xi scale
		 */

		List<Double> yi_dummy = new ArrayList<Double>();

		int j,l=1,m;
		double a,b,slp,cons;
		int prev_j=1;
		//cout<<size(1)<<"   " <<size1(1)<<endl;
		try{

			for(m=1;m<=xi.length;m++) //xi size
			{
				for(j=1;j<x.length;j++) //x size
				{

					if((xi[m-1]>=x[j-1]) && (xi[m-1]<=x[j+l-1]))
					{
						//cout<<"there"<<endl;
						a=x[j-1];
						b=x[j+l-1];

						//y=slp*x+cons between a and b
						slp = (yCoordinates[j+1-1]-yCoordinates[j-1] ) / (b-a);
						cons = yCoordinates[j-1] - (slp * a);

						yi_dummy.add((slp*xi[m-1])+cons);

						prev_j = j;
						//cout<<c1*y(j)+c2*y(j+l)<<" ";

						break;

					}


				}

				if(yi_dummy.size()!=m){
					//cout<<"Concern!"<<endl;
					//cout<<m<<" "<<j<<" "<<" "<<(size1(1)-1)<<" "<<(xi(m)>=x(size1(1)-1))<<" "<<(xi(m)<=x(size1(1)))<<" "<<xi(m)<<" "<<(x(size1(1)-1))<<" "<<(x(size1(1)));
					//cout<<endl;
					j=prev_j;
					yi_dummy.add(yCoordinates[j+1-1]);
				}
			}



			yCoordinates = new double[(yi_dummy.size())];

			for(int i = 0;i<yi_dummy.size();i++)
			{
				yCoordinates[i] = yi_dummy.get(i);
			}

		}
		catch(Exception e)
		{
			Log.e(LOG,"Exception in interpolateForSVM : "+e);
		}


	}

	private double[] findPoint(double left_xcoord,double right_xcoord,double left_ycoord, double right_ycoord,double pt_xcoord, double pt_ycoord, double distance){


		int precision = 100; //higher for more precision
		double foundflag = 0; //return value 1
		double xcoord = -1; //return value 2
		double ycoord = -1; //return 3

		double current_distance;

		if((right_xcoord - left_xcoord) == 0){

			//linearly spaced points y between left_ycoord and right_ycoord
			double step = (right_ycoord - left_ycoord) / (precision-1);

			double[] y = new double[precision];

			for(int i=0;i<precision;i++){//100 points
				y[i] = (left_ycoord + (i*step));
			}
			double[] distance_diff = new double[y.length];

			for(int i=0;i<y.length;i++){

				current_distance = Math.sqrt((Math.pow(y[i] - pt_ycoord,2)) + (Math.pow(pt_xcoord-right_xcoord,2)));
				distance_diff[i] = current_distance - distance;

				if((i>0) && ((distance_diff[i] * distance_diff[i-1])<=0) ){
					foundflag = 1;
					xcoord = right_xcoord;
					ycoord = y[i];
					break;
				}

			}

		}

		else{
			//equation of line y=mx+c
			double m = (right_ycoord - left_ycoord)/(right_xcoord - left_xcoord);
			double c = right_ycoord - (m*right_xcoord);

			//linearly spaced points y between left_ycoord and right_ycoord
			double step = (right_xcoord - left_xcoord) / (precision-1);

			double[] x = new double[precision];
			double[] y = new double[precision];


			double temp_x;
			for(int i=0;i<precision;i++){//100 points
				temp_x = left_xcoord + (i*step);
				x[i] = (temp_x);
				y[i] = ((m*temp_x) + c);
			}

			double[] distance_diff = new double[(x.length)];

			for(int i=0;i<x.length;i++){

				current_distance = Math.sqrt((Math.pow(y[i] - pt_ycoord,2)) + (Math.pow(x[i]-pt_xcoord,2)));
				distance_diff[i] = current_distance - distance;

				if((i>0) && ((distance_diff[i] * distance_diff[i-1])<=0) ){
					foundflag = 1;
					xcoord = x[i];
					ycoord = y[i];
					break;
				}

			}

		}

		double[] return_vector = new double[3];
		return_vector[0] = foundflag;
		return_vector[1] = xcoord;
		return_vector[2] = ycoord;

		return return_vector;
	}

	private void curveLengthBasedNormalization(){

		double diffx,diffy,diffxsqr,diffysqr,sumxy,xysqrt;
		double curve_len = 0;
		int number_points = xCoordinates.length;

		for(int i = 1;i<number_points;i++)
		{
			diffx = xCoordinates[i] - xCoordinates[i-1];
			diffxsqr = diffx*diffx;
			diffy = yCoordinates[i] - yCoordinates[i-1];
			diffysqr = diffy*diffy;
			sumxy = diffxsqr + diffysqr;
			xysqrt = Math.sqrt(sumxy);
			curve_len = curve_len + xysqrt;
		}

		double required_distance = curve_len/(number_points-1);

		List<Double> norm_xpoints = new ArrayList<Double>();
		List<Double> norm_ypoints = new ArrayList<Double>();
		norm_xpoints.add(xCoordinates[0]);
		norm_ypoints.add(yCoordinates[0]);

		double pt_xcoord = xCoordinates[0],pt_ycoord = yCoordinates[0],xcoord=0,ycoord=0;

		int prev_pt_index=1,end_flag=0,found_flag,point_index,itr;

		double[] found_point;

		for(int base_index=1;base_index<=(number_points-1);base_index++){
			found_flag=0;
			point_index = prev_pt_index;
			itr = 1;

			while(found_flag!=1){

				if((point_index+1) > (number_points)){
					end_flag = 1;
					break;
				}

				if(itr==1){
					//find point  xcoord ycoord found_flag
					found_point = findPoint(pt_xcoord,xCoordinates[point_index-1],pt_ycoord,
							yCoordinates[point_index-1],pt_xcoord, pt_ycoord, required_distance);
					found_flag= (found_point[0]==1) ? 1 :0;
					xcoord = found_point[1];
					ycoord = found_point[2];
				}

				else{
					found_point = findPoint(xCoordinates[point_index-1],xCoordinates[point_index],yCoordinates[point_index-1],
							yCoordinates[point_index],pt_xcoord, pt_ycoord, required_distance);
					point_index = point_index +1;
					found_flag= (found_point[0]==1) ? 1 :0;
					xcoord = found_point[1];
					ycoord = found_point[2];
				}

				itr++;

			}

			if(end_flag==1){
				break;
			}

			prev_pt_index = point_index ;
			pt_xcoord = xcoord;
			pt_ycoord = ycoord;

			norm_xpoints.add(xcoord);
			norm_ypoints.add(ycoord);

		}

		xCoordinates = new double[norm_xpoints.size()];
		yCoordinates = new double[norm_ypoints.size()];

		for(int i=0;i<norm_xpoints.size();i++){
			xCoordinates[i] = norm_xpoints.get(i);
			yCoordinates[i] = norm_ypoints.get(i);
		}

		norm_xpoints=null;
		norm_ypoints=null;


	}

	private void smoothen()
	{
		/*
	Low pass Filtering of stroke x1 and the smoothened stroke is returned
		 */
		try{
			//cout<<"Inside smooth_stroke"<<endl;
			int win = 15;int win2; double sigma = 4;
			int n = xCoordinates.length;
			win2=(win-1)/2;
			double pi=3.1428;
			double[] gau = new double[win];
			List<Double> gau1= new ArrayList<Double>();


			List<Double> xmod = new ArrayList<Double>();
			List<Double> ymod= new ArrayList<Double>();
			List<Double> xmod1= new ArrayList<Double>();
			List<Double> ymod1= new ArrayList<Double>();

			double sum=0;
			for (int i=1;i<=win;i++)
			{
				gau[i-1] = (1/(Math.sqrt(2*pi)*sigma)*Math.exp(((-1*(double)(i-win2)*(double)(i-win2))/(2*sigma*sigma))));
				sum=sum+gau[i-1];
			}

			for (int i=0;i<win;i++)
				gau1.add (gau[i]/sum);


			for (int i=0;i<(n+(2*win2)+win);i++)
			{
				if (i<win2)
				{
					xmod.add(xCoordinates[0]);
					ymod.add(yCoordinates[0]);
					xmod1.add(xmod.get(i));
					ymod1.add(ymod.get(i));

				}
				if (i>=win2 && i<(n+win2))
				{
					xmod.add(xCoordinates[i-win2]);
					ymod.add(yCoordinates[i-win2]);
					xmod1.add(xmod.get(i));
					ymod1.add(ymod.get(i));

				}
				if (i>=(n+win2) && i<(n+(2*win2)))
				{
					xmod.add(xCoordinates[n-1]);
					ymod.add(yCoordinates[n-1]);
					xmod1.add(xmod.get(i));
					ymod1.add(ymod.get(i));

				}

				if (i>=(n+(2*win2)) && i<(n+(2*win2)+win))
				{
					xmod1.add((double) 0);
					ymod1.add((double) 0);
				}

			}

			for (int i = gau1.size();i<(xmod.size()+win);i++)
				gau1.add(0.0);


			List<Double> pts_xn = new ArrayList<Double>(),
					pts_yn= new ArrayList<Double>();

			for (int k=0;k<(xmod.size()+win-1);k++)
			{
				int dummy1=xmod.size();
				double sumx=0,sumy=0;

				for(int j=0;j<dummy1;j++)
				{// cout<<k-j<<endl;
					if((k-j+1)>0)
					{
						sumx+=xmod1.get(j)*gau1.get((k-j));
						sumy+=ymod1.get(j)*gau1.get((k-j));
					}
				}
				if (k>=(win-1) && k<(xmod.size()))
				{
					pts_xn.add(sumx);
					pts_yn.add(sumy);
				}


			}

			xCoordinates = new double[pts_xn.size()];
			yCoordinates = new double[pts_yn.size()];

			for(int i=0;i<pts_xn.size();i++){
				xCoordinates[i] = pts_xn.get(i);
				yCoordinates[i] = pts_yn.get(i);
			}

			pts_xn=null;
			pts_yn=null;

		}

		catch(Exception e){
			Log.e(LOG,"Exception in smooth_stroke : "+e);
		}
	}//end of smoothening fn

	private void recomputeBounds(){

		xMin = 1000;
		yMin = 1000;
		xMax = -1000;
		yMax = -1000;

		double x,y;

		for(int i=0;i<xCoordinates.length;i++){
			x = xCoordinates[i];
			y = yCoordinates[i];

			if(x<xMin)
				xMin = x;
			if(y<yMin)
				yMin = y;
			if(x>xMax)
				xMax = x;
			if(y>yMax)
				yMax = y;

		}

		width = xMax - xMin;
		height = yMax - yMin;

	}

	public static Stroke interpolate(Stroke stroke, int size){

		float[] xCoords = new float[stroke.size()];
		float[] yCoords = new float[stroke.size()];

		for(int i=0;i<stroke.size();i++){
			xCoords[i] = stroke.getX(i);
			yCoords[i] = stroke.getY(i);
		}

		/*
		Interpolates the matrix y and returns a new matrix with number_of_rows=size
		 */

		try{

			float[] x = new float[((xCoords.length))];
			float step = (float)(size - 1) / (float)(xCoords.length-1);

			for(int i=0;i<(xCoords.length);i++){
				x[i] = (1 + (i*step));
				//cout<<x(i+1)<<" ";
			}
			//cout<<endl;
			float[] xi = new float[size];
			for(int i=0;i<(size);i++){
				xi[i] = i+1;
			}

			//cout<<x(1,1)<<" "<<xi(1,1)<<" "<<x(x.nrows,1)<<" "<<xi(xi.nrows,1)<<endl;
			xCoords = interp(x,xi,xCoords);


			float[] y = new float[((yCoords.length))];
			step = (float)(size - 1) / (float)(yCoords.length-1);

			for(int i=0;i<(yCoords.length);i++){
				y[i] = (1 + (i*step));
				//cout<<x(i+1)<<" ";
			}

			float[] yi = new float[size];
			for(int i=0;i<(size);i++){
				yi[i] = i+1;
			}

			yCoords = interp(y,yi,yCoords);

			stroke = new Stroke();
			
			for(int i=0;i<yCoords.length;i++){
				stroke.addPoint(xCoords[i],yCoords[i]);
			}
			
			return stroke;
			

		}
		catch(Exception e)
		{
			Log.e(LOG,"Exception in interpolateForSVM : "+e);
			return stroke;
		}



	}

	private static float[] interp(float[] x, float[] xi, float[] xcoords){

		/*
		Interpolation of x to (xCoords,yCoords) on xi scale
		 */

		List<Float> yi_dummy = new ArrayList<Float>();

		int j,l=1,m;
		float a,b,slp,cons;
		int prev_j=1;
		//cout<<size(1)<<"   " <<size1(1)<<endl;
		try{

			for(m=1;m<=xi.length;m++) //xi size
			{
				for(j=1;j<x.length;j++) //x size
				{

					if((xi[m-1]>=x[j-1]) && (xi[m-1]<=x[j+l-1]))
					{
						//cout<<"there"<<endl;
						a=x[j-1];
						b=x[j+l-1];

						//y=slp*x+cons between a and b
						slp = (xcoords[j]-xcoords[j-1] ) / (b-a);
						cons = xcoords[j-1] - (slp * a);

						yi_dummy.add((slp*xi[m-1])+cons);

						prev_j = j;
						//cout<<c1*y(j)+c2*y(j+l)<<" ";

						break;

					}


				}

				if(yi_dummy.size()!=m){
					//cout<<"Concern!"<<endl;
					//cout<<m<<" "<<j<<" "<<" "<<(size1(1)-1)<<" "<<(xi(m)>=x(size1(1)-1))<<" "<<(xi(m)<=x(size1(1)))<<" "<<xi(m)<<" "<<(x(size1(1)-1))<<" "<<(x(size1(1)));
					//cout<<endl;
					j=prev_j;
					yi_dummy.add(xcoords[j]);
				}
			}



			xcoords = new float[(yi_dummy.size())];

			for(int i = 0;i<yi_dummy.size();i++)
			{
				xcoords[i] = yi_dummy.get(i);
			}

			return xcoords;
			
		}
		catch(Exception e)
		{
			Log.e(LOG,"Exception in interpolateForSVM : "+e);
			return xcoords;
		}


	}
	
	public int getInitialInterpolationSize() {
		return initialInterpolationSize;
	}

	public void setInitialInterpolationSize(int initialInterpolationSize) {
		this.initialInterpolationSize = initialInterpolationSize;
	}

	//Main function for testing purposes
	public static void main(String[] args){
		Stroke stroke = new Stroke();
		String xString = "0.0547862797980000	0.0562669895590000	0.0562669895590000	0.0604911930860000	0.0644924566150000	0.0719774365430000	0.0863223671910000	0.107827551663000	0.133865892887000	0.157292842865000	0.177739918232000	0.199439331889000	0.236615285277000	0.291570156813000	0.347873598337000	0.397311329842000	0.431130021811000	0.454187333584000	0.457956403494000	0.450037240982000	0.430887013674000	0.401788562536000	0.373316347599000	0.351362347603000	0.337005198002000	0.333239346743000	0.333159804344000";
		String yString = "0.544680833817000	0.544680833817000	0.541009187698000	0.527182102203000	0.493867814541000	0.441851854324000	0.380104631186000	0.328320145607000	0.286400824785000	0.260289072990000	0.244931012392000	0.240815341473000	0.242718279362000	0.249794378877000	0.259582430124000	0.275919854641000	0.294449865818000	0.314258635044000	0.342926084995000	0.385499596596000	0.443292379379000	0.506973385811000	0.560908854008000	0.596312463284000	0.615673184395000	0.617206335068000	0.617206335068000";

		String[] splitX = xString.split("	");
		String[] splitY = yString.split("	");

		for(int i=0;i<splitX.length;i++){
			stroke.addPoint(Float.valueOf(splitX[i]),Float.valueOf(splitY[i]));
		}

		Preprocessor preprocessor = new Preprocessor(stroke);
		List<Double> featureVector = preprocessor.getFeatureVector(32);
		Log.i(LOG,"featureVector[0] "+featureVector.get(0));
	}


}

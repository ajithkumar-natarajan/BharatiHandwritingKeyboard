package com.iitm.stroke;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Stroke implements Comparable<Stroke>{

	private List<Point> stroke;
	
	public float xMin,yMin,xMax,yMax;
	public float width,height;

	public Stroke(){
		this.stroke = new ArrayList<Point>();
		xMin = 1000;
		yMin = 1000;
		xMax = -1000;
		yMax = -1000;
	}

	public Stroke(Stroke another){
		this.stroke = new ArrayList<Point>();
		
		this.xMin = 1000;
		this.yMin = 1000;
		this.xMax = -1000;
		this.yMax = -1000;
		
		for(Point p:another.stroke){
			this.addPoint(p.x,p.y);
		}
	}
	
	public int size(){
		return this.stroke.size();
	}

	public void addPoint(Point point){
		stroke.add(point);
		updateBounds(point.x,point.y);
	}

	public void addPoint(float x, float y){
		Point point = new Point(x,y);
		addPoint(point);

	}

	public void clear(){
		this.stroke.clear();
		xMin = 1000;
		yMin = 1000;
		xMax = -1000;
		yMax = -1000;

		width = xMax - xMin;
		height = yMax - yMin;
	}

	@Deprecated
	//Dangerous to use as point co-ords could get altered
	public Point getPoint(int i){
		return stroke.get(i);
	}
	
	public float getX(int i){
		return stroke.get(i).x;
	}
	
	public float getY(int i){
		return stroke.get(i).y;
	}
	
	private void updateBounds(float x,float y){
		
		if(x<xMin)
			xMin = x;
		if(y<yMin)
			yMin = y;
		if(x>xMax)
			xMax = x;
		if(y>yMax)
			yMax = y;
		
		width = xMax - xMin;
		height = yMax - yMin;

	}

	@Override
	public int compareTo(Stroke another) {
		
		// Default compare is xMin values
		// Ascending Order
		
		if(this.xMin == another.xMin)
			return 0;
		
		return (((this.xMin - another.xMin)>0) ? 1 : -1);
	}

	public static Comparator<Stroke> XMaxComparator = 
			new Comparator<Stroke>(){

		@Override
		public int compare(Stroke lhs, Stroke rhs) {
			
			if((lhs.xMax - rhs.xMax)>=0)
				return 0;
			
			// Ascending Order
			return (((lhs.xMax - rhs.xMax)>=0) ? 1 : -1);
		}

	};

}

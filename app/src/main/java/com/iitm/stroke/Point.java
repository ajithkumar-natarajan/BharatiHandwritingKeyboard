package com.iitm.stroke;

public class Point{
	
	public float x;
	public float y;
	
	Point(){
		
	}
	
	Point(float x,float y){
		this.x = x;
		this.y = y;
	}

	public static float distanceBetween(Point one, Point another){
		
		float distance = 0;
		
		distance = (float) (Math.sqrt( (Math.pow((one.x - another.x) , 2))
				+ (Math.pow((one.y - another.y) , 2) )));
		
		return distance;
		
	}
	
}

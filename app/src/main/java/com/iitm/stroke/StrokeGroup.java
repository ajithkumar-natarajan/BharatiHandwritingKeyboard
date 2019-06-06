package com.iitm.stroke;

import java.util.ArrayList;
import java.util.List;

public class StrokeGroup {

	public List<Stroke> character;
	
	public StrokeGroup(){
		character = new ArrayList<Stroke>();
	}
	
	public StrokeGroup(List<Stroke> character){
		this.character = character;
	}
	
	public void addStroke(Stroke stroke){
		character.add(stroke);	
	}

	public int size(){
		return character.size();
	}

	public Stroke get(int index){
		return character.get(index);
	}
}

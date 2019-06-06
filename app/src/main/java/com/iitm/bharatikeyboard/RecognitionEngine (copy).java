package com.iitm.bharatikeyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.iitm.bharatikeyboard.BharatiIME.LanguageRules;
import com.iitm.bharatikeyboard.BharatiIME.SVParams;
import com.iitm.stroke.Point;
import com.iitm.stroke.Stroke;
import com.iitm.stroke.StrokeGroup;
import com.iitm.utils.Preprocessor;
import com.iitm.utils.StraightLineChecker;

import android.util.Log;

public class RecognitionEngine {

	public static float PERCENT_SIZE_FOR_MAIN = (float) 0.33;

	public static float MAIN_THRESHOLD_X = (float) 0.1;  //threshold for identifying the first main
	public static float MAIN_THRESHOLD_Y = (float) 0.1;

	public static float DOT_THRESHOLD_NUMERAL = (float) 0.06;

	public static float INTERSECTION_THRESHOLD = (float) 0.04;
	
	public static float CHAR_TO_CHAR_GAP_MAIN = (float) 0.05; //used for smaller bharati main strokes 
	public static float CHAR_TO_CHAR_GAP_ENGLISH = (float) 0.05; // same for english main stroke space
	
	public static float MAIN_X_OVERLAP_THRESHOLD = (float) 0.25; //max allowed x overlap for 2 main strokes 
	public static float MAIN_Y_OVERLAP_THRESHOLD = (float) 0.66; //min allowed y overlap for 2 main strokes 

	private List<Stroke> strokeList;
	private List<StrokeGroup> strokeGroupList;

	private String LOG = "RecognitionEngine";

	public RecognitionEngine(List<Stroke> strokeList){
		Log.i(LOG,"Recognition Engine created for "+strokeList.size()+" strokes");

		this.strokeList = strokeList;
		strokeGroupList = new ArrayList<StrokeGroup>();
	}

	private void sortStrokesOnX(){
		//first stroke temporally is always assumed to be main stroke
		//rest of the strokes sorted on left most point locations

		List<Stroke> sortedStrokeListWithoutFirst = strokeList.subList(1,strokeList.size());
		Collections.sort(sortedStrokeListWithoutFirst);	

		for(int i=0;i<sortedStrokeListWithoutFirst.size();i++)
			strokeList.set(i+1,sortedStrokeListWithoutFirst.get(i));

	}

	@Deprecated
	private void splitWordIntoChars(){

		//TODO :: Algorithmic Modifications
		//Possible problem because of sorting by minimum values
		//when aux is slightly left of main left

		if((strokeList.isEmpty()) || (strokeList.size()<1))
			return;

		float mainYMax = strokeList.get(0).yMax;
		float mainXMax = strokeList.get(0).xMax;
		float mainYMin = strokeList.get(0).yMin;
		@SuppressWarnings("unused")
		float mainXmin = strokeList.get(0).xMin;
		float mainHeight = strokeList.get(0).height;

		//Bootstrapping Algorithm
		float xspace,currStrokeYMin,currStrokeYMax,currStrokeHeight;

		StrokeGroup strokeGroup = new StrokeGroup(); 
		strokeGroup.addStroke(strokeList.get(0));

		for(int i=1;i<strokeList.size();i++){

			xspace = strokeList.get(i).xMin - mainXMax;
			currStrokeYMin = strokeList.get(i).yMin;
			currStrokeYMax = strokeList.get(i).yMax;
			currStrokeHeight = strokeList.get(i).height;

			if( (xspace>0) && (currStrokeYMin<mainYMax) && (currStrokeYMax>mainYMin) 
					&& ((currStrokeHeight/mainHeight)>PERCENT_SIZE_FOR_MAIN) ){

				//current stroke is the next main stroke
				//form a new character
				strokeGroupList.add(strokeGroup);
				strokeGroup = new StrokeGroup();
				strokeGroup.addStroke(strokeList.get(i));

				//update the main stroke references
				mainYMax = strokeList.get(i).yMax;
				mainXMax = strokeList.get(i).xMax;
				mainYMin = strokeList.get(i).yMin;
				mainXmin = strokeList.get(i).xMin;
				mainHeight = strokeList.get(i).height;

			}

			else{
				//auxiliary stroke
				strokeGroup.addStroke(strokeList.get(i));
			}

		}

		strokeGroupList.add(strokeGroup);

	}

	private void splitWordIntoCharsImproved(){


		//If temporal first stroke is Non Dot it is taken as Main
		//Else Spatial First non Dot stroke is taken as Main
		//Bootstrapping starting with the first main to identify all the main strokes
		//Aux strokes assigned to main in the nearest distance

		//TODO : use more logical algo than simply assigning aux to nearest neighbour

		if((strokeList.isEmpty()) || (strokeList.size()<1))
			return;

		int mainStrokeIndex = 0;

		for(mainStrokeIndex=0;mainStrokeIndex<strokeList.size();mainStrokeIndex++){
			if((strokeList.get(mainStrokeIndex).height>MAIN_THRESHOLD_Y) ||
					(strokeList.get(mainStrokeIndex).width>MAIN_THRESHOLD_X))
				break;
		}

		if(mainStrokeIndex >= strokeList.size())
			return;
		
		StrokeGroup strokeGroup = new StrokeGroup();
		strokeGroup.addStroke(strokeList.get(mainStrokeIndex));
		strokeGroupList.add(strokeGroup);


		float mainYMax = strokeList.get(mainStrokeIndex).yMax;
		float mainXMax = strokeList.get(mainStrokeIndex).xMax;
		float mainYMin = strokeList.get(mainStrokeIndex).yMin;
		@SuppressWarnings("unused")
		float mainXMin = strokeList.get(mainStrokeIndex).xMin;
		float mainHeight = strokeList.get(mainStrokeIndex).height;
		float mainWidth = strokeList.get(mainStrokeIndex).width;

		float xspace,currStrokeYMin,currStrokeYMax,currStrokeHeight,currStrokeWidth,currStrokeXMax,currStrokeXMin;

		List<Integer> mainStrokeIndexList = new ArrayList<Integer>();
		mainStrokeIndexList.add(mainStrokeIndex);

		//Getting all the main strokes first
		for(int i=(mainStrokeIndex+1);i<strokeList.size();i++){
			
			xspace = strokeList.get(i).xMin - mainXMax;
			currStrokeYMin = strokeList.get(i).yMin;
			currStrokeYMax = strokeList.get(i).yMax;
			currStrokeXMin = strokeList.get(i).xMin;
			currStrokeXMax = strokeList.get(i).xMax;
			currStrokeHeight = strokeList.get(i).height;
			currStrokeWidth = strokeList.get(i).width;
		

			if((  ((( (Math.min(currStrokeYMax,mainYMax) - Math.max(currStrokeYMin,mainYMin)) )
					>MAIN_Y_OVERLAP_THRESHOLD * Math.min(currStrokeHeight,mainHeight)) && //condition for halant
					StraightLineChecker.isLine(strokeList.get(i)) && (currStrokeHeight>2*currStrokeWidth)) ||


					((((currStrokeYMin<mainYMax) && (currStrokeYMax>mainYMin) 
					&& ( (((currStrokeHeight/mainHeight)>PERCENT_SIZE_FOR_MAIN))) &&
					( (currStrokeXMin>mainXMax)||( Math.abs(Math.min(currStrokeXMax,mainXMax) - Math.max(currStrokeXMin,mainXMin)) )
							<MAIN_X_OVERLAP_THRESHOLD * Math.min(currStrokeWidth,mainWidth) ) ))) //for the aux strokes looking at x overlap 
							
					//removed the previous halant checker from here
							
					)){ 

					
				if((xspace<0) && isStrokeY(strokeList.get(mainStrokeIndex),strokeList.get(i))){
					continue;
				}

				mainStrokeIndexList.add(i);

				strokeGroup = new StrokeGroup();
				strokeGroup.addStroke(strokeList.get(i));
				strokeGroupList.add(strokeGroup);

				//update the main stroke references
				mainYMax = strokeList.get(i).yMax;
				mainXMax = strokeList.get(i).xMax;
				mainYMin = strokeList.get(i).yMin;
				mainXMin = strokeList.get(i).xMin;
				mainHeight = strokeList.get(i).height;

			}

		}


		//attaching the auxiliary to nearest mainstorokes
		int currentMainStrokeIndex = 0;
		int nextMainStrokeIndex = 0;

		for(int i=mainStrokeIndex;i<strokeList.size();i++){ //ignoring all strokes before first main stroke

			if(mainStrokeIndexList.contains(i)){
				currentMainStrokeIndex = i;

				if(mainStrokeIndexList.indexOf(currentMainStrokeIndex) + 1 >= mainStrokeIndexList.size())
					nextMainStrokeIndex = currentMainStrokeIndex;
				else
					nextMainStrokeIndex = mainStrokeIndexList.get((mainStrokeIndexList.indexOf(currentMainStrokeIndex) + 1));

				continue;
			}


			//overlapping with currentMainStrokeIndex
			if(strokeList.get(i).xMin<strokeList.get(currentMainStrokeIndex).xMax){ 

				strokeGroupList.get(mainStrokeIndexList.indexOf(currentMainStrokeIndex))
				.addStroke(strokeList.get(i));

			}

			//overlapping with only nextMainStroke
			else if(strokeList.get(i).xMax>strokeList.get(nextMainStrokeIndex).xMin){ 

				strokeGroupList.get(mainStrokeIndexList.indexOf(nextMainStrokeIndex))
				.addStroke(strokeList.get(i));

			}

			//in between currentMainStrokeIndex and nextMainStroke
			else{
				//Decision based on closeness
				if( (strokeList.get(i).xMin - strokeList.get(currentMainStrokeIndex).xMax) >
				( strokeList.get(nextMainStrokeIndex).xMin) - strokeList.get(i).xMax){
					//closer to nextMainStroke
					strokeGroupList.get(mainStrokeIndexList.indexOf(nextMainStrokeIndex))
					.addStroke(strokeList.get(i));

				}

				else{
					//closer to currentMainStroke
					strokeGroupList.get(mainStrokeIndexList.indexOf(currentMainStrokeIndex))
					.addStroke(strokeList.get(i));
				}


			}

		}

	}

	private void splitWordsEnglish(){

		//Difference from splitWordsIntoCharsImproved : In bootstrapping Main stroke 
		//and sorting (Strokes aren't sortd on space)

		//TODO : use more logical algo than simply assigning aux to nearest neighbour

		if((strokeList.isEmpty()) || (strokeList.size()<1))
			return;

		int mainStrokeIndex = 0;

		for(mainStrokeIndex=0;mainStrokeIndex<strokeList.size();mainStrokeIndex++){
			if((strokeList.get(mainStrokeIndex).height>MAIN_THRESHOLD_Y) ||
					(strokeList.get(mainStrokeIndex).width>MAIN_THRESHOLD_X))
				break;
		}

		StrokeGroup strokeGroup = new StrokeGroup();
		strokeGroup.addStroke(strokeList.get(mainStrokeIndex));
		strokeGroupList.add(strokeGroup);


		float mainYMax = strokeList.get(mainStrokeIndex).yMax;
		float mainXMax = strokeList.get(mainStrokeIndex).xMax;
		float mainYMin = strokeList.get(mainStrokeIndex).yMin;
		@SuppressWarnings("unused")
		float mainXmin = strokeList.get(mainStrokeIndex).xMin;
		float mainHeight = strokeList.get(mainStrokeIndex).height;

		float xspace,currStrokeYMin,currStrokeYMax,currStrokeHeight;

		List<Integer> mainStrokeIndexList = new ArrayList<Integer>();
		mainStrokeIndexList.add(mainStrokeIndex);

		//Getting all the main strokes first
		for(int i=(mainStrokeIndex+1);i<strokeList.size();i++){
			xspace = strokeList.get(i).xMin - mainXMax;
			currStrokeYMin = strokeList.get(i).yMin;
			currStrokeYMax = strokeList.get(i).yMax;
			currStrokeHeight = strokeList.get(i).height;

			//removed xspace checking to be more inclusive

			if( (currStrokeYMin<mainYMax) && (currStrokeYMax>mainYMin) && (i-1>=0) 
					&& (!isIntersecting(strokeList.get(i),strokeList.get(i-1)))
					&& (!isIntersecting(strokeList.get(i),strokeList.get(mainStrokeIndexList.get(mainStrokeIndexList.size()-1))))//&&((xspace>CHAR_TO_CHAR_GAP_ENGLISH)) 
							&& ((currStrokeHeight/mainHeight)>(PERCENT_SIZE_FOR_MAIN/2)) ){

				Log.e("Dev","Inside main stroke identifting; index : "+i + " xspace "+xspace);
				
				mainStrokeIndexList.add(i);

				strokeGroup = new StrokeGroup();
				strokeGroup.addStroke(strokeList.get(i));
				strokeGroupList.add(strokeGroup);

				//update the main stroke references
				mainYMax = strokeList.get(i).yMax;
				mainXMax = strokeList.get(i).xMax;
				mainYMin = strokeList.get(i).yMin;
				mainXmin = strokeList.get(i).xMin;
				mainHeight = strokeList.get(i).height;

			}

		}


		//attaching the auxiliary to nearest mainstorokes
		int currentMainStrokeIndex = 0;
		int nextMainStrokeIndex = 0;

		for(int i=mainStrokeIndex;i<strokeList.size();i++){ //ignoring all strokes before first main stroke

			if(mainStrokeIndexList.contains(i)){
				currentMainStrokeIndex = i;

				if(mainStrokeIndexList.indexOf(currentMainStrokeIndex) + 1 >= mainStrokeIndexList.size())
					nextMainStrokeIndex = currentMainStrokeIndex;
				else
					nextMainStrokeIndex = mainStrokeIndexList.get((mainStrokeIndexList.indexOf(currentMainStrokeIndex) + 1));

				continue;
			}

			//xMin(aux) > xMin(currentMainStrokeIndex) and xMin(aux) < xMin(nextMainStrokeIndex)

			//overlapping with currentMainStrokeIndex
			if((strokeList.get(i).xMin<strokeList.get(currentMainStrokeIndex).xMax) && 
					(strokeList.get(i).xMin>strokeList.get(currentMainStrokeIndex).xMin) ){ 

				strokeGroupList.get(mainStrokeIndexList.indexOf(currentMainStrokeIndex))
				.addStroke(strokeList.get(i));

			}

			//overlapping with only nextMainStroke
			else if((strokeList.get(i).xMax>strokeList.get(nextMainStrokeIndex).xMin) &&
					(strokeList.get(i).xMin<strokeList.get(nextMainStrokeIndex).xMin)){ 

				strokeGroupList.get(mainStrokeIndexList.indexOf(nextMainStrokeIndex))
				.addStroke(strokeList.get(i));

			}

			//in between currentMainStrokeIndex and nextMainStroke
			else{
				//Decision based on closeness
				if( (strokeList.get(i).xMin - strokeList.get(currentMainStrokeIndex).xMax) >
				( strokeList.get(nextMainStrokeIndex).xMin) - strokeList.get(i).xMax){
					//closer to nextMainStroke
					strokeGroupList.get(mainStrokeIndexList.indexOf(nextMainStrokeIndex))
					.addStroke(strokeList.get(i));

				}

				else{
					//closer to currentMainStroke
					strokeGroupList.get(mainStrokeIndexList.indexOf(currentMainStrokeIndex))
					.addStroke(strokeList.get(i));
				}


			}

		}

	}

	@SuppressWarnings("deprecation")
	private boolean isIntersecting(Stroke oneStroke, Stroke twoStroke){

		//don't bother when no overlap

		boolean intersectingStrokes = false;


		for(int oneIndex=0; oneIndex<oneStroke.size();oneIndex++){

			for(int twoIndex=0; twoIndex<twoStroke.size();twoIndex++){

				if(Point.distanceBetween(oneStroke.getPoint(oneIndex), twoStroke.getPoint(twoIndex)) < INTERSECTION_THRESHOLD ){
					intersectingStrokes = true;
					break;
				}

			}

		}
		
		Log.e("Dev","isIntersecting return is "+intersectingStrokes);

		return intersectingStrokes;
	}

	private boolean isStrokeY(Stroke oneStroke, Stroke twoStroke){

		//to avoid confusion over Y in main stroke identification

		if(StraightLineChecker.isLine(oneStroke) && StraightLineChecker.isLine(twoStroke))
			return true;

		return false;
	}

	public String recognizeBharatiWord(SVParams mainSVParams,SVParams topSVParams,LanguageRules languageRules){

		String wordLabel = "";

		sortStrokesOnX();
		splitWordIntoCharsImproved();

		String logString = ("Sorting and Splitting done. #stroke-groups : "+strokeGroupList.size()+" with sizes ");

		for(int sgInd =0;sgInd<strokeGroupList.size();sgInd++)
			logString += strokeGroupList.get(sgInd).size() + " ";

		Log.i(LOG,logString);

		for(StrokeGroup sg : strokeGroupList){ //for every character in word

			Stroke mainStroke = sg.get(0); //First stroke is main stroke

			List<String> output_char_consonant = new ArrayList<String>();
			List<String> output_char_vowel = new ArrayList<String>();

			String exceptionalStroke = "";
			int y_base_flag= 0;
			int bottom_hcount = 0;
			int base_char_flag = 0;


		boolean finishedMainStroke = false;

			for(int i=0;i<sg.size();i++){ //for every stroke in character

				Stroke currentStroke = sg.get(i);

				//Handling dots

				if((currentStroke.xMin> ((mainStroke.xMin+mainStroke.xMax)/2))&&(currentStroke.xMin< mainStroke.xMax + ((mainStroke.xMin+mainStroke.xMax)/3)) && (currentStroke.yMax <mainStroke.yMin) //position
						&& (currentStroke.height< (0.33*mainStroke.height)) && ((currentStroke.width)< (0.33*(mainStroke.width))) //size < 1/3 main stroke
						){
					Log.i(LOG,"Bottom Right Dot not removed");
					output_char_consonant.add("bottom_right_dot");
					continue;
				}



				//Main stroke recognition
				if(!finishedMainStroke){

					mainStroke = new Stroke(currentStroke);

					finishedMainStroke = true;

					Log.i(LOG,"Main stroke Recogn.");


						Preprocessor preprocessor = new Preprocessor(currentStroke);
						List<Double> featureVector = preprocessor.getFeatureVector(32);
						String svm_output = mainSVParams.recognizeFeatureVector(featureVector);

						if(svm_output.equals("circle_excep")){
							//exceptionalStroke += "main_circle";
							base_char_flag = 1;
							if(sg.size()>=2){
								Stroke firstStroke = sg.get(0);
								Stroke secondStroke = sg.get(1);
								if(firstStroke.yMin<secondStroke.yMin
										&& secondStroke.yMax < firstStroke.yMax)
								{
									output_char_consonant.add("nga_base");
								}
							}
							else{
								exceptionalStroke += "main_circle";
							}
						}

						if(svm_output.equals("cha_base")){
							//output_char_consonant.add("cha_base");
							base_char_flag = 2;
							if(sg.size()>=2){
								Stroke firstStroke = sg.get(0);
								Stroke secondStroke = sg.get(1);
								if(firstStroke.yMin < secondStroke.yMin
										&& secondStroke.yMax < firstStroke.yMax){
									output_char_consonant.add("nja_base");
								}
							}
							else
								output_char_consonant.add("cha_base");
						}

						if(svm_output.equals("vowel_base")){
							output_char_vowel.add(svm_output.trim());
							continue;
						}


						else if(languageRules.isExceptionalStroke(svm_output)){
							exceptionalStroke += svm_output.trim();
							break;
						}

						else{
							output_char_consonant.add(svm_output.trim());
							continue;
						}
				}



				//Auxiliary Stroke Recognition
				else{

					Log.i(LOG,"Aux stroke Recogn.");


					if(y_base_flag==1){
						exceptionalStroke="";
						output_char_consonant.add("y_base");
						y_base_flag = 2;
						continue;
					}

					else if(exceptionalStroke.equals("main_circle")){
						exceptionalStroke = "main_circle+";
						break;
					}



					//Top Stroke
					if((currentStroke.yMin+currentStroke.yMax)/2 > (mainStroke.yMin+mainStroke.yMax)/2){

						Log.i(LOG,"INSIDE Top Stroke");

						//Straight Line case
						if((StraightLineChecker.isLine(currentStroke))){

							Log.i(LOG,"Recognized as Line");

							//checking slope
							if( (Math.abs(currentStroke.getY(currentStroke.size()-1) - currentStroke.getY(0)) > 0.22 * Math.abs(currentStroke.getX(currentStroke.size()-1) - currentStroke.getX(0)))
									&& ((currentStroke.getY(currentStroke.size()-1) - currentStroke.getY(0)) * (currentStroke.getX(currentStroke.size()-1)  - currentStroke.getX(0))< 0) )
								output_char_vowel.add("top_slash");
							else if(currentStroke.width > currentStroke.height)
								output_char_vowel.add("top_hbar");
							else
								output_char_vowel.add("top_vbar");
						}
						//checking for halant
						else if(!StraightLineChecker.isLine(currentStroke) && currentStroke.width == currentStroke.height){
							exceptionalStroke += "main_halant";
							Log.i(LOG,"recognized halant");
						}

						else{
							Preprocessor preprocessor = new Preprocessor(currentStroke);
							List<Double> featureVector = preprocessor.getFeatureVector(32);
							String svm_output = topSVParams.recognizeFeatureVector(featureVector);

							output_char_vowel.add(svm_output.trim());
						}

					}

					//Bottom Stroke
					else{

						//Non Straight Line case
						if((currentStroke.size()>2) && (!StraightLineChecker.isLine(currentStroke))){
							output_char_consonant.add("bottom_right_dot");
						}



						//Straight Line Bottom Strokes
						else{
							double aspect_ratio;

							if(currentStroke.height == 0)
								aspect_ratio = 10000; //huge value
							else
								aspect_ratio = currentStroke.width / currentStroke.height ;

							//Horizontal Stroke
							if(aspect_ratio>0.3){
								if(bottom_hcount==0){
									output_char_consonant.add("bottom_hbar");
									bottom_hcount++;
								}

								else{
									output_char_consonant.remove(output_char_consonant.size() - 1);
									output_char_consonant.add("bottom_two_hbar");
								}
							}

							//Vertical Stroke
							else
								output_char_consonant.add("bottom_vbar");

						}
					}

				}

			}

			//Apply exceptional stroke rules
			wordLabel += applyBharatiRules(languageRules, output_char_consonant, output_char_vowel) + " ";

			if(!exceptionalStroke.equals("")){

				wordLabel += languageRules.getExceptionalStrokeLabel(exceptionalStroke) + " ";
			}

		}//end of character in loop over entire word

		return wordLabel;
	}

	public String recognizeNumeralWord(SVParams numeralSVParams, LanguageRules numeralRules){

		String wordLabel = "";

		sortStrokesOnX();
		splitWordIntoCharsImproved();

		String logString = ("Sorting and Splitting done. #stroke-groups : "+strokeGroupList.size()+" with sizes ");

		for(int sgInd =0;sgInd<strokeGroupList.size();sgInd++)
			logString += strokeGroupList.get(sgInd).size() + " ";

		Log.i(LOG,logString);


		for(StrokeGroup sg : strokeGroupList){ //for every digit in word

			int numberOfValidStrokes = 0;
			String strokeLabelString = "";

			for(int i=0;i<sg.size();i++){ //for every stroke in digit

				Stroke currentStroke = sg.get(i);

				if((currentStroke.size()<3)||(( (currentStroke.height < (DOT_THRESHOLD_NUMERAL))
						&& ((currentStroke.width)<(DOT_THRESHOLD_NUMERAL)) )) ){
					Log.i(LOG,"DOT removed");
					continue; //ignoring Dots!
				}

				numberOfValidStrokes++;

				if(numberOfValidStrokes>2) //no numeral has more than 2 strokes
					break;

				//Straight Line Stroke
				if((currentStroke.size()>2) && (StraightLineChecker.isLine(currentStroke))){
					double aspect_ratio;

					if(currentStroke.height == 0)
						aspect_ratio = 10000; //huge value
					else
						aspect_ratio = currentStroke.width / currentStroke.height ;

					if(aspect_ratio>1)
						strokeLabelString += "hbar ";

					else
						strokeLabelString += "vbar ";
				}

				//Non Straight Line Stroke
				else{
					Preprocessor preprocessor = new Preprocessor(currentStroke);
					preprocessor.setInitialInterpolationSize(90);
					List<Double> featureVector = preprocessor.getFeatureVector(26);
					String svm_output = numeralSVParams.recognizeFeatureVector(featureVector);

					strokeLabelString += svm_output;
					strokeLabelString +=" ";
				}

			}

			strokeLabelString = strokeLabelString.substring(0,strokeLabelString.length()-1);//final space remove

			wordLabel += numeralRules.getLabel(strokeLabelString);
		}

		return wordLabel;
	}

	public String recognizeEnglishWord(SVParams englishSVParams, LanguageRules englishRules){

		String wordLabel = "";

		//sortStrokesOnX();
		splitWordsEnglish();

		String logString = ("Sorting and Splitting done. #stroke-groups : "+strokeGroupList.size()+" with sizes ");

		for(int sgInd =0;sgInd<strokeGroupList.size();sgInd++)
			logString += strokeGroupList.get(sgInd).size() + " ";

		Log.i(LOG,logString);


		for(StrokeGroup sg : strokeGroupList){ //for every digit in word

			String strokeLabelString = "";
			boolean finishedMainStroke = false;
			Stroke mainStroke = null;

			for(int i=0;i<sg.size();i++){ //for every stroke in letter

				Stroke currentStroke = sg.get(i);

				//dot over i and j
				if(finishedMainStroke && (mainStroke!=null) && ((currentStroke.yMin >mainStroke.yMin + (mainStroke.height/2)) && (currentStroke.yMax <mainStroke.yMax + (mainStroke.height))
						&& (currentStroke.xMin >(mainStroke.xMin - mainStroke.width/2)) && (currentStroke.xMax <(mainStroke.xMax + mainStroke.width/2))//position
						&& (currentStroke.height< (0.33*mainStroke.height)) && ((currentStroke.width)< (0.33*(mainStroke.width))) //size < 1/3 main stroke
						) ){

					strokeLabelString += "dot ";
					continue;

				}

				if((currentStroke.size()<3)||(( (currentStroke.height < (DOT_THRESHOLD_NUMERAL))
						&& ((currentStroke.width)<(DOT_THRESHOLD_NUMERAL)) )) ){
					Log.i(LOG,"DOT removed");
					continue; //ignoring Dots!
				}

				if(!finishedMainStroke){
					mainStroke = new Stroke(currentStroke);
					finishedMainStroke = true;
				}


				//Straight Line Stroke
				if((currentStroke.size()>2) && (StraightLineChecker.isLine(currentStroke))){
					double aspect_ratio;

					if(currentStroke.height == 0)
						aspect_ratio = 10000; //huge value
					else
						aspect_ratio = currentStroke.width / currentStroke.height ;

					//checking slope
					//slant if between 30 deg and 60 deg
					if( (Math.abs(currentStroke.getY(currentStroke.size()-1) - currentStroke.getY(0)) > 0.57735 * Math.abs(currentStroke.getX(currentStroke.size()-1) - currentStroke.getX(0)))
							&& ((currentStroke.getY(currentStroke.size()-1) - currentStroke.getY(0)) * (currentStroke.getX(currentStroke.size()-1)  - currentStroke.getX(0))< 0)
							&& (Math.abs((currentStroke.getY(currentStroke.size()-1) - currentStroke.getY(0))) < 1.73205 * Math.abs((currentStroke.getX(currentStroke.size()-1)  - currentStroke.getX(0)))) )
						strokeLabelString += "back_slash ";

					else if( (Math.abs(currentStroke.getY(currentStroke.size()-1) - currentStroke.getY(0)) < 1.73205 * Math.abs(currentStroke.getX(currentStroke.size()-1) - currentStroke.getX(0)))
							&& ((currentStroke.getY(currentStroke.size()-1) - currentStroke.getY(0)) * (currentStroke.getX(currentStroke.size()-1)  - currentStroke.getX(0))> 0)
							&& (Math.abs((currentStroke.getY(currentStroke.size()-1) - currentStroke.getY(0))) > 0.57735 * Math.abs((currentStroke.getX(currentStroke.size()-1)  - currentStroke.getX(0))) ))
						strokeLabelString += "slash ";

					else if(aspect_ratio>1)
						strokeLabelString += "hbar ";

					else
						strokeLabelString += "vbar ";
				}

				//Non Straight Line Stroke
				else{
					Preprocessor preprocessor = new Preprocessor(currentStroke);
					List<Double> featureVector = preprocessor.getFeatureVector(32);
					String svm_output = englishSVParams.recognizeFeatureVector(featureVector);

					strokeLabelString += svm_output;
					strokeLabelString +=" ";
				}

			}

			strokeLabelString = strokeLabelString.substring(0,strokeLabelString.length()-1);//final space remove

			if(englishRules.getLabel(strokeLabelString).equals("")){
				String[] splittedString = strokeLabelString.split(" ");

				String reverseStrokeLabelString = ""; //checking the strokes in reverse order
				for(int splitIndex=splittedString.length-1;splitIndex>=0;splitIndex--){
					reverseStrokeLabelString += splittedString[splitIndex];
				}
				reverseStrokeLabelString = reverseStrokeLabelString.substring(0,reverseStrokeLabelString.length()-1);//final space remove

				if(englishRules.getLabel(reverseStrokeLabelString).equals("")){
					for(int splitIndex=0;splitIndex<splittedString.length;splitIndex++) //sort of like default labels
						wordLabel += englishRules.getLabel(splittedString[splitIndex]);
				}

				else
					wordLabel += englishRules.getLabel(reverseStrokeLabelString);

			}

			else
				wordLabel += englishRules.getLabel(strokeLabelString);

		}

		return wordLabel;

	}

	private String applyBharatiRules(LanguageRules languageRules, List<String> output_char_consonant,
			List<String> output_char_vowel){

		String charcacterLabel="";
		String vowelLabel="";
		String consonantLabel="";
		String output_char_consonant_string = "";
		String output_char_vowel_string = "";

		for(int i=0;i<output_char_consonant.size();i++){
			if(i!=0)
				output_char_consonant_string += " ";
			output_char_consonant_string += output_char_consonant.get(i);
		}

		for(int i=0;i<output_char_vowel.size();i++){
			if(i!=0)
				output_char_vowel_string += " ";
			output_char_vowel_string += output_char_vowel.get(i);
		}

		consonantLabel = languageRules.getLabel(output_char_consonant_string);
		vowelLabel = languageRules.getLabel(output_char_vowel_string);

		if((consonantLabel.equals("")) && (output_char_consonant.size()>0)){ //Default labels in case no match
			output_char_consonant_string = output_char_consonant.get(0);
			consonantLabel = languageRules.getLabel(output_char_consonant_string);
		}


		if((!consonantLabel.equals("")) && (!vowelLabel.equals("")))
			charcacterLabel = consonantLabel+" "+vowelLabel;
		else if(!consonantLabel.equals(""))
			charcacterLabel = consonantLabel;
		else
			charcacterLabel = vowelLabel;

		return charcacterLabel;

	}

}

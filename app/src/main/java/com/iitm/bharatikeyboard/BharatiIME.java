package com.iitm.bharatikeyboard;

//import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.iitm.stroke.Stroke;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Row;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;

public class BharatiIME extends InputMethodService
		implements OnKeyboardActionListener{

	private KeyboardView kv;
	private Keyboard keyboard;
	public LinearLayout keyboardContent;
	public DrawingCanvas drawingCanvas;
//	public Button recognizeButton, backSpaceButton,
//			spaceButton,numberModeSwitch,autoRecogSwitch,undoButton,commaButton,
//			dotButton,langButton,closePopupButton;
	public Button recognizeButton, backSpaceButton, spaceButton, numberModeSwitch, autoRecogSwitch, undoButton, commaButton, dotButton;
	public ImageButton manualButton,settingsButton;

	public int pcount;

	public Button newLineButton;
	public Spinner languageDropDown;

	public boolean isOnNumeralSwitch = false;

	public boolean autoRecogSwitchOn = true;
	boolean caps = false;

	public View view;
	public SVParams sVParamsBharatiMain,sVParamsBharatiAux,sVParamsDigit, sVParamsEnglish;
	public LanguageRules numeralRules, bharatiRules, englishRules;

	final private String LOG = "BharatiKeyboard";
	public String current = null;

//	public File mypath;

	private String uniqueId;

	private List<String> points;
	private List<List<String>> strokes;
//	private List<List<String>> strokes2;

	private List<MotionEvent> eventList;
	private List<MotionEvent> actionUpEvents;
	private int numberOfStrokes;
	private int numberOfStrokes2;

	private AutoRecognizeDaemon autoRecognizeDaemon;
	private volatile boolean autoRecognizeDaemonStop = false;
	private volatile int onTouchCountForDaemon = 0;
	private volatile int onTouchCountForDaemon2 = 0;

	private Handler handler;

	private int autoRecognizeTime = 800; //0.8 second

	List<Stroke> strokeList;
	List<Stroke> strokeList2;
	Stroke stroke;

	List<String> s;

	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

	@Override
	public void onStartInputView(EditorInfo info, boolean restarting) {
		Log.v(LOG,"onStartInputView ");
		handler = new Handler();

		autoRecogSwitch.setText("AUTO ON");
		autoRecogSwitchOn = true;

		autoRecognizeDaemonStop = false;
		onTouchCountForDaemon = 0;
		autoRecognizeDaemon = new AutoRecognizeDaemon();
		//autoRecognizeDaemon.setDaemon(true);
		autoRecognizeDaemon.start();
	}

	@Override
	public int onStartCommand (Intent intent, int flags, int startId){

		if (intent !=null && intent.getExtras()!=null)
		{
			String key = "TIME";
			int value = intent.getIntExtra(key,4);
			// float value = intent.getParcelableExtra(key);
			autoRecognizeTime = value*200;
		}

		if (autoRecognizeTime<800)
			autoRecognizeTime = 800;


		return START_STICKY;
	}

	@Override
	public View onCreateInputView() {
		Log.i(LOG,"inside onCreateInputView ");

		long startTime = System.currentTimeMillis();

		sVParamsBharatiAux = new SVParams("resource/SVParamsBharathi_auxv2.txt","resource");
		sVParamsBharatiMain = new SVParams("resource/SVParamsBharathiv2.txt","resource");
		sVParamsDigit = new SVParams("resource/SVParamsdigit.txt","resource");
		sVParamsEnglish = new SVParams("resource/SVParamsenglish.txt","resource");

		bharatiRules = new LanguageRules("resource/rulefiles/deva_rulelist.txt");
		numeralRules = new LanguageRules("resource/rulefiles/numRulelist.txt");
		englishRules = new LanguageRules("resource/rulefiles/english_rulelist.txt");

		long endTime = System.currentTimeMillis();

		Log.v(LOG, "sVParamsBharati ready "+sVParamsBharatiAux.isSV_ready());
		Log.v(LOG, "sVParamsBharatiMain ready "+sVParamsBharatiMain.isSV_ready());
		Log.v(LOG, "sVParamsDigit ready "+sVParamsDigit.isSV_ready());
		Log.v(LOG,"Time taken in ms : "+(endTime-startTime));

		//debugging disabled if using WebView to display paid for content or if using JavaScript interfaces
		//WebView.setWebContentsDebuggingEnabled(false);

		uniqueId = "masupial";
		current = uniqueId + ".csv";

		view = (LinearLayout) getLayoutInflater().inflate(R.layout.keyboard,null);
		//mContent = (LinearLayout) mView.findViewById(R.id.linearLayout2);

		keyboardContent = (LinearLayout) view.findViewById(R.id.linearLayout);
		Log.i(LOG,"mContent==null "+(keyboardContent==null));
		drawingCanvas = new DrawingCanvas(this, null);
		drawingCanvas.setBackgroundColor(Color.WHITE);
		Log.i(LOG,"mSignature==null "+(drawingCanvas==null));
		keyboardContent.addView(drawingCanvas, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		//clearButton = (Button)view.findViewById(R.id.clear);
		backSpaceButton = (Button)view.findViewById(R.id.backspace);
		recognizeButton = (Button)view.findViewById(R.id.recognize);
		spaceButton = (Button)view.findViewById(R.id.space);
		numberModeSwitch = (Button)view.findViewById(R.id.numeral_switch);
		autoRecogSwitch = (Button)view.findViewById(R.id.auto_recog_switch);
		newLineButton = (Button)view.findViewById(R.id.new_line);
		undoButton = (Button)view.findViewById(R.id.undo);

		commaButton = (Button)view.findViewById(R.id.comma);
		manualButton = (ImageButton)view.findViewById(R.id.manual);
		dotButton = (Button)view.findViewById(R.id.dot);
		settingsButton = (ImageButton)view.findViewById(R.id.settings);

		//langButton = (Button)view.findViewById(R.id.language);

		pcount = 0;

		//Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/NavBharati.tff");
		//settingsButton.setTypeface(tf);

		recognizeButton.setEnabled(false);

		languageDropDown = (Spinner) view.findViewById(R.id.scripts_spinner);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.script_choices, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		languageDropDown.setAdapter(adapter);
		languageDropDown.setSelection(1); //default is devanagiri

		points = new ArrayList<String>();
		stroke = new Stroke();

		strokes = new ArrayList<List<String>>();
		eventList = new ArrayList<MotionEvent>();

		strokeList = new ArrayList<Stroke>();
		numberOfStrokes=0;

		/*
		clearButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{

			}
		});
		*/

		backSpaceButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Log.v(LOG, "Previous Char Remove");
				drawingCanvas.clear();
				points = new ArrayList<String>();
				stroke = new Stroke();
				strokes = new ArrayList<List<String>>();
				strokeList = new ArrayList<Stroke>();
				InputConnection ic = getCurrentInputConnection();
				ic.deleteSurroundingText(1, 0);

				playClick(AudioManager.FX_KEYPRESS_DELETE);
				//mSignature.redrawPathForBackSpace();

				if(numberOfStrokes<=0)
					recognizeButton.setEnabled(false);

				//clearin everthing new routine!
				numberOfStrokes =0;
				recognizeButton.setEnabled(false);
				onTouchCountForDaemon=0;

			}
		});


		//TODO : Backspace Longpress

//		backSpaceButton.setOnTouchListener( new OnTouchListener(){
//
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
//				backSpaceButton.performClick();
//				return false;
//			}
//
//		}
//				);
//
		numberModeSwitch.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Log.v(LOG, "Numeral Switch Press");
				String display_string="";

				if(!isOnNumeralSwitch){ //need to shift to number mode
					//so alphabets will be displayed
					int unicode_shift = 0x0000,val;
					int language=languageDropDown.getSelectedItemPosition() + 1;
					switch(language){
						case 2 : unicode_shift = 0x0000; break;
						case 3 : unicode_shift = 0x0B80 - 0x0900;break; //tamil
						case 4 : unicode_shift = 0x0C00 - 0x0900;break; //telugu
						case 5 : unicode_shift = 0x0C80 - 0x0900;break; //kannada
						case 6 : unicode_shift = 0x0D00 - 0x0900;break; //malayalam
						case 7 : unicode_shift = 0x0980 - 0x0900;break; //bengali
						case 8 : unicode_shift = 0x0A00 - 0x0900;break; //gurmukhi
						case 9 : unicode_shift = 0x0A80 - 0x0900;break; //gujarati
						case 10 : unicode_shift = 0x0B00 - 0x0900;break; //oriya
					}

					val = Integer.parseInt("0905".trim(),16);
					char symbol = (char) (val+unicode_shift);
					display_string += Character.toString(symbol);

					//					val = Integer.parseInt("0906".trim(),16);
					//					symbol = (char) (val+unicode_shift);
					//					display_string += Character.toString(symbol);

				}

				else
					display_string = "123";

				isOnNumeralSwitch =!isOnNumeralSwitch; //toggle state
				numberModeSwitch.setText(display_string);
				playClick(AudioManager.FX_KEYPRESS_STANDARD);

			}
		});

		spaceButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{

				Log.v(LOG, "Space Bar");
				drawingCanvas.clear();
				points = new ArrayList<String>();
				stroke = new Stroke();

				strokes = new ArrayList<List<String>>();
				strokeList = new ArrayList<Stroke>();

				InputConnection ic = getCurrentInputConnection();
				ic.commitText(" ", 1);

				playClick(AudioManager.FX_KEYPRESS_SPACEBAR);
				//mSignature.redrawPathForBackSpace();

				if(numberOfStrokes<=0)
					recognizeButton.setEnabled(false);

				//clearin everthing new routine!
				numberOfStrokes =0;
				recognizeButton.setEnabled(false);
				onTouchCountForDaemon = 0;

			}
		});


		spaceButton.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {

				// TODO Auto-generated method stub
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showInputMethodPicker();

				/*
				Intent dialogIntent = new Intent(android.provider.Settings.ACTION_SYNC_SETTINGS);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(dialogIntent);*/

				return true;
			}
		});

		undoButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {


				if(!points.isEmpty())
				{
					strokes.add(points);
					strokeList.add(stroke);

					//onTouchCountForDaemon = onTouchCountForDaemon - points.size();
					points.clear();
					stroke.clear();
				}

				/*if(!strokes2.isEmpty())
				{
					Collections.copy(strokes,strokes2);
					Collections.copy(strokeList,strokeList2);
					Collections.copy(drawingCanvas.pathList,drawingCanvas.pathList2);
					drawingCanvas.invalidate();
				}
				else */
				if(!strokes.isEmpty())
				{
					//List<String> s;
					s = strokes.get(numberOfStrokes-1);
					onTouchCountForDaemon = onTouchCountForDaemon - s.size();

					strokes.remove(numberOfStrokes-1);
					strokeList.remove(numberOfStrokes-1);

					numberOfStrokes--;
					drawingCanvas.backTrack();



					/*
					// i is set eventList.size() - 2 to avoid last stroke ACTION_UP event
					// i is limited to 1 since the first event would anyway be ACTION_DOWN
					for (int i = (eventList.size() - 2);i > 0;i--)
					{
						if(eventList.get(i).getAction() == MotionEvent.ACTION_DOWN)
						{
							eventList = eventList.subList(0,i-1);
							break;
						}
					}

					numberOfStrokes--;

					//drawingCanvas.clear();
					drawingCanvas.redrawPathForBackSpace(); */
				}
			}
		});


		undoButton.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {

				Log.v(LOG, "Panel Cleared");

				drawingCanvas.clear();

				numberOfStrokes =0;
				points = new ArrayList<String>();
				stroke = new Stroke();
				strokes = new ArrayList<List<String>>();
				strokeList = new ArrayList<Stroke>();
				recognizeButton.setEnabled(false);
				eventList.clear();
				onTouchCountForDaemon = 0;

				return true;

			}
		});

		recognizeButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Log.v(LOG, "Panel Saved");
				try {
					onTouchCountForDaemon = 0;
					drawingCanvas.recogniseAction(view, "test_cases");

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				drawingCanvas.clear();
				points = new ArrayList<String>();
				stroke = new Stroke();

				//Collections.copy(strokes2,strokes);
				/*for(int i=0;i<strokes.size();i++)
				{
					strokes2.add(new ArrayList<String>(strokes.get(i)));
				}*/
				Log.v("CC","strokes2==strokes");
				strokes = new ArrayList<List<String>>();

				//Collections.copy(strokeList2,strokeList);
				strokeList = new ArrayList<Stroke>();

				//numberOfStrokes2 = numberOfStrokes;
				numberOfStrokes = 0;

				//onTouchCountForDaemon2 = onTouchCountForDaemon;
				onTouchCountForDaemon = 0;

				playClick(AudioManager.FX_KEYPRESS_RETURN);

			}
		});

		newLineButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Log.v(LOG, "New Line");
				drawingCanvas.clear();
				points = new ArrayList<String>();
				stroke = new Stroke();

				strokes = new ArrayList<List<String>>();
				strokeList = new ArrayList<Stroke>();

				InputConnection ic = getCurrentInputConnection();
				ic.commitText("\n", 1);

				//mSignature.redrawPathForBackSpace();

				if(numberOfStrokes<=0)
					recognizeButton.setEnabled(false);

				//clearin everthing new routine!
				numberOfStrokes =0;
				recognizeButton.setEnabled(false);
				onTouchCountForDaemon = 0;
			}
		});

		autoRecogSwitch.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if(autoRecogSwitchOn) {//showing Manual when pressedf

					autoRecognizeDaemonStop=true;
					autoRecogSwitch.setText("AUTO \nOFF");
				}

				else{//shwoing auto when pressed

					autoRecognizeDaemonStop = false;
					onTouchCountForDaemon = 0;
					autoRecognizeDaemon = new AutoRecognizeDaemon();
					autoRecognizeDaemon.start();
					autoRecogSwitch.setText("AUTO \nON");
				}

				autoRecogSwitchOn = !autoRecogSwitchOn;
				playClick(AudioManager.FX_KEYPRESS_STANDARD);
			}
		});

		commaButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Log.v(LOG, "comma punctuation");
				drawingCanvas.clear();
				points = new ArrayList<String>();
				stroke = new Stroke();

				strokes = new ArrayList<List<String>>();
				strokeList = new ArrayList<Stroke>();

				InputConnection ic = getCurrentInputConnection();
				ic.commitText(",", 1);

				//mSignature.redrawPathForBackSpace();

				if(numberOfStrokes<=0)
					recognizeButton.setEnabled(false);

				//clearin everthing new routine!
				numberOfStrokes =0;
				recognizeButton.setEnabled(false);
				onTouchCountForDaemon = 0;
			}
		});

		dotButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				String[] parr = {".",",","?","!","\"","\'","(",")",";",":"};

				Log.v(LOG, "punctuation");
				drawingCanvas.clear();
				points = new ArrayList<String>();
				stroke = new Stroke();

				strokes = new ArrayList<List<String>>();
				strokeList = new ArrayList<Stroke>();

				InputConnection ic = getCurrentInputConnection();
				ic.commitText(parr[pcount], 1);
				//pcount += 1;
				//pcount = pcount % 10;

				//mSignature.redrawPathForBackSpace();

				if(numberOfStrokes<=0)
					recognizeButton.setEnabled(false);

				//clearin everthing new routine!
				numberOfStrokes =0;
				recognizeButton.setEnabled(false);
				onTouchCountForDaemon = 0;
			}
		});


		manualButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Log.v(LOG, "Manual");

				int language=languageDropDown.getSelectedItemPosition() + 1;

				switch(language){
					case 2 :
						Intent de = new Intent(getApplicationContext(),DevanagariActivity.class);
						de.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(de);
						break; //devanagari

					case 3 :
						Intent ta = new Intent(getApplicationContext(),TamilActivity.class);
						ta.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(ta);
						break; //tamil
					case 4 :
						Intent te = new Intent(getApplicationContext(),TeluguActivity.class);
						te.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(te);
						break; //telugu
					case 5 :
						Intent ka = new Intent(getApplicationContext(),KannadaActivity.class);
						ka.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(ka);
						break; //kannada
					case 6 :
						Intent ma = new Intent(getApplicationContext(),MalayalamActivity.class);
						ma.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(ma);

						break; //malayalam
					case 7 :
						Intent be = new Intent(getApplicationContext(),BengaliActivity.class);
						be.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(be);
						break; //bengali
					case 8 :
						Intent gur = new Intent(getApplicationContext(),GurmukhiActivity.class);
						gur.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(gur);
						break; //gurmukhi
					case 9 :
						Intent gu = new Intent(getApplicationContext(),GujaratiActivity.class);
						gu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(gu);
						break; //gujarati
					case 10 :
						Intent od = new Intent(getApplicationContext(),OdiaActivity.class);
						od.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(od);
						break; //oriya
				}

			}
		});


		settingsButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Log.v(LOG, "Settings");

				Intent i = new Intent(BharatiIME.this,SettingsActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//i.putExtra ("RT",autoRecognizeTime);
				startActivity(i);

			}
		});

		/*
		langButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Log.v(LOG, "Choose language");


				Intent i = new Intent(BharatiIME.this,ChooseLanguage.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);


			}
		}); */


		return view;
	}

	/*
	public void manualOnClick() {
		Intent intent = new Intent(getApplicationContext(), helpActivity.class);
		startActivity(intent);
	}
	*/

	@Override
	public void onDestroy()
	{
		Log.e(LOG, "onDestory");

		sVParamsBharatiMain = null;
		sVParamsBharatiAux = null;
		sVParamsDigit = null;
		bharatiRules = null;

		super.onDestroy();
	}

	@Override
	public void onFinishInputView(boolean finishingInput){
		//origninal function code
		if (!finishingInput) {
			InputConnection ic = getCurrentInputConnection();
			if (ic != null) {
				ic.finishComposingText();
			}
		}

		//my modification
		Log.e(LOG, "onFinishInputView");
		autoRecognizeDaemonStop = true;

	}

	//imitating runonuithread from Activity
	private void runOnUiThread(Runnable runnable) {
		handler.post(runnable);
	}

	@Override
	public void onPress(int primaryCode) {
		// TODO Auto-generated method stub
		Log.e(LOG,"inside onPress ");

	}

	@Override
	public void onRelease(int primaryCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		InputConnection ic = getCurrentInputConnection();
		playClick(primaryCode);
		switch(primaryCode){
			case Keyboard.KEYCODE_DELETE :
				ic.deleteSurroundingText(1, 0);
				break;
			case Keyboard.KEYCODE_SHIFT:
				caps = !caps;
				keyboard.setShifted(caps);
				kv.invalidateAllKeys();
				break;
			case Keyboard.KEYCODE_DONE:
				ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
				break;
			default:
				char code = (char)primaryCode;
				if(Character.isLetter(code) && caps){
					code = Character.toUpperCase(code);
				}
				ic.commitText(String.valueOf(code),1);
		}

	}

	@Override
	public void onText(CharSequence text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeRight() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeUp() {
		// TODO Auto-generated method stub

	}

	private void playClick(int keyCode){
		AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
		switch(keyCode){
			case 32:
				am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
				break;
			case Keyboard.KEYCODE_DONE:
			case 10:
				am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
				break;
			case Keyboard.KEYCODE_DELETE:
				am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
				break;
			default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
		}
	}

	private PopupWindow popupWindow;

	private void initiatePopupWindow() {

	}


	public class AutoRecognizeDaemon extends Thread{
		public void run(){
			while(!autoRecognizeDaemonStop){
				try {
					int onTouchCount = onTouchCountForDaemon;

					Thread.sleep(autoRecognizeTime); //wait for 1 second and see if no input from user

					//					Log.v("AutoRecognizeDaemon","daemon thread params "
					//					+ "onTouchCount "+onTouchCount+" onTouchCountForDaemon "+onTouchCountForDaemon);
					//

					if((!autoRecognizeDaemonStop)&&(onTouchCountForDaemon>0) && (onTouchCount == onTouchCountForDaemon)){
						Log.v("AutoRecognizeDaemon","daemon thread about to performClick");
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								recognizeButton.setEnabled(false); //prevent user from calling again till output written
								recognizeButton.performClick();
							}
						});

					}


				} catch (Exception e) {
					Log.e("AutoRecognizeDaemon","Error in Daemon while calling performClick");
					Log.e("AutoRecognizeDaemon",e.getLocalizedMessage());
				}

				//Log.v("AutoRecognizeDaemon","daemon thread working");

			}
		}
	}

	public class DrawingCanvas extends View{
		private static final float STROKE_WIDTH = 8f;
		private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
		private Paint paint = new Paint();
		private Path path = new Path(); // Initially it was private
		private List<Path> pathList;
		private List<Path> pathList2;

		private float lastTouchX;
		private float lastTouchY;
		private float prevEventX;
		private float prevEventY;
		private final RectF dirtyRect = new RectF();

		public DrawingCanvas(Context context, AttributeSet attrs)
		{
			super(context, attrs);
			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setStrokeWidth(STROKE_WIDTH);
			prevEventX=-1;prevEventY=-1;
			pathList = new ArrayList<Path>();

			setWillNotDraw(false);

		}

		@TargetApi(Build.VERSION_CODES.GINGERBREAD) public void recogniseAction(View v, String fname) throws IOException
		{
			Log.e("Handwriting", "Entry: "+fname);

			try {
				if(!points.isEmpty())
				{        // Conditional points appending is necessitated due to points being appended in undo onclick()
					strokes.add(points);
					strokeList.add(stroke);
				}

				Log.i("Dev","Calling recognize for #strokes = "+strokeList.size());

				RecognitionEngine recognitionEngine = new RecognitionEngine(strokeList);

				if(isOnNumeralSwitch){
					displayRecognizedWord(recognitionEngine.recognizeNumeralWord(sVParamsDigit,numeralRules));
				}

				else{

					if(languageDropDown.getSelectedItemPosition() == 0)//english
						displayRecognizedWord(recognitionEngine.recognizeEnglishWord(sVParamsEnglish,englishRules));

					else
						processBharatiUnicodes(recognitionEngine.recognizeBharatiWord
								(sVParamsBharatiMain,sVParamsBharatiAux,bharatiRules));
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(LOG,"Exception in Recognise Action : "+e);
				e.printStackTrace();
			}
		}

		public void displayRecognizedWord(String outputString)
		{
			if(!outputString.equals("")){
				InputConnection ic = getCurrentInputConnection();
				ic.commitText(""+outputString+"", 1);  // can add blank space after every recognition if required
			}
		}

		public void processBharatiUnicodes(String output_unicode){

			int language=languageDropDown.getSelectedItemPosition() + 1;

			int unicode_shift=0x0000; //to get different Indian languages
			int val;

			String[] splitted_String = output_unicode.split(" ");
			String outputString = "";

			switch(language){
				case 2 : unicode_shift = 0x0000; break;
				case 3 : unicode_shift = 0x0B80 - 0x0900;break; //tamil
				case 4 : unicode_shift = 0x0C00 - 0x0900;break; //telugu
				case 5 : unicode_shift = 0x0C80 - 0x0900;break; //kannada
				case 6 : unicode_shift = 0x0D00 - 0x0900;break; //malayalam
				case 7 : unicode_shift = 0x0980 - 0x0900;break; //bengali
				case 8 : unicode_shift = 0x0A00 - 0x0900;break; //gurmukhi
				case 9 : unicode_shift = 0x0A80 - 0x0900;break; //gujarati
				case 10 : unicode_shift = 0x0B00 - 0x0900;break; //oriya
			}

			splitted_String = handleChillas(splitted_String);

			for(int split_ind=0;split_ind<splitted_String.length;split_ind++){
				Log.e(LOG, "splitted_String[split_ind] " +splitted_String[split_ind].trim());
				if(splitted_String[split_ind].trim().equals(""))
					continue;
				val = Integer.parseInt(splitted_String[split_ind].trim(),16);
				Log.e(LOG, "val " +val);
				char symbol = (char) (val+unicode_shift);
				Log.e(LOG, "symbol " +symbol);
				outputString += Character.toString(symbol);
			}

			Log.e(LOG, "output_string " +outputString);

			displayRecognizedWord(outputString);

		}

		private String[] handleChillas(String[] splitted_String){

			List<Integer> validUnicodeList = new ArrayList<Integer>();

			for(int split_ind=0;split_ind<splitted_String.length;split_ind++){

				if(splitted_String[split_ind].trim().equals(""))
					continue;

				validUnicodeList.add(split_ind);

				if(splitted_String[split_ind].trim().equals("097A")){ //chilla

					//if previous stroke chilla compatible stroke
					if(bharatiRules.getChillaStroke(splitted_String[validUnicodeList.get(Math.max(0,validUnicodeList.size()-2))]) != null){
						splitted_String[validUnicodeList.get(Math.max(0,validUnicodeList.size()-2))] =
								bharatiRules.getChillaStroke(splitted_String[validUnicodeList.get(Math.max(0,validUnicodeList.size()-2))]);
						splitted_String[split_ind] = "";
					}

					else
						splitted_String[split_ind] = "094D";//replace with halant

				}


			}

			return splitted_String;

		}

		public void clear()
		{
			//List<Path> p = new ArrayList<Path>();
			//Path pt=new Path();
			//pt.addCircle(20,25,5,Direction.CW);
			//pathList.add(pt);
			//pathList.add(path);
			//path.reset();
			//pt = pathList.get(0);
			//path.set(pathList.get(0));

			//path.reset();
			//path.addCircle(20,25,5,Direction.CW);
			//Path pt = pathList.get(0);
			//Log.i("pt","pt_isEmpty - " + pt.isEmpty());
			//path.addPath(pathList.get(0));

			//pathList.add(new Path(path));
			path.reset();

			//Collections.copy(pathList2,pathList);
			pathList.clear();

			invalidate();
			points.clear(); // clear the buffer
			stroke = null;
			prevEventX=-1;prevEventY=-1;
		}


		@Override
		protected void onDraw(Canvas canvas)
		{
			//Log.e("LOG","Inside onDraw with" +" draw_circle_flag "+draw_circle_flag+ " prevEventX "+prevEventX+" prevEventY "+prevEventY);
			//canvas.drawPath(path, paint);
			//		if(false)
			//			canvas.drawCircle(prevEventX,prevEventY,(float) 6f, paint);
			//		else
			/*
			if(path.isEmpty() && !pathList.isEmpty())
			{
				canvas.drawPath(pathList.get(pathList.size()-1), paint);
				pathList.remove(numberOfStrokes-1);
			}
			else
			{
				canvas.drawPath(path,paint);
			}
			*/

			canvas.drawPath(path, paint);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event)
		{
			onTouchCountForDaemon++;
			MotionEvent eventCopy = MotionEvent.obtain(event);
			eventList.add(eventCopy);
			//			Log.e("Backspace","Inside onTouchEvent with size of eventList "+eventList.size() +
			//					" and action type "+eventList.get(eventList.size()-1).getAction());

			float eventX = event.getX();
			float eventY = event.getY();
			//Log.e(LOG,"Screen Height "+String.valueOf(this.getHeight()));
			//format of output to textfile
			DecimalFormat decimalFormat = new DecimalFormat("0.000000000000");
			float firstCoordinate ;
			float secondCoordinate ;
			recognizeButton.setEnabled(true);

			switch (event.getAction())
			{
				case MotionEvent.ACTION_DOWN:


					if(points.isEmpty() && (pathList.size() != 0))
					{
						//pathList.remove(pathList.size()-1);
					}

					pathList.add(new Path(path));
					Log.e("ENQ","pathList size - " + pathList.size());


					prevEventX=-1;prevEventY=-1;
					path.moveTo(eventX, eventY);
					path.addCircle(eventX, eventY, (float) ((float) HALF_STROKE_WIDTH/(1.5)), Direction.CW);
					lastTouchX = eventX;
					lastTouchY = eventY;
					if(numberOfStrokes>0){

						strokes.add(points);
						strokeList.add(stroke);

						Log.e("Dev","numberOfStrokes "+numberOfStrokes+" strokeList size "+strokeList.size());

						points = new ArrayList<String>();
						stroke = new Stroke();
					}
					numberOfStrokes++;
					Log.e("Dev1","numberOfStrokes "+numberOfStrokes+" strokeList size "+strokeList.size());
					prevEventX=eventX;
					prevEventY=eventY;
					firstCoordinate = eventX/(this.getHeight());//3.9692/1548 for nexus5
					secondCoordinate = (this.getHeight()-eventY)/(this.getHeight());
					points.add(""+decimalFormat.format(firstCoordinate) +" " + decimalFormat.format(secondCoordinate) );//+",\n");

					////TO Do Number format add!!!!/////
					stroke.addPoint(firstCoordinate,secondCoordinate);

					//interchanged X and Y deliberately coz we are gonna be using app
					//in landscape in this version
					//Log.e(LOG, ""+points.size()+ " Strokes size "+strokes.size());
					Log.e(LOG, ""+stroke.size()+ " Strokes size "+strokeList.size());

					//				Log.e("Backspace","Inside onTouch DOWN Event with size of eventList "+eventList.size() +
					//						" and number of strokes "+numberOfStrokes);


					//expandDirtyRect(eventX, eventY);

					//			invalidate((int) (eventX- HALF_STROKE_WIDTH),
					//					(int) (eventY - HALF_STROKE_WIDTH),
					//					(int) (eventX + HALF_STROKE_WIDTH),
					//					(int) (eventY+ HALF_STROKE_WIDTH));

					break; //return true;

				case MotionEvent.ACTION_MOVE:

				case MotionEvent.ACTION_UP:

					//case MotionEvent.ACTION_CANCEL:
					if((prevEventX==eventX)&&(prevEventY==eventY))
						return false;
					prevEventX=eventX;
					prevEventY=eventY;
					resetDirtyRect(eventX, eventY);
					int historySize1 = event.getHistorySize();
					//Log.i("LOG","History Size "+historySize);
					for (int i = 0; i < historySize1; i++)
					{
						float historicalX = event.getHistoricalX(i);
						float historicalY = event.getHistoricalY(i);
						expandDirtyRect(historicalX, historicalY);
						path.lineTo(historicalX, historicalY);
					}
					path.lineTo(eventX, eventY);
					firstCoordinate = eventX/(this.getHeight());//3.9692/1548 for nexus5
					secondCoordinate = (this.getHeight()-eventY)/(this.getHeight());
					points.add(""+decimalFormat.format(firstCoordinate) +" " + decimalFormat.format(secondCoordinate) );//+",\n");

					////TODO Number format add!!!!/////
					stroke.addPoint(firstCoordinate,secondCoordinate);

					//interchanged X and Y deliberately coz we are gonna be using app
					//in landscape in this version
					//Log.e(LOG, ""+points.size());

					//pathList.add(path); // keeping the path strokewise
					//Log.e("ENQ","pathList size - " + pathList.size());

					break;

				default:
					Log.i("LOG","Ignored touch event: " + event.toString());
					return false;
			}

/*			if(event.getAction() == MotionEvent.ACTION_UP)
			{
				pathList.add(path); // keeping the path strokewise
			}*/


			//invalidate();
			invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
					(int) (dirtyRect.top - HALF_STROKE_WIDTH),
					(int) (dirtyRect.right + HALF_STROKE_WIDTH),
					(int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

			lastTouchX = eventX;
			lastTouchY = eventY;

			return true;
		}


		public void backTrack(){
			//pathList.remove(pathList.size()-1);
			/*
			path.reset();
			invalidate();
			*/

			//Path p = new Path();
			//p = pathList.remove(numberOfStrokes);
			path.set(pathList.remove(pathList.size() - 1));
			invalidate();



			//path.rewind();
			//path.addPath(p);
			//Canvas canvas = new Canvas();
			//canvas.drawPath(p,paint);


			//invalidate(0,getMeasuredHeight(),getMeasuredWidth(),0);

			Log.i("CHK","path_isEmpty " + path.isEmpty() + " pathList_size " + pathList.size());


		}

		@Deprecated
		public void redrawPathForBackSpace(){
			int strkCount=0;
			List<MotionEvent> eventListCopy = new ArrayList<MotionEvent> (eventList);
			eventList.clear();

			int numberOfStrokesCopy = numberOfStrokes;
			numberOfStrokes = 0;
			Log.e("Backspace","Before : : eventList "+eventListCopy.size()+
					" and numberOfStrokes "+numberOfStrokesCopy);

			for (int i=0;i<eventListCopy.size();i++){
				//Log.v("Backspace","Action "+eventListCopy.get(i).getAction());
				/* if(eventListCopy.get(i).getAction() == MotionEvent.ACTION_DOWN){
					strkCount++;
					if(strkCount==(numberOfStrokesCopy))
						break;
				}*/

				this.onTouchEvent(eventListCopy.get(i));
				//Log.e("Backspace","ontouch output is "+this.onTouchEvent(eventListCopy.get(i)));

			}
			Log.e("Backspace","After: : eventList "+eventList.size()+" and strkCount "+numberOfStrokes);

			//eventList = new ArrayList<MotionEvent>(eventListCopy);

		}

		private void expandDirtyRect(float historicalX, float historicalY)
		{
			if (historicalX < dirtyRect.left)
			{
				dirtyRect.left = historicalX;
			}
			else if (historicalX > dirtyRect.right)
			{
				dirtyRect.right = historicalX;
			}

			if (historicalY < dirtyRect.top)
			{
				dirtyRect.top = historicalY;
			}
			else if (historicalY > dirtyRect.bottom)
			{
				dirtyRect.bottom = historicalY;
			}
		}

		private void resetDirtyRect(float eventX, float eventY)
		{
			dirtyRect.left = Math.min(lastTouchX, eventX);
			dirtyRect.right = Math.max(lastTouchX, eventX);
			dirtyRect.top = Math.min(lastTouchY, eventY);
			dirtyRect.bottom = Math.max(lastTouchY, eventY);
		}
	}

	public class SVParams {

		//instance variables

		private boolean SV_ready;

		public int totalnSVs;

		public String strokeNamesFilename;
		public String dataPath;
		public String nSVsFilename;
		public String SVIndexFilename;
		public String alphasSVsFilename;
		public String biasFilename;
		public String base_dir;
		public int lineLength;

		List<Float> y;

		//required for cpp
		public int nClasses;
		int[] svIndices;
		float[] svAlpha;
		float[] nSVsPerClass;
		float[] svsForCpp;
		float[][] svs;
		public int inputDimension;
		public float sigma;
		float[] bias;

		List<String> strokeNames;


		@Deprecated
		SVParams(){

		}

		SVParams(String filename,String baseDirectory){

			BufferedReader br = null;

			try{

				//reading in all the values in the file
				br = new BufferedReader(new InputStreamReader(getAssets().open(filename),"UTF-8"));
				base_dir = baseDirectory;
				SV_ready = false;
				inputDimension = Integer.valueOf(br.readLine());
				nClasses = Integer.valueOf(br.readLine());
				sigma = Integer.valueOf(br.readLine());
				strokeNamesFilename = baseDirectory + br.readLine();
				dataPath = baseDirectory + br.readLine();
				nSVsFilename = br.readLine();
				SVIndexFilename = br.readLine();
				alphasSVsFilename = br.readLine();
				biasFilename = br.readLine();

				y = new ArrayList<Float>(nClasses);

				br.close();
				//calling the functions to load the svs, alpha and bias values
				loadStrokeNames();
				loadSVIndices();
				loadnSVsPerClass();
				loadBiasValues();
				loadAlphasSVs();


			}catch(Exception e){
				Log.e("SVParams","Error while loading SVParams file");
				Log.e("SVParams","Exception : "+e);
			}

		}

		private void loadStrokeNames(){
			BufferedReader br = null;

			try{

				strokeNames = new ArrayList<String>();

				String filename = "";
				filename += strokeNamesFilename;
				br = new BufferedReader(new InputStreamReader(getAssets().open(filename),"UTF-8"));

				String line="";

				for(int i=0;i<nClasses;i++){
					line = br.readLine();
					strokeNames.add(line.trim());
				}

				br.close();
			}catch(Exception e){
				Log.e("SVParams","Error in loadStrokeNames");
				Log.e("SVParams","Exception : "+e);
			}


		}

		private void loadSVIndices(){

			BufferedReader br = null;

			try{

				String filename = dataPath;
				filename += SVIndexFilename;
				br = new BufferedReader(new InputStreamReader(getAssets().open(filename),"UTF-8"));

				svIndices = new int[nClasses+1];

				for(int i=0;i<(nClasses+1);i++){
					svIndices[i] = Integer.valueOf(br.readLine());
				}

				br.close();

			}catch(Exception e){
				Log.e("SVParams","Error in loadSVIndices");
				Log.e("SVParams","Exception : "+e.getLocalizedMessage());
			}

		}

		private void loadnSVsPerClass(){

			BufferedReader br = null;

			try{

				String filename = dataPath;
				filename += nSVsFilename;
				br = new BufferedReader(new InputStreamReader(getAssets().open(filename),"UTF-8"));

				nSVsPerClass = new float[nClasses+1];

				for(int i=0;i<(nClasses+1);i++){
					nSVsPerClass[i] = Integer.valueOf(br.readLine());
				}

				br.close();

			}catch(Exception e){
				Log.e("SVParams","Error in loadnSVsPerClass");
				Log.e("SVParams","Exception : "+e.getLocalizedMessage());
			}
		}

		private void loadBiasValues(){
			BufferedReader br = null;

			try{

				String filename = dataPath;
				filename += biasFilename;
				br = new BufferedReader(new InputStreamReader(getAssets().open(filename),"UTF-8"));

				bias = new float[nClasses];

				for(int i=0;i<(nClasses);i++){
					bias[i] = (Float.valueOf(br.readLine()));
				}

				br.close();

			}catch(Exception e){
				Log.e("SVParams","Error in loadBiasValues");
				Log.e("SVParams","Exception : "+e.getLocalizedMessage());
			}

		}

		private void loadAlphasSVs(){

			BufferedReader br = null;

			try{

				String filename = dataPath;
				filename += alphasSVsFilename;
				br = new BufferedReader(new InputStreamReader(getAssets().open(filename),"UTF-8"));

				String line = br.readLine();
				//giving spacing as per spacing in SVTorch generated file
				String[] splittedString = line.split("\\s+");
				totalnSVs = Integer.valueOf(splittedString[0]);

				svAlpha = new float[totalnSVs];
				svs = new float[totalnSVs][];
				svsForCpp = new float[totalnSVs*inputDimension];

				for(int i=0;i<totalnSVs;i++){
					svs[i] = (new float[inputDimension]);
				}

				for(int i=0;i<totalnSVs;i++){

					line = br.readLine();
					splittedString = line.split(" ");

					svAlpha[i] = (Float.valueOf(splittedString[0]));

					//Log.i(LOG,splittedString[0]);

					for(int j=0;j<inputDimension;j++){
						svs[i][j] = ( Float.valueOf(splittedString[j+1]));
						svsForCpp[(inputDimension*i)+j] = svs[i][j] ;
					}

				}

				SV_ready = true;

				br.close();

			}catch(Exception e){
				Log.e("SVParams","Error in loadAlphasSVs");
				Log.e("SVParams","Exception : "+e.getLocalizedMessage());
			}


		}

		public String recognizeFeatureVector(List<Double> teststroke)
		{
			/*
			Run over all classes and Getsvm() for each class. Using the svm values form the charvect.
			 */
			ArrayList<Double> charVect = new ArrayList<Double>(nClasses);
			int characterClassPredicted=0;
			double maxValueSVM = 0.0;

			for(int i=0;i<nClasses;i++)
			{
				charVect.add(getSVM(i,teststroke,this.svAlpha,this.svsForCpp,this.svIndices,this.bias,this.inputDimension,this.sigma));

				if(i==0)
					maxValueSVM = charVect.get(i);

				else if(charVect.get(i) > maxValueSVM){
					maxValueSVM = charVect.get(i);
					characterClassPredicted = i;
				}

			}

			Log.v("SVParams","SVM returning "+strokeNames.get(characterClassPredicted));

			return strokeNames.get(characterClassPredicted);

		}

		private double kernelGaussian(int SVIndex, List<Double> teststroke, float[] SVs, int inputDimension, float sigma){
			/*
			Calculate the kernel gaussian value for the teststroke
			Returns exp( -{ (||teststroke-SVs[SVIndex]||^2) /sigma^2 })
			 */

			double oneBySigmaSquared = 1./(sigma*sigma);
			double sum = 0;

			try
			{
				for(int t = 0; t < inputDimension; t++)
				{

					double z = teststroke.get(t) - SVs[(SVIndex*inputDimension) + t];
					sum =sum - z*z;
				}
			}
			catch(Exception e)
			{
				Log.i(LOG,"SVMRecognize:Exception in kernelGaussian");
				Log.i(LOG,"Exception : "+e.getLocalizedMessage());
			}

			return Math.exp(sum*oneBySigmaSquared);

		}

		private double getSVM(int i, List<Double> teststroke, float[] sv_alpha, float[] SVs, int[] SVIndices, float[] bias, int inputDimension, float sigma){
			/*
			Run over all SVIndices for a class , compute kernelGaussian for each SVIndex and add bias
			 */
			int j;
			double term = 0, y = 0;
			try
			{

				for(j = SVIndices[i]; j < SVIndices[i+1]; j++)
				{
					term = kernelGaussian(j, teststroke,SVs,inputDimension,sigma);
					y += term * sv_alpha[j];
				}
				y += bias[i];
			}
			catch(Exception e)
			{
				Log.e(LOG,"SVMRecognize:Exception in Getsvm");
				Log.e(LOG,"Exception : "+e.getLocalizedMessage());
			}
			return y;
		}

		public boolean isSV_ready() {
			return SV_ready;
		}

		public void setSV_ready(boolean sV_ready) {
			SV_ready = sV_ready;
		}

	}

	public class LanguageRules{

		private Map<String,String> rulesMap;
		private Map<String,String> exceptionalRulesMap;
		private Map<String,String> chillaStrokesSet;

		@Deprecated
		public LanguageRules(){

		}

		public LanguageRules(String filename){
			BufferedReader br = null;
			rulesMap = new HashMap<String,String>();
			addExceptionalStrokes();
			addChillaStrokes();

			try{
				br = new BufferedReader(new InputStreamReader(getAssets().open(filename),"UTF-8"));

				String line = br.readLine();
				while(line != null){

					String[] splitString = line.split(" - ");

					rulesMap.put(splitString[0].trim(),splitString[1].substring(0,splitString[1].lastIndexOf(";")).trim());

					line = br.readLine();

				}

				br.close();

			}catch(Exception e){
				Log.e("Language Rules","Exception : "+e.getLocalizedMessage());
			}
		}

		public String  getLabel(String rule){

			Log.v("Language Rules","getLabel rule "+rule);
			String label = rulesMap.get(rule.trim());

			Log.v("Language Rules","getLabel returning "+label);

			if(label==null)
				return "";

			return label;

		}

		private void addExceptionalStrokes(){
			exceptionalRulesMap = new HashMap<String,String>();
			exceptionalRulesMap.put("main_halant","094D"); //halant
			exceptionalRulesMap.put("main_circle+","0903");//aha
			exceptionalRulesMap.put("main_circle","0902"); //am
			exceptionalRulesMap.put("chilla","097A"); //chilla
			exceptionalRulesMap.put("chandra_bindu","0901"); //chandrabindu
		}

		private void addChillaStrokes(){
			chillaStrokesSet = new HashMap<String,String>();
			chillaStrokesSet.put("0923","097A");
			chillaStrokesSet.put("0928","097B");
			chillaStrokesSet.put("0931","097C");
			chillaStrokesSet.put("0932","097D");
			chillaStrokesSet.put("0933","097E");
			chillaStrokesSet.put("0915","097F");

		}

		public String getChillaStroke(String key){
			return chillaStrokesSet.get(key);
		}

		public String getExceptionalStrokeLabel(String rule){
			String label = exceptionalRulesMap.get(rule.trim());

			if(label==null)
				return "";

			return label;
		}

		public boolean isExceptionalStroke(String label){
			return exceptionalRulesMap.containsKey(label.trim());
		}

	}
}

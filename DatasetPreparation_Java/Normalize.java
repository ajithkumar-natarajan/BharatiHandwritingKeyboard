import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class strokeCoordinates{
    ArrayList<Float> x;
    ArrayList<Float> y;

    public strokeCoordinates(){
        x = new ArrayList<>();
        y = new ArrayList<>();
    }

    public ArrayList<Float> getX(){
        return this.x;
    }
    
    public ArrayList<Float> getY(){
        return this.y;
    }
    
    public void setX(float x){
        this.x.add(x);
    }

    public void setY(float y){
        this.y.add(y);
    }
}

public class Segregator{
    public static void main(String[] args)throws Exception {
        File inputReader = new File("/home/ajithkumar/BharatiScript/OnlineEngine/Classifier/DatasetPreparation_Java/inputStroke.txt");

        BufferedReader bufferedReader = new BufferedReader(new FileReader(inputReader));

        String inputStrokeString;
        // ArrayList<Float> xCoordinates = new ArrayList<>();
        strokeCoordinates stroke = new strokeCoordinates();
        ArrayList<strokeCoordinates> strokesCoordinatesList = new ArrayList<>();

        int begIndex, endIndex;

        while ((inputStrokeString = bufferedReader.readLine()) != null){
            begIndex = inputStrokeString.indexOf('q');
            endIndex = inputStrokeString.indexOf(',');
            // Pattern xPattern = Pattern.compile("q(.*?),");
            // Matcher xmatcher = xPattern.matcher(inputStrokeString);
            if(begIndex != -1){
                stroke.setX(Float.valueOf(inputStrokeString.substring(begIndex+1, endIndex)));
                // Pattern yPattern = Pattern.compile(",(.*?);");
                // Matcher ymatcher = yPattern.matcher(inputStrokeString);
                stroke.setY(Float.valueOf(inputStrokeString.substring(endIndex+1, inputStrokeString.length()-1)));
                continue;
            }
            begIndex = inputStrokeString.indexOf('l');
            // Pattern xEndPattern = Pattern.compile("l(.*?),");
            // xmatcher = xEndPattern.matcher(inputStrokeString);
            if(begIndex != -1){
                stroke.setX(Float.valueOf(inputStrokeString.substring(begIndex+1, endIndex)));
                // Pattern yEndPattern = Pattern.compile(",(.*?);");
                // Matcher ymatcher = yEndPattern.matcher(inputStrokeString);
                stroke.setY(Float.valueOf(inputStrokeString.substring(endIndex+1, inputStrokeString.length()-1)));                

                strokesCoordinatesList.add(stroke);
                stroke = new strokeCoordinates();
            }
        }

        for(strokeCoordinates list : strokesCoordinatesList){
            ArrayList<Float> xValues = list.getX();
            ArrayList<Float> yValues = list.getY();

            int length = xValues.size();

            System.out.println("Next stroke-");

            for(int i=0; i<length; i++)
                System.out.println(xValues.get(i)+" "+yValues.get(i));
        }
        // System.out.println(System.getProperty("user.dir"));

        bufferedReader.close();
    }
}
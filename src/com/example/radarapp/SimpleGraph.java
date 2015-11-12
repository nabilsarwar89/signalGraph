package com.example.radarapp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

public class SimpleGraph extends Activity
{

    private XYPlot plot;
	private short [] aData;
	private short [] leftChannelAudio;
	private short [] rightChannelAudio;
	private XYSeries leftVals;
	private XYSeries rightVals;
	private Context context;
	private WaveFile w;
	private String location;
	private Boolean chosen;
	private InputStream is;

    @Override
    public void onCreate(Bundle b)
    {
        super.onCreate(b);
        setContentView(R.layout.activity_simple_graph);
        context = this.getApplicationContext();
        
        plot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        
        chosen = getIntent().getBooleanExtra("CHOSEN", false);
        location = getIntent().getStringExtra("FILE_PATH");
       
        try{
        	if (chosen){
        		
        		File file = new File(location);
        		is = new BufferedInputStream(new FileInputStream(file));
        	}

        	else {
        		int resId = context.getResources().getIdentifier("ranging", "raw", context.getPackageName());
        		is = context.getResources().openRawResource(resId);
        		}
		w = new WaveFile(is);
		aData = w.getSampleAmplitudes();
        }
        catch (Exception e){
        	e.printStackTrace();
        }
        
        leftChannelAudio = new short[16];
        rightChannelAudio = new short[16];
        for(int i = 0; i < 16; i = i + 2)
		{

        	leftChannelAudio[i] = aData[i];
        	rightChannelAudio[i] = aData[i+1];
		    }
        
        Short[] s1 = new Short[leftChannelAudio.length];
        Short[] s2 = new Short[rightChannelAudio.length];
        for (int i =0; i<16; i++){
        	short j = leftChannelAudio[i];
        	short h = rightChannelAudio[i];
        	s1[i] = Short.valueOf(j);
        	s2 [i] = Short.valueOf(h);
        	}
        
        int totalSamples = aData.length;
		float totalTime = w.length();
		float microSecPerSample = 1000000*(totalTime/totalSamples);
		microSecPerSample = Math.round(microSecPerSample*10)/100;
		Float[] x1 = new Float[16];
		Float[] x2 = new Float[16];
		for (int i=0, j = 1; i<x1.length; i=i+2, j=j+4){
			x1[i] = Float.valueOf(microSecPerSample*j);
			x1[i+1] = Float.valueOf(microSecPerSample*(j+1));
			x2[i] = Float.valueOf(microSecPerSample*(j+2));
			x2[i+1] = Float.valueOf(microSecPerSample*(j+3));			
		}
		//test data sets used before using actual data:
		//Number [] x1 = {0.11, 0.22, 0.55, 0.66, 0.99, 1.10, 1.43, 1.54, 1.87, 1.98, 2.31, 2.42};
		//Number [] x2 = {0.33, 0.44, 0.77, 0.88, 1.21, 1.32, 1.65, 1.76, 2.09, 2.20, 2.53, 2.64};
        leftVals = new SimpleXYSeries(Arrays.asList(x1),
                Arrays.asList(s1),
                "Left");
        rightVals = new SimpleXYSeries(Arrays.asList(x2),
        		Arrays.asList(s2),
        		"Right");
		
        LineAndPointFormatter leftFormat = new LineAndPointFormatter();
        leftFormat.setPointLabelFormatter(new PointLabelFormatter());
        leftFormat.configure(getApplicationContext(),
                R.xml.line_point_format_plf1);
        plot.addSeries(leftVals, leftFormat);

        LineAndPointFormatter rightFormat = new LineAndPointFormatter();
        rightFormat.setPointLabelFormatter(new PointLabelFormatter());
        rightFormat.configure(getApplicationContext(),
                R.xml.line_point_format_plf2);
        plot.addSeries(rightVals, rightFormat);

        //plot.setTicksPerRangeLabel(3);
        plot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
        plot.setRangeValueFormat(new DecimalFormat("######"));
        int rangeEstimate = 0;
        for (int i=0; i<leftChannelAudio.length; i++){
        	int temp = leftChannelAudio[i];
        	if (temp<0){ temp = -temp;}
        	if (temp>rangeEstimate){
        		rangeEstimate = temp;
        	}
        }
        if (rangeEstimate>1000 && rangeEstimate<10000){
        	plot.setRangeBoundaries(-10000, 10000, BoundaryMode.FIXED);
        	plot.setRangeStepValue(1000);
        }
        else if (rangeEstimate>10000 && rangeEstimate<15000){
        	plot.setRangeBoundaries(-15000, 15000, BoundaryMode.FIXED);
        	plot.setRangeStepValue(1500);
        }
        else if(rangeEstimate>15000){
        	plot.setRangeBoundaries(-30000, 30000, BoundaryMode.FIXED);
        	plot.setRangeStepValue(3000);
        }
        else{
        	plot.setRangeBoundaries(-100, 100, BoundaryMode.FIXED);
        	plot.setRangeStepValue(10);
        }
        
        Toast.makeText(getApplicationContext(), "Total Samples: " + totalSamples, Toast.LENGTH_SHORT).show();
        
        Toast.makeText(getApplicationContext(), "total Time: " + totalTime, Toast.LENGTH_LONG).show();

    }
    
}
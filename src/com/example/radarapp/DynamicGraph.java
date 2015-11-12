package com.example.radarapp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;

import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

public class DynamicGraph extends Activity {

    private class PlotPainter implements Observer {
        Plot plot;

        public PlotPainter(Plot p) {
            this.plot = p;
        }

        @Override
        public void update(Observable o, Object arg) {
            plot.redraw();
        }
    }

    private XYPlot dynamicPlot;
    private PlotPainter plotUpdater;
    WaveDataSource waveData;
    private Thread nThread;
    private Context context;
	private WaveFile w;
	private short [] rawData;
	private short [] leftChannelData;
	private short [] rightChannelAudio;
	private Float[] x1;
	private Float[] x2;
	private String location;
	private Boolean chosen;
	private InputStream is;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_graph);
        context = this.getApplicationContext();
        
        chosen = getIntent().getBooleanExtra("CHOSEN", false);
        location = getIntent().getStringExtra("FILE_PATH");
        try{
        	if (chosen){
        	File file = new File(location);
    		is = new BufferedInputStream(new FileInputStream(file));
        	}
        	else {
        		int resId = context.getResources().getIdentifier("doppler", "raw", context.getPackageName());
        		is = context.getResources().openRawResource(resId);
        		}
		w = new WaveFile(is);
		rawData = w.getSampleAmplitudes();
        int rLength = rawData.length/2;	        
        leftChannelData = new short[rLength];
        rightChannelAudio = new short[rLength];
        
        for(int i = 0; i < rLength; i = i + 2)
		{
        	leftChannelData[i] = rawData[i];
		    rightChannelAudio[i+1] = rawData[i+1];
		    }

        int totalSamples = rLength;
		float totalTime = w.length();
		float microSecPerSample = 1000000*(totalTime/totalSamples);
		float tenthOfMicroSecPerSample = Math.round(microSecPerSample*10)/100;
		x1 = new Float[rLength];
		x2 = new Float[rLength];
		for (int i=0, j = 1; i<rLength; i=i+2, j=j+4){
			x1[i] = Float.valueOf(tenthOfMicroSecPerSample*j);
			x1[i+1] = Float.valueOf(tenthOfMicroSecPerSample*(j+1));
			x2[i] = Float.valueOf(tenthOfMicroSecPerSample*(j+2));
			x2[i+1] = Float.valueOf(tenthOfMicroSecPerSample*(j+3));			
		}
        }catch (Exception e){e.printStackTrace();}
        
        dynamicPlot = (XYPlot) findViewById(R.id.dynamicXYPlot);

        plotUpdater = new PlotPainter(dynamicPlot);

        dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));

        waveData = new WaveDataSource();
        waveData.setDataSets(leftChannelData, rightChannelAudio);
        DynamicSeries leftDataSeries = new DynamicSeries(waveData, 0, "Left");
        DynamicSeries rightDataSeries = new DynamicSeries(waveData, 1, "Right");

        LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                                Color.rgb(0, 0, 0), null, null, null);
        formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter1.getLinePaint().setStrokeWidth(10);
        dynamicPlot.addSeries(leftDataSeries,
                formatter1);

        LineAndPointFormatter formatter2 =
                new LineAndPointFormatter(Color.rgb(0, 0, 200), null, null, null);
        formatter2.getLinePaint().setStrokeWidth(10);
        formatter2.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

        //formatter2.getFillPaint().setAlpha(220);
        dynamicPlot.addSeries(rightDataSeries, formatter2);

        waveData.addObserver(plotUpdater);

        dynamicPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
        dynamicPlot.setDomainStepValue(5);

        dynamicPlot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
        
        dynamicPlot.setRangeStepValue(100);
        
        dynamicPlot.setRangeValueFormat(new DecimalFormat("######"));
        int estimate = leftChannelData[150];
        
        if (estimate<500 && estimate>-500){
        dynamicPlot.setRangeBoundaries(-200, 200, BoundaryMode.FIXED);
        }
        else if (estimate<1000 && estimate>-1000 && (estimate>500 || estimate<-500)){
        	dynamicPlot.setRangeBoundaries(-1000, 1000, BoundaryMode.FIXED);
        	dynamicPlot.setRangeStepValue(200);
        }
        else if (estimate<10000 && estimate>-10000 && (estimate>1000 || estimate<-1000)){
        	dynamicPlot.setRangeBoundaries(-10000, 10000, BoundaryMode.FIXED);
        	dynamicPlot.setRangeStepValue(1000);
        }
        else if (estimate>10000 || estimate<-10000){
        	dynamicPlot.setRangeBoundaries(-15000, 15000, BoundaryMode.FIXED);
        	dynamicPlot.setRangeStepValue(1000);
        }else{
        	dynamicPlot.setRangeBoundaries(-100, 100, BoundaryMode.FIXED);
        	dynamicPlot.setRangeStepValue(10);
        }

        DashPathEffect dashFx = new DashPathEffect(
                new float[] {PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        dynamicPlot.getGraphWidget().getDomainGridLinePaint().setPathEffect(dashFx);
        dynamicPlot.getGraphWidget().getRangeGridLinePaint().setPathEffect(dashFx);
    }

    @Override
    public void onResume() {
        nThread = new Thread(waveData);
        nThread.start();
        super.onResume();
    }

    @Override
    public void onPause() {
    	waveData.stopThread();
        super.onPause();
    }


}
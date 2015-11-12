package com.example.radarapp;


import java.io.InputStream;
import java.util.Arrays;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;


public class FftGraphActivity extends ActionBarActivity {
	
	private XYPlot plot;
	private XYSeries leftVals;
	private double c = 300000000d;
	private double tp = 0.250d;
	private double fs;
	private int N;
	private double fc;
	private double[][] sif;
	private int count = 0;
	private Double[] vel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fft_graph);
		Context context = this.getApplicationContext();
		
		int resId = context.getResources().getIdentifier("doppler", "raw", context.getPackageName());
		InputStream is = context.getResources().openRawResource(resId);
		WaveFile w = new WaveFile(is);

		//double maxAmplitude = 1 << (w.getHeader().getBitsPerSample() - 1);
		//boolean signed = true;
		//if(w.getHeader().getBitsPerSample()==8){
		//	signed = false;
		//}
		//double[] rawData = normalizeAmp(w.getSampleAmplitudes(true), maxAmplitude, signed);
		//Acquiring the whole sample data (both channels)
		double[] rawData = w.getSampleAmplitudes(true);
		fs = w.getHeader().getSampleRate();
		double nAsDouble = tp*fs;
		N = (int) nAsDouble;
		fc = 2590000000.0d;
		
		/*
		 * separating the right channel data into a new array
		 */
		int rawDataChannelLength = rawData.length/2;
		double[] rightChannelAudio = new double[rawDataChannelLength];
        for(int i = 0; i < rawDataChannelLength; i++)
		{ 
		    rightChannelAudio[i] =  rawData[2*i+1];
		    }
        
        
        
        //now perform inverse operation to the right channel data:
       
        double[] s = new double[rawDataChannelLength];
        for (int i = 0; i<rawDataChannelLength; i++){
        	s[i] = -1*(rightChannelAudio[i]);
        }
        
        
        //the following Matlab code is used as a reference to implement the loop of sif[][]
        //for ii = 1:round(size(s,1)/N)-1
        //sif(ii,:) = s(1+(ii-1)*N:ii*N);
        //end
        //float f = rawDataLength/N;
        int x = (int)Math.round(s.length/N)-1;
        sif = new double[x][N];//2d array with x-rows and N-columns
        /*
         * The following loop is a little complicated as it requires three variables in the loop
         * with i = 0, it starts the inner loop with j=1 and k=0.
         * When the inner  loop completes one full cycle, j becomes 11025 and k=11024
         * Then the inner loop starts again with i=1, j=11026 and k=0;
         * So this reads 11025 samples from s[] to each row of sif[]
         */
        for (int i = 1; i<x+1; i++){
        	int j = 1+(i-1)*N;
        	int k = 0;
        	for (j=1+(i-1)*N; j<(i*N); j++){
        		sif[i-1][k] = s[j-1];
        		k = k+1;
        	}        	
        }
        
        double sum = 0;
        for (int i=0; i<s.length; i++){
        	double k = s[i];
        	sum = k+sum;
        }
        
        double average = sum/s.length;
        //average = 1.4581/100000;
        
        for (int i=0; i<x; i++){
        	int j = 0;
        	for (j=0; j<N; j++)
        	sif[i][j] = sif[i][j]-average;
        }
        
        //double zpad = 8*N/2;
        
        
        DoubleFFT_2D fft2d = new DoubleFFT_2D(x, N*4);
        fft2d.realInverseFull(sif, false);
        //making all values positive (matlab abs() function)
        //and performing the dbv (20*log10(abs(input)))
        for (int i=0; i<x; i++){
        	int j = 0;
        	double mmax = 0.0;
        	for (j=0; j<N; j++){
        		double z = sif[i][j];
        		if(z<0){ 
        			z = -z;
        		}
        		if(z==0){
        			sif[i][j] = 0;
        		}
        		else{
        		sif[i][j] = 20*(Math.log10(z));
        		if (j<N/2 && sif[i][j]>mmax){
        			mmax = sif[i][j];
        		}
        		}
        	}
        }
        
        
        
        int vDim = N*2;
        //double[][] v = new double[x][vDim];//to get half of the data from sif
        //for (int i =0; i<x; i++){
        	//int j = 0;
        //	double mmax = 0;
        	//for (j=0; j<vDim; j++){
        		//v[i][j]=sif[i][j];
        //		if(v[i][j]>mmax){
        	//		mmax = v[i][j];//to capture mmax, the largest element in v
        		//}
        	//}
        //}
        //now creating a array for delta_f
        double[] delta_f = new double[vDim];
        delta_f [0]=0.0;
        for (int i = 1; i<vDim; i++){
        	delta_f[i] = i;
        }
        
        double lambda = c/fc;
        double lambdaHalf = lambda/2;
        vel = new Double[vDim];
        for (int i = 0; i<vDim; i++){
        	vel[i] = Double.valueOf(delta_f[i]*lambdaHalf);
        }
        Button b = (Button) findViewById(R.id.button1);
	     b.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	            	double value = vel[count];
	            	count++;
	            	Toast.makeText(getApplicationContext(),
	            			"The number "+count+" velocity is: "+value+" metre/sec", Toast.LENGTH_SHORT).show();
	            	
	            }
	        });
        
        Double[] velocity = {5.0, 4.0, 3.0, 7.0, 2.0, 3.0, 4.0, 5.0, 7.0, 10.0};
        //calculate the  time
        Double[] time = new Double[10];
        //double timeTot = tp*x;
        time[0] = Double.valueOf(1.0);
        for (int i = 1; i<10; i++){
        	time[i] = Double.valueOf(i*1.0);
        }
        plot = (XYPlot) findViewById(R.id.dopplerPlot);
        leftVals = new SimpleXYSeries(Arrays.asList(velocity),
        		Arrays.asList(time),
        		"Velocity");
        LineAndPointFormatter leftFormat = new LineAndPointFormatter();
        leftFormat.setPointLabelFormatter(new PointLabelFormatter());
        leftFormat.configure(getApplicationContext(),
                R.xml.line_point_format_plf1);
        plot.addSeries(leftVals, leftFormat);

        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
        //.setRangeBoundaries(1, 10, BoundaryMode.FIXED);
        plot.setDomainBoundaries(0, 40, BoundaryMode.FIXED);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fft_graph, menu);
		return true;


	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public double[] normalizeAmp(double[]amp, double maxAmp, boolean signed){
		int numSamples = amp.length;
		//boolean s = signed;
		//if (!s){	// one more bit for unsigned value
			//maxAmp <<=1;
		//}
		
		double [] normalizedAmp = new double[numSamples];
		for (int i = 0; i < numSamples; i++) {
			double n = amp[i];
			normalizedAmp[i] = n/maxAmp;
		}
	
	return normalizedAmp;
	}
}

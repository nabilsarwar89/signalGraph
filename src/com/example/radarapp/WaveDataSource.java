package com.example.radarapp;

import java.util.Observable;
import java.util.Observer;

//import com.example.radarapp.DynamicGraph.WaveDataSource.MyObservable;

class WaveDataSource implements Runnable {

    class MyObservable extends Observable {
        @Override
        public void notifyObservers() {
            setChanged();
            super.notifyObservers();
        }
    }

    public static final int leftSeries = 0;
    public static final int rightSeries = 1;
    private static final int SAMPLE_SIZE = 30;
    private MyObservable notifier;
    private boolean running = false;
	private short[] y1;
	private short[] y2;
	//private Float[] x1;
	//private Float[] x2;
	private int i=0;
	private int j=0;
	//private int k=0;
	//private int l=0;
	//private int ySize1;
	//private int ySize2;
	//private int xSize1;
	//private int xSize2;
	private short amp1;
	private short amp2;
	//private float sec1;
	//private float sec2;

    {
        notifier = new MyObservable();
    }
    
    public void setDataSets(short[] ya, short[] yb){
    	this.y1 = ya;
    	this.y2 = yb;
    	//this.x1 = xa;
    	//this.x2 = xb;
    	//this.ySize1 = ya.length;
    	//this.ySize2 = yb.length;
    	//this.xSize1 = xa.length;
    	//this.xSize2 = xb.length;
    }

    public void stopThread() {
        running = false;
    }

    //@Override
    public void run() {
        try {

            running = true;
            while (running) {

                Thread.sleep(50);
                notifier.notifyObservers();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getItemCount(int series) {
        return SAMPLE_SIZE;
    }

    public Number getX(int series, int index) {
        if (index >= SAMPLE_SIZE) {
            throw new IllegalArgumentException();
        }
     return index;
    }

    public Number getY(int series, int index) {
        if (index >= SAMPLE_SIZE) {
            throw new IllegalArgumentException();
        }
                            
        switch (series) {
            case leftSeries:
            	amp1 = y1[i];
            	i++;
                return amp1;
            case rightSeries:
            	amp2 = y2[j];
            	j++;
                return amp2;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void addObserver(Observer observer) {
        notifier.addObserver(observer);
    }

    public void removeObserver(Observer observer) {
        notifier.deleteObserver(observer);
    }

}
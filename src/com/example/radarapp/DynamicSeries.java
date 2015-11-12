package com.example.radarapp;

import com.androidplot.xy.XYSeries;



class DynamicSeries implements XYSeries {
    private WaveDataSource datasource;
    private int seriesIndex;
    private String title;

    public DynamicSeries(WaveDataSource datasource, int seriesIndex, String title) {
        this.datasource = datasource;
        this.seriesIndex = seriesIndex;
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int size() {
        return datasource.getItemCount(seriesIndex);
    }

    @Override
    public Number getX(int index) {
    	
        Number n = datasource.getX(seriesIndex, index);
        return n;
    }

    @Override
    public Number getY(int index) {
    	
        Number n = datasource.getY(seriesIndex, index);
        return n;
    }
}
package com.example.radarapp;

/**
 * This class uses the WaveHead class to initialise and read the wave file
 * and allow acquiring of amplitude and time data
 */

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class WaveFile{

	private WaveHeader header;
	private byte[] audioData;
	private int bytesPerSample;
	private int sampleRate;

	public WaveFile(InputStream is) {
		header = new WaveHeader(is);
		sampleRate = header.getSampleRate();
		initializeWave(is, header);
	}

	private void initializeWave(InputStream is, WaveHeader header) {
		if (header.isValid()) {
			try {
				audioData = new byte[is.available()];
				is.read(audioData);
			}
			catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}
	
	public double[] getSampleAmplitudes(Boolean asDouble){
		ShortBuffer buffer = ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN)
	               .asShortBuffer();
	int numOfSamples = buffer.remaining();
	double[] amplitudes = new double[numOfSamples];
	for (int i = 0; i < numOfSamples; i++) {
	    amplitudes[i] = buffer.get() * (1.0/32768.0);
	}
	return amplitudes;
	//	bytesPerSample = header.getBitsPerSample() / 8;
	//	int numSamples = audioData.length / bytesPerSample;
	//	double[] amplitudes = new double[numSamples];
		
	//	int position = 0;
	//	for (int i = 0; i < numSamples; i++) {
	//		double amplitude = 0;
	//		for (int j = 0; j < bytesPerSample; j++) {
	//			amplitude += (double) ((audioData[position++] & 0xFF) << (j * 8))/32767.0;
				//amp = (b2 << 8 | b1 & 0xFF) / 32767.0;
	//		}
	//		amplitudes[i] = amplitude;
	//	}
	//	return amplitudes;
	}
	
	public short[] getSampleAmplitudes(){
		bytesPerSample = header.getBitsPerSample() / 8;
		int numSamples = audioData.length / bytesPerSample;
		short[] amplitudes = new short[numSamples];
		
		int position = 0;
		for (int i = 0; i < numSamples; i++) {
			short amplitude = 0;
			for (int j = 0; j < bytesPerSample; j++) {
				amplitude |= (short) ((audioData[position++] & 0xFF) << (j * 8));
			}
			amplitudes[i] = amplitude;
		}
		return amplitudes;
	}
	
	

	public int getSampleRate(){
		return sampleRate;
	}

	public WaveHeader getHeader() {
		return header;
	}
	
	public byte[] getBytes() {
		return audioData;
	}

	public int size() {
		return audioData.length;
	}
	
	public float length() {
		float second = (float) header.getSubChunk2Size() / header.getByteRate();
		return second;
	}

	public String timestamp() {
		float totalTimeInSec = this.length();
		float second = totalTimeInSec % 60;
		int minute = (int) totalTimeInSec / 60 % 60;
		int hour = (int) (totalTimeInSec / 3600);

		StringBuffer stringBuffer = new StringBuffer();
		if (hour > 0) {
			stringBuffer.append(hour + ":");
		}
		if (minute > 0) {
			stringBuffer.append(minute + ":");
		}
		stringBuffer.append(second);

		return stringBuffer.toString();
	}
	
	public String toString(){
		StringBuffer stringBuffer = new StringBuffer(header.toString());
		stringBuffer.append("\n");
		stringBuffer.append("Length is: " + timestamp());
		return stringBuffer.toString();
	}

}
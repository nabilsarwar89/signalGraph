package com.example.radarapp;

import java.io.IOException;
import java.io.InputStream;
/**
 * This class is derived from the musicg library
 * Author name: Jacquet Wong
 * Available at: https://code.google.com/p/musicg
 * Copy of Licence can be found at: http://www.apache.org/licenses/LICENSE-2.0
 * This class is however included in the same package with some modifications
 */ 
public class WaveHeader {

	public static final String RIFF_HEADER = "RIFF";
	public static final String WAVE_HEADER = "WAVE";
	public static final String FMT_HEADER = "fmt ";
	public static final String DATA_HEADER = "data";
	public static final int HEADER_BYTE_LENGTH = 44;

	private boolean valid;
	private String chunkId;
	private long chunkSize;
	private String format;	
	private String subChunk1Id;
	private long subChunk1Size;
	private int audioFormat;
	private int channels;
	private long sampleRate;
	private long byteRate;
	private int blockAlign;
	private int bitsPerSample;
	private String subChunk2Id;
	private long subChunk2Size;



	public WaveHeader(InputStream is) {
		valid = loadHeader(is);
	}

	private boolean loadHeader(InputStream inputStream) {

		byte[] headerBuffer = new byte[HEADER_BYTE_LENGTH];
		try {
			inputStream.read(headerBuffer);

			// read header
			int pointer = 0;
			chunkId = new String(new byte[] { headerBuffer[pointer++],
					headerBuffer[pointer++], headerBuffer[pointer++],
					headerBuffer[pointer++] });
			// little endian
			chunkSize = (long) (headerBuffer[pointer++] & 0xff)
					| (long) (headerBuffer[pointer++] & 0xff) << 8
					| (long) (headerBuffer[pointer++] & 0xff) << 16
					| (long) (headerBuffer[pointer++] & 0xff << 24);
			format = new String(new byte[] { headerBuffer[pointer++],
					headerBuffer[pointer++], headerBuffer[pointer++],
					headerBuffer[pointer++] });
			subChunk1Id = new String(new byte[] { headerBuffer[pointer++],
					headerBuffer[pointer++], headerBuffer[pointer++],
					headerBuffer[pointer++] });
			subChunk1Size = (long) (headerBuffer[pointer++] & 0xff)
					| (long) (headerBuffer[pointer++] & 0xff) << 8
					| (long) (headerBuffer[pointer++] & 0xff) << 16
					| (long) (headerBuffer[pointer++] & 0xff) << 24;
			audioFormat = (int) ((headerBuffer[pointer++] & 0xff) | (headerBuffer[pointer++] & 0xff) << 8);
			channels = (int) ((headerBuffer[pointer++] & 0xff) | (headerBuffer[pointer++] & 0xff) << 8);
			sampleRate = (long) (headerBuffer[pointer++] & 0xff)
					| (long) (headerBuffer[pointer++] & 0xff) << 8
					| (long) (headerBuffer[pointer++] & 0xff) << 16
					| (long) (headerBuffer[pointer++] & 0xff) << 24;
			byteRate = (long) (headerBuffer[pointer++] & 0xff)
					| (long) (headerBuffer[pointer++] & 0xff) << 8
					| (long) (headerBuffer[pointer++] & 0xff) << 16
					| (long) (headerBuffer[pointer++] & 0xff) << 24;
			blockAlign = (int) ((headerBuffer[pointer++] & 0xff) | (headerBuffer[pointer++] & 0xff) << 8);
			bitsPerSample = (int) ((headerBuffer[pointer++] & 0xff) | (headerBuffer[pointer++] & 0xff) << 8);
			subChunk2Id = new String(new byte[] { headerBuffer[pointer++],
					headerBuffer[pointer++], headerBuffer[pointer++],
					headerBuffer[pointer++] });
			subChunk2Size = (long) (headerBuffer[pointer++] & 0xff)
					| (long) (headerBuffer[pointer++] & 0xff) << 8
					| (long) (headerBuffer[pointer++] & 0xff) << 16
					| (long) (headerBuffer[pointer++] & 0xff) << 24;


		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		if (bitsPerSample!=8 && bitsPerSample!=16){			
			return false;
		}
		
		if (chunkId.toUpperCase().equals(RIFF_HEADER)
				&& format.toUpperCase().equals(WAVE_HEADER) && audioFormat == 1) {
			return true;
		}
		
		return false;
	}

	public boolean isValid() {
		return valid;
	}

	public String getChunkId() {
		return chunkId;
	}

	public long getChunkSize() {
		return chunkSize;
	}

	public String getFormat() {
		return format;
	}

	public String getSubChunk1Id() {
		return subChunk1Id;
	}

	public long getSubChunk1Size() {
		return subChunk1Size;
	}

	public int getAudioFormat() {
		return audioFormat;
	}

	public int getChannels() {
		return channels;
	}

	public int getSampleRate() {
		return (int) sampleRate;
	}

	public int getByteRate() {
		return (int) byteRate;
	}

	public int getBlockAlign() {
		return blockAlign;
	}

	public int getBitsPerSample() {
		return bitsPerSample;
	}

	public String getSubChunk2Id() {
		return subChunk2Id;
	}

	public long getSubChunk2Size() {
		return subChunk2Size;
	}

	public void setSampleRate(int sampleRate){
		int newSubChunk2Size = (int)(this.subChunk2Size * sampleRate / this.sampleRate);
		// if num bytes for each sample is even, the size of newSubChunk2Size also needed to be in even number
		if ((bitsPerSample/8)%2==0){			
			if (newSubChunk2Size%2!=0){
				newSubChunk2Size++;
			}
		}
		
		this.sampleRate = sampleRate;
		this.byteRate = sampleRate*bitsPerSample/8;
		this.chunkSize = newSubChunk2Size+36;
		this.subChunk2Size = newSubChunk2Size;
	}
	
	public void setChunkId(String chunkId) {
		this.chunkId = chunkId;
	}

	public void setChunkSize(long chunkSize) {
		this.chunkSize = chunkSize;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setSubChunk1Id(String subChunk1Id) {
		this.subChunk1Id = subChunk1Id;
	}

	public void setSubChunk1Size(long subChunk1Size) {
		this.subChunk1Size = subChunk1Size;
	}

	public void setAudioFormat(int audioFormat) {
		this.audioFormat = audioFormat;
	}

	public void setChannels(int channels) {
		this.channels = channels;
	}

	public void setByteRate(long byteRate) {
		this.byteRate = byteRate;
	}

	public void setBlockAlign(int blockAlign) {
		this.blockAlign = blockAlign;
	}

	public void setBitsPerSample(int bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
	}

	public void setSubChunk2Id(String subChunk2Id) {
		this.subChunk2Id = subChunk2Id;
	}

	public void setSubChunk2Size(long subChunk2Size) {
		this.subChunk2Size = subChunk2Size;
	}

	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append("chunkId: " + chunkId);
		sb.append("\n");
		sb.append("chunkSize: " + chunkSize);
		sb.append("\n");
		sb.append("format: " + format);
		sb.append("\n");
		sb.append("subChunk1Id: " + subChunk1Id);
		sb.append("\n");
		sb.append("subChunk1Size: " + subChunk1Size);
		sb.append("\n");
		sb.append("audioFormat: " + audioFormat);
		sb.append("\n");
		sb.append("channels: " + channels);
		sb.append("\n");
		sb.append("sampleRate: " + sampleRate);
		sb.append("\n");
		sb.append("byteRate: " + byteRate);
		sb.append("\n");
		sb.append("blockAlign: " + blockAlign);
		sb.append("\n");
		sb.append("bitsPerSample: " + bitsPerSample);
		sb.append("\n");
		sb.append("subChunk2Id: " + subChunk2Id);
		sb.append("\n");
		sb.append("subChunk2Size: " + subChunk2Size);		
		return sb.toString();
	}
}


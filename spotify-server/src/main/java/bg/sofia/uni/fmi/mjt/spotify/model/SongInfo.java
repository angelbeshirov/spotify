package bg.sofia.uni.fmi.mjt.spotify.model;

import java.io.Serializable;

/**
 * Contains the info needed for the client to
 * start receiving song data.
 *
 * @author angel.beshirov
 */
public class SongInfo implements Serializable {
    private static final long serialVersionUID = 867518632342411123L;
    private String encoding;
    private float sampleRate;
    private int sampleSizeInBits;
    private int channels;
    private int frameSize;
    private float frameRate;
    private boolean bigEndian;

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getSampleSizeInBits() {
        return sampleSizeInBits;
    }

    public void setSampleSizeInBits(int sampleSizeInBits) {
        this.sampleSizeInBits = sampleSizeInBits;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public int getFrameSize() {
        return frameSize;
    }

    public void setFrameSize(int frameSize) {
        this.frameSize = frameSize;
    }

    public float getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(float frameRate) {
        this.frameRate = frameRate;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    public void setBigEndian(boolean bigEndian) {
        this.bigEndian = bigEndian;
    }

    @Override
    public String toString() {
        return "SongInfo{" +
                "encoding='" + encoding + '\'' +
                ", sampleRate=" + sampleRate +
                ", sampleSizeInBits=" + sampleSizeInBits +
                ", channels=" + channels +
                ", frameSize=" + frameSize +
                ", frameRate=" + frameRate +
                ", bigEndian=" + bigEndian +
                '}';
    }
}

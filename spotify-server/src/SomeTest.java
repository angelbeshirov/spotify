import jdk.jfr.StackTrace;
import org.junit.Test;

import javax.sound.sampled.*;
import java.io.*;

/**
 * @author angel.beshirov
 */
public class SomeTest {

    @Test
    public void testPlaying() throws IOException, UnsupportedAudioFileException, LineUnavailableException, InterruptedException {
        FileInputStream inputStream = new FileInputStream(new File("src\\main\\resources\\ConCalma.wav"));
        AudioInputStream stream = AudioSystem.getAudioInputStream(new File("src\\main\\resources\\ConCalma.wav"));
        SourceDataLine dataLine = AudioSystem.getSourceDataLine(stream.getFormat());
        dataLine.open();
        dataLine.start();

        final int bufferSize = 2048; // in Bytes
//        dataLine.open(audioFormat, bufferSize);
//        dataLine.start();
        byte counter = 0;
        final byte[] buffer = new byte[bufferSize];
        byte sign = 1;
        int k = 0;
        while ((k = inputStream.read(buffer)) != -1) {
//            int threshold = audioFormat.getFrameRate() / sliderValue;
//            for (int i = 0; i < bufferSize; i++) {
//                if (counter > threshold) {
//                    sign = (byte) -sign;
//                    counter = 0;
//                }
//                buffer[i] = (byte) (sign * 30);
//                counter++;
//            }
            // the next call is blocking until the entire buffer is
            // sent to the SourceDataLine
            dataLine.write(buffer, 0, k);
        }
    }

    @Test
    public void test2() {
        try {
            File yourFile;
            AudioInputStream stream;
            AudioFormat format;
            DataLine.Info info;
            Clip clip;

            stream = AudioSystem.getAudioInputStream(new File("src\\main\\resources\\ConCalma.wav"));
            format = stream.getFormat();
            info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            clip.start();

            while(true);
        }
        catch (Exception e) {
            //whatevers
        }
    }
}

package haven;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import java.io.*;
import java.util.Random;

public class ZeeMidiRadio {

    static String pathFolder = Utils.getpref("midiRadioPath","");

    private static Sequencer sequencer = null;

    static void playRandomMidi(){

        if (sequencer!=null && sequencer.isRunning()){
            ZeeConfig.println("already playing midi");
            return;
        }

        if (pathFolder==null || pathFolder.isBlank()){
            ZeeConfig.println("midi radio path is empty");
            return;
        }

        try {

            // select random midi file
            final File dir = new File(pathFolder);
            File[] files = dir.listFiles((dir1, name) -> (name.toLowerCase().endsWith(".mid") || name.toLowerCase().endsWith(".midi")));
            if (files == null || files.length==0){
                ZeeConfig.println("no midi files in folder "+pathFolder);
                return;
            }
            Random rand = new Random();
            File midiFileSelected = files[rand.nextInt(files.length)];//TODO check file format

            // open default sequencer
            sequencer = MidiSystem.getSequencer();
            stopPlayingMidi();
            sequencer.open();

            // create midi stream
            InputStream is = new BufferedInputStream(new FileInputStream(midiFileSelected));

            // play
            sequencer.setSequence(is);
            sequencer.start();

            ZeeConfig.println("playing "+midiFileSelected.getName());

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    static void stopPlayingMidi(){
        try {
            if (sequencer!=null) {
                if (sequencer.isRunning()) {
                    ZeeConfig.println("midi stop");
                    sequencer.stop();
                }
                if (sequencer.isOpen()) {
                    ZeeConfig.println("midi close");
                    sequencer.close();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
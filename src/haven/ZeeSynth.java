package haven;

import java.util.Arrays;
import java.util.List;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;

public class ZeeSynth extends Thread{

    private static List<String> notes = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private static MidiChannel[] channels;
    private static int INSTRUMENT = 0; // 0 is a piano, 9 is percussion, other channels are for other instruments
    private static int VOLUME = 80; // between 0 et 127
    int lastVolume = -1;

    private static String[] playNotes;

    public ZeeSynth(String[] notess) {
        playNotes = notess;
    }

    public void run() {

        String[] split;

        try {
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            channels = synth.getChannels();

            for (String s: playNotes){
                split = s.split(",");
                if(split.length==1) {
                    rest(Integer.parseInt(split[0]));
                }else if(split.length==2){
                    if(lastVolume<0){
                        lastVolume = VOLUME;
                    }
                    play(split[0], Integer.parseInt(split[1]), lastVolume);
                }else if(split.length==3){
                    lastVolume = Integer.parseInt(split[2]);
                    play(split[0], Integer.parseInt(split[1]), lastVolume);
                }else
                    System.out.println("ZeeSynth empty notes");
            }

            synth.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
        "2F#,500,100",
        "2G,250,120",
     */
    private static void play(String note, int duration, int volume) throws InterruptedException
    {
        channels[INSTRUMENT].noteOn(getMidiNoteId(note), volume );
        Thread.sleep( duration );
        channels[INSTRUMENT].noteOff(getMidiNoteId(note));
    }

    private static void rest(int duration) throws InterruptedException
    {
        Thread.sleep(duration);
    }

    private static int getMidiNoteId(String note)
    {
        int octave = Integer.parseInt(note.substring(0, 1));
        return notes.indexOf(note.substring(1)) + 12 * octave + 12;
    }
}

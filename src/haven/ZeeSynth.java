package haven;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;
import javax.sound.sampled.*;

public class ZeeSynth extends Thread{

    private static List<String> midiNotes = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private static MidiChannel[] midiChannels;
    private static int midiVolume = 80; // between 0 et 127
    private static int midiInstrument; // 0 piano, 9 percussion
    int midiLastVolume = -1;
    private static String[] midiPlayNotes;

    private String filePath = null;
    public static float volumeFile = 1.0F;
    private Clip clip;

    public ZeeSynth(String[] notess) {
        midiInstrument = 0;//piano
        midiPlayNotes = notess;
    }

    public ZeeSynth(String[] midiNotes, int instr) {
        midiInstrument = instr;
        midiPlayNotes = midiNotes;
    }

    public ZeeSynth(String filePath) {
        this.filePath = filePath;
    }

    private void player(){
        File audio = null;
        AudioInputStream stream = null;
        //AIFC, AIFF, AU, SND, and WAVE
        try {
            volumeFile = Float.parseFloat(Utils.getpref("sfxvol", "1.0"));
            audio = new File(filePath).getCanonicalFile();
            stream = AudioSystem.getAudioInputStream(audio);
            clip = AudioSystem.getClip();
            clip.addLineListener(new LineListener() {
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        ZeeConfig.playingAudio = null;
                        clip.close();
                    }
                }
            });
            clip.open(stream);
            setVolume(volumeFile);
            //synchronized (clip){
                clip.start();
            //}
        } catch (Exception ex) {
            ex.printStackTrace();
            if(clip!=null && clip.isOpen()) {
                clip.close();
            }
        }finally {
            ZeeConfig.playingAudio = null;
        }
    }

    public void run() {
        try {

            //play file
            if(this.filePath != null){
                player();
                return;
            }

            //do midi
            String[] split;
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            midiChannels = synth.getChannels();

            for (String s: midiPlayNotes){
                split = s.split(",");
                if(split.length==1) {
                    rest(Integer.parseInt(split[0]));
                }else if(split.length==2){
                    if(midiLastVolume <0){
                        midiLastVolume = midiVolume;
                    }
                    play(split[0], Integer.parseInt(split[1]), midiLastVolume);
                }else if(split.length==3){
                    midiLastVolume = Integer.parseInt(split[2]);
                    play(split[0], Integer.parseInt(split[1]), midiLastVolume);
                }else
                    System.out.println("ZeeSynth empty notes");
            }

            synth.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            ZeeConfig.playingAudio = null;
        }
    }

    public float getVolume() {
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        return (float) Math.pow(10f, gainControl.getValue() / 20f);
    }

    public void setVolume(float volume) {
        if (volume < 0f || volume > 1f)
            throw new IllegalArgumentException("Volume not valid: " + volume);
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(20f * (float) Math.log10(volume));
    }

    /*
        "2F#,500,100",
        "2G,250,120",
     */
    private static void play(String note, int duration, int volume) throws InterruptedException
    {
        midiChannels[midiInstrument].noteOn(getMidiNoteId(note), volume );
        Thread.sleep( duration );
        midiChannels[midiInstrument].noteOff(getMidiNoteId(note));
    }

    private static void rest(int duration) throws InterruptedException
    {
        Thread.sleep(duration);
    }

    private static int getMidiNoteId(String note)
    {
        int octave = Integer.parseInt(note.substring(0, 1));
        return midiNotes.indexOf(note.substring(1)) + 12 * octave + 12;
    }
}

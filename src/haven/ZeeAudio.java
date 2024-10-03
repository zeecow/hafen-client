package haven;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.*;

public class ZeeAudio extends Thread{

    public static final Resource errsfx = Resource.local().loadwait("sfx/error");
    public static final Resource msgsfx = Resource.local().loadwait("sfx/msg");
    public static final Audio.Clip msgsfxLow = Audio.resclip(Resource.local().loadwait("sfx/hud/mmap/wood4"));
    public static final Audio.Clip msgsfxPlayer = Audio.resclip(Resource.local().loadwait("sfx/hud/mmap/bell1"));

    private static List<String> midiNotes = Arrays.asList("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");
    private static MidiChannel[] midiChannels;
    private static int midiVolume = 80; // between 0 et 127
    private static int midiInstrument; // 0 piano, 9 percussion
    int midiLastVolume = -1;
    private static String[] midiPlayNotes;

    private String filePath = null;
    public static float volumeFile = 1.0F;
    private Clip clip;

    public ZeeAudio(String[] notess) {
        midiInstrument = 0;//piano
        midiPlayNotes = notess;
    }

    public ZeeAudio(String[] midiNotes, int instr) {
        midiInstrument = instr;
        midiPlayNotes = midiNotes;
    }

    public ZeeAudio(String filePath) {
        this.filePath = filePath;
    }

    static boolean speechBusy = false;
    static String speechLast = "";
    static long speechLastMs = 0;
    static List<String> speechQueue = new ArrayList<>();
    public static void textToSpeakLinuxFestival(String text) {

        if (text==null || text.isEmpty())
            return;

        // spam control
        if (speechBusy){
            // queue text if not already queued
            if (!speechQueue.contains(text)) {
                //ZeeConfig.println("speech queue > add "+speechQueue.size()+": " + text);
                speechQueue.add(text);
            }
            return;
        }
        // allow repeated texts if more than X ms elapsed
        else if (speechLast.contentEquals(text) && speechLastMs>0 && ZeeThread.now()-speechLastMs < 2000) {
            return;
        }
        // text queued successfully
        else {
            //ZeeConfig.println("speech queue > add 1: " + text);
            speechQueue.add(text);
        }

        // start speaking from queue
        speechBusy = true;
        new Thread(() -> {
            try {
                while (speechQueue.size() > 0) {
                    String lettersNumsSpaceOnly = speechQueue.get(0).replaceAll("[^a-zA-Z0-9\\s,.!?\\-]", "");
                    ZeeConfig.println("speaking: " + lettersNumsSpaceOnly);
                    String cmd = "echo " + lettersNumsSpaceOnly + " | festival --tts";
                    final Process p = Runtime.getRuntime().exec(new String[]{"bash", "-l", "-c", cmd});
                    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = null;
                    while ((line = input.readLine()) != null)
                        System.out.println(line);
                    //wait command finish
                    p.waitFor();
                    //set flags after command finished
                    speechLast = speechQueue.remove(0);
                    speechLastMs = ZeeThread.now();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            speechQueue.clear();
            speechLast = "";
            speechBusy = false;
        }).start();
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
            if(midiPlayNotes==null)//TODO temporary fix for category having no sound set
                return;
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




    static boolean monitorAudioClip;
    static HashMap<String,List<Integer>> mapAudioResClip;
    static ZeeWindow.ZeeButton audioFilterUpdBtn;
    @SuppressWarnings("unchecked")
    static void checkAudioClip(String resname, int subClip) {

        if (mapAudioResClip == null)
            mapAudioResClip = new HashMap();

        List<Integer> subclips = mapAudioResClip.get(resname);
        if (subclips==null){
            subclips = new ArrayList();
            subclips.add(subClip);
            mapAudioResClip.put(resname, subclips);
        }
        else if(!subclips.contains(subClip)){
            subclips.add(subClip);
            mapAudioResClip.put(resname, subclips);
        }
        if (audioFilterUpdBtn!=null){
            audioFilterUpdBtn.change("refresh "+mapAudioResClip.size());
        }
    }
    static void windowAudioFilter() {

        // first run requirement
        monitorAudioClip = true;

        // create window
        String title = "Audio Filter";
        Window win = ZeeConfig.getWindow(title);
        if(win==null) {
            win = ZeeConfig.gameUI.add(new Window(Coord.of(225, 100), title) {
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("close")) {
                        monitorAudioClip = false;
                        this.reqdestroy();
                    }
                }
            },ZeeConfig.gameUI.sz.div(3));
            audioFilterUpdBtn = win.add(new ZeeWindow.ZeeButton("refresh " + (mapAudioResClip == null ? 0 : mapAudioResClip.size())) {
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate")) {
                        updAudioFilterWindow();
                    }
                }
            }, 0, 0);
            win.add(new Label("leftclick plays, midclick blocks"),0,audioFilterUpdBtn.sz.y);
        }
    }
    private static void updAudioFilterWindow() {
        String title = "Audio Filter";
        Window win = ZeeConfig.getWindow(title);
        if (win==null){
            ZeeConfig.msgError("no audio filter window");
            return;
        }
        if (mapAudioResClip!=null) {
            //rem buttons
            for (ButtonAudioFilter b : win.children(ButtonAudioFilter.class)) {
                b.reqdestroy();
            }
            //add buttons
            int x = 0, y = audioFilterUpdBtn.sz.y + 25;
            Widget wdg;
            for (Map.Entry<String, List<Integer>> ent : mapAudioResClip.entrySet()) {
                // resname button
                wdg = win.add(new ButtonAudioFilter(ent.getKey(),ent.getKey()), x, y);
                wdg.settip("block all variations");
                x += wdg.sz.x;
                // subclips buttons
                List<Integer> subclips = ent.getValue();
                for (Integer clips : subclips) {
                    wdg = win.add(new ButtonAudioFilter(clips.toString(),ent.getKey()), x, y);
                    x += wdg.sz.x;
                }
                y += 25;
                x = 0;
            }
            win.pack();
        }
    }

    private static class ButtonAudioFilter extends ZeeWindow.ZeeButton {
        boolean isSubClip;
        String resname;
        public ButtonAudioFilter(String buttonText, String resname) {
            super(buttonText);
            this.resname = resname;
            try {
                isSubClip = true;
                Integer.parseInt(buttonText);
            }catch (NumberFormatException e){
                isSubClip = false;
            }
        }
        // avoid click btn sound
        protected void unpress() {
        }
        protected void depress() {
        }
        // ignore rwidget warnings
        public void wdgmsg(String msg, Object... args) {
        }
        public void wdgmsg(Widget sender, String msg, Object... args) {
        }
        // play on mouse up
        public boolean mouseup(Coord c, int button) {
            if (button==1) {
                //play audio
                String id = "cl";
                List<Audio.Clip> clips = AudioSprite.clips(Loading.waitfor(Resource.remote().load(resname)), id);
                if (isSubClip) {
                    int i = Integer.parseInt(buttonText);
                    Audio.play(clips.get(i).stream());
                }else {
                    Audio.play(clips.get(0).stream());
                }
            }else if (button==2){
                // toggle filter audio
                // set button text to red?
                ZeeConfig.msgLow(this.buttonText);
                this.change(buttonText, Color.red);
            }
            return super.mouseup(c,button);
        }
    }
}

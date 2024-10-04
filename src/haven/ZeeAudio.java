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




    static boolean aBlockMonitor;
    static HashMap<String,List<Integer>> mapButtonsResClip;
    static ZeeWindow.ZeeButton aBlockUpdBtn;
    static final String AUDIO_BLOCKER_WIN_TITLE = "Audio Blocker";
    @SuppressWarnings("unchecked")
    static void aBlockCheck(String resname, int subClip) {
        if (mapButtonsResClip == null) {
            mapButtonsResClip = new HashMap<>();
        }
        List<Integer> subclips = mapButtonsResClip.get(resname);
        if (subclips==null){
            subclips = new ArrayList();
            subclips.add(subClip);
            mapButtonsResClip.put(resname, subclips);
        }
        else if(!subclips.contains(subClip)){
            subclips.add(subClip);
            mapButtonsResClip.put(resname, subclips);
        }
        if (aBlockUpdBtn !=null){
            aBlockUpdBtn.change("refresh "+ aBlockCountWinClips());
        }
        aBlockButtonBlink(resname,subClip);
    }
    private static void aBlockButtonBlink(String resname, int subClip) {
        //TODO blink button when sound is played
    }
    private static void aBlockAddSavedPrefsToWindow() {
        println("aBlockAddSavedPrefsToWindow > ");
        // load saved config
        if (!ZeeConfig.mapAudioBlocker.isEmpty()) {
            if (mapButtonsResClip == null)
                mapButtonsResClip = new HashMap<>();
            //deep copy saved prefs to temp mapButtons
            for (Map.Entry<String, List<Integer>> ent : ZeeConfig.mapAudioBlocker.entrySet()) {
                List<Integer> savedClips = ent.getValue();
                List<Integer> btnsClips = mapButtonsResClip.get(ent.getKey());
                if (savedClips!=null) {
                    for (Integer clip : savedClips) {
                        if (btnsClips==null)
                            btnsClips = new ArrayList<>();
                        if (!btnsClips.contains(clip))
                            btnsClips.add(clip);
                    }
                    mapButtonsResClip.put(ent.getKey(), btnsClips);
                }
            }
            aBlockWinUpd();
        }
    }

    static void aBlockWin() {

        // first run requirement
        aBlockMonitor = true;

        // create window
        Window win = ZeeConfig.getWindow(AUDIO_BLOCKER_WIN_TITLE);
        if(win==null) {
            win = ZeeConfig.gameUI.add(new Window(Coord.of(225, 100), AUDIO_BLOCKER_WIN_TITLE) {
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("close")) {
                        aBlockMonitor = false;
                        this.reqdestroy();
                    }
                }
            },ZeeConfig.gameUI.sz.div(3));
            aBlockUpdBtn = win.add(new ZeeWindow.ZeeButton("refresh " + (mapButtonsResClip == null ? 0 : aBlockCountWinClips().toString())) {
                public void wdgmsg(String msg, Object... args) {
                    if (msg.contentEquals("activate")) {
                        aBlockWinUpd();
                    }
                }
            }, 0, 0);
            win.add(new Label("leftclick plays, midclick blocks"),0, aBlockUpdBtn.sz.y);
        }
        aBlockAddSavedPrefsToWindow();
        aBlockWinUpd();
    }
    private static void aBlockWinUpd() {
        println("aBlockWinUpd > ");
        Window win = ZeeConfig.getWindow(AUDIO_BLOCKER_WIN_TITLE);
        if (win==null){
            ZeeConfig.msgError("no audio blocker window");
            return;
        }
        if (mapButtonsResClip !=null) {
            //rem buttons
            for (ButtonAudioFilter b : win.children(ButtonAudioFilter.class)) {
                b.reqdestroy();
            }
            //add buttons
            int x = 0, y = aBlockUpdBtn.sz.y + 25;
            Widget wdg;
            for (Map.Entry<String, List<Integer>> ent : mapButtonsResClip.entrySet()) {
                // resname button
                wdg = win.add(new ButtonAudioFilter(ent.getKey(),ent.getKey()), x, y);
                wdg.settip(ent.getKey());//settip("block all variations");
                x += wdg.sz.x;
                // subclips buttons
                List<Integer> subclips = ent.getValue();
                if (subclips!=null){
                    for (Integer clips : subclips) {
                        wdg = win.add(new ButtonAudioFilter(clips.toString(), ent.getKey()), x, y);
                        wdg.settip(ent.getKey());
                        x += wdg.sz.x;
                    }
                }
                y += 25;
                x = 0;
            }
            win.pack();
        }
        ZeeConfig.println("pref "+ZeeConfig.mapAudioBlocker);
        ZeeConfig.println("temp "+mapButtonsResClip);
    }
    private static Integer aBlockCountWinClips() {
        int ret = mapButtonsResClip.size();
        for (Map.Entry<String, List<Integer>> ent : mapButtonsResClip.entrySet()) {
            if (ent.getValue()!=null){
                ret += ent.getValue().size();
                if (ent.getValue().contains(0))
                    ret--;//resname button counts as clip 0
            }
        }
        return ret;
    }
    private static class ButtonAudioFilter extends ZeeWindow.ZeeButton {
        boolean isBlocking;
        String resname;
        Integer subClip;
        final Color colorBlocked = Color.decode("#8c1000");
        public ButtonAudioFilter(String buttonText, String resname) {
            super(buttonText);
            this.resname = resname;
            try {
                subClip = Integer.parseInt(buttonText);
            }catch (NumberFormatException e){
                subClip = null;
            }
            isBlocking = false;
            if (ZeeConfig.mapAudioBlocker.containsKey(resname)){
                List<Integer> savedClips = ZeeConfig.mapAudioBlocker.get(resname);
                //null means block any subclips
                if (savedClips==null || (subClip!=null && savedClips.contains(subClip))){
                    //ZeeConfig.println("savedClips "+savedClips+" , subclip "+subClip+" , "+resname);
                    this.isBlocking = true;
                }
            }
            if (isBlocking)
                this.change(buttonText, colorBlocked);
            else
                this.change(buttonText);
        }
        // avoid click btn sound
        protected void unpress(){}
        protected void depress(){}
        // ignore rwidget warnings
        public void wdgmsg(String msg, Object... args){}
        public void wdgmsg(Widget sender, String msg, Object... args){}
        // play on mouse up
        public boolean mouseup(Coord c, int button) {
            try {
                //play audio
                if (button == 1) {
                    String id = "cl";// TODO add others?
                    List<Audio.Clip> clips = AudioSprite.clips(Resource.remote().loadwait(resname), id);
                    if (subClip !=null) {
                        int i = Integer.parseInt(buttonText);
                        Audio.play(clips.get(i).stream());
                    } else {
                        Audio.play(clips.get(0).stream());
                    }
                }
                // toggle filter audio
                else if (button == 2) {
                    isBlocking = !isBlocking;
                    // update buttons colors
                    if (isBlocking)
                        this.change(buttonText, colorBlocked);
                    else
                        this.change(buttonText);
                    //update map
                    HashMap<String, List<Integer>> map = ZeeConfig.mapAudioBlocker;
                    if (isBlocking){
                        // add to map
                        if (!map.containsKey(resname)){
                            // new resname
                            List<Integer> savedSubClips = null;
                            if (subClip!=null) {
                                savedSubClips = new ArrayList<>();
                                savedSubClips.add(subClip);
                            }
                            map.put(resname,savedSubClips);
                        }else{
                            // resname already present
                            List<Integer> savedSubClips = map.get(resname);
                            if (subClip==null) {
                                //block all subclips from resname
                                savedSubClips = null;
                            }else{
                                // block specific clip
                                if (savedSubClips==null)
                                    savedSubClips = new ArrayList<>();
                                if (!savedSubClips.contains(subClip))
                                    savedSubClips.add(subClip);
                            }
                            map.put(resname,savedSubClips);
                        }
                    }
                    else{
                        // remove from map
                        if (map.containsKey(resname)){
                            List<Integer> savedSubClips = map.get(resname);
                            if (savedSubClips!=null)
                                savedSubClips.remove(subClip);
                            if (savedSubClips==null || savedSubClips.isEmpty())
                                savedSubClips = null;
                            if (savedSubClips!=null)
                                map.put(resname,savedSubClips);
                            else
                                map.remove(resname);
                        }
                    }
                    //save map to prefs
                    Utils.setpref(ZeeConfig.MAP_AUDIO_BLOCKER,ZeeConfig.serialize(ZeeConfig.mapAudioBlocker));
                    ZeeConfig.println("saved map "+ZeeConfig.mapAudioBlocker);
                    // update window
                    aBlockWinUpd();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return super.mouseup(c,button);
        }
    }

    private static void println(String s) {
        ZeeConfig.println(s);
    }
}

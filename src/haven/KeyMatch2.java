package haven;

import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.*;

public class KeyMatch2 extends KeyMatch {
    
    public KeyMatch2(char chr, boolean casematch, int code, boolean extmatch, String keyname, int modmask, int modmatch) {
	super(chr, casematch, code, extmatch, keyname, modmask, modmatch);
    }
    
    public static KeyMatch from(KeyMatch key) {
	if(key == null) {
	    return null;
	}
	return new KeyMatch2(key.chr, key.casematch, key.code, key.extmatch, key.keyname, key.modmask, key.modmatch);
    }
    
    @Override
    public boolean match(KeyEvent ev, int modign) {
	return match2(ev, modign);
    }
    
    public static KeyMatch forchar(char chr, int modmask, int modmatch) {
	return (new KeyMatch2(chr, false, VK_UNDEFINED, false, Character.toString(chr), modmask, modmatch));
    }
    
    public static KeyMatch forchar(char chr, int mods) {
	return (forchar(chr, S | C | M, mods));
    }
    
    public static KeyMatch forcode(int code, int modmask, int modmatch) {
	return (new KeyMatch2('\0', false, code, false, KeyEvent.getKeyText(code), modmask, modmatch));
    }
    
    public static KeyMatch forcode(int code, int mods) {
	return (forcode(code, S | C | M, mods));
    }
    
    public static KeyMatch forevent(KeyEvent ev, int modmask) {
	int mod = mods(ev) & modmask;
	char key = Character.toUpperCase(ev.getKeyChar());
	int code = ev.getExtendedKeyCode();
	if(key == KeyEvent.CHAR_UNDEFINED)
	    key = 0;
	if(code != VK_UNDEFINED) {
	    String nm;
	    if(ev.getKeyCode() != VK_UNDEFINED)
		nm = KeyEvent.getKeyText(ev.getKeyCode());
	    else if(!Character.isISOControl(key))
		nm = Character.toString(key);
	    else
		nm = String.format("%X", code);
	    return (new KeyMatch('\0', false, code, true, nm, modmask, mod));
	}
	if(!Character.isISOControl(key))
	    return (new KeyMatch(key, false, VK_UNDEFINED, false, Character.toString(key), modmask, mod));
	return (null);
    }
}

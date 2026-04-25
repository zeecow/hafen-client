/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven.iosys.tk;

import haven.*;
import java.lang.annotation.*;
import java.util.*;
import java.awt.image.BufferedImage;

public interface Toolkit {
    public Cursor.Caps cursorcaps();
    public Cursor makecursor(BufferedImage img, Coord hotspot);
    public Windeye window();
    public void dispose();

    public static interface Factory {
	public Toolkit open(String... args);
	public default int order() {return(0);}
    }

    @dolda.jglob.Discoverable
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Available {
	String name();
    }

    public static Map<String, Toolkit.Factory> toolkits() {
	return(Found.toolkits());
    }

    public static interface Event {
    }

    public static interface CloseRequest extends Event {
    }

    public static interface KeyEvent extends Event {
	public String string();
	public Key key();
	public Set<Key.Mod> mods();
    }
    public static interface KeyDownEvent extends KeyEvent {}
    public static interface KeyUpEvent extends KeyEvent {}

    public static interface MouseEvent extends Event {
	public Coord wndc();
	public Collection<Integer> held();
	public Set<Key.Mod> mods();
    }
    public static interface MouseButtonEvent extends MouseEvent {
	public int button();
    }
    public static interface MouseDownEvent extends MouseButtonEvent {}
    public static interface MouseUpEvent extends MouseButtonEvent {}
    public static interface MouseMoveEvent extends MouseEvent {}
    public static interface MouseWheelEvent extends MouseEvent {
	public int amount();
    }

    public static interface EventListener {
	public void event(Event ev);
    }
}

class Found {
    private static Map<String, Toolkit.Factory> find() {
	Map<String, Toolkit.Factory> ret = new HashMap<>();
	ClassLoader loader = Toolkit.Available.class.getClassLoader();
	for(String clnm : dolda.jglob.Loader.get(Toolkit.Available.class).names()) {
	    try {
		Class<?> cl;
		try {
		    cl = loader.loadClass(clnm);
		} catch(ClassNotFoundException e) {
		    continue;
		}
		String nm = cl.getAnnotation(Toolkit.Available.class).name();
		Toolkit.Factory fac;
		try {
		    fac = (Toolkit.Factory)Utils.invoke(cl.getDeclaredMethod("get"), null);
		} catch(NoSuchMethodException e) {
		    throw(new AssertionError(e));
		} catch(Unavailable e) {
		    fac = new Toolkit.Factory() {
			    public Toolkit open(String... args) {throw(e);}
			    public int priority() {return(-1000);}
			};
		}
		ret.put(nm, fac);
	    } catch(UnsupportedClassVersionError e) {
		continue;
	    }
	}
	return(ret);
    }

    private static Map<String, Toolkit.Factory> toolkits;
    static Map<String, Toolkit.Factory> toolkits() {
	if(toolkits == null) {
	    synchronized(Found.class) {
		if(toolkits == null) {
		    toolkits = find();
		}
	    }
	}
	return(toolkits);
    }
}

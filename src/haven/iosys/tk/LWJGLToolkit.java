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

import java.util.*;
import java.awt.event.*;
import org.lwjgl.opengl.awt.*;
import haven.*;
import haven.render.*;
import haven.render.gl.*;
import haven.render.lwjgl.*;
import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import static java.awt.event.KeyEvent.*;

@Toolkit.Available(name = "lwjgl")
public class LWJGLToolkit extends AWTToolkit {
    private LWJGLToolkit() {}

    private static Factory factory = new Factory() {
	private LWJGLToolkit instance = null;

	public Toolkit open(String... args) {
	    synchronized(this) {
		if(instance == null)
		    instance = new LWJGLToolkit();
	    }
	    return(instance);
	}

	public int priority() {return(-11);}
    };
    public static Factory get() {
	return(factory);
    }

    private static final int[][] glversions = {
	{4, 6}, {4, 5}, {4, 4}, {4, 3}, {4, 2}, {4, 1}, {4, 0},
	{3, 3}, {3, 2},
    };
    public class LWJGLPanel extends AWTGLCanvas {
	public LWJGLEnvironment env;

	public LWJGLPanel() {
	    super();
	}

	public void initGL() {}
	public void paintGL() {}

	protected ContextData createContext() throws AWTException {
	    for(int[] ver : glversions) {
		GLData caps = new GLData();
		caps.majorVersion = ver[0];
		caps.minorVersion = ver[1];
		caps.profile = GLData.Profile.CORE;
		try {
		    return(createContext(caps));
		} catch(AWTException e) {
		    /* Try next */
		}
	    }
	    /* Try to get whatever and see if LWJGLEnvironment considers
	     * that to pass muster. */
	    return(createContext(new GLData()));
	}

	private void glrun(Runnable task) {
	    awtrun(() -> runInContext(task));
	}

	private void glswap(haven.render.gl.GL gl) {
	    GLException.checkfor(gl, null);
	    swapBuffers();
	    GLException.checkfor(gl, null);
	}

	private void initgl() {
	    setSwapInterval(1);
	}

	private void process() {
	    LWJGLEnvironment env = this.env;
	    Area shape = Area.sized(Coord.of(getWidth(), getHeight()));
	    if(!env.shape().equals(shape)) {
		env.reshape(shape);
	    }
	    runInContext(() -> {
		env.process(LWJGLWrap.instance);
	    });
	}

	private LWJGLEnvironment env() {
	    if(env == null) {
		glrun(() -> {
		    synchronized(this) {
			if(env == null) {
			    org.lwjgl.opengl.GL.createCapabilities();
			    Area shape = Area.sized(Coord.of(getWidth(), getHeight()));
			    this.env = new LWJGLEnvironment(shape) {
				public void submit(Render cmd) {
				    super.submit(cmd);
				    EventQueue.invokeLater(LWJGLPanel.this::process);
				}
			    };
			    initgl();
			}
		    }
		});
		if(env == null)
		    throw(new RuntimeException("LWJGL environment initialization mysteriously failed"));
	    }
	    return(env);
	}
    }

    public class LWJGLWindow extends AWTWindow {
	public final LWJGLPanel panel;
	private final EventQueue dsp;

	public LWJGLWindow() {
	    panel = new LWJGLPanel();
	    frame.add(panel);
	    frame.pack();
	    panel.requestFocus();
	    (dsp = new EventQueue()).register();
	}

	protected LWJGLPanel panel() {return(panel);}

	public Environment env() {
	    return(panel.env());
	}

	public void swapbuffers(Render buf) {
	    GLRender gbuf = (GLRender)buf;
	    if(gbuf.env != panel.env)
		throw(new IllegalArgumentException());
	    gbuf.submit(panel::glswap);
	    java.awt.EventQueue.invokeLater(dsp::process);
	}
    }

    public Windeye window() {
	return(new LWJGLWindow());
    }
}

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

package haven;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

public class Button extends SIWidget {
    public static final BufferedImage bl = Resource.loadsimg("gfx/hud/buttons/tbtn/left");
    public static final BufferedImage br = Resource.loadsimg("gfx/hud/buttons/tbtn/right");
    public static final BufferedImage bt = Resource.loadsimg("gfx/hud/buttons/tbtn/top");
    public static final BufferedImage bb = Resource.loadsimg("gfx/hud/buttons/tbtn/bottom");
    public static final BufferedImage dt = Resource.loadsimg("gfx/hud/buttons/tbtn/dtex");
    public static final BufferedImage ut = Resource.loadsimg("gfx/hud/buttons/tbtn/utex");
    public static final BufferedImage bm = Resource.loadsimg("gfx/hud/buttons/tbtn/mid");
    public static final int hs = bl.getHeight(), hl = bm.getHeight();
    public static final Resource click = Loading.waitfor(Resource.local().load("sfx/hud/btn"));
    public static final Audio.Clip clbtdown = Loading.waitfor(Resource.local().load("sfx/hud/lbtn")).layer(Resource.audio, "down");
    public static final Audio.Clip clbtup = Loading.waitfor(Resource.local().load("sfx/hud/lbtn")).layer(Resource.audio, "up");
    public static final int margin = UI.scale(10);
    public boolean lg;
    public Text text;
    public BufferedImage cont;
    public Runnable action = null;
    static Text.Foundry tf = new Text.Foundry(Text.serif.deriveFont(Font.BOLD, UI.scale(12f))).aa(true);
    static Text.Furnace nf = new PUtils.BlurFurn(new PUtils.TexFurn(tf, Window.ctex), UI.rscale(0.75), UI.rscale(0.75), new Color(80, 40, 0));
    private boolean a = false, dis = false;
    private UI.Grab d = null;
	
    @RName("btn")
    public static class $Btn implements Factory {
	public Widget create(UI ui, Object[] args) {
	    if(args.length > 2)
		return(new Button(UI.scale(Utils.iv(args[0])), (String)args[1], Utils.bv(args[2])));
	    else
		return(new Button(UI.scale(Utils.iv(args[0])), (String)args[1]));
	}
    }
    @RName("ltbtn")
    public static class $LTBtn implements Factory {
	public Widget create(UI ui, Object[] args) {
	    return(wrapped(UI.scale(Utils.iv(args[0])), (String)args[1]));
	}
    }
	
    public static Button wrapped(int w, String text) {
	Button ret = new Button(w, tf.renderwrap(text, w - margin));
	return(ret);
    }
        
    private static boolean largep(int w) {
	return(w >= (bl.getWidth() + bm.getWidth() + br.getWidth()));
    }

    private Button(int w, boolean lg) {
	super(new Coord(w, lg?hl:hs));
	this.lg = lg;
    }

    public Button(int w, String text, boolean lg, Runnable action) {
	this(w, lg);
	this.text = nf.render(text);
	this.cont = this.text.img;
	this.action = action;
    }

    public Button(int w, String text, boolean lg) {
	this(w, text, lg, null);
	this.action = () -> wdgmsg("activate");
    }

    public Button(int w, String text, Runnable action) {
	this(w, text, largep(w), action);
    }

    public Button(int w, String text) {
	this(w, text, largep(w));
    }

    public Button(int w, Text text) {
	this(w, largep(w));
	this.text = text;
	this.cont = text.img;
    }
	
    public Button(int w, BufferedImage cont) {
	this(w, largep(w));
	this.cont = cont;
    }
	
    public Button action(Runnable action) {
	this.action = action;
	return(this);
    }

    public void draw(BufferedImage img) {
	Graphics g = img.getGraphics();
	int yo = lg?((hl - hs) / 2):0;

	if (ZeeConfig.simpleButtons) {
		int pad = 4;
		g.drawImage(a ? dt : ut,
				UI.scale(4) -(pad/2), // left
				yo + UI.scale(4) -(pad/2), // top
				sz.x - UI.scale(8) +pad, // right
				hs - UI.scale(8) +pad, //bottom
				null
		);
	}else
		g.drawImage(a?dt:ut, UI.scale(4), yo + UI.scale(4), sz.x - UI.scale(8), hs - UI.scale(8), null);

	Coord tc = sz.sub(Utils.imgsz(cont)).div(2);
	if(a)
	    tc = tc.add(UI.scale(1), UI.scale(1));
	g.drawImage(cont, tc.x, tc.y, null);

	if (!ZeeConfig.simpleButtons) {
		g.drawImage(bl, 0, yo, null);
		g.drawImage(br, sz.x - br.getWidth(), yo, null);
		g.drawImage(bt, bl.getWidth(), yo, sz.x - bl.getWidth() - br.getWidth(), bt.getHeight(), null);
		g.drawImage(bb, bl.getWidth(), yo + hs - bb.getHeight(), sz.x - bl.getWidth() - br.getWidth(), bb.getHeight(), null);
		if (lg)
			g.drawImage(bm, (sz.x - bm.getWidth()) / 2, 0, null);
	}

	g.dispose();

	if(dis)
	    PUtils.monochromize(img, Color.LIGHT_GRAY);
    }
	
    public void change(String text, Color col) {
	this.text = tf.render(text, col);
	this.cont = this.text.img;
	redraw();
    }
    
    public void change(String text) {
	this.text = nf.render(text);
	this.cont = this.text.img;
	redraw();
    }

    public void disable(boolean dis) {
	this.dis = dis;
	redraw();
    }

    public void click() {
	if(action != null)
		if (!ui.modctrl && ZeeConfig.DEF_LIST_CONFIRM_BUTTON.contains(this.text.text))
			ZeeConfig.msgError("Ctrl + click to confirm button");
		else
	    	action.run();
    }

    public boolean gkeytype(GlobKeyEvent ev) {
	click();
	return(true);
    }
    
    public void uimsg(String msg, Object... args) {
	if(msg == "ch") {
	    if(args.length > 1)
		change((String)args[0], (Color)args[1]);
	    else
		change((String)args[0]);
	} else if(msg == "dis") {
	    disable(Utils.bv(args[1]));
	} else {
	    super.uimsg(msg, args);
	}
    }
    
    public void mousemove(MouseMoveEvent ev) {
	super.mousemove(ev);
	if(d != null) {
	    boolean a = ev.c.isect(Coord.z, sz);
	    if(a != this.a) {
		this.a = a;
		redraw();
	    }
	}
    }

    protected void depress() {
	ui.sfx(click);
    }

    protected void unpress() {
	ui.sfx(click);
    }

    public boolean mousedown(MouseDownEvent ev) {
	if((ev.b != 1) || dis)
	    return(super.mousedown(ev));
	a = true;
	d = ui.grabmouse(this);
	depress();
	redraw();
	return(true);
    }
	
    public boolean mouseup(MouseUpEvent ev) {
	if (ev.b==2) {
		ZeeConfig.midclickButtonWidget(this);
		return false;
	}
	if((d != null) && ev.b == 1) {
	    d.remove();
	    d = null;
	    a = false;
	    redraw();
	    if(ev.c.isect(Coord.z, sz)) {
		unpress();
		click();
	    }
	    return(true);
	}
	return(super.mouseup(ev));
    }

	@Override
	public void wdgmsg(String msg, Object... args) {
		super.wdgmsg(msg, args);
		if (msg.contentEquals("activate")){
			// build and drink if stamina low enough
			if (ZeeConfig.isBuildAndDrink && this.text.text.contentEquals("Build")) {
				if (ZeeConfig.getMeterStamina() > 85)
					return;
				new ZeeThread() {
					public void run() {
						try {
							sleep(PING_MS);
							if (ZeeConfig.playerHasAnyPose(ZeeConfig.POSE_PLAYER_BUILD)) {
								ZeeManagerItems.drinkFromBeltHandsInv();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
		}
	}
}

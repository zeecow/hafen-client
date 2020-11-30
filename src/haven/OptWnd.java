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


import java.util.LinkedList;
import java.awt.event.KeyEvent;

public class OptWnd extends Window {
    public static final Coord PANEL_POS = new Coord(220, 30);
    public static final Coord Q_TYPE_PADDING = new Coord(3, 0);
    private final Panel display, general, camera, shortcuts;
    public final Panel main, video, audio, keybind;
    public Panel current;
    private WidgetList<KeyBinder.ShortcutWidget> shortcutList;
    
    public void chpanel(Panel p) {
	Coord cc = this.c.add(this.sz.div(2));
	if(current != null)
	    current.hide();
	(current = p).show();
	pack();
	move(cc.sub(this.sz.div(2)));
    }

    public class PButton extends Button {
	public final Panel tgt;
	public final int key;

	public PButton(int w, String title, int key, Panel tgt) {
	    super(w, title);
	    this.tgt = tgt;
	    this.key = key;
	}

	public void click() {
	    chpanel(tgt);
	}

	public boolean keydown(java.awt.event.KeyEvent ev) {
	    if((this.key != -1) && (ev.getKeyChar() == this.key)) {
		click();
		return (true);
	    }
	    return (false);
	}
    }
    
    private static class AButton extends Button {
	public final Action act;
	public final int key;
	
	public AButton(int w, String title, int key, Action act) {
	    super(w, title);
	    this.act = act;
	    this.key = key;
	}
	
	public void click() {
	    if(ui.gui != null) {act.run(ui.gui);}
	}
	
	public boolean keydown(java.awt.event.KeyEvent ev) {
	    if((this.key != -1) && (ev.getKeyChar() == this.key)) {
		click();
		return (true);
	    }
	    return (false);
	}
    }

    public class Panel extends Widget {
	public Panel() {
	    visible = false;
	    c = Coord.z;
	}
    }

    private void error(String msg) {
	GameUI gui = getparent(GameUI.class);
	if(gui != null)
	    gui.error(msg);
    }

    public class VideoPanel extends Panel {
	Panel prev;

	public VideoPanel(Panel prev) {
	    super();
	    this.prev = prev;
	}

	public class CPanel extends Widget {
	    public GSettings prefs;

	    public CPanel(GSettings gprefs) {
		this.prefs = gprefs;
		Composer composer = new Composer(this)
		    .vmrgn(UI.scale(5))
		    .hmrgn(UI.scale(5));
		composer.add(new CheckBox("Render shadows") {
			{a = prefs.lshadow.val;}

			public void set(boolean val) {
			    try {
				GSettings np = prefs.update(null, prefs.lshadow, val);
				ui.setgprefs(prefs = np);
			    } catch(GSettings.SettingException e) {
				error(e.getMessage());
				return;
			    }
			    a = val;
			}
		    });
		composer.add(new Label("Render scale"));
		{
		    Label dpy = new Label("");
		    final int steps = 4;
		    composer.addr(
			new HSlider(UI.scale(160), -2 * steps, 2 * steps, (int)Math.round(steps * Math.log(prefs.rscale.val) / Math.log(2.0f))) {
			    protected void added() {
				dpy();
			    }
			    void dpy() {
				dpy.settext(String.format("%.2f\u00d7", Math.pow(2, this.val / (double)steps)));
			    }
			    public void changed() {
				try {
				    float val = (float)Math.pow(2, this.val / (double)steps);
				    ui.setgprefs(prefs = prefs.update(null, prefs.rscale, val));
				} catch(GSettings.SettingException e) {
				    error(e.getMessage());
				    return;
				}
				dpy();
			    }
			},
			dpy
		    );
		}
		composer.add(new CheckBox("Vertical sync") {
			{a = prefs.vsync.val;}

			public void set(boolean val) {
			    try {
				GSettings np = prefs.update(null, prefs.vsync, val);
				ui.setgprefs(prefs = np);
			    } catch(GSettings.SettingException e) {
				error(e.getMessage());
				return;
			    }
			    a = val;
			}
		    });
		composer.add(new Label("Framerate limit (active window)"));
		{
		    Label dpy = new Label("");
		    final int max = 250;
		    composer.addr(
			    new HSlider(UI.scale(160), 1, max, (prefs.hz.val == Float.POSITIVE_INFINITY) ? max : prefs.hz.val.intValue()) {
			    protected void added() {
				dpy();
			    }
			    void dpy() {
				if(this.val == max)
				    dpy.settext("None");
				else
				    dpy.settext(Integer.toString(this.val));
			    }
			    public void changed() {
				try {
				    if(this.val > 10)
					this.val = (this.val / 2) * 2;
				    float val = (this.val == max) ? Float.POSITIVE_INFINITY : this.val;
				    ui.setgprefs(prefs = prefs.update(null, prefs.hz, val));
				} catch(GSettings.SettingException e) {
				    error(e.getMessage());
				    return;
				}
				dpy();
			    }
			},
			dpy
		    );
		}
		composer.add(new Label("Framerate limit (background window)"));
		{
		    Label dpy = new Label("");
		    final int max = 250;
		    composer.addr(
			    new HSlider(UI.scale(160), 1, max, (prefs.bghz.val == Float.POSITIVE_INFINITY) ? max : prefs.bghz.val.intValue()) {
			    protected void added() {
				dpy();
			    }
			    void dpy() {
				if(this.val == max)
				    dpy.settext("None");
				else
				    dpy.settext(Integer.toString(this.val));
			    }
			    public void changed() {
				try {
				    if(this.val > 10)
					this.val = (this.val / 2) * 2;
				    float val = (this.val == max) ? Float.POSITIVE_INFINITY : this.val;
				    ui.setgprefs(prefs = prefs.update(null, prefs.bghz, val));
				} catch(GSettings.SettingException e) {
				    error(e.getMessage());
				    return;
				}
				dpy();
			    }
			},
			dpy
		    );
		}
		composer.add(new Label("Frame sync mode"));
		{
		    boolean[] done = {false};
		    RadioGroup grp = new RadioGroup(this, composer) {
			    public void changed(int btn, String lbl) {
				if(!done[0])
				    return;
				try {
				    ui.setgprefs(prefs = prefs.update(null, prefs.syncmode, JOGLPanel.SyncMode.values()[btn]));
				} catch(GSettings.SettingException e) {
				    error(e.getMessage());
				    return;
				}
			    }
			};
		    composer.hmrgn(UI.scale(5));
		    composer.add(new Label("\u2191 Better performance, worse latency"));
		    grp.add("One-frame overlap");
		    grp.add("Tick overlap");
		    grp.add("CPU-sequential");
		    grp.add("GPU-sequential");
		    composer.add(new Label("\u2193 Worse performance, better latency"));
		    grp.check(prefs.syncmode.val.ordinal());
		    done[0] = true;
		}
		/* XXXRENDER
		composer.add(new CheckBox("Antialiasing") {
			{a = cf.fsaa.val;}

			public void set(boolean val) {
			    try {
				cf.fsaa.set(val);
			    } catch(GLSettings.SettingException e) {
				error(e.getMessage());
				return;
			    }
			    a = val;
			    cf.dirty = true;
			}
		    });
		composer.add(new Label("Anisotropic filtering"));
		if(cf.anisotex.max() <= 1) {
		    composer.add(new Label("(Not supported)"));
		} else {
		    final Label dpy = new Label("");
		    composer.addRow(
			    new HSlider(UI.scale(160), (int)(cf.anisotex.min() * 2), (int)(cf.anisotex.max() * 2), (int)(cf.anisotex.val * 2)) {
			    protected void added() {
				dpy();
			    }
			    void dpy() {
				if(val < 2)
				    dpy.settext("Off");
				else
				    dpy.settext(String.format("%.1f\u00d7", (val / 2.0)));
			    }
			    public void changed() {
				try {
				    cf.anisotex.set(val / 2.0f);
				} catch(GLSettings.SettingException e) {
				    error(e.getMessage());
				    return;
				}
				dpy();
				cf.dirty = true;
			    }
			},
			dpy
		    );
		}
		*/
		composer.add(new Label("UI scale (requires restart)"));
		{
		    Label dpy = new Label("");
		    final double smin = 1, smax = Math.floor(UI.maxscale() / 0.25) * 0.25;
		    final int steps = (int)Math.round((smax - smin) / 0.25);
		    composer.addr(
			new HSlider(UI.scale(160), 0, steps, (int)Math.round(steps * (Utils.getprefd("uiscale", 1.0) - smin) / (smax - smin))) {
			    protected void added() {
				dpy();
			    }
			    void dpy() {
				dpy.settext(String.format("%.2f\u00d7", smin + (((double)this.val / steps) * (smax - smin))));
			    }
			    public void changed() {
				double val = smin + (((double)this.val / steps) * (smax - smin));
				Utils.setprefd("uiscale", val);
				dpy();
			    }
			},
			dpy
		    );
		}
		composer.add(new Button(UI.scale(200), "Reset to defaults") {
			public void click() {
			    ui.setgprefs(GSettings.defaults());
			    curcf.destroy();
			    curcf = null;
			    back.destroy();
			    back = null;
			}
		    });
		pack();
	    }
	}

	private CPanel curcf = null;
	private PButton back = null;

	public void attach(UI ui) {
	    super.attach(ui);
	    if (curcf != null && back != null) {
		return;
	    }
	    if (curcf != null) {
		curcf.destroy();
	    }
	    curcf = new CPanel(ui.gprefs);
	    if (back != null) {
		back.destroy();
	    }
	    back = new PButton(UI.scale(200), "Back", 27, prev);
	    Composer composer = new Composer(this);
	    composer.add(curcf);
	    composer.add(back);
	    pack();
	}

	public void draw(GOut g) {
	    if((curcf == null) || (ui.gprefs != curcf.prefs)) {
		if(curcf != null)
		    curcf.destroy();
		if(back != null)
		    back.destroy();
		curcf = new CPanel(ui.gprefs);
		back = new PButton(UI.scale(200), "Back", 27, prev);
		Composer composer = new Composer(this).vmrgn(UI.scale(5));
		composer.add(curcf);
		composer.add(back);
		pack();
	    }
	    super.draw(g);
	}
    }

    private static final Text kbtt = RichText.render("$col[255,255,0]{Escape}: Cancel input\n" +
						     "$col[255,255,0]{Backspace}: Revert to default\n" +
						     "$col[255,255,0]{Delete}: Disable keybinding", 0);
    public class BindingPanel extends Panel {
	private void addbtn(Composer cont, int width, String nm, KeyBinding cmd) {
	    cont.addrf(width / 2, new Label(nm), new SetButton(width / 2, cmd));
	}

	public BindingPanel(Panel back) {
	    super();
	    Scrollport scrollport = new Scrollport(UI.scale(new Coord(300, 300)));
	    Composer scroll = new Composer(scrollport.cont)
		.vmrgn(UI.scale(5))
		.hpad(UI.scale(5));
	    int width = scrollport.cont.sz.x - UI.scale(5);
	    scroll.adda(new Label("Main menu"), width / 2, 0.5);
	    addbtn(scroll, width, "Inventory", GameUI.kb_inv);
	    addbtn(scroll, width, "Equipment", GameUI.kb_equ);
	    addbtn(scroll, width, "Character sheet", GameUI.kb_chr);
	    addbtn(scroll, width, "Map window", GameUI.kb_map);
	    addbtn(scroll, width, "Kith & Kin", GameUI.kb_bud);
	    addbtn(scroll, width, "Options", GameUI.kb_opt);
	    addbtn(scroll, width, "Search actions", GameUI.kb_srch);
	    addbtn(scroll, width, "Toggle chat", GameUI.kb_chat);
	    addbtn(scroll, width, "Quick chat", ChatUI.kb_quick);
	    addbtn(scroll, width, "Take screenshot", GameUI.kb_shoot);
	    addbtn(scroll, width, "Minimap icons", GameUI.kb_ico);
	    addbtn(scroll, width, "Toggle UI", GameUI.kb_hide);
	    scroll.adda(new Label("Map options"), width / 2, 0.5);
	    addbtn(scroll, width, "Display claims", GameUI.kb_claim);
	    addbtn(scroll, width, "Display villages", GameUI.kb_vil);
	    addbtn(scroll, width, "Display realms", GameUI.kb_rlm);
	    addbtn(scroll, width, "Display grid-lines", MapView.kb_grid);
	    scroll.adda(new Label("Camera control"), width / 2, 0.5);
	    addbtn(scroll, width, "Rotate left", MapView.kb_camleft);
	    addbtn(scroll, width, "Rotate right", MapView.kb_camright);
	    addbtn(scroll, width, "Zoom in", MapView.kb_camin);
	    addbtn(scroll, width, "Zoom out", MapView.kb_camout);
	    addbtn(scroll, width, "Reset", MapView.kb_camreset);
	    scroll.adda(new Label("Map window"), width / 2, 0.5);
	    addbtn(scroll, width, "Reset view", MapWnd.kb_home);
	    addbtn(scroll, width, "Place marker", MapWnd.kb_mark);
	    addbtn(scroll, width, "Toggle markers", MapWnd.kb_hmark);
	    addbtn(scroll, width, "Compact mode", MapWnd.kb_compact);
	    scroll.adda(new Label("Walking speed"), width / 2, 0.5);
	    addbtn(scroll, width, "Increase speed", Speedget.kb_speedup);
	    addbtn(scroll, width, "Decrease speed", Speedget.kb_speeddn);
	    for(int i = 0; i < 4; i++)
		addbtn(scroll, width, String.format("Set speed %d", i + 1), Speedget.kb_speeds[i]);
	    scroll.adda(new Label("Combat actions"), width / 2, 0.5);
	    for(int i = 0; i < Fightsess.kb_acts.length; i++)
		addbtn(scroll, width, String.format("Combat action %d", i + 1), Fightsess.kb_acts[i]);
	    addbtn(scroll, width, "Switch targets", Fightsess.kb_relcycle);
	    Composer composer = new Composer(this).vmrgn(UI.scale(5));
	    composer.adda(scrollport, scrollport.cont.sz.x / 2, 0.5);
	    composer.vmrgn(0);
	    composer.adda(new PointBind(UI.scale(200)), scrollport.cont.sz.x / 2, 0.5);
	    composer.adda(new PButton(UI.scale(200), "Back", 27, back), scrollport.cont.sz.x / 2, 0.5);
	    pack();
	}

	public class SetButton extends KeyMatch.Capture {
	    public final KeyBinding cmd;

	    public SetButton(int w, KeyBinding cmd) {
		super(w, cmd.key());
		this.cmd = cmd;
	    }

	    public void set(KeyMatch key) {
		super.set(key);
		cmd.set(key);
	    }

	    public void draw(GOut g) {
		if(cmd.key() != key)
		    super.set(cmd.key());
		super.draw(g);
	    }

	    protected KeyMatch mkmatch(KeyEvent ev) {
		return(KeyMatch.forevent(ev, ~cmd.modign));
	    }

	    protected boolean handle(KeyEvent ev) {
		if(ev.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
		    cmd.set(null);
		    super.set(cmd.key());
		    return(true);
		}
		return(super.handle(ev));
	    }

	    public Object tooltip(Coord c, Widget prev) {
		return(kbtt.tex());
	    }
	}
    }


    public static class PointBind extends Button {
	public static final String msg = "Bind other elements...";
	public static final Resource curs = Resource.local().loadwait("gfx/hud/curs/wrench");
	private UI.Grab mg, kg;
	private KeyBinding cmd;

	public PointBind(int w) {
	    super(w, msg, false);
	    tooltip = RichText.render("Bind a key to an element not listed above, such as an action-menu " +
				      "button. Click the element to bind, and then press the key to bind to it. " +
				      "Right-click to stop rebinding.",
				      300);
	}

	public void click() {
	    if(mg == null) {
		change("Click element...");
		mg = ui.grabmouse(this);
	    } else if(kg != null) {
		kg.remove();
		kg = null;
		change(msg);
	    }
	}

	private boolean handle(KeyEvent ev) {
	    switch(ev.getKeyCode()) {
	    case KeyEvent.VK_SHIFT: case KeyEvent.VK_CONTROL: case KeyEvent.VK_ALT:
	    case KeyEvent.VK_META: case KeyEvent.VK_WINDOWS:
		return(false);
	    }
	    int code = ev.getKeyCode();
	    if(code == KeyEvent.VK_ESCAPE) {
		return(true);
	    }
	    if(code == KeyEvent.VK_BACK_SPACE) {
		cmd.set(null);
		return(true);
	    }
	    if(code == KeyEvent.VK_DELETE) {
		cmd.set(KeyMatch.nil);
		return(true);
	    }
	    KeyMatch key = KeyMatch.forevent(ev, ~cmd.modign);
	    if(key != null)
		cmd.set(key);
	    return(true);
	}

	public boolean mousedown(Coord c, int btn) {
	    if(mg == null)
		return(super.mousedown(c, btn));
	    Coord gc = ui.mc;
	    if(btn == 1) {
		this.cmd = KeyBinding.Bindable.getbinding(ui.root, gc);
		return(true);
	    }
	    if(btn == 3) {
		mg.remove();
		mg = null;
		change(msg);
		return(true);
	    }
	    return(false);
	}

	public boolean mouseup(Coord c, int btn) {
	    if(mg == null)
		return(super.mouseup(c, btn));
	    Coord gc = ui.mc;
	    if(btn == 1) {
		if((this.cmd != null) && (KeyBinding.Bindable.getbinding(ui.root, gc) == this.cmd)) {
		    mg.remove();
		    mg = null;
		    kg = ui.grabkeys(this);
		    change("Press key...");
		} else {
		    this.cmd = null;
		}
		return(true);
	    }
	    if(btn == 3)
		return(true);
	    return(false);
	}

	public Resource getcurs(Coord c) {
	    if(mg == null)
		return(null);
	    return(curs);
	}

	public boolean keydown(KeyEvent ev) {
	    if(kg == null)
		return(super.keydown(ev));
	    if(handle(ev)) {
		kg.remove();
		kg = null;
		cmd = null;
		change("Click another element...");
		mg = ui.grabmouse(this);
	    }
	    return(true);
	}
    }

    public OptWnd(boolean gopts) {
	super(Coord.z, "Options", true);
	main = add(new Panel());
	video = add(new VideoPanel(main));
	audio = add(new Panel());
	keybind = add(new BindingPanel(main));
	display = add(new Panel());
	general = add(new Panel());
	camera = add(new Panel());
	shortcuts = add(new Panel());
	int y;

	addPanelButton("Video settings", 'v', video, 0, 0);
	addPanelButton("Audio settings", 'a', audio, 0, 1);
	addPanelButton("Camera settings", 'c', camera, 0, 2);

	addPanelButton("General settings", 'g', general, 1, 0);
	addPanelButton("Display settings", 'd', display, 1, 1);
	addPanelButton("Radar settings", 'r', Action.TOGGLE_MINIMAP_ICONS_SETTINGS, 1, 2);
	addPanelButton("Shortcut settings", 's', shortcuts, 1, 3);

	if(gopts) {
	    main.add(new Button(UI.scale(200), "Switch character") {
		public void click() {
		    getparent(GameUI.class).act("lo", "cs");
		}
	    }, UI.scale(0, 120));
	    main.add(new Button(UI.scale(200), "Log out") {
		public void click() {
		    getparent(GameUI.class).act("lo");
		}
	    }, UI.scale(0, 150));
	}
	main.add(new Button(UI.scale(200), "Close") {
	    public void click() {
		OptWnd.this.hide();
	    }
	}, UI.scale(0, 180));

	y = 0;
	audio.add(new Label("Master audio volume"), new Coord(0, y));
	y += 15;
	audio.add(new HSlider(UI.scale(200), 0, 1000, (int) (Audio.volume * 1000)) {
	    public void changed() {
		Audio.setvolume(val / 1000.0);
	    }
	}, new Coord(0, y));
	y += 30;
	audio.add(new Label("In-game event volume"), new Coord(0, y));
	y += 15;
	audio.add(new HSlider(UI.scale(200), 0, 1000, 0) {
	    protected void attach(UI ui) {
		super.attach(ui);
		val = (int) (ui.audio.pos.volume * 1000);
	    }

	    public void changed() {
		ui.audio.pos.setvolume(val / 1000.0);
	    }
	}, new Coord(0, y));
	y += 20;
	audio.add(new Label("Ambient volume"), new Coord(0, y));
	y += 15;
	audio.add(new HSlider(UI.scale(200), 0, 1000, 0) {
	    protected void attach(UI ui) {
		super.attach(ui);
		val = (int) (ui.audio.amb.volume * 1000);
	    }

	    public void changed() {
		ui.audio.amb.setvolume(val / 1000.0);
	    }
	}, new Coord(0, y));
	y += 35;
	audio.add(new PButton(UI.scale(200), "Back", 27, main), new Coord(0, y));
	audio.pack();

	chpanel(this.main);
	initDisplayPanel();
	initGeneralPanel();
	initCameraPanel();
	main.pack();
	chpanel(main);
    }
    
    @Override
    protected void attach(UI ui) {
	super.attach(ui);
	initShortcutsPanel();
    }
    
    private void addPanelButton(String name, char key, Panel panel, int x, int y) {
	main.add(new PButton(UI.scale(200), name, key, panel), UI.scale(PANEL_POS.mul(x, y)));
    }
    
    private void addPanelButton(String name, char key, Action action, int x, int y) {
	main.add(new AButton(UI.scale(200), name, key, action), UI.scale(PANEL_POS.mul(x, y)));
    }

    private void initCameraPanel() {
	int x = 0, y = 0, my = 0;

	int tx = x + camera.add(new Label("Camera:"), x, y).sz.x + 5;
	camera.add(new Dropbox<String>(100, 5, 16) {
	    @Override
	    protected String listitem(int i) {
		return new LinkedList<>(MapView.camlist()).get(i);
	    }

	    @Override
	    protected int listitems() {
		return MapView.camlist().size();
	    }

	    @Override
	    protected void drawitem(GOut g, String item, int i) {
		g.text(item, Coord.z);
	    }

	    @Override
	    public void change(String item) {
		super.change(item);
		MapView.defcam(item);
		if(ui.gui != null && ui.gui.map != null) {
		    ui.gui.map.camera = ui.gui.map.restorecam();
		}
	    }
	}, tx, y).sel = MapView.defcam();

	y += 35;
	camera.add(new Label("Brighten view"), x, y);
	y += 15;
	camera.add(new HSlider(UI.scale(200), 0, 500, 0) {
	    public void changed() {
		CFG.CAMERA_BRIGHT.set(val / 1000.0f);
		if(ui.sess != null && ui.sess.glob != null) {
		    ui.sess.glob.brighten();
		}
	    }
	}, x, y).val = (int) (1000 * CFG.CAMERA_BRIGHT.get());


	y += 25;
	my = Math.max(my, y);

	camera.add(new PButton(UI.scale(200), "Back", 27, main), 0, my + 35);
	camera.pack();
    }


    private void initGeneralPanel() {
	int x = 0;
	int y = 0, my = 0;
	general.add(new CFGBox("Store minimap tiles", CFG.STORE_MAP), x, y);
    
	int STEP = UI.scale(25);
	y += STEP;
	general.add(new CFGBox("Store chat logs", CFG.STORE_CHAT_LOGS, "Logs are stored in 'chats' folder"), new Coord(x, y));
    
	y += STEP;
	general.add(new CFGBox("Single item CTRL choose", CFG.MENU_SINGLE_CTRL_CLICK, "If checked, will automatically select single item menus if CTRL is pressed when menu is opened."), x, y);
    
	y += STEP;
	general.add(new CFGBox("Add \"Pick All\" option", CFG.MENU_ADD_PICK_ALL, "If checked, will add new option that will allow to pick all same objects."), x, y);
    
	y += STEP;
	general.add(new CFGBox("Show F-key tool bar", CFG.SHOW_TOOLBELT_0), x, y);
    
	y += STEP;
	general.add(new CFGBox("Show extra tool bar", CFG.SHOW_TOOLBELT_1), x, y);
	
	y += STEP;
	Coord tsz = general.add(new Label("Default speed:"), x, y).sz;
	general.adda(new Speedget.SpeedSelector(UI.scale(100)), new Coord(x + tsz.x + UI.scale(5), y + tsz.y / 2), 0, 0.5);
    
	y += STEP;
	Label label = general.add(new Label(String.format("Auto pickup radius: %.2f", CFG.AUTO_PICK_RADIUS.get() / 11.0)), x, y);
	y += UI.scale(15);
	general.add(new CFGHSlider(UI.scale(120), CFG.AUTO_PICK_RADIUS, 33, 88) {
	    @Override
	    public void changed() {
		label.settext(String.format("Auto pickup radius: %.02f", val / 11.0));
	    }
	}, x, y);
    
	y += UI.scale(35);
	general.add(new Button(UI.scale(120), "Toggle at login") {
	    @Override
	    public void click() {
		if(ui.gui != null) {
		    LoginTogglesCfgWnd.toggle(ui.gui);
		} else {
		    LoginTogglesCfgWnd.toggle(ui.root);
		}
	    }
	}, x, y);
    
	my = Math.max(my, y);
	x += UI.scale(250);
	y = 0;
    
	general.add(new Label("Choose menu items to select automatically:"), x, y);
	y += UI.scale(15);
	final FlowerList list = general.add(new FlowerList(), x, y);
    
	y += list.sz.y + UI.scale(5);
	final TextEntry value = general.add(new TextEntry(UI.scale(155), "") {
	    @Override
	    public void activate(String text) {
		list.add(text);
		settext("");
	    }
	}, x, y);
    
	general.add(new Button(UI.scale(45), "Add") {
	    @Override
	    public void click() {
		list.add(value.text);
		value.settext("");
	    }
	}, x + UI.scale(160), y - UI.scale(2));
    
	my = Math.max(my, y);
    
	general.add(new PButton(UI.scale(200), "Back", 27, main), 0, my + UI.scale(35));
	general.pack();
    }

    private void initDisplayPanel() {
	int x = 0;
	int y = 0;
	int my = 0;
	display.add(new CFGBox("Always show kin names", CFG.DISPLAY_KINNAMES), new Coord(x, y));
    
	int STEP = UI.scale(25);
	y += STEP;
	display.add(new CFGBox("Show flavor objects", CFG.DISPLAY_FLAVOR), new Coord(x, y));

	y += STEP;
	display.add(new CFGBox("Show gob info", CFG.DISPLAY_GOB_INFO, "Enables damage and crop/tree growth stage displaying", true), x, y);
    
	y += STEP;
	display.add(new CFGBox("Show gob hitboxes", CFG.DISPLAY_GOB_HITBOX, "Enables hitboxes around all objects", true), x, y);
	
	y += STEP;
	display.add(new CFGBox("Draw hitboxes on top", CFG.DISPLAY_GOB_HITBOX_TOP, "Draws hitboxes on top of everything", true), x, y);

	y += STEP;
	display.add(new CFGBox("Show food categories", CFG.DISPLAY_FOD_CATEGORIES, "Shows list of food categories in the tooltip", true), x, y);

	y += STEP;
	display.add(new CFGBox("Show timestamps in chat messages", CFG.SHOW_CHAT_TIMESTAMP), new Coord(x, y));

	y += STEP;
	display.add(new CFGBox("Swap item quality and number", CFG.SWAP_NUM_AND_Q), x, y);

	y += STEP;
	display.add(new CFGBox("Show item progress as number", CFG.PROGRESS_NUMBER), x, y);

	y += STEP;
	display.add(new CFGBox("Show biomes on minimap", CFG.MMAP_SHOW_BIOMES), x, y);

	y += STEP;
	display.add(new CFGBox("Simple crops", CFG.SIMPLE_CROPS, "Requires area reload"), x, y);

	y += 35;
	display.add(new CFGBox("Show object radius", CFG.SHOW_GOB_RADIUS, "Shows radius of mine supports, beehives etc.", true), x, y);

	y += STEP;
	display.add(new Button(UI.scale(120), "Show as buffs") {
	    @Override
	    public void click() {
		if(ui.gui != null) {
		    ShowBuffsCfgWnd.toggle(ui.gui);
		} else {
		    ShowBuffsCfgWnd.toggle(ui.root);
		}
	    }
	}, x, y);

	my = Math.max(my, y);
	x += UI.scale(250);
	y = 0;
	my = Math.max(my, y);
	int tx = x + display.add(new CFGBox("Show quality as:", CFG.Q_SHOW_SINGLE), x, y).sz.x;
	display.add(new QualityBox(UI.scale(100), 6, UI.scale(16), CFG.Q_SINGLE_TYPE), tx + UI.scale(5), y);

	y += STEP;
	display.add(new CFGBox("Show on SHIFT:", CFG.Q_SHOW_SHIFT), x, y);
	display.add(new QualityBox(UI.scale(100), 6, UI.scale(16), CFG.Q_SHIFT_TYPE), tx + UI.scale(5), y);

	y += STEP;
	display.add(new CFGBox("Show on CTRL:", CFG.Q_SHOW_CTRL), x, y);
	display.add(new QualityBox(UI.scale(100), 6, UI.scale(16), CFG.Q_CTRL_TYPE), tx + UI.scale(5), y);

	y += STEP;
	display.add(new CFGBox("Show on ALT:", CFG.Q_SHOW_ALT), x, y);
	display.add(new QualityBox(UI.scale(100), 6, UI.scale(16), CFG.Q_ALT_TYPE), tx + UI.scale(5), y);

	y += 50;
	display.add(new CFGBox("Real time curios", CFG.REAL_TIME_CURIO, "Show curiosity study time in real life hours, instead of server hours"), new Coord(x, y));

	y += STEP;
	display.add(new CFGBox("Show LP/H for curios", CFG.SHOW_CURIO_LPH, "Show how much learning point curio gives per hour"), new Coord(x, y));

	y += STEP;
	display.add(new CFGBox("Show item durability", CFG.SHOW_ITEM_DURABILITY), new Coord(x, y));

	y += STEP;
	display.add(new CFGBox("Show item wear bar", CFG.SHOW_ITEM_WEAR_BAR), new Coord(x, y));

	y += STEP;
	display.add(new CFGBox("Show item armor", CFG.SHOW_ITEM_ARMOR), new Coord(x, y));

	y += STEP;
	display.add(new CFGBox("Show hunger meter", CFG.HUNGER_METER) {
	    @Override
	    public void set(boolean a) {
		super.set(a);
		if(ui.gui != null && ui.gui.chrwdg != null) {
		    if(a) {
			ui.gui.addcmeter(new HungerMeter(ui.gui.chrwdg.glut));
		    } else {
			ui.gui.delcmeter(HungerMeter.class);
		    }
		}
	    }
	}, x, y);

	y += STEP;
	display.add(new CFGBox("Show FEP meter", CFG.FEP_METER) {
	    @Override
	    public void set(boolean a) {
		super.set(a);
		if(ui.gui != null && ui.gui.chrwdg != null) {
		    if(a) {
			ui.gui.addcmeter(new FEPMeter(ui.gui.chrwdg.feps));
		    } else {
			ui.gui.delcmeter(FEPMeter.class);
		    }
		}
	    }
	}, x, y);
 
	my = Math.max(my, y);
	x += UI.scale(250);
	y = 0;
	display.add(new CFGBox("Use new combat UI", CFG.ALT_COMBAT_UI), x, y);
	
	y += STEP;
	display.add(new CFGBox("Show combat damage", CFG.SHOW_COMBAT_DMG), x, y);
	
	y += STEP;
	display.add(new CFGBox("Clear player damage after combat", CFG.CLEAR_PLAYER_DMG_AFTER_COMBAT), x, y);
	
	y += STEP;
	display.add(new CFGBox("Clear all damage after combat", CFG.CLEAR_ALL_DMG_AFTER_COMBAT), x, y);
	
	y += STEP;
	display.add(new CFGBox("Simplified combat openings", CFG.SIMPLE_COMBAT_OPENINGS, "Show openings as solid colors with numbers"), x, y);
	
	y += STEP;
	display.add(new CFGBox("Display combat keys", CFG.SHOW_COMBAT_KEYS), x, y);
	
	my = Math.max(my, y);

	display.add(new PButton(UI.scale(200), "Back", 27, main), new Coord(0, my + UI.scale(35)));
	display.pack();
    }

    private void populateShortcutsPanel(KeyBinder.KeyBindType type) {
        shortcutList.clear(true);
	KeyBinder.makeWidgets(type).forEach(shortcutList::additem);
    }
    
    private void initShortcutsPanel() {
	TabStrip<KeyBinder.KeyBindType> tabs = new TabStrip<>(this::populateShortcutsPanel);
	tabs.insert(0, null, "General", null).tag = KeyBinder.KeyBindType.GENERAL;
	tabs.insert(1, null, "Combat", null).tag = KeyBinder.KeyBindType.COMBAT;
	shortcuts.add(tabs);
	int y = tabs.sz.y;
	
	shortcutList = shortcuts.add(new WidgetList<KeyBinder.ShortcutWidget>(UI.scale(300, 24), 16) {
	    @Override
	    public boolean mousedown(Coord c0, int button) {
		boolean result = super.mousedown(c0, button);
		KeyBinder.ShortcutWidget item = itemat(c0);
		if(item != null) {
		    c0 = c0.add(0, sb.val * itemsz.y);
		    item.mousedown(c0.sub(item.parentpos(this)), button);
		}
		return result;
	    }
	    
	    @Override
	    public Object tooltip(Coord c0, Widget prev) {
		KeyBinder.ShortcutWidget item = itemat(c0);
		if(item != null) {
		    c0 = c0.add(0, sb.val * itemsz.y);
		    return item.tooltip(c0, prev);
		}
		return super.tooltip(c, prev);
	    }
	}, 0, y);
	shortcutList.canselect = false;
	tabs.select(KeyBinder.KeyBindType.GENERAL, false);
 
	shortcuts.pack();
	shortcuts.add(new PButton(UI.scale(200), "Back", 27, main), shortcuts.sz.x / 2 - 100, shortcuts.sz.y + 35);
	shortcuts.pack();
    }
    
    public OptWnd() {
	this(true);
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if((sender == this) && (msg == "close")) {
	    hide();
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }

    public void show() {
	chpanel(main);
	super.show();
    }

    public static class CFGBox extends CheckBox implements CFG.Observer<Boolean> {

	protected final CFG<Boolean> cfg;

	public CFGBox(String lbl, CFG<Boolean> cfg) {
	    this(lbl, cfg, null, false);
	}

	public CFGBox(String lbl, CFG<Boolean> cfg, String tip) {
	    this(lbl, cfg, tip, false);
	}

	public CFGBox(String lbl, CFG<Boolean> cfg, String tip, boolean observe) {
	    super(lbl);

	    this.cfg = cfg;
	    defval();
	    if(tip != null) {
		tooltip = Text.render(tip).tex();
	    }
	    if(observe){ cfg.observe(this); }
	}

	protected void defval() {
	    a = cfg.get();
	}

	@Override
	public void set(boolean a) {
	    this.a = a;
	    cfg.set(a);
	}

	@Override
	public void destroy() {
	    cfg.unobserve(this);
	    super.destroy();
	}

	@Override
	public void updated(CFG<Boolean> cfg) {
	    a = cfg.get();
	}
    }
    
    public static class CFGHSlider extends HSlider {
	private final CFG<Integer> cfg;
	
	public CFGHSlider(int w, CFG<Integer> cfg, int min, int max) {
	    super(w, min, max, cfg.get());
	    this.cfg = cfg;
	}
	
	@Override
	public void released() {
	    cfg.set(val);
	}
    }

    public class QualityBox extends Dropbox<QualityList.SingleType> {
	protected final CFG<QualityList.SingleType> cfg;

	public QualityBox(int w, int listh, int itemh, CFG<QualityList.SingleType> cfg) {
	    super(w, listh, itemh);
	    this.cfg = cfg;
	    this.sel = cfg.get();
	}

	@Override
	protected QualityList.SingleType listitem(int i) {
	    return QualityList.SingleType.values()[i];
	}

	@Override
	protected int listitems() {
	    return QualityList.SingleType.values().length;
	}

	@Override
	protected void drawitem(GOut g, QualityList.SingleType item, int i) {
	    g.image(item.tex(), Q_TYPE_PADDING);
	}

	@Override
	public void change(QualityList.SingleType item) {
	    super.change(item);
	    if(item != null) {
		cfg.set(item);
	    }
	}
    };
}

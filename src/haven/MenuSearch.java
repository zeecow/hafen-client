package haven;

import java.util.*;
import java.awt.image.BufferedImage;
import haven.MenuGrid.Pagina;
import haven.MenuGrid.PagButton;

public abstract class MenuSearch extends Window {
    public final MenuGrid menu;
    public final Results rls;
    public final TextEntry sbox;
    protected List<Result> cur = Collections.emptyList();
    protected List<Result> filtered = Collections.emptyList();
    private boolean recons = true;

    public class Result {
	public final PagButton btn;

	protected Result(PagButton btn) {
	    this.btn = btn;
	}
    }

    public static final Text.Foundry elf = CharWnd.attrf;
    public static final int elh = elf.height() + UI.scale(2);
    public class Results extends SListBox<Result, Widget> {
	private Results(Coord sz) {
	    super(sz, elh);
	}

	protected List<Result> items() {return(filtered);}

	protected Widget makeitem(Result el, int idx, Coord sz) {
	    return(new ItemWidget<Result>(this, sz, el) {
		    {
			add(new IconText(sz) {
				protected BufferedImage img() {return(item.btn.img());}
				protected String text() {return(el.btn.name());}
				protected int margin() {return(0);}
				protected Text.Foundry foundry() {return(elf);}
			    }, Coord.z);
		    }

		    private double lastcl = 0;
		    public boolean mousedown(MouseDownEvent ev) {
			boolean psel = sel == item;
			super.mousedown(ev);
			double now = Utils.rtime();
			if(psel) {
			    if(now - lastcl < 0.5)
				menu.use(item.btn, new MenuGrid.Interaction(1, ui.modflags()), false);
			}
			lastcl = now;
			return(true);
		    }
		});
	}
    }

    public MenuSearch(String title, MenuGrid menu) {
	super(Coord.z, title);
	this.menu = menu;
	rls = add(new Results(UI.scale(250, 500)), Coord.z);
	sbox = add(new TextEntry(UI.scale(250), "") {
		protected void changed() {
		    refilter();
		}

		public void activate(String text) {
		    if(rls.sel != null)
			menu.use(rls.sel.btn, new MenuGrid.Interaction(1, ui.modflags()), false);
		    if(!ui.modctrl)
			MenuSearch.this.wdgmsg("close");
		}
	    }, 0, rls.sz.y);
	pack();
    }

    public MenuSearch(MenuGrid menu) {
	this("Action search", menu);
    }

    protected void refilter() {
	List<Result> found = new ArrayList<>();
	String needle = sbox.text().toLowerCase();
	for(Result res : this.cur) {
	    if(res.btn.name().toLowerCase().indexOf(needle) >= 0)
		found.add(res);
	}
	this.filtered = found;
	int idx = filtered.indexOf(rls.sel);
	if(idx < 0) {
	    if(filtered.size() > 0) {
		rls.change(filtered.get(0));
		rls.display(0);
	    }
	} else {
	    rls.display(idx);
	}
    }

    protected abstract boolean generate(List<PagButton> buf);

    protected void updlist() {
	recons = false;
	List<PagButton> buf = new ArrayList<>();
	if(generate(buf))
	    recons = true;
	Map<PagButton, Result> prev = new HashMap<>();
	for(Result pr : this.cur)
	    prev.put(pr.btn, pr);
	List<Result> results = new ArrayList<>();
	for(PagButton btn : buf) {
	    Result pr = prev.get(btn);
	    if(pr != null)
		results.add(pr);
	    else
		results.add(new Result(btn));
	}
	this.cur = results;
	refilter();
    }

    protected void recons() {
	recons = true;
    }

    public void tick(double dt) {
	if(recons)
	    updlist();
	super.tick(dt);
    }

    public boolean keydown(KeyDownEvent ev) {
	if(ev.code == ev.awt.VK_DOWN) {
	    int idx = filtered.indexOf(rls.sel);
	    if((idx >= 0) && (idx < filtered.size() - 1)) {
		idx++;
		rls.change(filtered.get(idx));
		rls.display(idx);
	    }
	    return(true);
	} else if(ev.code == ev.awt.VK_UP) {
	    int idx = filtered.indexOf(rls.sel);
	    if(idx > 0) {
		idx--;
		rls.change(filtered.get(idx));
		rls.display(idx);
	    }
	    return(true);
	} else {
	    return(super.keydown(ev));
	}
    }

    public static class Main extends MenuSearch {
	private Pagina root;

	public Main(MenuGrid menu) {
	    super(menu);
	    setroot(null);
	}

	protected boolean generate(List<PagButton> buf) {
	    boolean recons = false;
	    Pagina root = this.root;
	    Collection<Pagina> leaves = new ArrayList<>();
	    synchronized(menu.paginae) {
		leaves.addAll(menu.paginae);
	    }
	    for(Pagina pag : leaves) {
		try {
		    if(root == null) {
			buf.add(pag.button());
		    } else {
			for(Pagina parent = pag; parent != null; parent = parent.parent()) {
			    if(parent == root) {
				buf.add(pag.button());
				break;
			    }
			}
		    }
		} catch(Loading l) {
		    recons = true;
		}
	    }
	    Collections.sort(buf, Comparator.comparing(PagButton::name));
	    return(recons);
	}

	public void setroot(Pagina nr) {
	    root = nr;
	    recons();
	    rls.sb.val = 0;
	}

	public void tick(double dt) {
	    if(menu.cur != root)
		setroot(menu.cur);
	    super.tick(dt);
	}
    }
}

package haven;


public class MapWnd2 extends MapWnd {
    private boolean switching = true;
    
    public MapWnd2(MapFile file, MapView mv, Coord sz, String title) {
	super(file, mv, sz, title);
    }
    
    @Override
    protected String cfgName(String cap) {
	return super.cfgName(cap) + (compact() ? ".compact" : "");
    }
    
    private boolean compact() {
	return tool != null && !tool.visible;
    }
    
    @Override
    public void compact(boolean a) {
	switching = true;
	String name = cfgName(caption());
	storeCfg();
	super.compact(a);
	name = cfgName(caption());
	cfg = WidgetCfg.get(name);
	initCfg();
	switching = false;
    }
    
    @Override
    public void resize(Coord sz) {
	super.resize(sz);
	cfg.sz = asz;
	if(!switching) {storeCfg();}
    }
    
    @Override
    protected void initCfg() {
	if(cfg != null && cfg.sz != null) {
	    asz = cfg.sz;
	    super.resize(asz);
	}
	super.initCfg();
    }
    
    @Override
    protected void setCfg() {
	super.setCfg();
	cfg.sz = asz;
    }
    
}

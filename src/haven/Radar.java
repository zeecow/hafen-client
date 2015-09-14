package haven;

public class Radar {
    public static void add(Gob gob) {
	if(gob.getattr(Marker.class) == null) {
	    gob.setattr(new Marker(gob));
	}
    }

    private static Tex tex(String resname) {
	if(resname == null){return null;}
	try {
	    if(resname.equals("gfx/terobjs/items/arrow")) {
		System.out.println(resname);
		try {
		    return Resource.remote().load("gfx/invobjs/arrow-bone").get().layer(Resource.imgc).tex();
		}catch(Loading ignored){}
		//return Resource.loadtex("gfx/invobjs/arrow-bone");
	    }
	}catch(Loading ignored){}
	return null;
    }

    private static Resource loadres(String name){
	Resource res = null;
	res = Resource.local().load(name).get();
	return res;
    }

    public static class Marker extends GAttrib {
	private Tex tex;

	public Marker(Gob gob) {
	    super(gob);
	}

	public Tex tex() {
	    GobIcon gi = gob.getattr(GobIcon.class);
	    if(gi != null) {
		return gi.tex();
	    }
	    if(tex == null) {
		tex = Radar.tex(resname());
	    }
	    return tex;
	}

	private String resname() {
	    Drawable d = gob.getattr(Drawable.class);
	    String name = null;
	    try {
		if(d != null) {
		    if(d instanceof Composite) {
			name = ((Composite) d).base.get().name;
		    } else {
			name = d.getres().name;
		    }
		}
	    } catch(Loading ignored) {
	    }
	    return name;
	}
    }

}

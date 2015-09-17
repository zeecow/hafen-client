package haven;

public class Radar {
    public static void add(Gob gob, Indir<Resource> res) {
	if(gob.getattr(Marker.class) == null) {
	    gob.setattr(new Marker(gob, res));
	}
    }

    private static Tex tex(String resname) {
	if(resname == null){return null;}
	try {
	    if(resname.equals("gfx/terobjs/items/arrow")) {
		System.out.println(resname);
		try {
		    return loadres("gfx/invobjs/arrow-bone").layer(Resource.imgc).tex();
		}catch(Loading ignored){}
		//return Resource.loadtex("gfx/invobjs/arrow-bone");
	    }
	}catch(Loading ignored){}
	return null;
    }

    private static Resource loadres(String name) {
	return Resource.remote().load(name).get();
    }

    public static class Marker extends GAttrib {
	private final Indir<Resource> res;
	private Tex tex;

	public Marker(Gob gob, Indir<Resource> res) {
	    super(gob);
	    this.res = res;
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
	    String name = null;
	    if(res instanceof Resource.Named) {
		name = ((Resource.Named) res).name;
	    } else {
		try {
		    name = res.get().name;
		} catch (Resource.Loading ignored) {
		}
	    }
	    return name;
	}
    }

}

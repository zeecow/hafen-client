/* Preprocessed source code */
/* $use: lib/globfx */
/* $use: lib/env */

package haven.res.gfx.fx.ismoke;

import haven.*;
import haven.render.*;
import haven.render.sl.*;
import haven.res.lib.env.*;
import java.awt.Color;
import java.nio.*;
import java.util.*;
import static haven.render.sl.Cons.*;
import static haven.render.sl.Type.*;

/* >spr: ISmoke */
/* >rlink: ISmoke */
@haven.FromResource(name = "gfx/fx/ismoke", version = 105)
public class ISmoke extends Sprite implements Rendered, Sprite.CDel, TickList.TickNode, TickList.Ticking {
    static final VertexArray.Layout fmt =
	new VertexArray.Layout(new VertexArray.Layout.Input(Homo3D.vertex,     new VectorFormat(3, NumberFormat.FLOAT32), 0,  0, 20),
			       new VertexArray.Layout.Input(Homo3D.normal,     new VectorFormat(3, NumberFormat.SNORM8),  0, 12, 20),
			       new VertexArray.Layout.Input(VertexColor.color, new VectorFormat(4, NumberFormat.UNORM8),  0, 16, 20));
    Model model = null;
    VertexArray va = null;
    final Material mat;
    final List<Boll> bollar = new ArrayList<Boll>();
    final Random rnd = new Random();
    final Pipe.Op loc;
    final Color col;
    final float sz, den, fadepow, initzv, life, srad;
    final List<RenderTree.Slot> slots = new ArrayList<>(1);
    final Gob gob = (owner instanceof Gob) ? (Gob)owner : owner.context(Gob.class);
    boolean spawn = !ZeeConfig.hideFxSmoke;;

    public ISmoke(Owner owner, Resource res, Message sdt) {
	super(owner, res);
	mat = res.layer(Material.Res.class, sdt.uint8()).get();
	sz = sdt.uint8() / 10.0f;
	String locn = sdt.string();
	if(locn.equals(""))
	    loc = null;
	else
	    loc = owner.getres().layer(Skeleton.BoneOffset.class, locn).forpose(null).get();
	col = Utils.col16(sdt.uint16());
	den = sdt.uint8();
	fadepow = sdt.uint8() / 10.0f;
	life = sdt.uint8() / 10.0f;
	int h = sdt.uint8();
	initzv = h / life;
	srad = sdt.uint8() / 10.0f;
    }

    public ISmoke(Owner owner, Resource res, Object... args) {
	super(owner, res);
	new Throwable().printStackTrace();
	int a = 0;
	String fl = (String)args[a++];
	mat = ((fl.indexOf('o') >= 0) ? res : Resource.classres(ISmoke.class)).layer(Material.Res.class, (Integer)args[a++]).get();
	sz = ((Number)args[a++]).floatValue();
	String locn = (String)args[a++];
	if(locn.equals(""))
	    loc = null;
	else
	    loc = owner.getres().layer(Skeleton.BoneOffset.class, locn).forpose(null).get();
	col = (Color)args[a++];
	den = ((Number)args[a++]).floatValue();
	fadepow = ((Number)args[a++]).floatValue();
	life = ((Number)args[a++]).floatValue();
	float h = ((Number)args[a++]).floatValue();
	initzv = h / life;
	srad = ((Number)args[a++]).floatValue();
    }

    float de = 0;
    public boolean tick(double ddt) {
	return(!spawn && bollar.isEmpty());
    }
    public void autotick(double ddt) {
	float dt = (float)ddt;
	de += dt;
	while(spawn && (de > 0.1)) {
	    de -= 0.1;
	    int n = (int)((1.0f + (rnd.nextFloat() * 0.5f)) * den);
	    for(int i = 0; i < n; i++)
		bollar.add(new Boll(Coord3f.o.sadd(0, rnd.nextFloat() * (float)Math.PI * 2, (float)Math.sqrt(rnd.nextFloat()) * srad)));
	}
	Coord3f nv = Environ.get(gob.glob).wind().mul(0.4f);
	nv = nv.rot(Coord3f.zu, (float)gob.a);
	for(Iterator<Boll> i = bollar.iterator(); i.hasNext();) {
	    Boll boll = i.next();
	    if(boll.tick(dt, nv))
		i.remove();
	}
    }
    public TickList.Ticking ticker() {return(this);}

    public void autogtick(Render g) {
	updpos(g);
    }

    class Boll {
	static final float sr = 0.3f, sv = 0.3f;
	float x, y, z;
	float xv, yv, zv;
	float t = 0;

	Boll(Coord3f pos) {
	    x = pos.x + (float)(rnd.nextGaussian() * sr);
	    y = pos.y + (float)(rnd.nextGaussian() * sr);
	    z = pos.z;
	    xv = (float)rnd.nextGaussian() * sv;
	    yv = (float)rnd.nextGaussian() * sv;
	    zv = initzv;
	}

	public boolean tick(float dt, Coord3f nv) {
	    /* XXX: Apparently, nextGaussian() is a major hotspot.  */
	    float xvd = xv - nv.x, yvd = yv - nv.y, zvd = zv - nv.z;
	    float xa = (-xvd * 0.2f) + ((float)Utils.fgrandoom(rnd) * 0.5f), ya = (-yvd * 0.2f) + ((float)Utils.fgrandoom(rnd) * 0.5f), za = ((-zvd + initzv) * 0.2f) + ((float)Utils.fgrandoom(rnd) * 2.0f);
	    xv += dt * xa;
	    yv += dt * ya;
	    zv += dt * za;
	    x += xv * dt;
	    y += yv * dt;
	    z += zv * dt;
	    t += dt;
	    return(t > life);
	}
    }

    public void draw(Pipe state, Render out) {
	if(model != null)
	    out.draw(state, model);
    }

    public void dispose() {
	if(model != null) {
	    model.dispose();
	    model = null;
	}
	if(va != null) {
	    va.dispose();
	    va = null;
	}
    }

    private void updpos(Render d) {
	int nb = bollar.size();
	if(nb < 1) {
	    dispose();
	    return;
	}
	if((va == null) || (va.bufs[0].size() < nb * fmt.inputs[0].stride)) {
	    if(va != null)
		va.dispose();
	    int n = 3 * nb / 2;
	    va = new VertexArray(fmt, new VertexArray.Buffer(n * fmt.inputs[0].stride, DataBuffer.Usage.STREAM, null)).shared();
	}
	d.update(va.bufs[0], this::fill);
	if((model == null) || (model.n != nb)) {
	    if(model != null)
		model.dispose();
	    model = new Model(Model.Mode.POINTS, va, null, 0, nb);
	    for(RenderTree.Slot slot : this.slots)
		slot.update();
	}
    }

    private FillBuffer fill(VertexArray.Buffer dst, Environment env) {
	byte r = (byte)col.getRed();
	byte g = (byte)col.getGreen();
	byte b = (byte)col.getBlue();
	float a = col.getAlpha() / 255.0f;
	FillBuffer ret = env.fillbuf(dst);
	ByteBuffer buf = ret.push();
	for(Boll boll : bollar) {
	    buf.putFloat(boll.x).putFloat(boll.y).putFloat(boll.z);
	    // XXXRENDER: It would be very nice to be able to specify static vertex parameters for an entire draw call.
	    buf.put((byte)0).put((byte)0).put((byte)127).put((byte)0);
	    byte ca = (byte)(a * (float)Utils.clip(1.0 - Math.pow(boll.t / life, fadepow), 0, 1) * 255);
	    buf.put(r).put(g).put(b).put(ca);
	}
	return(ret);
    }

    private static final Uniform bollsz = new Uniform(FLOAT, p -> ((DrawState)p.get(RUtils.adhoc)).sz(), RUtils.adhoc);
    private static final ShaderMacro prog = new ShaderMacro() {
	    public void modify(final ProgramContext prog) {
		final Function pdiv = new Function.Def(FLOAT) {{
		    Expression vec = param(PDir.IN, VEC4).ref();
		    code.add(new Return(div(pick(vec, "x"), pick(vec, "w"))));
		}};
		Homo3D homo = Homo3D.get(prog);
		prog.vctx.ptsz.mod(in -> mul(sub(pdiv.call(homo.pprjxf(add(homo.eyev.depref(), vec4(bollsz.ref(), l(0.0), l(0.0), l(0.0))))),
						 pdiv.call(prog.vctx.posv.depref())),
					     pick(FrameConfig.u_screensize.ref(), "x")),
				   0);
		prog.vctx.ptsz.force();
		Tex2D.get(prog).texcoord().mod(in -> FragmentContext.ptc, 0);
	    }
	};

    class DrawState extends RUtils.AdHoc {
	DrawState() {super(prog);}
	float sz() {return(sz);}
    }
    private final State draw = new DrawState();
    public void added(RenderTree.Slot slot) {
	slot.ostate(Pipe.Op.compose(mat, States.maskdepth, loc,
				    eyesort, /* XXXRENDER disable MSAA */
				    VertexColor.instance, draw));
	slots.add(slot);
    }
    public void removed(RenderTree.Slot slot) {
	slots.remove(slot);
    }

    public void delete() {
	spawn = false;
    }
}

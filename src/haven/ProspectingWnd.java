package haven;

import haven.render.*;
import me.ender.Reflect;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Math.*;

public class ProspectingWnd extends WindowX {
    private static final Queue<Dowse> EFFECTS = new ConcurrentLinkedQueue<>();
    public static final Queue<ProspectingWnd> WINDOWS = new ConcurrentLinkedQueue<>();
    public static final Queue<QualityList.Quality> QUALITIES = new ConcurrentLinkedQueue<>();
    private RenderTree.Slot slot;
    
    public ProspectingWnd(Coord sz, String cap) {
	super(sz, cap);
    }
    
    @Override
    public void destroy() {
	WINDOWS.remove(this);
	if(slot != null) {slot.remove();}
	super.destroy();
    }
    
    @Override
    protected void attach(UI ui) {
	super.attach(ui);
	WINDOWS.add(this);
	attachEffect();
    }
    
    private void fx(Dowse fx) {
	slot = ui.gui.map.drawadd(fx);
    }
    
    private static void attachEffect() {
	if(!WINDOWS.isEmpty() && !EFFECTS.isEmpty()) {
	    WINDOWS.remove().fx(EFFECTS.remove());
	}
    }
    
    public static void overlay(Gob gob, Gob.Overlay overlay) {
	if(!QUALITIES.isEmpty()) {
	    
	    double a1 = Reflect.getFieldValueDouble(overlay.spr, "a1");
	    double a2 = Reflect.getFieldValueDouble(overlay.spr, "a2");
	    
	    EFFECTS.add(new Dowse(gob, a1, a2, QUALITIES.remove()));
	    attachEffect();
	}
    }
    
    public static void item(WItem item) {
	if(item != null) {
	    QUALITIES.add(item.itemq.get().single());
	}
    }
    
    public static class Dowse extends Sprite {
	private static final VertexArray.Layout fmt = new VertexArray.Layout(
	    new VertexArray.Layout.Input(Homo3D.vertex, new VectorFormat(3, NumberFormat.FLOAT32), 0, 0, 16),
	    new VertexArray.Layout.Input(VertexColor.color, new VectorFormat(4, NumberFormat.UNORM8), 0, 12, 16)
	);
	private static final Pipe.Op state = Pipe.Op.compose(VertexColor.instance, Rendered.last, States.Depthtest.none, States.maskdepth, Rendered.postpfx);
	
	private final Coord3f c;
	private final double a1;
	private final double a2;
	private final double r;
	private final Model model;
	
	protected Dowse(Gob gob, double a1, double a2, QualityList.Quality q) {
	    super(gob, null);
	    this.c = new Coord3f((float) gob.rc.x, (float) -gob.rc.y, 0.1f);
	    this.a1 = a1;
	    this.a2 = a2;
	    if(q == null) {
		r = 100;
	    } else {
		r = 110 * (q.value - 10);
	    }
	    model = new Model(Model.Mode.TRIANGLE_FAN, new VertexArray(fmt, new VertexArray.Buffer(v2(), DataBuffer.Usage.STREAM)), null);
	}
	
	private ByteBuffer v2() {
	    ByteBuffer buf = ByteBuffer.allocate(128);
	    buf.order(ByteOrder.nativeOrder());
	    byte alpha = (byte) 80;
	    
	    buf.putFloat(c.x).putFloat(c.y).putFloat(c.z);
	    buf.put((byte) 255).put((byte) 0).put((byte) 0).put(alpha);
	    for (double ca = a1; ca < a2; ca += PI * 0x0.04p0) {
		buf = Utils.growbuf(buf, 16);
		buf.putFloat(c.x + (float) (cos(ca) * r)).putFloat(c.y + (float) (sin(ca) * r)).putFloat(c.z);
		buf.put((byte) 255).put((byte) 0).put((byte) 0).put(alpha);
	    }
	    buf = Utils.growbuf(buf, 16);
	    buf.putFloat(c.x + (float) (cos(a2) * r)).putFloat(c.y + (float) (sin(a2) * r)).putFloat(c.z);
	    buf.put((byte) 255).put((byte) 0).put((byte) 0).put(alpha);
	    ((Buffer) buf).flip();
	    return (buf);
	}
	
	
	public void added(RenderTree.Slot slot) {
	    slot.ostate(state);
	    slot.add(model);
	}
    }
}

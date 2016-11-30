package haven;

import javax.media.opengl.GL;
import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class ColoredRadius extends Sprite {
    final GLState smat;
    final VertexBuf.VertexArray posa;
    final VertexBuf.NormalArray nrma;
    final ShortBuffer sidx, eidx;
    private Coord lc;

    protected ColoredRadius(Sprite.Owner owner, float r, Color color) {
	super(owner, null);
	this.smat = new States.ColState(color);
	int i = Math.max(24, (int) (Math.PI * 2 * r / 11.0D));
	FloatBuffer posa = Utils.mkfbuf(i * 3 * 2);
	FloatBuffer nrma = Utils.mkfbuf(i * 3 * 2);
	ShortBuffer sidx = Utils.mksbuf(i * 6);
	ShortBuffer eidx = Utils.mksbuf(i);
	for (int j = 0; j < i; j++) {
	    float s = (float) Math.sin(Math.PI * 2 * j / i);
	    float c = (float) Math.cos(Math.PI * 2 * j / i);
	    posa.put(j * 3, c * r).put(j * 3 + 1, s * r).put(j * 3 + 2, 10.0F);
	    posa.put((i + j) * 3, c * r).put((i + j) * 3 + 1, s * r).put((i + j) * 3 + 2, -10.0F);
	    nrma.put(j * 3, c).put(j * 3 + 1, s).put(j * 3 + 2, 0.0F);
	    nrma.put((i + j) * 3, c).put((i + j) * 3 + 1, s).put((i + j) * 3 + 2, 0.0F);
	    int k = j * 6;
	    sidx.put(k, (short) j).put(k + 1, (short) (j + i)).put(k + 2, (short) ((j + 1) % i));
	    sidx.put(k + 3, (short) (j + i)).put(k + 4, (short) ((j + 1) % i + i)).put(k + 5, (short) ((j + 1) % i));
	    eidx.put(j, (short)j);
	}
	this.posa = new VertexBuf.VertexArray(posa);
	this.nrma = new VertexBuf.NormalArray(nrma);
	this.sidx = sidx;
	this.eidx = eidx;
    }

    @Override
    public void draw(GOut g) {
	g.state(smat);
	g.apply();

	this.posa.bind(g, false);
	this.nrma.bind(g, false);
	this.sidx.rewind();
	g.gl.glDrawElements(GL.GL_TRIANGLES, this.sidx.capacity(), GL.GL_UNSIGNED_SHORT, this.sidx);

	this.eidx.rewind();
	g.gl.glLineWidth(3.0F);
	g.gl.glDrawElements(GL.GL_LINE_LOOP, this.eidx.capacity(), GL.GL_UNSIGNED_SHORT, this.eidx);

	this.posa.unbind(g);
	this.nrma.unbind(g);
    }

    @Override
    public boolean setup(RenderList d) {
	d.prepo(Rendered.eyesort);
	d.prepo(Material.nofacecull);
	Location.goback(d.state(), "gobx");
	d.state().put(States.color, null);
	return true;
    }

    @Override
    public boolean tick(int dt) {
	Coord c = ((Gob) this.owner).rc.floor();
	if((this.lc == null) || (!this.lc.equals(c))) {
	    setz(this.owner.glob(), c);
	    this.lc = c;
	}
	return false;
    }

    private void setz(Glob glob, Coord c) {
	FloatBuffer posa = this.posa.data;
	int i = this.posa.size() / 2;
	try {
	    float f1 = glob.map.getcz(c.x, c.y);
	    for (int j = 0; j < i; j++) {
		float f2 = glob.map.getcz(c.x + posa.get(j * 3), c.y - posa.get(j * 3 + 1)) - f1;
		posa.put(j * 3 + 2, f2 + 10.0F);
		posa.put((i + j) * 3 + 2, f2);
	    }
	} catch (Loading ignored) {}
    }
}
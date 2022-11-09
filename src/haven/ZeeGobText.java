package haven;

import haven.render.Homo3D;
import haven.render.Pipe;
import haven.render.RenderTree;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;

public class ZeeGobText extends Sprite implements RenderTree.Node, PView.Render2D {
    private Text.Foundry font;
    private static final HashMap<CachedTexKey, CachedTexVal> texts = new HashMap<>();

    private static class CachedTexKey {

        Color col, colBorder;
        String text;

        CachedTexKey(String text, Color col) {
            this.colBorder = null;
            this.col = col;
            this.text = text;
        }

        CachedTexKey(String text, Color col, Color colBorder) {
            this.colBorder = colBorder;
            this.col = col;
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            CachedTexKey that = (CachedTexKey) o;
            boolean sameBorder = false;
            if (colBorder==null && that.colBorder==null)
                sameBorder = true;
            else if(colBorder!=null && Objects.equals(colBorder,that.colBorder))
                sameBorder = true;
            return sameBorder && Objects.equals(col, that.col) && Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            if (colBorder!=null)
                return Objects.hash(colBorder, col, text);
            return Objects.hash(col, text);
        }
    }

    private static class CachedTexVal {
        Tex tex;
        int cnt = 1;
        CachedTexVal(Tex tex) {
            this.tex = tex;
        }
    }

    public final String text;
    private Tex tex;
    private int zOfs;
    private Color col, colBorder;

    public ZeeGobText( String text, Color colText, Color colBorder, int zOfs, Text.Foundry font) {
        super(null, null);
        this.font = font;
        this.text = text;
        if (colBorder==null) {
            this.colBorder = null;
            this.tex = font.render(text, colText).tex();
        } else {
            this.colBorder = colBorder;
            this.tex = font.renderstroked(text, colText, colBorder).tex();
        }
        this.zOfs = zOfs;
        this.col = colText;
        CachedTexKey key;
        if(colBorder==null)
            key = new CachedTexKey(text, colText);
        else
            key = new CachedTexKey(text, colText, colBorder);
        CachedTexVal ctv = texts.get(key);
        if(ctv != null) {
            ctv.cnt++;
            this.tex = ctv.tex;
        } else {
            texts.put(key, new CachedTexVal(this.tex));
        }
    }

    public void draw(GOut g, Pipe state) {
        Coord sc = Homo3D.obj2view(new Coord3f(0, 0, 6 + zOfs), state, Area.sized(g.sz())).round2();
        g.aimage(tex, sc, 0.5, 0.5);
    }

    @Override
    public void removed(RenderTree.Slot slot) {
        CachedTexKey key;
        if(colBorder==null)
            key = new CachedTexKey(text, col);
        else
            key = new CachedTexKey(text, col, colBorder);
        CachedTexVal ctv = texts.get(key);
        if(ctv != null && --ctv.cnt == 0) {
            texts.remove(key);
        }
    }
}

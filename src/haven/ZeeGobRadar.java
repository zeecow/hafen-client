package haven;

import haven.VertexBuf.VertexData;
import haven.render.*;
import haven.render.DataBuffer.Usage;
import haven.render.Model.Indices;
import haven.render.Model.Mode;
import haven.render.Pipe.Op;
import haven.render.RenderTree.Slot;

import java.awt.*;
import java.nio.FloatBuffer;

public class ZeeGobRadar extends Sprite {
    static Op smat;
    final VertexData posa;
    final VertexBuf vbuf;
    final Model smod;

    public ZeeGobRadar(Gob gob, Coord3f dimension, Color c) {
        super(gob,null);
        smat = new BaseColor(c);
        // https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/chapter07/chapter7.html
        float[] positions = getCoordsRectangle(dimension.x,dimension.y,dimension.z);
        short[] indices = getIndicesRectangle();
        FloatBuffer var5 = FloatBuffer.wrap(positions);
        VertexData vertexData = new VertexData(var5);
        //FloatBuffer var6 = FloatBuffer.wrap(positions);
        //NormalData normalData = new NormalData(var6);
        VertexBuf vertexBuf = new VertexBuf(vertexData);
        this.smod = new Model(Mode.TRIANGLES, vertexBuf.data(), new Indices(indices.length, NumberFormat.UINT16, Usage.STATIC, DataBuffer.Filler.of(indices)));
        this.posa = vertexData;
        this.vbuf = vertexBuf;
    }

    private short[] getIndicesRectangle() {
        return new short[] {
                // top face
                0, 1, 3, 3, 1, 2,
                // front face
                // 4, 0, 3, 5, 4, 3,
                // ? face
                //3, 2, 7, 5, 3, 7,
                // ? face
                //6, 1, 0, 6, 0, 4,
                // ? face
                //2, 1, 6, 2, 6, 7,
                // ? face
                //7, 6, 4, 7, 4, 5,
        };
    }

    private float[] getCoordsRectangle(float x, float y, float z) {
        return new float[] {
            -x,  y,  z,
            -x, -y,  z,
             x, -y,  z,
             x,  y,  z,
            -x,  y, -z,
             x,  y, -z,
            -x, -y, -z,
             x, -y, -z
        };
    }

    public void added(Slot var1) {
        var1.ostate(Op.compose(Rendered.postpfx, new States.Facecull(States.Facecull.Mode.NONE), Location.goback("gobx")));
        var1.add(this.smod, smat);
    }
}

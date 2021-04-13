package haven.res.gfx.fx.msrad;

import haven.MenuGrid;
import haven.MenuGrid.Pagina;

/* >spr: MSRad */
public class ShowSupports extends MenuGrid.PagButton {
    public ShowSupports(Pagina pag) {
	super(pag);
    }

    public static class Fac implements Factory {
	public MenuGrid.PagButton make(Pagina pag) {
	    return(new ShowSupports(pag));
	}
    }

    public void use() {
	MSRad.show(!MSRad.show);
    }
}

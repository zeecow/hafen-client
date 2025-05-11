/* Preprocessed source code */
package haven.res.ui.croster;

import haven.Button;
import haven.IButton;

import java.awt.image.BufferedImage;

@haven.FromResource(name = "ui/croster", version = 75)
public class TypeButton extends IButton {
    public final int order;

    public TypeButton(BufferedImage up, BufferedImage down, int order) {
	super(up, down);
	this.order = order;
    }

    protected void depress() {
	ui.sfx(Button.clbtdown.stream());
    }

    protected void unpress() {
	ui.sfx(Button.clbtup.stream());
    }
}

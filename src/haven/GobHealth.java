/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.awt.*;
import java.awt.image.BufferedImage;

import haven.render.*;

public class GobHealth extends GAttrib implements Gob.SetupMod {
    public final int hp;
    public final MixColor fx;
    private static final Text.Foundry gobhpf = new Text.Foundry(Text.sans.deriveFont(Font.BOLD), 14);
    private static final BufferedImage[] gobhp = new BufferedImage[]{
	Text.renderstroked("25%", new Color(255, 100, 100), Color.BLACK, gobhpf).img,
	Text.renderstroked("50%", new Color(235, 130, 130), Color.BLACK, gobhpf).img,
	Text.renderstroked("75%", new Color(230, 185, 185), Color.BLACK, gobhpf).img
    };
    
    public GobHealth(Gob g, int hp) {
	super(g);
	this.hp = hp;
	this.fx = new MixColor(255, 0, 0, 128 - ((hp * 128) / 4));
    }
    
    public Pipe.Op gobstate() {
	if(hp >= 4)
	    return(null);
	return(fx);
    }

    public BufferedImage text() {
	if(hp <= gobhp.length && hp > 0) {
	    return gobhp[hp - 1];
	}
	return null;
    }

    public double asfloat() {
	return(((double)hp) / 4.0);
    }
}

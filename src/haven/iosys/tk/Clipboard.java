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

package haven.iosys.tk;

import java.util.*;
import java.util.function.*;
import java.nio.file.Path;
import java.awt.image.BufferedImage;

public interface Clipboard {
    public void put(Contents c);
    public Contents get();

    public static class Contents implements Iterable<Item<?>> {
	public final Collection<Item<?>> items;

	public Contents(Collection<Item<?>> items) {
	    this.items = items;
	}

	public Iterator<Item<?>> iterator() {
	    return(items.iterator());
	}

	public <F> Item<F> find(Format<F> fmt) {
	    for(Item<?> item : this) {
		if(item.fmt == fmt)
		    return(item.check(fmt));
	    }
	    return(null);
	}
    }

    public static class Format<T> {
	public static final Format<CharSequence> TEXT = new Format<CharSequence>();
	public static final Format<BufferedImage> IMAGE = new Format<BufferedImage>();
	public static final Format<Collection<Path>> PATHS = new Format<Collection<Path>>();

	private Format() {
	}
    }

    public static class Item<T> {
	public final Format<T> fmt;
	public final Consumer<Consumer<? super T>> item;

	public Item(Format<T> fmt, Consumer<Consumer<? super T>> item) {
	    this.fmt = fmt;
	    this.item = item;
	}
	public Item(Format<T> fmt, Supplier<? extends T> item) {
	    this(fmt, cb -> cb.accept(item.get()));
	}
	public Item(Format<T> fmt, T item) {
	    this(fmt, cb -> cb.accept(item));
	}

	@SuppressWarnings("unchecked")
	public <F> Item<F> check(Format<F> expected) {
	    if(fmt != expected)
		return(null);
	    return((Item<F>)this);
	}

	@SuppressWarnings("unchecked")
	public T fetch() {
	    Object[] buf = {null, null};
	    item.accept(value -> {
		synchronized(buf) {
		    buf[0] = value;
		    buf[1] = true;
		    buf.notifyAll();
		}
	    });
	    synchronized(buf) {
		boolean irq = false;
		while(buf[1] == null) {
		    try {
			buf.wait();
		    } catch(InterruptedException e) {
			irq = true;
		    }
		}
		if(irq)
		    Thread.currentThread().interrupt();
		return((T)buf[0]);
	    }
	}
    }

    public static enum Std {
	PRIMARY, CLIPBOARD;
    }
}

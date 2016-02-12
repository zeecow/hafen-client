package haven;

import haven.QualityList.SingleType;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemFilter {
    private static final Pattern q = Pattern.compile("(?:(\\w+))?(?:^|:)([\\w\\*]+)?(?:([<>=+~])(\\d+(?:\\.\\d+)?)?([<>=+~])?)?");
    private static final Pattern float_p = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    public boolean matches(List<ItemInfo> info) {
	for (ItemInfo item : info) {
	    if(item instanceof ItemInfo.Name) {
		if(match((ItemInfo.Name) item)) { return true;}
	    } else if(item instanceof ItemInfo.Contents) {
		if(match((ItemInfo.Contents) item)) {return true;}
	    } else if(item instanceof Curiosity) {
		if(match((Curiosity) item)) {return true;}
	    }
	}
	return match(new QualityList(info));

    }

    protected boolean match(Curiosity item) { return false; }

    protected boolean match(QualityList quality) {return false;}

    protected boolean match(ItemInfo.Contents item) {
	return false;
    }

    protected boolean match(ItemInfo.Name item) {
	return false;
    }

    public static ItemFilter create(String query) {
	Compound result = new Compound();
	Matcher m = q.matcher(query);
	while (m.find()) {
	    String tag = m.group(1);
	    String text = m.group(2);
	    String sign = m.group(3);
	    String value = m.group(4);
	    String opt = m.group(5);

	    if(text == null) {
		text = "";
	    } else {
		text = text.toLowerCase();
	    }

	    ItemFilter filter = null;
	    if(sign != null && tag == null) {
		switch (text) {
		    case "xp":
		    case "lp":
		    case "mw":
			tag = text;
			break;
		    case "q":
			tag = "q";
			text = "single";
			break;
		}
	    }
	    if(tag == null) {
		filter = new Text(text, false);
	    } else {
		tag = tag.toLowerCase();
		switch (tag) {
		    case "txt":
			filter = new Text(text, true);
			break;
		    case "xp":
		    case "lp":
		    case "mw":
			filter = new XP(tag, sign, value, opt);
			break;
		    case "has":
			filter = new Has(text, sign, value, opt);
			break;
		    case "q":
			filter = new Q(text, sign, value, opt);
			break;
		}
	    }
	    if(filter != null) {
		result.add(filter);
	    }
	}
	return result;
    }

    public static class Compound extends ItemFilter {
	List<ItemFilter> filters = new LinkedList<ItemFilter>();

	@Override
	public boolean matches(List<ItemInfo> info) {
	    if(filters.isEmpty()) {return false;}
	    for (ItemFilter filter : filters) {
		if(!filter.matches(info)) {return false;}
	    }
	    return true;
	}

	public void add(ItemFilter filter) {
	    filters.add(filter);
	}
    }

    private static class Complex extends ItemFilter {
	protected final String text;
	protected final Sign sign;
	protected final Sign opts;
	protected float value;
	protected final boolean all;
	protected final boolean any;

	public Complex(String text, String sign, String value, String opts) {
	    this.text = text.toLowerCase();
	    this.sign = getSign(sign);
	    this.opts = getSign(opts);
	    float tmp = 0;
	    try {
		tmp = Float.parseFloat(value);
	    } catch (Exception ignored) {}
	    this.value = tmp;

	    all = text.equals("*") || text.equals("all");
	    any = text.equals("any");
	}

	protected boolean test(double actual, double target) {
	    switch (sign) {
		case GREATER:
		    return actual > target;
		case LESS:
		    return actual <= target;
		case EQUAL:
		    return actual == target;
		case GREQUAL:
		    return actual >= target;
		default:
		    return actual > 0;
	    }
	}

	protected Sign getSign(String sign) {
	    if(sign == null) {
		return getDefaultSign();
	    }
	    switch (sign) {
		case ">":
		    return Sign.GREATER;
		case "<":
		    return Sign.LESS;
		case "=":
		    return Sign.EQUAL;
		case "+":
		    return Sign.GREQUAL;
		case "~":
		    return Sign.WAVE;
		default:
		    return getDefaultSign();
	    }
	}

	protected Sign getDefaultSign() {
	    return Sign.DEFAULT;
	}

	public enum Sign {GREATER, LESS, EQUAL, GREQUAL, WAVE, DEFAULT}
    }

    private static class Has extends Complex {
	public Has(String text, String sign, String value, String opts) {
	    super(text, sign, value, opts);
	}

	@Override
	protected boolean match(ItemInfo.Contents item) {
	    String name = this.name(item.sub).toLowerCase();
	    float num = count(name);
	    return name.contains(text) && test(num, value);
	}

	@Override
	protected Sign getDefaultSign() {
	    return Sign.GREQUAL;
	}

	private float count(String txt) {
	    float n = 0;
	    if(txt != null) {
		try {
		    Matcher matcher = float_p.matcher(txt);
		    if(matcher.find()) {
			n = Float.parseFloat(matcher.group(1));
		    }
		} catch (Exception ignored) {}
	    }
	    return n;
	}

	private String name(List<ItemInfo> sub) {
	    String txt = null;
	    for (ItemInfo subInfo : sub) {
		if(subInfo instanceof ItemInfo.Name) {
		    ItemInfo.Name name = (ItemInfo.Name) subInfo;
		    txt = name.str.text;
		}
	    }
	    return txt;
	}
    }

    public static class Text extends ItemFilter {
	private String text;
	private final boolean full;

	public Text(String text, boolean full) {
	    this.full = full;
	    this.text = text.toLowerCase();
	}

	public void update(String text) {
	    this.text = text.toLowerCase();
	}

	@Override
	protected boolean match(ItemInfo.Name item) {
	    return item.str.text.toLowerCase().contains(text);
	}
    }

    private static class XP extends Complex {
	public XP(String text, String sign, String value, String opt) {super(text, sign, value, opt);}

	@Override
	protected boolean match(Curiosity item) {
	    if("lp".equals(text)) {
		return test(item.exp, value);
	    } else if("xp".equals(text)) {
		return test(item.enc, value);
	    } else if("mw".equals(text)) {
		return test(item.mw, value);
	    }
	    return false;
	}


	@Override
	protected Sign getDefaultSign() {
	    return Sign.GREQUAL;
	}
    }

    private static class Q extends Complex {
	public Q(String text, String sign, String value, String opts) { super(text, sign, value, opts); }

	@Override
	protected boolean match(QualityList quality) {
	    if(quality.isEmpty()) {return false;}

	    SingleType type = null;
	    if(text != null && !text.isEmpty()) {
		type = getTextType(text);
	    }

	    if(type == null) {
		type = getGenericType();
	    }

	    if(type == null) {
		return test(quality.single().value, value);
	    } else {
		return test(quality.single(type).value, value);
	    }
	}

	private SingleType getTextType(String text) {
	    SingleType[] types = SingleType.values();
	    for (SingleType type : types) {
		if(type.name().toLowerCase().startsWith(text)) {
		    return type;
		}
	    }
	    return null;
	}

	private SingleType getGenericType() {
	    switch (opts) {
		case GREATER:
		    return SingleType.Max;
		case LESS:
		    return SingleType.Min;
		case EQUAL:
		case WAVE:
		    return SingleType.Average;
		default:
		    return null;
	    }
	}
    }
}
package haven;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class RadarCFG {
    public static final List<Group> groups = new LinkedList<>();

    static {
	String xml = Config.loadFile("radar.xml");
	if(xml != null) {
	    try {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
		groups.clear();
		NodeList groupNodes = doc.getElementsByTagName("group");
		for(int i = 0; i < groupNodes.getLength(); i++) {
		    groups.add(new Group((Element) groupNodes.item(i)));
		}
	    } catch(ParserConfigurationException | IOException | SAXException ignored) {
		ignored.printStackTrace();
	    }
	}
    }

    public static void init() {

    }

    private static Resource loadres(String name) {
	return Resource.remote().load(name).get();
    }

    public static class Group {
	public String name;
	public Boolean visible = null;
	public Integer priority = null;
	public List<MarkerCFG> markerCFGs;

	public Group(Element config) {
	    name = config.getAttribute("name");
	    if(config.hasAttribute("show")) {
		visible = config.getAttribute("show").toLowerCase().equals("true");
	    }
	    if(config.hasAttribute("priority")) {
		try {
		    priority = Integer.parseInt(config.getAttribute("priority"));
		} catch(NumberFormatException ignored) {
		}
	    }
	    NodeList children = config.getElementsByTagName("match");
	    markerCFGs = new LinkedList<>();
	    for(int i = 0; i < children.getLength(); i++) {
		markerCFGs.add(MarkerCFG.parse((Element) children.item(i), this));
	    }

	}
    }

    public static class MarkerCFG {
	private Group parent;
	private Match type;
	private String pattern;
	private Boolean visible = null;
	private Integer priority = null;
	public String icon = null;
	public String name = null;
	private Tex tex;

	public static MarkerCFG parse(Element config, Group parent) {
	    MarkerCFG cfg = new MarkerCFG();

	    cfg.parent = parent;
	    Match[] types = Match.values();
	    String name = null;
	    for(Match type : types) {
		name = type.name();
		if(config.hasAttribute(name)) {
		    break;
		}
	    }
	    if(name == null) {
		throw new RuntimeException();
	    }
	    cfg.type = Match.valueOf(name);
	    cfg.pattern = config.getAttribute(name);
	    if(config.hasAttribute("show")) {
		cfg.visible = config.getAttribute("show").toLowerCase().equals("true");
	    }
	    if(config.hasAttribute("image")) {
		cfg.icon = config.getAttribute("image");
	    }
	    if(config.hasAttribute("priority")) {
		try {
		    cfg.priority = Integer.parseInt(config.getAttribute("priority"));
		} catch(NumberFormatException ignored) {
		}
	    }

	    return cfg;
	}

	public Tex tex() {
	    if(tex == null && icon != null) {
		try {
		    Resource.Image img = loadres(icon).layer(Resource.imgc);

		    Tex tex = img.tex();
		    if((tex.sz().x <= 20) && (tex.sz().y <= 20)) {
			this.tex = tex;
		    } else {
			BufferedImage buf = img.img;
			buf = PUtils.rasterimg(PUtils.blurmask2(buf.getRaster(), 1, 1, Color.BLACK));
			buf = PUtils.convolvedown(buf, new Coord(20, 20), GobIcon.filter);
			this.tex = new TexI(buf);
		    }

		} catch(Loading ignored) {
		}
	    }
	    return tex;
	}


	public boolean match(String target) {
	    return type.match(pattern, target);
	}

	public boolean visible() {
	    if(parent.visible != null && !parent.visible) {
		return false;
	    } else if(visible != null) {
		return visible;
	    } else {
		return true;
	    }
	}

	public int priority() {
	    return (priority != null) ? priority : ((parent != null && parent.priority != null) ? parent.priority : 0);
	}
    }

    enum Match {
	exact {
	    @Override
	    public boolean match(String pattern, String target) {
		return target.equals(pattern);
	    }
	},
	regex {
	    @Override
	    public boolean match(String pattern, String target) {
		return target.matches(pattern);
	    }
	},
	startsWith {
	    @Override
	    public boolean match(String pattern, String target) {
		return target.startsWith(pattern);
	    }
	},
	contains {
	    @Override
	    public boolean match(String pattern, String target) {
		return target.contains(pattern);
	    }
	};

	public abstract boolean match(String pattern, String target);
    }
}

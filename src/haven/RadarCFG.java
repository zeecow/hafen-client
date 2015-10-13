package haven;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class RadarCFG {
    public static final List<Group> groups = new LinkedList<>();
    private static DocumentBuilder builder;

    static {
	String xml = Config.loadFile("radar.xml");
	if(xml != null) {
	    try {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		builder = documentBuilderFactory.newDocumentBuilder();
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

    public static synchronized void save() {
	try {
	    Document doc = builder.newDocument();

	    // construct XML
	    Element root = doc.createElement("icons");
	    doc.appendChild(root);
	    for(Group group : groups) {
		Element el = doc.createElement("group");
		group.write(el);
		root.appendChild(el);
	    }

	    // write XML
	    OutputStream out = new ByteArrayOutputStream();
	    Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	    DOMSource source = new DOMSource(doc);
	    StreamResult console = new StreamResult(out);
	    transformer.transform(source, console);
	    Config.saveFile("radar.xml", out.toString());

	} catch(Exception e) {
	    e.printStackTrace();
	}
    }

    private static Resource loadres(String name) {
	return Resource.remote().load(name).get();
    }

    private static Tex makeicon(String icon) {
	Tex tex = null;
	try {
	    Resource.Image img = loadres(icon).layer(Resource.imgc);

	    tex = img.tex();
	    if((tex.sz().x > 20) || (tex.sz().y > 20)) {
		BufferedImage buf = img.img;
		buf = PUtils.rasterimg(PUtils.blurmask2(buf.getRaster(), 1, 1, Color.BLACK));
		buf = PUtils.convolvedown(buf, new Coord(20, 20), GobIcon.filter);
		tex = new TexI(buf);
	    }

	} catch(Loading ignored) {
	}
	return tex;
    }

    public static class Group {
	public static final Tex DEF_TEX = Resource.loadtex("gfx/hud/mmap/o");
	public String name, icon;
	public Boolean show = null;
	public Integer priority = null;
	public List<MarkerCFG> markerCFGs;
	private Tex tex = null;

	public Group(Element config) {
	    name = config.getAttribute("name");
	    if(config.hasAttribute("icon")) {
		icon = config.getAttribute("icon");
	    }
	    if(config.hasAttribute("show")) {
		show = config.getAttribute("show").toLowerCase().equals("true");
	    }
	    if(config.hasAttribute("priority")) {
		try {
		    priority = Integer.parseInt(config.getAttribute("priority"));
		} catch(NumberFormatException ignored) {
		}
	    }
	    NodeList children = config.getElementsByTagName("marker");
	    markerCFGs = new LinkedList<>();
	    for(int i = 0; i < children.getLength(); i++) {
		markerCFGs.add(MarkerCFG.parse((Element) children.item(i), this));
	    }

	}

	public void write(Element el) {
	    Document doc = el.getOwnerDocument();
	    el.setAttribute("name", name);
	    if(icon != null) {
		el.setAttribute("icon", icon);
	    }
	    if(show != null) {
		el.setAttribute("show", show.toString());
	    }
	    if(priority != null) {
		el.setAttribute("priority", priority.toString());
	    }
	    for(MarkerCFG marker : markerCFGs) {
		Element mel = doc.createElement("marker");
		marker.write(mel);
		el.appendChild(mel);
	    }
	}

	public Tex tex() {
	    if(tex == null) {
		if(icon != null) {
		    tex = makeicon(icon);
		} else {
		    tex = DEF_TEX;
		}
	    }
	    return tex;
	}
    }

    public static class MarkerCFG {
	public Group parent;
	private Match type;
	private String pattern;
	private Boolean show = null;
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
	    if(config.hasAttribute("name")) {
		cfg.name = config.getAttribute("name");
	    }
	    cfg.type = Match.valueOf(name);
	    cfg.pattern = config.getAttribute(name);
	    if(config.hasAttribute("show")) {
		cfg.show = config.getAttribute("show").toLowerCase().equals("true");
	    }
	    if(config.hasAttribute("icon")) {
		cfg.icon = config.getAttribute("icon");
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
		tex = makeicon(icon);
	    }
	    return tex;
	}


	public boolean match(String target) {
	    return type.match(pattern, target);
	}

	public boolean visible() {
	    if(parent.show != null && !parent.show) {
		return false;
	    } else if(show != null) {
		return show;
	    } else {
		return true;
	    }
	}

	public int priority() {
	    return (priority != null) ? priority : ((parent != null && parent.priority != null) ? parent.priority : 0);
	}

	public void write(Element el) {
	    el.setAttribute(type.name(), pattern);
	    if(icon != null) {
		el.setAttribute("icon", icon);
	    }
	    if(name != null) {
		el.setAttribute("name", name);
	    }
	    if(show != null) {
		el.setAttribute("show", show.toString());
	    }
	    if(priority != null) {
		el.setAttribute("priority", priority.toString());
	    }
	}
    }

    public static class GroupCheck extends CheckBox {
	public final Group group;

	public GroupCheck(Group group) {
	    super(group.name);
	    this.group = group;
	    this.hitbox = true;
	    this.a = group.show == null || group.show;
	}

	@Override
	public void changed(boolean val) {
	    group.show = val;
	}
    }

    public static class MarkerCheck extends CheckBox {
	public final MarkerCFG marker;

	public MarkerCheck(MarkerCFG marker) {
	    super(marker.name != null ? marker.name : marker.pattern);
	    this.marker = marker;
	    this.a = marker.show == null || marker.show;
	}

	@Override
	public void changed(boolean val) {
	    marker.show = val;
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

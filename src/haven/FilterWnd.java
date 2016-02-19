package haven;

public class FilterWnd extends GameUI.Hidewnd {
    TextEntry input;

    FilterWnd() {
	super(new Coord(120, 200), "Filter");
	//cap = null;

	input = add(new TextEntry(200, "") {
	    @Override
	    protected void changed() {
		checkInput();
	    }
	});

	pack();
	hide();
    }

    private void setFilter(String text) {
	ItemFilter filter = null;
	if(text != null) {
	    filter = ItemFilter.create(text);
	}
	GItem.setFilter(filter);
    }

    private void checkInput() {
	if(input.text.length() >= 2) {
	    setFilter(input.text);
	} else {
	    setFilter(null);
	}
    }

    @Override
    public void hide() {
	super.hide();
	setFilter(null);
    }

    @Override
    public void show() {
	super.show();
	setfocus(input);
	checkInput();
	raise();
    }
}

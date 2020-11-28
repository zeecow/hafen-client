import haven.ItemInfo;
import haven.L10N;

public class CustName
    implements ItemInfo.InfoFactory {
    public ItemInfo build(ItemInfo.Owner owner, Object[] args) {
	return new ItemInfo.Name(owner, L10N.label((String) args[1]));
    }
}
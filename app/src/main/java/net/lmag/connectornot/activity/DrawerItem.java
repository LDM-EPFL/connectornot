package net.lmag.connectornot.activity;

/**
 * Created by louismagarshack on 11/26/14.
 */
public class DrawerItem {

    private String mName;
    private ItemKind mKind;

    public static enum ItemKind {
        NO_SWITCH,
        ON_SWITCH,
        OFF_SWITCH
    }

    public DrawerItem(String name, ItemKind kind) {
        mName = name;
        mKind = kind;
    }

    public ItemKind getKind() {
        return mKind;
    }

    public String getName() {
        return mName;
    }
}

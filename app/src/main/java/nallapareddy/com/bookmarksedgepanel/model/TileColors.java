package nallapareddy.com.bookmarksedgepanel.model;

import nallapareddy.com.bookmarksedgepanel.R;

public enum TileColors {
    RED("Red", R.color.red),
    Gray("Gray", R.color.gray),
    BLUE("Blue", R.color.blue),
    GREEN("Green", R.color.green),
    PINK("Pink", R.color.pink),
    ORANGE("Orange", R.color.orange),
    TEAL("Teal", R.color.teal),
    INDIGO("Indigo", R.color.indigo),
    PURPLE("Purple", R.color.purple),
    BROWN("Brown", R.color.brown),
    BLACK("Black", R.color.black);

    String name;
    int colorId;

    TileColors(String name, int colorId) {
        this.name = name;
        this.colorId = colorId;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getColorId() {
        return colorId;
    }
}

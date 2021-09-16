package nallapareddy.com.bookmarksedgepanel.model;

public class Position {
    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    /**
     * Translates Strings of the form "(i,j)" to a position. Anything else may not work!
     * @param pos the String to translate. Needs to be in form "(i,j)"
     * @return Position translated
     */
    public static Position fromString(String pos) {
        pos = pos.substring(1, pos.length() - 1);
        int comma = pos.indexOf(',');
        int row = Integer.parseInt(pos.substring(0, comma));
        int col = Integer.parseInt(pos.substring(comma + 1));
        return new Position(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}

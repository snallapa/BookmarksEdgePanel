package nallapareddy.com.bookmarksedgepanel.model;

import android.content.Context;

import nallapareddy.com.bookmarksedgepanel.utils.ModelUtils;


public class BookmarkModel implements IBookmarkModel<Bookmark> {
    public static final int ROWS =  6;
    public static final int COLUMNS = 2;

    private Bookmark[][] bookmarks;
    private Context context;

    public BookmarkModel(Context context) {
        this.context = context.getApplicationContext();
        ModelUtils.convertPreferences(context);
        this.bookmarks = new Bookmark[ROWS][COLUMNS];
        //bookmarks = ModelUtils.readItems(context);
    }

    @Override
    public Bookmark getBookmark(Position pos) {
        return bookmarks[pos.getRow()][pos.getRow()];
    }

    @Override
    public void removeBookmark(Position pos) {
        setBookmark(pos, null);
    }

    @Override
    public void setBookmark(Position pos, Bookmark bookmark) {
        bookmarks[pos.getRow()][pos.getCol()] = bookmark;
    }

    @Override
    public void save() {
        //ModelUtils.writeItems(context, bookmarks);
    }

    @Override
    public GridDisplayType getDisplayType() {
        for(int i = 0; i < ROWS; i++) {
            if (bookmarks[i][COLUMNS - 1] != null) {
                return GridDisplayType.DOUBLE_COLUMN;
            }
        }
        return GridDisplayType.SINGLE_COLUMN;
    }


}

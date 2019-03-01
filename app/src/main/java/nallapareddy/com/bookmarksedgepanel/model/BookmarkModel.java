package nallapareddy.com.bookmarksedgepanel.model;

import android.content.Context;
import android.util.SparseIntArray;

import java.util.List;

import nallapareddy.com.bookmarksedgepanel.utils.ModelUtils;


public class BookmarkModel implements IBookmarkModel<Bookmark> {
    private List<Bookmark> bookmarks;
    private Context context;
    private SparseIntArray edgeBookmarks;
    public static final int LIMIT = 12;

    public BookmarkModel(Context context) {
        this.context = context.getApplicationContext();
        ModelUtils.convertPreferences(context);
        bookmarks = ModelUtils.readItems(context);
        this.edgeBookmarks = new SparseIntArray(LIMIT);
        for (int i = 0; i < bookmarks.size(); i++) {
            Bookmark bookmark = bookmarks.get(i);
            edgeBookmarks.append(bookmark.getEdgePosition(), i);
        }
    }

    @Override
    public List<Bookmark> getBookmarks() {
        return bookmarks;
    }

    @Override
    public Bookmark getBookmark(int position) {
        return bookmarks.get(position);
    }

    @Override
    public void removeBookmark(int position) {
        bookmarks.remove(position);
    }

    @Override
    public void addBookmark(Bookmark bookmark) {
        if (size() < LIMIT) {
            bookmarks.add(bookmark);
        }
    }

    @Override
    public int size() {
        return bookmarks.size();
    }

    @Override
    public void updateBookmark(int position, Bookmark bookmark) {
        bookmarks.set(position, bookmark);
    }

    @Override
    public void save() {
        ModelUtils.writeItems(context, bookmarks);
    }


    @Override
    public int getBookmarkForEdgePosition(int position) {
        return edgeBookmarks.get(position, -1);
    }

    @Override
    public void setEdgePosition(int oldPosition, int newPosition) {
        int i = edgeBookmarks.get(oldPosition);
        if (i != -1 ) {
            Bookmark bookmark = getBookmark(i);
            bookmark.setEdgePosition(newPosition);
            edgeBookmarks.delete(oldPosition);
            edgeBookmarks.append(newPosition, i);
        }
    }
}

package nallapareddy.com.bookmarksedgepanel.model;

import android.content.Context;

import java.util.List;

import nallapareddy.com.bookmarksedgepanel.utils.ModelUtils;


public class BookmarkModel implements IBookmarkModel<Bookmark> {
    private List<Bookmark> bookmarks;
    private Context context;
    public static final int LIMIT = 12;

    public BookmarkModel(Context context) {
        this.context = context;
        ModelUtils.convertPreferences(context);
        bookmarks = ModelUtils.readItems(context);
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
}

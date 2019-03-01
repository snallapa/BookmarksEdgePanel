package nallapareddy.com.bookmarksedgepanel.model;

import java.util.List;

/**
 * A representation of the model used to hold bookmarks.
 */

public interface IBookmarkModel<K> {

    List<K> getBookmarks();

    K getBookmark(int position);

    int getBookmarkForEdgePosition(int position);

    void setEdgePosition(int oldPosition, int newPosition);

    void removeBookmark(int position);

    void addBookmark(K bookmark);

    int size();

    void updateBookmark(int position, K bookmark);

    void save();
}

package nallapareddy.com.bookmarksedgepanel.model;

/**
 * A representation of the model used to hold bookmarks.
 */

public interface IBookmarkModel<K> {

    K getBookmark(Position position);

    void removeBookmark(Position position);

    void setBookmark(Position position, K bookmark);

    void save();

    GridDisplayType getDisplayType();
}

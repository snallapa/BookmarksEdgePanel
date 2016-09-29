package nallapareddy.com.bookmarksedgepanel.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nallapareddy.com.bookmarksedgepanel.model.Bookmark;

import static android.content.Context.MODE_PRIVATE;

public class ModelUtils {

    private static String BOOKMARKS_URI = "bookmarks_uri";
    private static String BOOKMARKS_TITLES = "bookmarks_title";
    private static String PREFERENCE_KEYS = "com.nallapareddy.bookmarks.BOOKMARKS_EDGE_PREFERENCES";

    private static final String NO_TITLE = "NO_TITLE";
    private static final String DELIMITER = "@";
    private static final String DELIMITER_NEW = "^";

    private static void saveBookmarks(Context context, List<Bookmark> bookmarks) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_KEYS, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(BOOKMARKS_URI, convertUriToString(bookmarks));
        editor.putString(BOOKMARKS_TITLES, concatTitles(bookmarks));
        editor.commit();
    }

    private static void clear(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_KEYS, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    public static void writeItems(Context context, List<Bookmark> bookmarks) {
        File filesDir = context.getFilesDir();
        File bookmarksFile = new File(filesDir, "bookmarks.txt");
        try {
            FileUtils.writeLines(bookmarksFile, bookmarks);
        } catch (IOException exception) {
            Log.e("PREFERENCES", "Could not write to file");
            saveBookmarks(context, bookmarks);
        }
    }

    public static List<Bookmark> readItems(Context context) {
        File filesDir = context.getFilesDir();
        File bookmarksFile = new File(filesDir, "bookmarks.txt");
        try {
            List<String> bookmarksString = new ArrayList<>(FileUtils.readLines(bookmarksFile));
            List<Bookmark> bookmarks = new ArrayList<>();
            for (String bookmarkString : bookmarksString) {
                String[] split = bookmarkString.split("\\" + DELIMITER_NEW);
                Bookmark bookmark = new Bookmark(Uri.parse(split[0].trim()));
                bookmark.setTitle(split[1]);
                bookmark.setFullInfo(split[2].equals("true"));
                bookmark.setShortUrl(split[3]);
                bookmark.setUseFavicon(split[4].equals("true"));
                bookmark.setTextOption(split[5]);
                bookmark.setColorPosition(Integer.parseInt(split[6]));
                bookmarks.add(bookmark);
            }
            return bookmarks;
        } catch (IOException exception) {
            Log.e("PREFERENCES", "Could not write to file");
            return getBookmarks(context);
        }
    }

    private static String convertUriToString(List<Bookmark> bookmarks) {
        String savedString = "";
        for (Bookmark bookmark : bookmarks) {
            savedString += DELIMITER + bookmark.getUri().toString();
        }
        return TextUtils.isEmpty(savedString) ? savedString : savedString.substring(1);
    }

    private static String concatTitles(List<Bookmark> bookmarks) {
        String savedString = "";
        for (Bookmark bookmark : bookmarks) {
            String title = bookmark.getTitle();
            if (!TextUtils.isEmpty(title)) {
                savedString += DELIMITER + title;
            } else {
                savedString += DELIMITER + NO_TITLE;
            }
        }
        return TextUtils.isEmpty(savedString) ? savedString : savedString.substring(1);
    }

    private static List<Bookmark> getBookmarks(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_KEYS, MODE_PRIVATE);
        String uriString = preferences.getString(BOOKMARKS_URI, "");
        String titleString = preferences.getString(BOOKMARKS_TITLES, "");
        List<String> uriStrings = splitStringToList(uriString, DELIMITER);
        List<String> titleStrings = splitStringToList(titleString, DELIMITER);
        List<Bookmark> bookmarks = new ArrayList<>();
        for (int i = 0; i < uriStrings.size(); i++) {
            String currentUri = uriStrings.get(i);
            if (TextUtils.isEmpty(currentUri)) {
                continue;
            }

            if (i < titleStrings.size()) {
                String currentTitleString = titleStrings.get(i);
                if (!TextUtils.isEmpty(currentTitleString) && !currentTitleString.trim().equals(NO_TITLE)) {
                    bookmarks.add(new Bookmark(Uri.parse(currentUri), currentTitleString));
                } else {
                    bookmarks.add(new Bookmark(Uri.parse(currentUri)));
                }
            } else {
                bookmarks.add(new Bookmark(Uri.parse(currentUri)));
            }
        }
        return bookmarks;
    }

    private static List<String> splitStringToList(String string, String expression) {
        String[] splitUri = string.split(expression);
        return Arrays.asList(splitUri);
    }

    public static void convertPreferences(Context context) {
        List<Bookmark> bookmarks = getBookmarks(context);
        if (bookmarks != null && !bookmarks.isEmpty()) {
            writeItems(context, bookmarks);
        }
        clear(context);
    }
}

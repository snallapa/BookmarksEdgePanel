package nallapareddy.com.bookmarksedgepanel.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.model.BookmarkModel;

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

    public static boolean writeItems(Context context, List<Bookmark> bookmarks) {
        File filesDir = context.getFilesDir();
        Gson gson = new Gson();
        File bookmarksFile = new File(filesDir, "bookmarks.json");
        try {
            String bookmarksJson = gson.toJson(bookmarks);
            FileUtils.write(bookmarksFile, bookmarksJson);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
//            Crashlytics.logException(e);
        }
        return false;
    }

    public static List<Bookmark> readItems(Context context) {
        File filesDir = context.getFilesDir();
        File oldFile = new File(filesDir, "bookmarks-new.txt");
        boolean conversion = false;
        if (oldFile.exists()) {
            List<Bookmark> bookmarks = readLineItems(context);
            boolean success = writeItems(context, bookmarks);
            if (success && oldFile.delete()) {
                conversion = true;
                //TODO Fabric Event
            }
        }
        File bookmarksFile = new File(filesDir, "bookmarks.json");
        if (!bookmarksFile.exists()) {
            //could be a new user
            return new ArrayList<>();
        }
        try {
            String json = FileUtils.readFileToString(bookmarksFile);
            Gson gson = new Gson();
            Type collectionType = new TypeToken<List<Bookmark>>(){}.getType();
            List<Bookmark> bookmarks = gson.fromJson(json, collectionType);
            if (conversion || (bookmarks.size() > 1 && bookmarks.get(0).getEdgePosition() == -1)) {
                int limit = BookmarkModel.LIMIT;
                for (int i = 0; i < bookmarks.size(); i++) {
                    Bookmark bookmark = bookmarks.get(i);
                    if (i < limit/2) {
                        bookmark.setEdgePosition(i * 2);
                    } else {
                        bookmark.setEdgePosition(2 * i - (limit - 1));
                    }
                }
            }
            return bookmarks;
        } catch (Exception e) {
            e.printStackTrace();
//            Crashlytics.logException(e);
        }
        return new ArrayList<>();
    }

    public static List<Bookmark> readOldItems(Context context) {
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
            return null;
        }
    }

    public static List<Bookmark> readLineItems(Context context) {
        File filesDir = context.getFilesDir();
        File bookmarksFile = new File(filesDir, "bookmarks-new.txt");
        try {
            List<String> bookmarksString = new ArrayList<>(FileUtils.readLines(bookmarksFile));
            List<Bookmark> bookmarks = new ArrayList<>();
            for (int i = 0; i < bookmarksString.size(); i++) {
                Bookmark bookmark = new Bookmark(Uri.parse(bookmarksString.get(i).trim()));
                i++;
                bookmark.setTitle(bookmarksString.get(i));
                i++;
                bookmark.setFullInfo(bookmarksString.get(i).equals("true"));
                i++;
                bookmark.setShortUrl(bookmarksString.get(i));
                i++;
                bookmark.setUseFavicon(bookmarksString.get(i).equals("true"));
                i++;
                bookmark.setTextOption(bookmarksString.get(i));
                i++;
                try {
                    bookmark.setColorPosition(Integer.parseInt(bookmarksString.get(i)));
                } catch (NumberFormatException e) {
                    bookmark.setColorPosition(0);
                    Log.e("PREFERENCES", "readItems: " + bookmarksString.get(i), e);
                }
                bookmarks.add(bookmark);
            }
            return bookmarks;
        } catch (IOException exception) {
            Log.e("PREFERENCES", "Could not read from file");
            return getBookmarks(context);
        } catch (Exception e) {
            Crashlytics.logException(e);
            if (bookmarksFile.exists()) {
                bookmarksFile.delete();
            }
            return new ArrayList<>();
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
        } else {
            try {
                bookmarks = readOldItems(context);
                if (bookmarks != null) {
                    writeItems(context, bookmarks);
                }
            } catch (Exception e) {
                //we are good because there are no obsolete items
            } finally {
                File filesDir = context.getFilesDir();
                File oldBookmarkFile = new File(filesDir, "bookmarks.txt");
                FileUtils.deleteQuietly(oldBookmarkFile);
            }
        }
        clear(context);
    }
}

package nallapareddy.com.bookmarksedgepanel.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
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
    private static String PREFERENCE_KEYS = "com.nallapareddy.bookmarks.BOOKMARKS_EDGE_PREFERENCES";

    private static final String DELIMITER = "@";


    private static Bookmark[][] convertToGrid(List<Bookmark> bookmarks) {
        Bookmark[][] newBookmarks = new Bookmark[BookmarkModel.ROWS][BookmarkModel.COLUMNS];
        if (bookmarks == null) {
            return newBookmarks;
        }
        for (int i = 0; i < bookmarks.size(); i++) {
            Bookmark bookmark = bookmarks.get(i);
            if (i < 6) {
                newBookmarks[i][0] = bookmark;
            } else {
                newBookmarks[i-6][1] = bookmark;
            }
        }
        return newBookmarks;
    }

    public static boolean writeItems(Context context, Bookmark[][] bookmarks) {
        File filesDir = context.getFilesDir();
        Gson gson = new Gson();
        File bookmarksFile = new File(filesDir, "bookmarksGrid.json");
        try {
            String bookmarksJson = gson.toJson(bookmarks);
            FileUtils.write(bookmarksFile, bookmarksJson);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return false;
    }

    public static Bookmark[][] readGridItems(Context context) {
        File filesDir = context.getFilesDir();
        File oldFile = new File(filesDir, "bookmarks.json");
        boolean conversion = false;
        if (oldFile.exists()) {
            List<Bookmark> bookmarks = readItems(context);
            Bookmark[][] newBookmarks = convertToGrid(bookmarks);
            boolean success = writeItems(context, newBookmarks);

            if (success && oldFile.delete()) {
                conversion = true;
            }
            Answers.getInstance().logCustom(new CustomEvent("Converted File")
                    .putCustomAttribute("success", conversion + ""));
            if (!conversion) {
                return newBookmarks;
            }

        }
        File bookmarksFile = new File(filesDir, "bookmarksGrid.json");
        if (!bookmarksFile.exists()) {
            return new Bookmark[BookmarkModel.ROWS][BookmarkModel.COLUMNS];
        }
        try {
            String json = FileUtils.readFileToString(bookmarksFile);
            Gson gson = new Gson();
            return gson.fromJson(json, Bookmark[][].class);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return new Bookmark[BookmarkModel.ROWS][BookmarkModel.COLUMNS];
    }


    public static List<Bookmark> readItems(Context context) {
        File filesDir = context.getFilesDir();
        File oldFile = new File(filesDir, "bookmarks-new.txt");
        boolean conversion = false;
        if (oldFile.exists()) {
            List<Bookmark> bookmarks = readLineItems(context);
            if (oldFile.delete()) {
                conversion = true;
            }
            Answers.getInstance().logCustom(new CustomEvent("Converted File")
                    .putCustomAttribute("success", conversion + ""));
            return bookmarks;
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
            return gson.fromJson(json, collectionType);
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        return new ArrayList<>();
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
                i++;
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

    private static List<Bookmark> getBookmarks(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_KEYS, MODE_PRIVATE);
        String uriString = preferences.getString(BOOKMARKS_URI, "");
        List<String> uriStrings = splitStringToList(uriString, DELIMITER);
        List<Bookmark> bookmarks = new ArrayList<>();
        for (int i = 0; i < uriStrings.size(); i++) {
            String currentUri = uriStrings.get(i);
            if (TextUtils.isEmpty(currentUri)) {
                continue;
            }
            bookmarks.add(new Bookmark(Uri.parse(currentUri)));
        }
        return bookmarks;
    }

    private static List<String> splitStringToList(String string, String expression) {
        String[] splitUri = string.split(expression);
        return Arrays.asList(splitUri);
    }
}

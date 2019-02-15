package nallapareddy.com.bookmarksedgepanel.tasks;


import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.utils.ContentType;

public class UrlDetailedTask extends AsyncTask<Uri, Void, String> {

    private Bookmark bookmark;
    private onUrlDetailedTaskFinished urlDetailedTaskFinished;

    public UrlDetailedTask(Bookmark bookmark, onUrlDetailedTaskFinished urlDetailedTaskFinished) {
        this.bookmark = bookmark;
        this.urlDetailedTaskFinished = urlDetailedTaskFinished;
    }

    private static final Pattern TITLE_TAG =
            Pattern.compile("<title.*>(.*)</title>", Pattern.CASE_INSENSITIVE|Pattern.DOTALL);

    @Override
    protected String doInBackground(Uri... uris) {
        Uri uri = uris[0];
        try {
            String stringUri = uri.toString();
            if (!bookmark.hasProtocol()) {
                String prefix = bookmark.isTryHttp() ? "http://" : "https://";
                stringUri = prefix + stringUri;
            }
            URL url = new URL(stringUri);
            URLConnection urlConnection = url.openConnection();
            ContentType contentType = ContentType.getContentTypeHeader(urlConnection);
            if (contentType == null || !contentType.getContentType().equals("text/html")) {
                return null;
            } else {
                Charset charset = ContentType.getCharset(contentType);
                if (charset == null) {
                    charset = Charset.defaultCharset();
                }
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
                int n =0, totalRead = 0;
                char[] buf = new char[1024];
                StringBuilder content = new StringBuilder();

                while (totalRead < 8192 && (n = reader.read(buf, 0, buf.length)) != -1) {
                    content.append(buf, 0, n);
                    totalRead += n;
                }
                reader.close();

                Matcher matcher = TITLE_TAG.matcher(content);
                if (matcher.find()) {
                /* replace any occurrences of whitespace (which may
                 * include line feeds and other uglies) as well
                 * as HTML brackets with a space */
                    return matcher.group(1).replaceAll("[\\s\\<>]+", " ").trim();
                }
                else
                    return null;
            }
        } catch (Exception e) {
            Log.e("Url Detailed Task", "URL was invalid cannot get more information");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String title) {
        if (!bookmark.isCanceled()
                && !TextUtils.isEmpty(title)
                && !title.toLowerCase().contains("302 found")) {
            bookmark.setTitle(title.replace("\n", ""));
            bookmark.setFullInfo(true);
            urlDetailedTaskFinished.finishedTask();
        } else {
            bookmark.setFullInfo(false);
            if (!bookmark.hasProtocol() && !bookmark.isTryHttp()) {
                bookmark.setTryHttp(true);
                urlDetailedTaskFinished.retryDetailedTask(bookmark);
            }
        }
    }

    public interface onUrlDetailedTaskFinished {
        void retryDetailedTask(Bookmark bookmark);

        void finishedTask();
    }
}

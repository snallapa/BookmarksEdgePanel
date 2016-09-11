package nallapareddy.com.bookmarksedgepanel.data;

import android.net.Uri;


public class Bookmark {
    private Uri uri;
    private String title;
    private boolean fullInfo;
    private boolean canceled;
    private String faviconUrl;
    private String shortUrl;
    private boolean tryHttp;

    public Bookmark(Uri uri) {
        this.uri = uri;
        setFaviconUrl(uri);
        setShortUrl(uri);
    }

    public Bookmark(Uri uri, String title) {
        this.uri = uri;
        this.title = title;
        this.fullInfo = true;
        setFaviconUrl(uri);
        setShortUrl(uri);
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isFullInfo() {
        return fullInfo;
    }

    public void setFullInfo(boolean fullInfo) {
        this.fullInfo = fullInfo;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public String getFaviconUrl() {
        return faviconUrl;
    }

    public void setFaviconUrl(String faviconUrl) {
        this.faviconUrl = faviconUrl;
    }

    public void setFaviconUrl(Uri uri) {
        String uriString = uri.toString();
        uriString = uriString.replace("http://", "").replace("https://", "");
        //low quality favicon image faviconUrl = "https://www.google.com/s2/favicons?domain_url=http%3A%2F%2F" + uriString;
        faviconUrl = "https://icons.better-idea.org/icon?url=" + uriString.trim() + "&size=40";
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public void setShortUrl(Uri uri) {
        String uriString = uri.toString();
        uriString = uriString.replace("www.", "").replace("http://", "").replace("https://","");
        if (uriString.contains(".")) {
            shortUrl = uriString.substring(0,uriString.lastIndexOf("."));
        }
    }

    public boolean isTryHttp() {
        return tryHttp;
    }

    public void setTryHttp(boolean tryHttp) {
        this.tryHttp = tryHttp;
    }
}

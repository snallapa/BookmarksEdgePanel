package nallapareddy.com.bookmarksedgepanel.model;

import android.net.Uri;

import org.parceler.Parcel;

@Parcel
public class Bookmark {
    Uri uri;
    String title;
    boolean fullInfo;
    boolean canceled;
    String faviconUrl;
    String shortUrl;
    boolean tryHttp;
    boolean useFavicon;
    String textOption;
    int colorPosition;

    public Bookmark() {

    }

    public Bookmark(Uri uri) {
        this.uri = uri;
        setFaviconUrl(uri);
        setShortUrl(uri);
        title = "";
        textOption = "";
        useFavicon = true;
    }

    public Bookmark(Uri uri, String title) {
        this(uri);
        this.title = title;
        this.fullInfo = true;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
        setFaviconUrl(uri);
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

    public boolean useFavicon() {
        return useFavicon;
    }

    public void setUseFavicon(boolean useFavicon) {
        this.useFavicon = useFavicon;
    }

    public String getTextOption() {
        return textOption;
    }

    public void setTextOption(String textOption) {
        this.textOption = textOption;
    }

    public int getColorPosition() {
        return colorPosition;
    }

    public void setColorPosition(int colorPosition) {
        this.colorPosition = colorPosition;
    }

    public int getColorId() {
        return TileColors.values()[colorPosition].getColorId();
    }

    public boolean hasProtocal() {
        return uri.toString().startsWith("http://") || uri.toString().startsWith("https://");
    }

    @Override
    public String toString() {
        return String.format("%s^%s^%s^%s^%s^%s^%s", uri.toString().trim(), title.trim(), fullInfo, shortUrl.trim(), useFavicon, textOption, colorPosition);
    }
}

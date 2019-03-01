package nallapareddy.com.bookmarksedgepanel.model;

import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import org.parceler.Parcel;

import java.io.Serializable;
import java.net.URLEncoder;

@Parcel
public class Bookmark implements Serializable {
    String uri;
    String title;
    boolean fullInfo;
    boolean canceled;
    String faviconUrl;
    String shortUrl;
    boolean tryHttp;
    boolean useFavicon;
    String textOption;
    int colorPosition;
    int edgePosition;

    public Bookmark() {
        colorPosition = 0;
        edgePosition = -1;
    }

    public Bookmark(Uri uri) {
        this.uri = uri.toString();
        setFaviconUrl(uri);
        setShortUrl(uri);
        title = "";
        textOption = "";
        useFavicon = true;
    }

    public Bookmark(Uri uri, int edgePosition) {
        this.uri = uri.toString();
        this.edgePosition = edgePosition;
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
        return Uri.parse(uri);
    }


    public void setUri(Uri uri) {
        this.uri = uri.toString();
        setFaviconUrl(uri);
    }

    public int getEdgePosition() {
        return edgePosition;
    }

    public void setEdgePosition(int edgePosition) {
        this.edgePosition = edgePosition;
    }

    public String getFileSafe() {
        String fileName = this.uri.replace("https://", "")
                .replace("http://", "");
        return URLEncoder.encode(fileName);
    }

    public Uri getBrowserUri() {
        Uri currentUri = getUri();
        if (!currentUri.toString().startsWith("http://") && !currentUri.toString().startsWith("https://")) {
            currentUri = Uri.parse("http://" + currentUri.toString());
        }
        return currentUri;
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
        //faviconUrl = "https://www.google.com/s2/favicons?domain_url=http%3A%2F%2F" + uriString;
        //faviconUrl = "https://icons.better-idea.org/icon?url=" + uriString.trim() + "&size=40";
        faviconUrl = "http://icons.duckduckgo.com/ip2/" + uriString.trim() + ".ico";
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

    private String getTextOption() {
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

    public boolean hasProtocol() {
        return uri.toString().startsWith("http://") || uri.toString().startsWith("https://");
    }

    public Spanned getSafeTitle() {
        return Html.fromHtml(getTitle());
    }

    public String getTileText() {
        if (TextUtils.isEmpty(getTextOption())) {
            if (TextUtils.isEmpty(getShortUrl())) {
                return  "";
            } else {
                return Character.toUpperCase(getShortUrl().charAt(0)) + "";
            }
        } else {
            return getTextOption();
        }
    }

    @Override
    public String toString() {
        title = title == null ? "" : title.replace("\n", "");
        shortUrl = shortUrl == null ? "" : shortUrl.replace("\n", "");
        textOption = textOption == null ? "" : textOption.replace("\n", "");
        return String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s",
                uri.toString().trim(),
                title.trim(),
                fullInfo,
                shortUrl.trim(),
                useFavicon,
                textOption,
                colorPosition);
    }
}

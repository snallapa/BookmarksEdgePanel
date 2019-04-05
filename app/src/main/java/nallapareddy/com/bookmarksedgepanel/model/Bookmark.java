package nallapareddy.com.bookmarksedgepanel.model;

import android.net.Uri;
import android.text.TextUtils;

import org.parceler.Parcel;

import java.io.Serializable;
import java.net.URLEncoder;

@Parcel
public class Bookmark implements Serializable {
    String uri;
    String faviconUrl;
    String shortUrl;
    boolean useFavicon;
    String textOption;
    int colorPosition;

    public Bookmark() {
        colorPosition = 0;
    }

    public Bookmark(Uri uri) {
        this.uri = uri.toString();
        setFaviconUrl(uri);
        setShortUrl(uri);
        textOption = "";
        useFavicon = true;
    }

    public Uri getUri() {
        return Uri.parse(uri);
    }


    public void setUri(Uri uri) {
        this.uri = uri.toString();
        setFaviconUrl(uri);
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


    public String getFaviconUrl() {
        return faviconUrl;
    }

    private void setFaviconUrl(Uri uri) {
        String uriString = uri.toString();
        uriString = uriString.replace("http://", "").replace("https://", "");
        int i = uriString.indexOf("/");
        if (i != -1) {
            uriString = uriString.substring(0, i);
        }

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

    private void setShortUrl(Uri uri) {
        String uriString = uri.toString();
        uriString = uriString.replace("www.", "").replace("http://", "").replace("https://","");
        if (uriString.contains(".")) {
            shortUrl = uriString.substring(0,uriString.lastIndexOf("."));
        }
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
}

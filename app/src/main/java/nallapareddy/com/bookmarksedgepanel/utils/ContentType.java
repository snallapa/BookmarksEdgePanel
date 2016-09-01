package nallapareddy.com.bookmarksedgepanel.utils;

import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ContentType {
    private static final Pattern CHARSET_HEADER = Pattern.compile("charset=([-_a-zA-Z0-9]+)", Pattern.CASE_INSENSITIVE| Pattern.DOTALL);

    private String contentType;
    private String charsetName;

    private ContentType(String headerValue) {
        if (headerValue == null)
            throw new IllegalArgumentException("ContentType must be constructed with a not-null headerValue");
        int n = headerValue.indexOf(";");
        if (n != -1) {
            contentType = headerValue.substring(0, n);
            Matcher matcher = CHARSET_HEADER.matcher(headerValue);
            if (matcher.find())
                charsetName = matcher.group(1);
        }
        else
            contentType = headerValue;
    }

    public static ContentType getContentTypeHeader(URLConnection conn) {
        int i = 0;
        boolean moreHeaders = true;
        do {
            String headerName = conn.getHeaderFieldKey(i);
            String headerValue = conn.getHeaderField(i);
            if (headerName != null && headerName.equals("Content-Type"))
                return new ContentType(headerValue);

            i++;
            moreHeaders = headerName != null || headerValue != null;
        }
        while (moreHeaders);

        return null;
    }

    public static Charset getCharset(ContentType contentType) {
        if (contentType != null && contentType.charsetName != null && Charset.isSupported(contentType.charsetName))
            return Charset.forName(contentType.charsetName);
        else
            return null;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCharsetName() {
        return charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }
}

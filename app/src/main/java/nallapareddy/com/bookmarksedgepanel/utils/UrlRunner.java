package nallapareddy.com.bookmarksedgepanel.utils;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import divstar.ico4a.codec.ico.ICODecoder;

public class UrlRunner implements Runnable {
    private static final Pattern LINK = Pattern.compile("<link.*?>");
    private static final List<String> DEFAULT_PATHS = Collections.unmodifiableList(Arrays.asList("/favicon.ico", "/apple-touch-icon.png", "/apple-touch-icon-precomposed.png"));

    private final String url;
    private final OutputStream fos;
    private final UrlCallback callback;

    public UrlRunner(String url, OutputStream fos, UrlCallback callback) {
        this.url = url;
        this.fos = fos;
        this.callback = callback;
    }

    private String formedURL() {
        URL propUrl = toURL(this.url);
        if (propUrl == null) {
            if (url.startsWith("http")) {
                return url;
            }
            return "https://" + url;
        }
        return propUrl.toString();
    }

    @Override
    public void run() {
        URL formedURL = toURL(this.url);
        if (formedURL == null) {
            return;
        }
        List<LinkTag> iconLinks = getIconLinks(formedURL);
        URL basedUrl = toURL(formedURL.getProtocol() + "://" + formedURL.getHost());
        if (basedUrl != null) {
            iconLinks.addAll(getIconLinks(basedUrl));
        }
        for (String defaultPath : DEFAULT_PATHS) {
            iconLinks.add(LinkTag.fromUrl(defaultPath));
        }
        Collections.sort(iconLinks, new Comparator<LinkTag>() {
            @Override
            public int compare(LinkTag first, LinkTag sec) {
                if (!first.imageType().equals(sec.imageType())) {
                    // if the first is ico, second is better
                    if (first.imageType().equals("ico") && !sec.imageType().equals("ico")) {
                        return -1;
                    }
                    // if second is ico, first is better
                    if (!first.imageType().equals("ico") && sec.imageType().equals("ico")) {
                        return 1;
                    }
                }
                // compare sizes from here
                return Integer.compare(first.getMaxSize(), sec.getMaxSize());
            }
        });
        // descending order
        Collections.reverse(iconLinks);

        int i = 0;
        String formedUrl = formedURL();
        while (i < iconLinks.size()) {
            LinkTag bestIconTag = iconLinks.get(i);
            String imageUrl;
            //try getting the base url and using that (no paths and stuff)
            try {
                URL propURL = new URL(formedUrl);
                String base = propURL.getProtocol() + "://" + propURL.getHost();
                imageUrl = absoluteURL(base, bestIconTag.getHref());
                if (copyImage(imageUrl, bestIconTag.imageType(), fos)) {
                    callback.onSuccess();
                    return;
                }
            } catch (MalformedURLException ignored) {
            }
            imageUrl = absoluteURL(formedUrl, bestIconTag.getHref());
            // if it does not work, try appending href to end
            if (copyImage(imageUrl, bestIconTag.imageType(), fos)) {
                callback.onSuccess();
                return;
            }

            i++;
        }
        callback.onFailure();
    }

    private static boolean copyImage(String imageUrl, String imageType, OutputStream fos) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(imageUrl).openConnection();
            urlConnection.setConnectTimeout(10000); // 10 seconds
            urlConnection.setReadTimeout(10000); // 10 seconds (buffered read prob slower?)
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; SM-A205U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Mobile Safari/537.36");
            int responseCode = urlConnection.getResponseCode();
            if (responseCode != 200 && responseCode != 300 && responseCode != 301) {
                return false;
            }
            InputStream imageStream = urlConnection.getInputStream();
            if (imageType.equals("ico")) {
                // convert icos to pngs, picasso cant handle them
                List<Bitmap> images = ICODecoder.read(imageStream);
                Bitmap bitmap = Collections.max(images, new Comparator<Bitmap>() {
                    @Override
                    public int compare(Bitmap b1, Bitmap b2) {
                        return Integer.compare(b1.getWidth(), b2.getWidth());
                    }
                });
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
            } else {
                copy(imageStream, fos);
                fos.flush();
                fos.close();
            }
            return true;
        } catch (IOException e) {
            Log.e("URLRunner", "image failed? trying another one " + imageUrl, e);
            return false;
        }
    }

    private static List<LinkTag> getIconLinks(URL url) {
        try {
            InputStream is = connectURL(url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            List<LinkTag> iconLinks = new ArrayList<>();
            while (line != null) {
                Matcher matcher = LINK.matcher(line);
                while (matcher.find()) {
                    LinkTag tag = LinkTag.parseFrom(matcher.group());
                    if (tag.isIcon()) {
                        iconLinks.add(tag);
                    }
                }
                line = reader.readLine();
            }
            return iconLinks;
        } catch (IOException | URISyntaxException e) {
            Log.e("URLRunner", "failed to get icon links " + url.toString(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Sets the user agent so we can try avoiding websites from blocking us
     *
     * @param url
     * @return
     * @throws IOException
     */
    private static InputStream connectURL(URL url) throws IOException, URISyntaxException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(10000); // 10 seconds
        urlConnection.setReadTimeout(10000); // 10 seconds (buffered read prob slower?)
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; SM-A205U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.82 Mobile Safari/537.36");
        return urlConnection.getInputStream();
    }

    private static void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) > 0) {
            target.write(buf, 0, length);
        }
    }

    private static String absoluteURL(String baseUrl, String href) {
        if (baseUrl.contains("?")) {
            baseUrl = baseUrl.substring(0, baseUrl.indexOf("?"));
        }
        if (href.startsWith("http")) {
            return href;
        }
        if (href.startsWith("//")) {
            return "https:" + href;
        }
        if (href.startsWith("/")) {
            if (baseUrl.endsWith("/")) {
                return baseUrl + href.substring(1); // remove beginning /
            }
            return baseUrl + href;
        }
        return href;
    }

    private static URL toURL(String inputURL) {
        try {
            if (!inputURL.startsWith("http")) {
                return new URL("https://" + inputURL);
            }
            return new URL(inputURL);
        } catch (MalformedURLException ignored) {
            return null;
        }
    }
}

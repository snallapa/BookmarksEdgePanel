package nallapareddy.com.bookmarksedgepanel.utils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LinkTag {
    private static final Set<String> VALID_REL_TYPES = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("icon", "apple-touch-icon", "apple-touch-icon-precomposed")
    ));

    private final List<String> rel;
    private final String href;
    private final String sizes;

    private LinkTag(List<String> rel, String href, String sizes) {
        this.rel = Collections.unmodifiableList(rel);
        this.href = href;
        this.sizes = sizes;
    }

    public List<String> getRel() {
        return rel;
    }

    public String getHref() {
        return href;
    }

    public String getRawSize() {
        return sizes;
    }

    public int getMaxSize() {
        if (sizes == null) {
            return -1;
        }
        String[] allSizes = sizes.split("\\s+");
        List<Integer> dimension = new ArrayList<>();
        for (String size : allSizes) {
            size = size.toLowerCase(Locale.ROOT);
            // if its 'any' skip it
            if (size.contains("x")) {
                String[] dimensions = size.split("x");
                dimension.add(Integer.parseInt(dimensions[0].trim()));
            }
        }
        if (dimension.isEmpty()) {
            return -1;
        }
        return Collections.max(dimension, Integer::compareTo);
    }

    public String imageType() {
        int extensionIndex = href.lastIndexOf(".");
        if (extensionIndex < 0) {
            return "bs";
        }
        return href.substring(extensionIndex + 1);
    }

    public boolean isIcon() {
        // no rel in this link tag?
        if (rel.isEmpty()) {
            return false;
        }
        for (String rel : rel) {
            if (VALID_REL_TYPES.contains(rel)) {
                return true;
            }
        }
        return false;
    }

    public static LinkTag fromUrl(String href) {
        return new LinkTag(Collections.singletonList("icon"), href, null);
    }

    public static LinkTag parseFrom(String linkTag) {
        String href = find(linkTag, "href");
        String sizes = find(linkTag, "sizes");
        String rel = find(linkTag, "rel");
        if (rel == null) {
            return new LinkTag(new ArrayList<>(), href, sizes);
        }
        String[] allRels = rel.split("\\s+");// split on white space
        return new LinkTag(Arrays.asList(allRels), href, sizes);
    }

    private static String find(String linkTag, String type) {
        int index = linkTag.indexOf(type);
        int endIndex = linkTag.indexOf("/>");
        if (endIndex < 0) {
            endIndex = linkTag.indexOf(">");
        }
        if (index < 0) {
            return null;
        }
        int equalIndex = index + type.length();
        char isQuote = linkTag.charAt(equalIndex + 1);
        String wholeAttribute;
        // it could be quoted
        if (isQuote == '"' || isQuote == '\'') {
            int numQuotes = 0;
            int i = index;
            while (i < endIndex && numQuotes < 2) {
                char currentChar = linkTag.charAt(i);
                if (currentChar == '"' || currentChar == '\'') {
                    numQuotes++;
                }
                i++;
            }
            wholeAttribute = linkTag.substring(index, i);
        }
        // otherwise its white space delimited
        else {
            int i = equalIndex;
            while (i < endIndex && !Character.isWhitespace(linkTag.charAt(i))) {
                i++;
            }
            wholeAttribute = linkTag.substring(index, i);
        }
        String[] split = wholeAttribute.split("=");
        if (split.length != 2) {
            return null;
        }
        return split[1]
                .replaceAll("[\"']", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("rel={%s} href={%s} sizes={%s}", rel, href, sizes);
    }
}

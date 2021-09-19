package nallapareddy.com.bookmarksedgepanel.utils;

import org.junit.Assert;
import org.junit.Test;

public class LinkTagTest {

    @Test
    public void parsesRel() {
        String foo = "<link rel=\"style\" bar=\"who cares\"/>";
        LinkTag tag = LinkTag.parseFrom(foo);
        Assert.assertEquals(1, tag.getRel().size());
        Assert.assertEquals("style", tag.getRel().get(0));
        Assert.assertNull(tag.getHref());
        Assert.assertNull(tag.getRawSize());
        Assert.assertFalse(tag.isIcon());
    }

    @Test
    public void parsesAllAttributes() {
        String foo = "<link rel=\"icon\" href=\"https://foo.com\" sizes=\"100x100\">";
        LinkTag tag = LinkTag.parseFrom(foo);
        Assert.assertEquals(1, tag.getRel().size());
        Assert.assertEquals("icon", tag.getRel().get(0));
        Assert.assertEquals("https://foo.com", tag.getHref());
        Assert.assertEquals("100x100", tag.getRawSize());
        Assert.assertTrue(tag.isIcon());
    }

    @Test
    public void parsesMultipleRel() {
        String foo = "<link rel=\"icon shortcut foo bar\" href=\"https://foo.com\" sizes=\"100x100\">";
        LinkTag tag = LinkTag.parseFrom(foo);
        Assert.assertEquals(4, tag.getRel().size());
        Assert.assertArrayEquals(new String[] {"icon", "shortcut", "foo", "bar"}, tag.getRel().toArray(new String[0]));
        Assert.assertTrue(tag.isIcon());
    }

    @Test
    public void parsesMultipleRelNotIcon() {
        String foo = "<link rel=\"shortcut foo bar lol\" href=\"https://foo.com\" sizes=\"100x100\">";
        LinkTag tag = LinkTag.parseFrom(foo);
        Assert.assertEquals(4, tag.getRel().size());
        Assert.assertArrayEquals(new String[] {"shortcut", "foo", "bar", "lol"}, tag.getRel().toArray(new String[0]));
        Assert.assertFalse(tag.isIcon());
    }

    @Test
    public void testImageType() {
        String png = "<link href=\"https://foo.com/test.png\">";
        Assert.assertEquals("png", LinkTag.parseFrom(png).imageType());

        String ico = "<link href=\"https://foo.com/asdjflsadf/asf.sadf/test.ico\">";
        Assert.assertEquals("ico", LinkTag.parseFrom(ico).imageType());
    }

    @Test
    public void testMaxSizes() {
        String test1 = "<link sizes=\"100x100 1000x1000\">";
        Assert.assertEquals(1000, LinkTag.parseFrom(test1).getMaxSize());

        String test2 = "<link sizes=\"any\">";
        Assert.assertEquals(-1, LinkTag.parseFrom(test2).getMaxSize());

        String test3 = "<link sizes=\"100x100\">";
        Assert.assertEquals(100, LinkTag.parseFrom(test3).getMaxSize());
    }

    @Test
    public void testNonQuoted() {
        String test1 = "<link rel=icon href=foo />";
        Assert.assertEquals("icon", LinkTag.parseFrom(test1).getRel().get(0));
        Assert.assertEquals("foo", LinkTag.parseFrom(test1).getHref());

        String test2 = "<link rel=icon href=foo/>";
        Assert.assertEquals("icon", LinkTag.parseFrom(test2).getRel().get(0));
        Assert.assertEquals("foo", LinkTag.parseFrom(test2).getHref());

        String test3 = "<link rel=icon href=foo>";
        Assert.assertEquals("icon", LinkTag.parseFrom(test3).getRel().get(0));
        Assert.assertEquals("foo", LinkTag.parseFrom(test3).getHref());
    }
}

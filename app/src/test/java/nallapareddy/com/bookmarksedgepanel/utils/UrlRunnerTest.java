package nallapareddy.com.bookmarksedgepanel.utils;

import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;

public class UrlRunnerTest {

    @Test
    public void testSmall() throws IOException {
//        new UrlRunner(new URL("https://tripadvisor.com"), new FileOutputStream("testing/tripadvisor.png")).run();
//        new UrlRunner(new URL("https://target.com"), new FileOutputStream("testing/target.png")).run();
//        new UrlRunner(new URL("https://steampowered.com"), new FileOutputStream("testing/steampowered.png")).run();
        new UrlRunner("https://messenger.com", new FileOutputStream("messenger.png"), new UrlCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure() {

            }
        }).run();

    }
//
//    @Test
//    public void testBig() throws MalformedURLException, FileNotFoundException {
//        new UrlRunner(new URL("https://en.wikipedia.org"), new FileOutputStream("testing/wikipedia.png")).run();
//        new UrlRunner(new URL("https://youtube.com"), new FileOutputStream("testing/youtube.png")).run();
//        new UrlRunner(new URL("https://amazon.com"), new FileOutputStream("testing/amazon.png")).run();
//        new UrlRunner(new URL("https://facebook.com"), new FileOutputStream("testing/facebook.png")).run();
//        new UrlRunner(new URL("https://twitter.com"), new FileOutputStream("testing/twitter.png")).run();
//        new UrlRunner(new URL("https://fandom.com"), new FileOutputStream("testing/fandom.png")).run();
//        new UrlRunner(new URL("https://pinterest.com"), new FileOutputStream("testing/pinterest.png")).run();
//        new UrlRunner(new URL("https://imdb.com"), new FileOutputStream("testing/imdb.png")).run();
//        new UrlRunner(new URL("https://reddit.com"), new FileOutputStream("testing/reddit.png")).run();
//        new UrlRunner(new URL("https://yelp.com"), new FileOutputStream("testing/yelp.ico")).run();
//        new UrlRunner(new URL("https://instagram.com"), new FileOutputStream("testing/instagram.png")).run();
//        new UrlRunner(new URL("https://ebay.com"), new FileOutputStream("testing/ebay.png")).run();
//        new UrlRunner(new URL("https://walmart.com"), new FileOutputStream("testing/walmart.png")).run();
//        new UrlRunner(new URL("https://craigslist.org"), new FileOutputStream("testing/craigslist.png")).run();
//        new UrlRunner(new URL("https://healthline.com"), new FileOutputStream("testing/healthline.png")).run();
//        new UrlRunner(new URL("https://tripadvisor.com"), new FileOutputStream("testing/tripadvisor.ico")).run();
//        new UrlRunner(new URL("https://linkedin.com"), new FileOutputStream("testing/linkedin.png")).run();
//        new UrlRunner(new URL("https://webmd.com"), new FileOutputStream("testing/webmd.png")).run();
//        new UrlRunner(new URL("https://netflix.com"), new FileOutputStream("testing/netflix.png")).run();
//        new UrlRunner(new URL("https://apple.com"), new FileOutputStream("testing/apple.png")).run();
//        new UrlRunner(new URL("https://homedepot.com"), new FileOutputStream("testing/homedepot.png")).run();
//        new UrlRunner(new URL("https://mail.yahoo.com"), new FileOutputStream("testing/mail.png")).run();
//        new UrlRunner(new URL("https://cnn.com"), new FileOutputStream("testing/cnn.png")).run();
//        new UrlRunner(new URL("https://etsy.com"), new FileOutputStream("testing/etsy.png")).run();
//        new UrlRunner(new URL("https://google.com"), new FileOutputStream("testing/google.png")).run();
//        new UrlRunner(new URL("https://yahoo.com"), new FileOutputStream("testing/yahoo.png")).run();
//        new UrlRunner(new URL("https://indeed.com"), new FileOutputStream("testing/indeed.png")).run();
//        new UrlRunner(new URL("https://target.com"), new FileOutputStream("testing/target.png")).run();
//        new UrlRunner(new URL("https://microsoft.com"), new FileOutputStream("testing/microsoft.png")).run();
//        new UrlRunner(new URL("https://nytimes.com"), new FileOutputStream("testing/nytimes.png")).run();
//        new UrlRunner(new URL("https://mayoclinic.org"), new FileOutputStream("testing/mayoclinic.png")).run();
//        new UrlRunner(new URL("https://espn.com"), new FileOutputStream("testing/espn.png")).run();
//        new UrlRunner(new URL("https://usps.com"), new FileOutputStream("testing/usps.png")).run();
//        new UrlRunner(new URL("https://quizlet.com"), new FileOutputStream("testing/quizlet.png")).run();
//        new UrlRunner(new URL("https://gamepedia.com"), new FileOutputStream("testing/gamepedia.png")).run();
////        new UrlRunner(new URL("https://lowes.com"), new FileOutputStream("testing/lowes.png")).run();
//        new UrlRunner(new URL("https://irs.gov"), new FileOutputStream("testing/irs.png")).run();
//        new UrlRunner(new URL("https://nih.gov"), new FileOutputStream("testing/nih.png")).run();
//        new UrlRunner(new URL("https://merriam-webster.com"), new FileOutputStream("testing/merriam.png")).run();
//        new UrlRunner(new URL("https://steampowered.com"), new FileOutputStream("testing/steampowered.png")).run();
//        new UrlRunner(new URL("https://mapquest.com"), new FileOutputStream("testing/mapquest.png")).run();
//        new UrlRunner(new URL("https://foxnews.com"), new FileOutputStream("testing/foxnews.png")).run();
//        new UrlRunner(new URL("https://allrecipes.com"), new FileOutputStream("testing/allrecipes.png")).run();
//        new UrlRunner(new URL("https://quora.com"), new FileOutputStream("testing/quora.png")).run();
//        new UrlRunner(new URL("https://aol.com"), new FileOutputStream("testing/aol.png")).run();
//        new UrlRunner(new URL("https://britannica.com"), new FileOutputStream("testing/britannica.png")).run();
//        new UrlRunner(new URL("https://live.com"), new FileOutputStream("testing/live.png")).run();
//        new UrlRunner(new URL("https://bestbuy.com"), new FileOutputStream("testing/bestbuy.png")).run();
//        new UrlRunner(new URL("https://rottentomatoes.com"), new FileOutputStream("testing/rottentomatoes.png")).run();
//        new UrlRunner(new URL("https://ca.gov"), new FileOutputStream("testing/ca.png")).run();
//        new UrlRunner(new URL("https://play.google.com"), new FileOutputStream("testing/play.png")).run();
//        new UrlRunner(new URL("https://cnet.com"), new FileOutputStream("testing/cnet.png")).run();
//        new UrlRunner(new URL("https://roblox.com"), new FileOutputStream("testing/roblox.png")).run();
//        new UrlRunner(new URL("https://usnews.com"), new FileOutputStream("testing/usnews.png")).run();
//        new UrlRunner(new URL("https://zillow.com"), new FileOutputStream("testing/zillow.png")).run();
//        new UrlRunner(new URL("https://businessinsider.com"), new FileOutputStream("testing/businessinsider.png")).run();
//        new UrlRunner(new URL("https://bulbagarden.net"), new FileOutputStream("testing/bulbagarden.png")).run();
//        new UrlRunner(new URL("https://paypal.com"), new FileOutputStream("testing/paypal.png")).run();
//        new UrlRunner(new URL("https://finance.yahoo.com"), new FileOutputStream("testing/finance.png")).run();
//        new UrlRunner(new URL("https://genius.com"), new FileOutputStream("testing/genius.png")).run();
//        new UrlRunner(new URL("https://usatoday.com"), new FileOutputStream("testing/usatoday.png")).run();
//        new UrlRunner(new URL("https://realtor.com"), new FileOutputStream("testing/realtor.png")).run();
//        new UrlRunner(new URL("https://medicalnewstoday.com"), new FileOutputStream("testing/medicalnewstoday.png")).run();
//        new UrlRunner(new URL("https://fedex.com"), new FileOutputStream("testing/fedex.png")).run();
//        new UrlRunner(new URL("https://bankofamerica.com"), new FileOutputStream("testing/bankofamerica.png")).run();
//        new UrlRunner(new URL("https://washingtonpost.com"), new FileOutputStream("testing/washingtonpost.png")).run();
//        new UrlRunner(new URL("https://investopedia.com"), new FileOutputStream("testing/investopedia.png")).run();
//        new UrlRunner(new URL("https://speedtest.net"), new FileOutputStream("testing/speedtest.png")).run();
//        new UrlRunner(new URL("https://spotify.com"), new FileOutputStream("testing/spotify.png")).run();
//        new UrlRunner(new URL("https://cdc.gov"), new FileOutputStream("testing/cdc.png")).run();
//        new UrlRunner(new URL("https://chase.com"), new FileOutputStream("testing/chase.png")).run();
//        new UrlRunner(new URL("https://hulu.com"), new FileOutputStream("testing/hulu.png")).run();
//        new UrlRunner(new URL("https://xfinity.com"), new FileOutputStream("testing/xfinity.png")).run();
//        new UrlRunner(new URL("https://msn.com"), new FileOutputStream("testing/msn.png")).run();
//        new UrlRunner(new URL("https://dictionary.com"), new FileOutputStream("testing/dictionary.png")).run();
//        new UrlRunner(new URL("https://weather.com"), new FileOutputStream("testing/weather.png")).run();
//        new UrlRunner(new URL("https://ups.com"), new FileOutputStream("testing/ups.png")).run();
//        new UrlRunner(new URL("https://verizon.com"), new FileOutputStream("testing/verizon.png")).run();
//        new UrlRunner(new URL("https://forbes.com"), new FileOutputStream("testing/forbes.png")).run();
//        new UrlRunner(new URL("https://wowhead.com"), new FileOutputStream("testing/wowhead.png")).run();
//        new UrlRunner(new URL("https://expedia.com"), new FileOutputStream("testing/expedia.png")).run();
//        new UrlRunner(new URL("https://urbandictionary.com"), new FileOutputStream("testing/urbandictionary.png")).run();
//        new UrlRunner(new URL("https://foodnetwork.com"), new FileOutputStream("testing/foodnetwork.png")).run();
//        new UrlRunner(new URL("https://nbcnews.com"), new FileOutputStream("testing/nbcnews.png")).run();
//        new UrlRunner(new URL("https://macys.com"), new FileOutputStream("testing/macys.png")).run();
//        new UrlRunner(new URL("https://apartments.com"), new FileOutputStream("testing/apartments.png")).run();
//        new UrlRunner(new URL("https://ign.com"), new FileOutputStream("testing/ign.png")).run();
//        new UrlRunner(new URL("https://capitalone.com"), new FileOutputStream("testing/capitalone.png")).run();
//        new UrlRunner(new URL("https://costco.com"), new FileOutputStream("testing/costco.png")).run();
//        new UrlRunner(new URL("https://theguardian.com"), new FileOutputStream("testing/theguardian.png")).run();
//        new UrlRunner(new URL("https://cnbc.com"), new FileOutputStream("testing/cnbc.png")).run();
//        new UrlRunner(new URL("https://glassdoor.com"), new FileOutputStream("testing/glassdoor.png")).run();
//        new UrlRunner(new URL("https://yellowpages.com"), new FileOutputStream("testing/yellowpages.png")).run();
//        new UrlRunner(new URL("https://att.com"), new FileOutputStream("testing/att.png")).run();
//        new UrlRunner(new URL("https://bbc.com"), new FileOutputStream("testing/bbc.png")).run();
//        new UrlRunner(new URL("https://khanacademy.org"), new FileOutputStream("testing/khanacademy.png")).run();
//        new UrlRunner(new URL("https://ny.gov"), new FileOutputStream("testing/ny.png")).run();
//        new UrlRunner(new URL("https://twitch.tv"), new FileOutputStream("testing/twitch.png")).run();
//        new UrlRunner(new URL("https://adobe.com"), new FileOutputStream("testing/adobe.png")).run();
//        new UrlRunner(new URL("https://cbssports.com"), new FileOutputStream("testing/cbssports.png")).run();    }
}

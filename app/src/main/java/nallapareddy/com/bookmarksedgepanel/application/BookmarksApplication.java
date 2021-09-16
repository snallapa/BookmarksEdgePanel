package nallapareddy.com.bookmarksedgepanel.application;


import android.app.Application;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import nallapareddy.com.bookmarksedgepanel.R;

public class BookmarksApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Roboto-Medium.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
    }
}



package nallapareddy.com.bookmarksedgepanel.application;


import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import nallapareddy.com.bookmarksedgepanel.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class BookmarksApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Medium.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

    }
}



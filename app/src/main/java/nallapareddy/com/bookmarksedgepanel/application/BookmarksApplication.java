package nallapareddy.com.bookmarksedgepanel.application;


import android.app.Application;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;

public class BookmarksApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Picasso.Builder builder = new Picasso.Builder(getApplicationContext());
        builder.downloader(new OkHttp3Downloader(new OkHttpClient()));
        Picasso picasso = builder.build();
        Picasso.setSingletonInstance(picasso);
    }
}

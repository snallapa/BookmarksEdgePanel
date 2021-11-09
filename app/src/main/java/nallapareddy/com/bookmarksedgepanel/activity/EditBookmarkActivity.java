package nallapareddy.com.bookmarksedgepanel.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.model.Position;
import nallapareddy.com.bookmarksedgepanel.model.TileColors;
import nallapareddy.com.bookmarksedgepanel.utils.UrlCallback;
import nallapareddy.com.bookmarksedgepanel.utils.UrlRunner;
import nallapareddy.com.bookmarksedgepanel.utils.ViewUtils;


public class EditBookmarkActivity extends AppCompatActivity {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @BindView(R.id.edge_bookmark_display)
    ImageView edgeBookmarkDisplay;
    @BindView(R.id.edge_bookmark_display_options)
    Spinner edgeBookmarkDisplayOptions;
    @BindView(R.id.edge_bookmark_display_options_layout)
    View edgeBookmarkDisplayOptionsView;
    @BindView(R.id.edge_bookmark_display_name)
    EditText edgeBookmarkShortUrl;
    @BindView(R.id.edge_bookmark_url)
    EditText edgeBookmarkUrl;
    @BindView(R.id.edge_bookmark_display_background_text)
    EditText edgeBookmarkBackgroundText;
    @BindView(R.id.edge_bookmark_display_background_color)
    Spinner edgeBookmarkBackgroundColor;
    @BindView(R.id.refresh_icon)
    Button refreshIconButton;

    private Bookmark currentBookmark;
    private Position currentPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bookmark);
        ButterKnife.bind(this);
        initializeAds();
        currentBookmark = Parcels.unwrap(getIntent().getParcelableExtra(ConfigureActivity.EXTRA_BOOKMARK));
        String stringPos = getIntent().getStringExtra(ConfigureActivity.EXTRA_POSITION);
        try {
            currentPosition = Position.fromString(stringPos);
        } catch (Exception e) {
            // we are missing a String extra??
            FirebaseCrashlytics.getInstance().recordException(e);
            Toast.makeText(this, "Sorry, this bookmark could not be edited", Toast.LENGTH_LONG).show();
            finish();
        }
        updateViews();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_bookmark:
                saveBookmark();
                break;
            case R.id.action_cancel_bookmark:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void saveBookmark() {
        String urlText = edgeBookmarkUrl.getText().toString();
        if (!urlText.replace("www.", "").contains(".")) {
            Toast.makeText(this, R.string.error_url, Toast.LENGTH_SHORT).show();
            return;
        }
        currentBookmark.setUri(Uri.parse(urlText));
        currentBookmark.setShortUrl(edgeBookmarkShortUrl.getText().toString().replace("\n", ""));
        currentBookmark.setColorPosition(edgeBookmarkBackgroundColor.getSelectedItemPosition());
        currentBookmark.setTextOption(edgeBookmarkBackgroundText.getText().toString().replace("\n", ""));
        Intent data = new Intent();
        data.putExtra(ConfigureActivity.EXTRA_BOOKMARK, Parcels.wrap(currentBookmark));
        data.putExtra(ConfigureActivity.EXTRA_POSITION, currentPosition.toString());
        setResult(RESULT_OK, data);
        finish();
    }

    private void updateViews() {
        setupDisplaySpinner();
        setupTileDisplay();
        edgeBookmarkShortUrl.setText(currentBookmark.getShortUrl());
        edgeBookmarkUrl.setText(currentBookmark.getUri().toString().trim());
        edgeBookmarkDisplayOptions.setSelection(currentBookmark.useFavicon() ? 0 : 1);
        showOptions();
    }

    private void setupDisplaySpinner() {
        edgeBookmarkDisplayOptions.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, EdgeImageOptions.values()));
    }

    private void setupTileDisplay() {
        edgeBookmarkBackgroundColor.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TileColors.values()));
        String textOption = currentBookmark.getTileText();
        edgeBookmarkBackgroundText.append(textOption);
        edgeBookmarkBackgroundColor.setSelection(currentBookmark.getColorPosition());
    }

    private void showOptions() {
        if (edgeBookmarkDisplayOptions.getSelectedItem() == EdgeImageOptions.FAVICON) {
            edgeBookmarkDisplayOptionsView.setVisibility(View.GONE);
            currentBookmark.setUseFavicon(true);
            refreshIconButton.setVisibility(View.VISIBLE);
        } else {
            currentBookmark.setUseFavicon(false);
            edgeBookmarkDisplayOptionsView.setVisibility(View.VISIBLE);
            refreshIconButton.setVisibility(View.GONE);
        }
        changeImageView();
    }

    private void changeImageView() {
        if (!currentBookmark.useFavicon()) {
            TileColors selectedItem = (TileColors) edgeBookmarkBackgroundColor.getSelectedItem();
            edgeBookmarkDisplay.setImageDrawable(ViewUtils.getTileDrawable(getApplicationContext(), edgeBookmarkBackgroundText.getText().toString(), selectedItem.getColorId()));
        } else {
            File fileStreamPath = getFileStreamPath(currentBookmark.getFileSafe());
            if (fileStreamPath.exists()) {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = openFileInput(currentBookmark.getFileSafe());
                } catch (FileNotFoundException e) {
                    edgeBookmarkDisplay.setImageResource(R.drawable.ic_prepare);
                }
                Bitmap b = BitmapFactory.decodeStream(fileInputStream);
                edgeBookmarkDisplay.setImageBitmap(b);
            } else {
                edgeBookmarkDisplay.setImageResource(R.drawable.ic_prepare);
            }
        }
    }

    @OnClick(R.id.reset_bookmark)
    public void resetBookmark() {
        currentBookmark.setUseFavicon(true);
        updateViews();
    }

    @OnClick(R.id.refresh_icon)
    public void refreshFavicon() {
        final Context context = this;
        Toast.makeText(context, R.string.bookmark_favicon_refreshing, Toast.LENGTH_SHORT).show();
        URL url = null;
        try {
            deleteFile(currentBookmark.getFileSafe()); // delete the old file
            FileOutputStream fos = openFileOutput(currentBookmark.getFileSafe(), Context.MODE_PRIVATE);
            executor.execute(new UrlRunner(currentBookmark.getUri().toString(), fos, new UrlCallback() {
                @Override
                public void onSuccess() {
                    handler.post(() -> changeImageView());
                }

                @Override
                public void onFailure() {
                    handler.post(() -> {
                        Toast.makeText(context, R.string.bookmark_favicon_refresh_error, Toast.LENGTH_LONG).show();
                        edgeBookmarkDisplay.setImageResource(R.drawable.ic_error_outline_black);
                    });
                }
            }));
        } catch (FileNotFoundException ignored) {

        }
    }

    @OnItemSelected(R.id.edge_bookmark_display_options)
    public void onDisplayOptionChanged() {
        showOptions();
    }

    @OnItemSelected(R.id.edge_bookmark_display_background_color)
    public void onColorChanged() {
        changeImageView();
    }

    @OnTextChanged(R.id.edge_bookmark_display_background_text)
    public void onTextChanged() {
        changeImageView();
    }

    private enum EdgeImageOptions {

        FAVICON("Favicon"), TILE("Default Tile");

        private final String text;

        EdgeImageOptions(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private void initializeAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });
        AdView adView = this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}


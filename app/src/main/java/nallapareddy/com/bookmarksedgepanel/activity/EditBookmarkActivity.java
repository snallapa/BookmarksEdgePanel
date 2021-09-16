package nallapareddy.com.bookmarksedgepanel.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.model.Position;
import nallapareddy.com.bookmarksedgepanel.model.TileColors;
import nallapareddy.com.bookmarksedgepanel.utils.ViewUtils;


public class EditBookmarkActivity extends AppCompatActivity {

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
        currentPosition = Position.fromString(stringPos);
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
        } else {
            currentBookmark.setUseFavicon(false);
            edgeBookmarkDisplayOptionsView.setVisibility(View.VISIBLE);
        }
        changeImageView();
    }

    private void changeImageView() {
        if (!currentBookmark.useFavicon()) {
            TileColors selectedItem = (TileColors) edgeBookmarkBackgroundColor.getSelectedItem();
            edgeBookmarkDisplay.setImageDrawable(ViewUtils.getTileDrawable(getApplicationContext(), edgeBookmarkBackgroundText.getText().toString(), selectedItem.getColorId()));
        } else {
            Picasso.get().load(currentBookmark.getFaviconUrl()).error(R.drawable.ic_error_outline_black).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    edgeBookmarkDisplay.setImageBitmap(bitmap);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    edgeBookmarkDisplay.setImageDrawable(errorDrawable);
                    Toast.makeText(EditBookmarkActivity.this, R.string.favicon_error, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }
    }

    @OnClick(R.id.reset_bookmark)
    public void resetBookmark() {
        currentBookmark.setUseFavicon(true);
        updateViews();
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


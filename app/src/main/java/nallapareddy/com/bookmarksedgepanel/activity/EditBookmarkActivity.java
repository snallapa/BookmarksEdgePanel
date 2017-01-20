package nallapareddy.com.bookmarksedgepanel.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;
import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.model.TileColors;
import nallapareddy.com.bookmarksedgepanel.tasks.UrlDetailedTask;
import nallapareddy.com.bookmarksedgepanel.utils.ViewUtils;


public class EditBookmarkActivity extends AppCompatActivity implements UrlDetailedTask.onUrlDetailedTaskFinished {

    private final String AD_CODE = "ca-app-pub-3135803015555141~4955511510";

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
    @BindView(R.id.edge_bookmark_title)
    EditText edgeBookmarkTitle;

    private Bookmark currentBookmark;
    private int currentPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bookmark);
        ButterKnife.bind(this);
        initializeAds();
        currentBookmark = Parcels.unwrap(getIntent().getParcelableExtra(ConfigureActivity.EXTRA_BOOKMARK));
        currentPosition = getIntent().getIntExtra(ConfigureActivity.EXTRA_POSITION, -1);
        if (currentPosition == -1) {
            throw new RuntimeException("No position was sent");
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
        String title = edgeBookmarkTitle.getText().toString();
        currentBookmark.setTitle(title.replace("\n", ""));
        currentBookmark.setFullInfo(!TextUtils.isEmpty(title));
        Intent data = new Intent();
        data.putExtra(ConfigureActivity.EXTRA_BOOKMARK, Parcels.wrap(currentBookmark));
        data.putExtra(ConfigureActivity.EXTRA_POSITION, currentPosition);
        setResult(RESULT_OK, data);
        finish();
    }

    private void updateViews() {
        setupDisplaySpinner();
        setupTileDisplay();
        edgeBookmarkShortUrl.setText(currentBookmark.getShortUrl());
        edgeBookmarkUrl.setText(currentBookmark.getUri().toString().trim());
        edgeBookmarkTitle.setText(currentBookmark.getSafeTitle());
        edgeBookmarkDisplayOptions.setSelection(currentBookmark.useFavicon() ? 0 : 1);
        showOptions();
    }

    private void setupDisplaySpinner() {
        edgeBookmarkDisplayOptions.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, EdgeImageOptions.values()));
    }

    private void setupTileDisplay() {
        edgeBookmarkBackgroundColor.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, TileColors.values()));
        String textOption = getTileText();
        edgeBookmarkBackgroundText.append(textOption);
        edgeBookmarkBackgroundColor.setSelection(currentBookmark.getColorPosition());
    }

    private String getTileText() {
        if (TextUtils.isEmpty(currentBookmark.getTextOption())) {
            if (TextUtils.isEmpty(currentBookmark.getShortUrl())) {
                return  "";
            } else {
                return Character.toUpperCase(currentBookmark.getShortUrl().charAt(0)) + "";
            }
        } else {
            return currentBookmark.getTextOption();
        }
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
            Picasso.with(this).load(currentBookmark.getFaviconUrl()).into(edgeBookmarkDisplay);
        }
    }

    @OnClick(R.id.reset_bookmark)
    public void resetBookmark() {
        new UrlDetailedTask(currentBookmark, this).execute(currentBookmark.getUri());
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

    @Override
    public void retryDetailedTask(Bookmark bookmark) {
        new UrlDetailedTask(bookmark, this).execute(bookmark.getUri());
    }

    @Override
    public void finishedTask() {
        currentBookmark.setUseFavicon(true);
        updateViews();
    }

    private enum EdgeImageOptions {

        FAVICON("Favicon"), TILE("Default Tile");

        private String text;

        EdgeImageOptions(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private void initializeAds() {
        MobileAds.initialize(getApplicationContext(), AD_CODE);
        AdView adView = ButterKnife.findById(this, R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}


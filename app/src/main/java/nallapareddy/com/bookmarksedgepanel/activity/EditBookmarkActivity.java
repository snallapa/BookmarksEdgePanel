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
        currentBookmark.setShortUrl(edgeBookmarkShortUrl.getText().toString());
        currentBookmark.setColorPosition(edgeBookmarkBackgroundColor.getSelectedItemPosition());
        currentBookmark.setTextOption(edgeBookmarkBackgroundText.getText().toString());
        String title = edgeBookmarkTitle.getText().toString();
        currentBookmark.setTitle(title);
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
        String textOption = TextUtils.isEmpty(currentBookmark.getTextOption()) ? Character.toUpperCase(currentBookmark.getShortUrl().charAt(0)) + "" : currentBookmark.getTextOption();
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
}


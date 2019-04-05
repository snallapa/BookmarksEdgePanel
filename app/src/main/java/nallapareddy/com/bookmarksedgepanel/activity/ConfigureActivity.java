package nallapareddy.com.bookmarksedgepanel.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.samsung.android.sdk.look.Slook;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.adapters.BookmarksGridAdapter;
import nallapareddy.com.bookmarksedgepanel.adapters.GridItemTouchHelperCallback;
import nallapareddy.com.bookmarksedgepanel.dialogs.AddNewBookmarkDialog;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.model.BookmarkModel;
import nallapareddy.com.bookmarksedgepanel.model.IBookmarkModel;
import nallapareddy.com.bookmarksedgepanel.model.Position;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static nallapareddy.com.bookmarksedgepanel.model.BookmarkModel.COLUMNS;
import static nallapareddy.com.bookmarksedgepanel.model.BookmarkModel.ROWS;


public class ConfigureActivity extends AppCompatActivity implements AddNewBookmarkDialog.onNewBookmarkAddedListener, BookmarksGridAdapter.OnGridItemClickListener {

    private final int REQUEST_CODE = 1729;

    private static final String AD_CODE = "ca-app-pub-3135803015555141~4955511510";

    static final String EXTRA_BOOKMARK = "extra_bookmark";
    static final String EXTRA_POSITION = "extra_position";

    private IBookmarkModel<Bookmark> model;
    private BookmarksGridAdapter gridAdapter;
    private ItemTouchHelper itemTouchHelper;
    private Menu menu;

    @BindView(R.id.bookmarks_grid_view)
    RecyclerView bookmarksGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);
        ButterKnife.bind(this);
        initializeAds();
        model = new BookmarkModel(getApplicationContext());
        setupRecyclerView();

        Slook slook = new Slook();
        try {
            slook.initialize(this);
        } catch (Exception e) {
            Log.e("Configure", "SLOOK not initialized");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_configure, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_start_edit:
                switchMode();
                break;
            case R.id.action_accept:
                switchMode();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchMode() {
        MenuItem accept = menu.findItem(R.id.action_accept);
        MenuItem edit = menu.findItem(R.id.action_start_edit);
        if (gridAdapter.inEditMode()) {
            gridAdapter.toggleEdit();
            accept.setVisible(true);
            edit.setVisible(false);
            Animatable icon = (Animatable) accept.getIcon();
            icon.start();
            edit.setIcon(R.drawable.ic_edit_accept_icon);
            accept.setTitle(R.string.edit_bookmark);
            model.save();
        } else {
            LayoutAnimationController controller =
                    AnimationUtils.loadLayoutAnimation(this, R.anim.layout_shake);
            bookmarksGrid.setLayoutAnimation(controller);
            gridAdapter.toggleEdit();
            gridAdapter.notifyDataSetChanged();
            bookmarksGrid.scheduleLayoutAnimation();
            accept.setVisible(false);
            edit.setVisible(true);
            Animatable icon = (Animatable) edit.getIcon();
            icon.start();
            accept.setIcon(R.drawable.ic_accept_edit_icon);
            edit.setTitle(R.string.save_bookmark);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }

    private void setupRecyclerView() {
        bookmarksGrid.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, BookmarkModel.COLUMNS, LinearLayoutManager.VERTICAL, false);
        bookmarksGrid.setLayoutManager(gridLayoutManager);
        gridAdapter = new BookmarksGridAdapter(model, ROWS, COLUMNS,this);
        bookmarksGrid.setAdapter(gridAdapter);
        setupReordering();
    }

    private void setupReordering() {
        ItemTouchHelper.Callback callback = new GridItemTouchHelperCallback(gridAdapter);
        this.itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(bookmarksGrid);
    }

    private void openAddDialog(Position pos) {
        android.app.FragmentManager supportFragmentManager = getFragmentManager();
        AddNewBookmarkDialog newBookmarkDialog = AddNewBookmarkDialog.newInstance(pos);
        newBookmarkDialog.show(supportFragmentManager, AddNewBookmarkDialog.TAG);
    }

    private void openEditActivity(Position pos) {
        Bookmark bookmark = model.getBookmark(pos);
                Answers.getInstance().logCustom(new CustomEvent("Edit Bookmark")
                        .putCustomAttribute("Bookmark", bookmark.getUri().toString()));
        Intent intent = new Intent(ConfigureActivity.this, EditBookmarkActivity.class);
        intent.putExtra(EXTRA_BOOKMARK, Parcels.wrap(bookmark));
        intent.putExtra(EXTRA_POSITION, pos.toString());
        startActivityForResult(intent, REQUEST_CODE);

    }


    @Override
    public void newBookmarkAdded(String uri, Position pos) {
        Uri newBookmark = Uri.parse(uri);
        if (newBookmark != null) {
            model.setBookmark(pos, new Bookmark(newBookmark));
        }
        gridAdapter.notifyTranslatedItemChanged(pos);
        model.save();
        Answers.getInstance().logCustom(new CustomEvent("New Bookmark")
                .putCustomAttribute("Bookmark", uri));
    }

    @Override
    protected void onStop() {
        super.onStop();
        model.save();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE) {
            return;
        }
        if (resultCode == RESULT_OK) {
            Position position = Position.fromString(data.getStringExtra(EXTRA_POSITION));
            Bookmark currentBookmark = Parcels.unwrap(data.getParcelableExtra(EXTRA_BOOKMARK));
            model.setBookmark(position, currentBookmark);
            gridAdapter.notifyTranslatedItemChanged(position);
            model.save();
        }
    }

    private void initializeAds() {
        MobileAds.initialize(getApplicationContext(), AD_CODE);
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onItemClicked(Position pos) {
        Bookmark bookmark = model.getBookmark(pos);
        if (bookmark == null) {
            openAddDialog(pos);
        } else {
            openEditActivity(pos);
        }
    }

    @Override
    public void onItemDeleted(Position pos) {
        Bookmark bookmark = model.getBookmark(pos);
        Answers.getInstance().logCustom(new CustomEvent("Delete Bookmark")
                .putCustomAttribute("Bookmark", bookmark.getUri().toString()));
        try {
            deleteFile(bookmark.getFileSafe());
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        model.removeBookmark(pos);
        gridAdapter.notifyTranslatedItemChanged(pos);
        model.save();
    }

    @Override
    public void onItemTouched(RecyclerView.ViewHolder vh) {
        itemTouchHelper.startDrag(vh);
    }
}

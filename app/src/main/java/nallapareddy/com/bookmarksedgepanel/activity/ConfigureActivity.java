package nallapareddy.com.bookmarksedgepanel.activity;

import static nallapareddy.com.bookmarksedgepanel.model.BookmarkModel.COLUMNS;
import static nallapareddy.com.bookmarksedgepanel.model.BookmarkModel.ROWS;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.samsung.android.sdk.look.Slook;

import org.parceler.Parcels;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import nallapareddy.com.bookmarksedgepanel.R;
import nallapareddy.com.bookmarksedgepanel.adapters.BookmarksGridAdapter;
import nallapareddy.com.bookmarksedgepanel.adapters.GridItemTouchHelperCallback;
import nallapareddy.com.bookmarksedgepanel.dialogs.AddNewBookmarkDialog;
import nallapareddy.com.bookmarksedgepanel.model.Bookmark;
import nallapareddy.com.bookmarksedgepanel.model.BookmarkModel;
import nallapareddy.com.bookmarksedgepanel.model.IBookmarkModel;
import nallapareddy.com.bookmarksedgepanel.model.Position;
import nallapareddy.com.bookmarksedgepanel.utils.UrlCallback;
import nallapareddy.com.bookmarksedgepanel.utils.UrlRunner;


public class ConfigureActivity extends AppCompatActivity implements AddNewBookmarkDialog.onNewBookmarkAddedListener, BookmarksGridAdapter.OnGridItemClickListener {

    static final String EXTRA_BOOKMARK = "extra_bookmark";
    static final String EXTRA_POSITION = "extra_position";


    private final Executor executor = Executors.newFixedThreadPool(COLUMNS * ROWS);
    private final Handler handler = new Handler(Looper.getMainLooper());


    private IBookmarkModel<Bookmark> model;
    private BookmarksGridAdapter gridAdapter;
    private ItemTouchHelper itemTouchHelper;
    private Menu menu;
    private FirebaseAnalytics firebaseAnalytics;
    private ActivityResultLauncher<Intent> editLauncher;

    @BindView(R.id.bookmarks_grid_view)
    RecyclerView bookmarksGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);
        ButterKnife.bind(this);
        initializeAds();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        model = new BookmarkModel(getApplicationContext());
        editLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                int resultCode = result.getResultCode();
                Intent data = result.getData();
                if (resultCode == RESULT_OK && data != null) {
                    Position position = Position.fromString(data.getStringExtra(EXTRA_POSITION));
                    Bookmark currentBookmark = Parcels.unwrap(data.getParcelableExtra(EXTRA_BOOKMARK));
                    model.setBookmark(position, currentBookmark);
                    gridAdapter.notifyTranslatedItemChanged(position);
                    model.save();
                    try {
                        FileOutputStream fos = openFileOutput(currentBookmark.getFileSafe(), Context.MODE_PRIVATE);
                        executor.execute(new UrlRunner(currentBookmark.getUri().toString(), fos, new UrlCallback() {
                            @Override
                            public void onSuccess() {
                                handler.post(() -> gridAdapter.notifyTranslatedItemChanged(position));

                            }

                            @Override
                            public void onFailure() {
                                handler.post(() -> gridAdapter.notifyTranslatedItemChanged(position));
                            }
                        }));
                    } catch (FileNotFoundException ignored) {

                    }
                }
            }
        });
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
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base));
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
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, bookmark.getUri().toString());
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        Intent intent = new Intent(ConfigureActivity.this, EditBookmarkActivity.class);
        intent.putExtra(EXTRA_BOOKMARK, Parcels.wrap(bookmark));
        intent.putExtra(EXTRA_POSITION, pos.toString());
        editLauncher.launch(intent);
    }


    @Override
    public void newBookmarkAdded(String uri, Position pos) {
        Uri newBookmark = Uri.parse(uri);
        if (newBookmark != null) {
            Bookmark bookmark = new Bookmark(newBookmark);
            try {
                FileOutputStream fos = openFileOutput(bookmark.getFileSafe(), Context.MODE_PRIVATE);
                executor.execute(new UrlRunner(bookmark.getUri().toString(), fos, new UrlCallback() {
                    @Override
                    public void onSuccess() {
                        handler.post(() -> gridAdapter.notifyTranslatedItemChanged(pos));

                    }

                    @Override
                    public void onFailure() {
                        handler.post(() -> gridAdapter.notifyTranslatedItemChanged(pos));
                    }
                }));
            } catch (FileNotFoundException ignored) {

            }
            model.setBookmark(pos, bookmark);
        }

        gridAdapter.notifyTranslatedItemChanged(pos);
        model.save();
        Bundle bundle = new Bundle();
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
    }

    @Override
    protected void onStop() {
        super.onStop();
        model.save();
    }


    private void initializeAds() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {

            }
        });
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
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, bookmark.getUri().toString());
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.REFUND, bundle);
        try {
            deleteFile(bookmark.getFileSafe());
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
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

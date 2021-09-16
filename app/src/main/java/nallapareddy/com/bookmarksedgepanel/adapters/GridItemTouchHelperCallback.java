package nallapareddy.com.bookmarksedgepanel.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

public class GridItemTouchHelperCallback extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter listener;

    public GridItemTouchHelperCallback(ItemTouchHelperAdapter listener) {
        this.listener = listener;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        int swipeFlags = 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return listener.onItemMove(viewHolder.getBindingAdapterPosition(), viewHolder1.getBindingAdapterPosition());
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    public interface ItemTouchHelperAdapter {

        boolean onItemMove(int fromPosition, int toPosition);

    }
}

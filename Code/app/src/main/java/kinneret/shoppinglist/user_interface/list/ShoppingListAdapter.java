package kinneret.shoppinglist.user_interface.list;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import kinneret.shoppinglist.R;
import kinneret.shoppinglist.list_objects.ShoppingList;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private final OnTouchListener listener;
    private static final String TAG = "ShoppingListAdapter";

    private final RecyclerView recyclerView;
    private final Context context;

    private final ArrayList<ShoppingList> data = new ArrayList<>();

    private final int viewSize = 0;


    public ShoppingListAdapter(Context context, RecyclerView recyclerView, OnTouchListener listener) {
        this.listener=listener;
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        return viewSize;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = R.layout.shopping_lists;

        ViewGroup vg = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(vg);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final ShoppingList list = data.get(position);

        // Reset values used by animation
        holder.itemView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        holder.resetBackgroundColor();
        holder.resetTextColor();
        holder.nameTextView.setText(list.name);
        holder.createdByTextView.setText(context.getString(R.string.created_by, list.createdBy));
        holder.data = list;
        /////////////////////////////
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onLongListClick(list);
                return true;
            }
        });



    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void addList(ShoppingList list) {
        if (!list.isDeleted()) {
                data.add(0, list);
                notifyItemInserted(0);
                Log.v(TAG, "addList");

        } else {
            Log.v(TAG, "addList not active");
        }
    }

    public void setLists(Collection<ShoppingList> lists) {
        data.clear();
        data.addAll(lists);

        // createdAt descend
        Collections.sort(data, new Comparator<ShoppingList>() {
            @Override
            public int compare(ShoppingList a, ShoppingList b) {
                return (int) (b.createdAt - a.createdAt);

            }
        });

        notifyItemRangeInserted(0, data.size());
    }

    public void updateList(ShoppingList list) {
        int index = data.indexOf(list);
        Log.v(TAG, "updateList index=" + index);

        if (index != -1) {
            data.set(index, list);
//notify should have updated the Draw of ItemTouchHelper.. but noooo
            notifyItemChanged(index);
        }
    }

    public void removeList(ShoppingList list) {
        int index = data.indexOf(list);
        Log.v(TAG, "removeList index=" + index);

        if (index != -1) {
            data.remove(index);

            notifyItemRemoved(index);
        }
    }
//todo aee if i dont ruin stuff
   /* public void setViewSize() {

        this.viewSize = 0;
        recyclerView.getItemAnimator().endAnimations();
        notifyDataSetChanged();
    }*/

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView[] textViews;
        public final int[] defaultTextColors;
        public final int defaultBackgroundColor;

        public final TextView nameTextView;
        public final TextView createdByTextView;
        public final ImageView checkImage;

        public ShoppingList data;

        public boolean isSwiped;

        public ViewHolder(ViewGroup vg) {
            super(vg);

            nameTextView = vg.findViewById(R.id.shopping_list_name);
            createdByTextView = vg.findViewById(R.id.shopping_list_created_by);
            checkImage = vg.findViewById(R.id.shopping_list_item_check);
            checkImage.setVisibility(View.INVISIBLE);

            textViews = new TextView[]{
                    nameTextView,
                    createdByTextView,
            };

            defaultTextColors = new int[textViews.length];
            for (int i = 0; i < defaultTextColors.length; i++) {
                defaultTextColors[i] = textViews[i].getCurrentTextColor();
            }

            defaultBackgroundColor = ((ColorDrawable) itemView.getBackground()).getColor();
        }

        public void resetTextColor() {
            for (int i = 0; i < defaultTextColors.length; i++) {
                textViews[i].setTextColor(defaultTextColors[i]);
            }
        }

        public void resetBackgroundColor() {
            itemView.setBackgroundColor(defaultBackgroundColor);
        }

    }

    public interface OnTouchListener {

        void onLongListClick(ShoppingList list);

    }

}

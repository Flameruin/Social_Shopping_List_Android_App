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
import kinneret.shoppinglist.list_objects.ShoppingListItem;

public class ShoppingListItemAdapter extends RecyclerView.Adapter<ShoppingListItemAdapter.ViewHolder>{
    private static final String TAG = "ShoppingListItemAdapter";

    private final RecyclerView recyclerView;
    private final Context context;
    private final ArrayList<ShoppingListItem> data = new ArrayList<>();

    private int viewSize = 0;

    public ShoppingListItemAdapter(Context context, RecyclerView recyclerView) {
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
        final ShoppingListItem item = data.get(position);

        // Reset values used by animation
        holder.itemView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        holder.resetBackgroundColor();
        holder.resetTextColor();

        holder.nameTextView.setText(item.name);
        //TODO fix to key
        holder.createdByTextView.setText(context.getString(R.string.created_by, item.createdBy));

        holder.descriptionTextView.setVisibility((item.description.length() > 0) ? View.VISIBLE : View.GONE);
        holder.descriptionTextView.setText(item.description);

        holder.checkImage.setVisibility((item.isChecked()) ? View.VISIBLE : View.GONE);

        holder.data = item;
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    public void addItem(ShoppingListItem item) {
        if (!item.isDeleted()) {
            if (!item.isChecked()) {
                data.add(0, item);
                //
                notifyItemInserted(0);

                Log.v(TAG, "addList checked position=0");
            } else {
                int size = data.size();
                int firstChecked = 0;
                while (firstChecked < size && !data.get(firstChecked).isChecked()) {
                    firstChecked++;
                }

                data.add(firstChecked, item);

                notifyItemInserted(firstChecked);

                Log.v(TAG, "addList position=" + firstChecked);
            }


        } else {
            Log.v(TAG, "addList not active");
        }
    }

    public void setItems(Collection<ShoppingListItem> items) {
        data.clear();
        data.addAll(items);

        // order by checked and time of creation//checked at bottom
        Collections.sort(data, new Comparator<ShoppingListItem>() {
            @Override
            public int compare(ShoppingListItem a, ShoppingListItem b) {
                if (!a.isChecked() == !b.isChecked()) {
                    return (int) (b.createdAt - a.createdAt);
                }

                return (!a.isChecked() && b.isChecked()) ? -1 : 1;
            }
        });

        notifyItemRangeInserted(0, data.size());
    }

    public void updateItem(ShoppingListItem item) {
        int index = data.indexOf(item);
        Log.v(TAG, "updateList index=" + index);
        if (index != -1) {
            data.set(index, item);
            notifyItemChanged(index);
        }
    }

    public void removeItem(ShoppingListItem item) {
        int index = data.indexOf(item);
        Log.v(TAG, "removeItem index=" + index);

        if (index != -1) {
            data.remove(index);

            notifyItemRemoved(index);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView[] textViews;
        public final int[] defaultTextColors;
        public final int defaultBackgroundColor;

        public final TextView nameTextView;
        public final TextView createdByTextView;
        public final ImageView checkImage;
        public final TextView descriptionTextView;

        public ShoppingListItem data;


        public boolean isSwiped;

        public ViewHolder(ViewGroup vg) {
            super(vg);

            nameTextView = vg.findViewById(R.id.shopping_list_name);
            descriptionTextView = vg.findViewById(R.id.shopping_list_item_description);
            createdByTextView = vg.findViewById(R.id.shopping_list_created_by);
            checkImage = vg.findViewById(R.id.shopping_list_item_check);

            textViews = new TextView[]{
                    nameTextView,
                    descriptionTextView,
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

}

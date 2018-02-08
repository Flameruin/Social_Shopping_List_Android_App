package kinneret.shoppinglist.user_interface.list.touch_helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import kinneret.shoppinglist.R;
import kinneret.shoppinglist.helper.Helper;
import kinneret.shoppinglist.user_interface.list.ShoppingListAdapter;


public class ListTouchHelperCallback extends ItemTouchHelper.Callback {

    @SuppressWarnings("unused")
    private final Context context;
    private final OnSwipeListener listener;

    ////

    ///

    private final int leftColor;
    private final Bitmap leftIcon;
    private final int rightColor;
    private final Bitmap rightIcon;

    private final Paint paint = new Paint();

    public ListTouchHelperCallback(Context context, OnSwipeListener listener) {
        this.context = context;
        this.listener = listener;

        // TODO: check if want different icons
        leftColor = context.getResources().getColor(R.color.com_smart_login_code);
        leftIcon = Helper.convertDrawableToBitmap(context.getResources().getDrawable(R.drawable.ic_group_white_24dp));
        rightColor = context.getResources().getColor(R.color.red);
        rightIcon = Helper.convertDrawableToBitmap(context.getResources().getDrawable(R.drawable.ic_delete_white_24dp));
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }


    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        ShoppingListAdapter.ViewHolder vh = (ShoppingListAdapter.ViewHolder) viewHolder;


        vh.isSwiped = true;
        //delete
        if (direction == ItemTouchHelper.LEFT){// ItemTouchHelper.START) {

            listener.onSwipeLeft(vh);

        //share
        } else if (direction == ItemTouchHelper.RIGHT){//.END) {
            listener.onSwipeRight(vh);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            dX = 0;
        }


        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            super.onChildDrawOver(c, recyclerView, viewHolder, 0, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;

            float width = itemView.getWidth();
            float height = itemView.getHeight();


            if (height > 0) {
                float left = itemView.getLeft();
                float top = itemView.getTop();
                float right = itemView.getRight();
                float bottom = itemView.getBottom();

                float centerY = (top + bottom) / 2;

                float iconSize = height * 0.64f;
                float margin = width * 0.025f;

                //TODO silly workaround just to make it clear need a better way
                if (dX > 0) {
                    paint.setColor(leftColor);
                    RectF background = new RectF(left, top, dX, bottom);
                    c.drawRect(background, paint);

                    float maxLeft = left + margin;
                    float iconLeft = Math.min(dX - iconSize - margin, maxLeft);
                    RectF iconRect = new RectF(iconLeft, centerY - iconSize / 2, iconLeft + iconSize, centerY + iconSize / 2);
                    c.drawBitmap(leftIcon, null, iconRect, paint);
                } else {
                    paint.setColor(rightColor);
                    RectF background = new RectF(right + dX, top, right, bottom);
                    c.drawRect(background, paint);

                    float minRight = right - margin;
                    float iconRight = Math.max(right + dX + iconSize + margin, minRight);
                    RectF iconRect = new RectF(iconRight - iconSize, centerY - iconSize / 2, iconRight, centerY + iconSize / 2);
                    c.drawBitmap(rightIcon, null, iconRect, paint);
                }
            }
        } else {
            super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

    }


    public interface OnSwipeListener {

        void onSwipeRight(ShoppingListAdapter.ViewHolder vh);

        void onSwipeLeft(ShoppingListAdapter.ViewHolder vh);

    }

}
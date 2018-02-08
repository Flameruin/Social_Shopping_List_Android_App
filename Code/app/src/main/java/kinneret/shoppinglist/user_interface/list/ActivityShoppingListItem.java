package kinneret.shoppinglist.user_interface.list;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import kinneret.shoppinglist.R;
import kinneret.shoppinglist.dialog.ItemDialogFragment;
import kinneret.shoppinglist.firebase.FirebaseDatabaseConnectionWatcher;
import kinneret.shoppinglist.list_objects.ShoppingListItem;
import kinneret.shoppinglist.user_interface.list.touch_helper.ItemTouchHelperCallback;
///

///
public class ActivityShoppingListItem extends AppCompatActivity implements ItemDialogFragment.ItemDialogListener {
    public static final String TAG = "ActivityShopingListItem";
    private static final int STATE_STARTING = -1;
    private static final int STATE_LOADING = 0;
    private static final int STATE_EMPTY = 1;
    private static final int STATE_EXITST = 2;
    public final String FIREBASE_TOPIC_ADD = "add";
////////////
    private String name = null;
    private String shareKey;
    private String lastAddedOwnKey;
    private View coordinatorLayout;
    private DatabaseReference dbRef;
    private SharedPreferences sharedPreferences;
    private RecyclerView itemView;

    private int state = STATE_STARTING;
    private FirebaseDatabaseConnectionWatcher fbDbConnectionWatcher;
    private FirebaseDatabase database;

    private ShoppingListItemAdapter adapter;
    private static ItemTouchHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list_item);
        coordinatorLayout = findViewById(R.id.itemCoordinator);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.itemToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setSubtitle(R.string.grocery_list); //need to put here recourse file if we want to allow multi languages

        // Initializations
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();

        //define name at start
        Bundle data = getIntent().getExtras();
        name= data.getString("p_name");
        shareKey = data.getString("p_key");

        // Subscribe to topic which broadcasts the new item notifications
        FirebaseMessaging.getInstance().subscribeToTopic(FIREBASE_TOPIC_ADD);

        // FAB - RED button plus
        FloatingActionButton fabCreateNewItem = findViewById(R.id.fab_create_new_item);
        fabCreateNewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewItemDialog();
            }
        });

        // Setup RecyclerView
        itemView = findViewById(R.id.shopping_list);
        itemView.setLayoutManager(new LinearLayoutManager(this));

        // Handle swipes
        helper = new ItemTouchHelper(
                new ItemTouchHelperCallback(
                        this,
                        new ItemTouchHelperCallback.OnSwipeListener() {

                            @Override
                            public void onSwipeLeft(ShoppingListItemAdapter.ViewHolder vh) {
                                // Delete
                                ShoppingListItem item = vh.data;
                                Log.v(TAG, "Delete " + item);
                                item.delete();
                                dbRef.child("items").child(shareKey).child(item.key).removeValue();
                                updateView();
                            }

                            @Override
                            public void onSwipeRight(ShoppingListItemAdapter.ViewHolder vh) {
                                // Archive
                                ShoppingListItem item = vh.data;
                                Log.v(TAG, "Check " + item);
                                item.check();

                                dbRef.child("items").child(shareKey).child(item.key).setValue(item);
                                updateView();
                            }

                        }

                )
        );

        helper.attachToRecyclerView(null);
        helper.attachToRecyclerView(itemView);

        // Setup adapter
        adapter = new ShoppingListItemAdapter(this, itemView);
        itemView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                updateUI();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                updateUI();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                updateUI();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                updateUI();
            }


            private void updateUI() {
                itemView.getItemAnimator().isRunning(new RecyclerView.ItemAnimator.ItemAnimatorFinishedListener() {
                    @Override
                    public void onAnimationsFinished() {
                        if (adapter.getItemCount() > 0) {
                            setUIState(STATE_EXITST);
                        } else {
                            setUIState(STATE_EMPTY);
                        }
                    }
                });
            }
        });

        // Load initial data
        dbRef.child("items").child(shareKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<ShoppingListItem> items = new ArrayList<>((int) dataSnapshot.getChildrenCount());

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Log.v(TAG, "Single " + dsp.toString());

                    ShoppingListItem item = ShoppingListItem.fromSnapshot(dsp);
                    items.add(item);

                }

                adapter.setItems(items);

                // Listen for changes
                final long ending;
                if(items.size() > 0) {
                    ending = items.get(items.size() - 1).createdAt;
                }
                else
                {
                    ending= System.currentTimeMillis();
                }
                dbRef.child("items").child(shareKey).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.v(TAG, "Added " + dataSnapshot.toString());

                        ShoppingListItem item = ShoppingListItem.fromSnapshot(dataSnapshot);
                             if (item.createdAt > ending) {
                            adapter.addItem(item);

                            if (lastAddedOwnKey != null && lastAddedOwnKey.equals(item.key)) {
                                itemView.scrollToPosition(0);
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.v(TAG, "Changed " + dataSnapshot.toString());
                        ShoppingListItem item = ShoppingListItem.fromSnapshot(dataSnapshot);
                        adapter.updateItem(item);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Log.v(TAG, "Removed " + dataSnapshot.toString());
                        ShoppingListItem item = ShoppingListItem.fromSnapshot(dataSnapshot);
                        adapter.removeItem(item);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        Log.v(TAG, "Moved " + dataSnapshot.toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Cancelled " + databaseError.toString());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Cancelled " + databaseError.toString());
            }
        });

        setupConnectionWatcher();
        setUIState(STATE_LOADING);

    }

    private void setupConnectionWatcher() {
        Snackbar.make(coordinatorLayout, R.string.snackbar_database_connecting, Snackbar.LENGTH_INDEFINITE).show();
        fbDbConnectionWatcher = new FirebaseDatabaseConnectionWatcher();
        fbDbConnectionWatcher.addListener(new FirebaseDatabaseConnectionWatcher.OnConnectionChangeListener() {
            @Override
            public void onConnected() {
                Snackbar.make(coordinatorLayout, R.string.snackbar_database_connected, Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onDisconnected() {
                Snackbar.make(coordinatorLayout, R.string.snackbar_database_reconnecting, Snackbar.LENGTH_INDEFINITE).show();
            }
        });
    }

    private void setUIState(int newState) {
        View[] views = new View[]{
                findViewById(R.id.loading_shopping_list),
                findViewById(R.id.empty_shopping_list),
                findViewById(R.id.shopping_list)
        };

        int oldState = this.state;
        this.state = newState;

        if (oldState == STATE_STARTING) {
            for (View v : views) v.setVisibility(View.GONE);
            View newView = views[newState];
            newView.setVisibility(View.VISIBLE);
        } else {
            if (oldState != newState) {
                final View oldView = views[oldState];
                View newView = views[newState];

                oldView.setVisibility(View.VISIBLE);
                newView.setVisibility(View.VISIBLE);
                oldView.setAlpha(1);
                newView.setAlpha(0);

                int duration = 600;
                Interpolator interpolator = new AccelerateInterpolator();

                oldView.animate().alpha(0).setDuration(duration).setInterpolator(interpolator).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        oldView.setVisibility(View.GONE);
                    }
                }).start();
                newView.animate().alpha(1).setDuration(duration).setInterpolator(interpolator).setListener(null).start();

                if (newState == STATE_EMPTY) {
                    View icon = findViewById(R.id.empty_shopping_list_icon);
                    View text = findViewById(R.id.empty_shopping_list_text);

                    icon.setRotation(0);
                    //come rotating over from top left (-2000,-2000) 360*4 times until we get to the middle of screen(0,0)
                    icon.setTranslationY(-2000);
                    icon.setTranslationX(-2000);
                    icon.animate().setDuration(2000).translationX(0).translationY(0).rotation(4 * 360).setInterpolator(new DecelerateInterpolator(2f)).start();

                    //text pop in middle of screen
                    text.setAlpha(0);
                    text.setScaleX(0);
                    text.setScaleY(0);
                    text.animate().setStartDelay(1000).setDuration(800).alpha(1).scaleY(1).scaleX(1).setInterpolator(new OvershootInterpolator(2f)).start();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //works as layout but android warns me so moved to menu
        //getMenuInflater().inflate(R.layout.help_menu, menu);
        getMenuInflater().inflate(R.menu.help_menu, menu);
        //remove irrelevant options for this ui
        menu.removeItem(R.id.it_swipe_rigt_share);
        menu.removeItem(R.id.it_long_click_enter_list);

        return true;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();

        FirebaseDatabase.getInstance().goOnline();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();

        FirebaseDatabase.getInstance().goOffline();
    }

    @Override
    public void onDialogAddItem(String inputName, String inputDescription) {
        String itemName = inputName.trim();
        String itemDescription = inputDescription.trim();

        if (itemName.length() > 0) {
            createItem(itemName, itemDescription);
        }
    }

    public void createItem(String itemName, String itemDescription) {
        ShoppingListItem listItem = new ShoppingListItem(name, itemName, itemDescription);
        //todo check it works
        DatabaseReference ref = dbRef.child("items").child(shareKey).push();

        //////
        ref.setValue(listItem);

        lastAddedOwnKey = ref.getKey();
        }


    public void openNewItemDialog() {
        ItemDialogFragment itemDialogFragment = new ItemDialogFragment();
        itemDialogFragment.show(getSupportFragmentManager(), "ItemDialogFragment");
    }


    private void updateView(){
        helper.attachToRecyclerView(null);
        helper.attachToRecyclerView(itemView);
    }


}

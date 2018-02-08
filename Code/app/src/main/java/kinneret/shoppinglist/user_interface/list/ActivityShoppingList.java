package kinneret.shoppinglist.user_interface.list;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.Map;

import kinneret.shoppinglist.R;
import kinneret.shoppinglist.dialog.ListDialogFragment;
import kinneret.shoppinglist.dialog.ShareDialogFragment;
import kinneret.shoppinglist.firebase.FirebaseDatabaseConnectionWatcher;
import kinneret.shoppinglist.list_objects.ShoppingList;
import kinneret.shoppinglist.user_interface.list.touch_helper.ListTouchHelperCallback;


public class ActivityShoppingList extends AppCompatActivity implements ListDialogFragment.ListDialogListener, ShareDialogFragment.ShareDialogListener , ShoppingListAdapter.OnTouchListener {

    private static final String TAG = "ActivityShoppingList";

    private FloatingActionButton fabCreateNewList;
    private String name;
    private String email;

    private ShoppingListAdapter listAdapter;
    public final String FIREBASE_TOPIC_ADD = "add";
    private String lastAddedOwnKey;
    private DatabaseReference dbRef;
    private FirebaseDatabaseConnectionWatcher fbDbConnectionWatcher;

    private FirebaseDatabase database;

    private View coordinatorLayout;
    private RecyclerView listRecyclerView;
    private static final int STATE_STARTING = -1;
    private static final int STATE_LOADING = 0;
    private static final int STATE_EMPTY = 1;
    private static final int STATE_EXISTS = 2;
    private int state = STATE_STARTING;
    /*
    THIS WORKS! This way I can reset the draw from swiping!
     calling ItemTouchHelper::attachToRecyclerView(RecyclerView) twice, calls the private method ItemTouchHelper::destroyCallbacks().
     removes item decoration and all listeners but also clears all RecoverAnimations!.
     https://developer.android.com/reference/android/support/v7/widget/helper/ItemTouchHelper.html#attachToRecyclerView(android.support.v7.widget.RecyclerView)
     */
    private static ItemTouchHelper helper;
    /*
    Should have made a new window just to see shared users but duw to time constraints this is it
    Maybe I will continue on my free time after we finish presenting
     */
    public static Map<String,Object> sharingMap = new HashMap<>();
    public static String listKey;

        //create new list
        private final View.OnClickListener createNewList = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewListDialog();
            }
        };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        coordinatorLayout = findViewById(R.id.listCoordinator);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.listToolbar);
        setSupportActionBar(toolbar);
       // String mail=(R.string.mail_adress,email);

        // Initializations
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();


        Bundle data = getIntent().getExtras();
        //define name at start
        name = data.getString("p_name");
        email = data.getString("p_email");
        email = clearStringForFirebase(email).toLowerCase();
        //gets thee user name and mail for a proper greeting

        String userName = getString(R.string.user_name,name);
        getSupportActionBar().setSubtitle(userName); //need to put here recourse file if we want to allow multi languages

        // Subscribe to topic which broadcasts the new item notifications
        FirebaseMessaging.getInstance().subscribeToTopic(FIREBASE_TOPIC_ADD);
        //Floating // FAB - RED button plus
        fabCreateNewList = findViewById(R.id.fab_create_new_list);
        fabCreateNewList.setOnClickListener(createNewList);         //create new list

        // Setup RecyclerView
        listRecyclerView = findViewById(R.id.shopping_list);
        listRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Handle swipes
       helper = new ItemTouchHelper(
                new ListTouchHelperCallback(
                        this,

                        new ListTouchHelperCallback.OnSwipeListener() {

                            @Override
                            public void onSwipeLeft(ShoppingListAdapter.ViewHolder vh) {
                                // Delete
                                ShoppingList list = vh.data;
                                Log.v(TAG, "Delete " + list);
                                list.delete();
                                dbRef.child("lists").child(list.key).child("sharedWith").child(email).removeValue();
                                updateView();
                            }

                            @Override
                            public void onSwipeRight(ShoppingListAdapter.ViewHolder vh) {
                                // Share
                                ShoppingList list = vh.data;
                                Log.v(TAG, "lists " + list);
                                //list.check();

                                sharingMap.putAll(ShoppingList.sharedMap);
                                sharingMap= ShoppingList.sharedMap;

                                listKey=list.key;
                                openNewShareDialog();
                                
                            }

                        }
                )
        );

        //anothe method that dosen't work to remove the swipe draw
        helper.attachToRecyclerView(null);
        helper.attachToRecyclerView(listRecyclerView);

        // Setup listAdapter
        listAdapter = new ShoppingListAdapter(this, listRecyclerView
                //TODO need to get long click on list touch and pass key to shoppinglist activity
                ,new ShoppingListAdapter.OnTouchListener() {
                    @Override
                    public void onLongListClick(ShoppingList list) {

                        Intent intent = new Intent(ActivityShoppingList.this, ActivityShoppingListItem.class);
                        intent.putExtra("p_name", name); //send the name to activity
//                      intent.putExtra("p_email", email);//send the email to activity
                        intent.putExtra("p_key",list.key);//key wil be used instead of push
                        //intent.putExtra("p_listName",list.name);//send title of list
                        Log.d(TAG, "onLongListClick: "+list.key);
                        startActivity(intent); //start ListItem activity
//
                    }
                }
    );
        listRecyclerView.setAdapter(listAdapter);
        listAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
                listRecyclerView.getItemAnimator().isRunning(new RecyclerView.ItemAnimator.ItemAnimatorFinishedListener() {
                    @Override
                    public void onAnimationsFinished() {
                        if (listAdapter.getItemCount() > 0) {
                            setUIState(STATE_EXISTS);
                        } else {
                            setUIState(STATE_EMPTY);
                        }
                    }
                });
            }
        });



// Load initial data
        dbRef.child("lists").orderByChild("sharedWith/"+email).equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<ShoppingList> lists = new ArrayList<>((int) dataSnapshot.getChildrenCount());

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Log.v(TAG, "Single " + dsp.toString());
                    //
                    ShoppingList list = ShoppingList.fromSnapshot(dsp);
                    list.sharedListFromSnapshot(dsp);
                    //
                    lists.add(list);

                }

                listAdapter.setLists(lists);

                // Listen for changes
                final long ending;
                if(lists.size() > 0) {
                    ending = lists.get(lists.size() - 1).createdAt;
                }
                else
                {
                    ending= System.currentTimeMillis();
                }
                dbRef.child("lists").orderByChild("sharedWith/"+email).equalTo(email).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.v(TAG, "Added " + dataSnapshot.toString());

                        ShoppingList list = ShoppingList.fromSnapshot(dataSnapshot);
                        if (list.createdAt > ending) {
                            listAdapter.addList(list);

                            if (lastAddedOwnKey != null && lastAddedOwnKey.equals(list.key)) {
                                listRecyclerView.scrollToPosition(0);
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.v(TAG, "Changed " + dataSnapshot.toString());
                        ShoppingList list = ShoppingList.fromSnapshot(dataSnapshot);

                        //
                        list.sharedListFromSnapshot(dataSnapshot);
                        //
                        listAdapter.updateList(list);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Log.v(TAG, "Removed " + dataSnapshot.toString());
                        ShoppingList list = ShoppingList.fromSnapshot(dataSnapshot);
                        //
                        list.sharedListFromSnapshot(dataSnapshot);
                        //
                        listAdapter.removeList(list);
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
                    icon.setTranslationX(-2000);
                    icon.setTranslationY(-2000);
                    icon.animate().setDuration(2000).translationX(0).translationY(0).rotation(4 * 360).setInterpolator(new DecelerateInterpolator(2f)).start();

                    text.setAlpha(0);
                    text.setScaleX(0);
                    text.setScaleY(0);
                    text.animate().setStartDelay(1000).setDuration(800).alpha(1).scaleY(1).scaleX(1).setInterpolator(new OvershootInterpolator(2f)).start();
                }
            }
        }
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

    private void openNewListDialog()
    {
        ListDialogFragment listDialogFragment = new ListDialogFragment();
        listDialogFragment.show(getSupportFragmentManager(),"ListDialogFragment");
    }


    private void openNewShareDialog()
    {
       // Bundle arg = new Bundle();
       // arg.putString("p_key",listKey);
        ShareDialogFragment shareDialogFragment = new ShareDialogFragment();
        shareDialogFragment.show(getSupportFragmentManager(),"ShareDialogFragment");

        updateView();
    }


    @Override
    public void onDialogAddList(String inputLingListName){//}, String shoppingListDescription) {

        String shoppingListName = inputLingListName.trim();
       // String listDescription = shoppingListDescription.trim();

        if (shoppingListName.length() > 0) {
            createList(shoppingListName);//, listDescription);
        }

    }
/*
adding users to share with
 */
    @Override
    public void onDialogAddSharing(String inpuUserName){
        String shareIdentefier = clearStringForFirebase(inpuUserName).toLowerCase();
        if (shareIdentefier.length() > 0) {
            sharingMap.put(shareIdentefier,shareIdentefier);
            dbRef.child("lists").child(listKey).child("sharedWith").setValue(sharingMap);

            Log.d(TAG, "onDialogAddSharing added: "+ shareIdentefier);
            Snackbar.make(coordinatorLayout, getString(R.string.snackbar_share_added,shareIdentefier), Snackbar.LENGTH_LONG).show();

            sharingMap.clear();
        }
    }

    public String clearStringForFirebase(String s)
    {
        //Keys must not contain '/', '.', '#', '$', '[', ']'
        s = s.trim();
        s = s.replaceAll("[#|$|/|.|\\[|\\]]","");
        return s;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help_menu, menu);
        menu.removeItem(R.id.it_swipe_rigt_check);
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
    public void onLongListClick(ShoppingList list){
                        Intent intent = new Intent(ActivityShoppingList.this, ActivityShoppingListItem.class);
                        startActivity(intent); //start Watch List activity
    }

    public void createList(String shoppingListName) {//}, String listDescription) {
        ShoppingList list = new ShoppingList(name, shoppingListName);//, itemDescription, itemPrice, itemUrgent);
        DatabaseReference ref = dbRef.child("lists").push();
        ref.setValue(list);
        Log.d(TAG, "createList: list" + list.toString());

        //makes a map with who to share with username
        ShoppingList.sharedMap = new HashMap<>();
        ShoppingList.sharedMap.put(email,email);

        DatabaseReference reff = dbRef.child("lists").child(ref.getKey()).child("sharedWith");
        reff.setValue(ShoppingList.sharedMap);
        lastAddedOwnKey = ref.getKey();
    }

    private void updateView(){
        helper.attachToRecyclerView(null);
        helper.attachToRecyclerView(listRecyclerView);
    }
}

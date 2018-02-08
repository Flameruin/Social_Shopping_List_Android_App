package kinneret.shoppinglist.list_objects;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class ShoppingList {
    @Exclude
    public final String sharedValue = "sharedWith";
    @Exclude
    public String key;
    @Exclude
    static public Map<String,Object> sharedMap ;

    public String createdBy;
    public String name;
    public Status status;
    public long createdAt;


    @SuppressWarnings("unused")
    public ShoppingList() {
        // Default constructor required for calls to DataSnapshot.getValue(ShoppingListItem.class)
    }

    public ShoppingList(String createdBy, String name){//, String description) {
        this.createdBy = createdBy;
        this.name = name;
      //  this.description = description;
        this.status = Status.ACTIVE;
        this.createdAt = System.currentTimeMillis();
    }

    public static ShoppingList fromSnapshot(DataSnapshot snapshot) {
        Log.v("ShoppingList here ",  snapshot.getValue().toString());

        ShoppingList list = snapshot.getValue(ShoppingList.class);
        list.key = snapshot.getKey();
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof ShoppingList && key.equals(((ShoppingList) obj).key);
    }

    @Override
    public String toString() {
        return "ShoppingList{" +
                "key='" + key + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", name='" + name + '\'' +
             //   ", description='" + description + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }


    public void delete() {
        status = Status.DELETED;
    }

    @Exclude
    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    public enum Status {
        ACTIVE , DELETED
    }

    public void sharedListFromSnapshot(DataSnapshot snapshot)
    {
        //noinspection unchecked
        sharedMap = (Map<String, Object>) snapshot.child(sharedValue).getValue();

    }

}

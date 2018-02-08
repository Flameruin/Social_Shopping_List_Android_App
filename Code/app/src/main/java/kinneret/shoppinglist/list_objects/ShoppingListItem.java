package kinneret.shoppinglist.list_objects;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ShoppingListItem {
    @Exclude
    public String key;

    public long createdAt;
    public String createdBy;
    public String name;
    public String description;
    public Status status;


    @SuppressWarnings("unused")
    public ShoppingListItem() {
        // Default constructor required for calls to DataSnapshot.getValue(ShoppingListItem.class)
    }

    public ShoppingListItem(String createdBy, String name, String description) {
        this.createdBy = createdBy;
        this.name = name;
        this.description = description;
        this.status = Status.ACTIVE;
        this.createdAt = System.currentTimeMillis();
    }

    public static ShoppingListItem fromSnapshot(DataSnapshot snapshot) {
        ShoppingListItem item = snapshot.getValue(ShoppingListItem.class);
        item.key = snapshot.getKey();
        return item;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof ShoppingListItem && key.equals(((ShoppingListItem) obj).key);
    }

    @Override
    public String toString() {
        return "ShoppingListItem{" +
                "key='" + key + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }

    public void check() {
        status = Status.CHECKED;
    }

    public void delete() {
        status = Status.DELETED;
    }

    @Exclude
    public boolean isChecked() {
        return status == Status.CHECKED;
    }
    @Exclude
    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    public enum Status {
        ACTIVE, CHECKED, DELETED
    }
}

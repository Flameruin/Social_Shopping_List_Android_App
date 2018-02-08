package kinneret.shoppinglist.list_objects;//package kinneret.shoppinglist.model;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.Exclude;
//import com.google.firebase.database.IgnoreExtraProperties;
//
//import java.util.Map;
//
//@IgnoreExtraProperties
//public class ShoppingListSharing {
//    @Exclude
//    public final String sharedValue = "sharedWith";
//    @Exclude
//    public String key;
//    @Exclude
//    public Map<String,Object> sharedMap;
//
//    @SuppressWarnings("unused")
//    public ShoppingListSharing() {
//        // Default constructor required for calls to DataSnapshot.getValue(ShoppingListItem.class)
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        return obj != null && obj instanceof ShoppingListSharing && key.equals(((ShoppingListSharing) obj).key);
//    }
//
//    public void sharedListFromSnapshot(DataSnapshot snapshot)
//    {
//        sharedMap = (Map<String, Object>) snapshot.getValue();
//
//    }
//
//}

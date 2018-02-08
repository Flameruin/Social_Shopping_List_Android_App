package kinneret.shoppinglist.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import kinneret.shoppinglist.R;
import kinneret.shoppinglist.helper.Helper;

public class ItemDialogFragment extends AppCompatDialogFragment {

    private ItemDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_item, null);

        builder.setView(view)
                .setTitle(R.string.dialog_item_title)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Helper.toggleKeyboard(getActivity());
                    }
                })
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        Dialog dialog = (Dialog) dialogInterface;

                        TextInputLayout inputName = dialog.findViewById(R.id.input_item_name);
                        TextInputLayout inputDesc = dialog.findViewById(R.id.input_item_description);
                        listener.onDialogAddItem(
                                inputName.getEditText().getText().toString(),
                                inputDesc.getEditText().getText().toString()
                        );
                        Helper.toggleKeyboard(getActivity());
                    }
                });

        Dialog d = builder.create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialogInterface) {
                Helper.toggleKeyboard(getActivity());
            }
        });

        return d;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            listener = (ItemDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ShareDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    public interface ItemDialogListener {
        void onDialogAddItem(String inputName, String inputDescription);
    }

}

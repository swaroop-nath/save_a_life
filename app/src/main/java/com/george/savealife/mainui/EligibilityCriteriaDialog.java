package com.george.savealife.mainui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Switch;

import com.george.savealife.R;

public class EligibilityCriteriaDialog extends DialogFragment {
    Context context;
    private EligibilityInterface eligibilityComm;
    private int[] flags;
    private Switch switchAIDS, switchHepatitis, switchAlcohol;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        eligibilityComm = (EligibilityInterface)context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_eligibility_criteria,null);
        ListView listView = layout.findViewById(R.id.listViewDialog);

        flags = eligibilityComm.getCheckedFlags();
        switchAIDS = eligibilityComm.getSwitchAIDS();
        switchHepatitis = eligibilityComm.getSwitchHepatitis();
        switchAlcohol = eligibilityComm.getSwitchAlcohol();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout);
        AlertDialog dialog = builder.create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle("Eligibility Criteria");

        int titleDividerId = getResources().getIdentifier("titleDivider","id","android");
        View titleDivider = layout.findViewById(titleDividerId);
        if (titleDivider != null)
            titleDivider.setBackgroundResource(R.color.secondaryText);

        String[] textArray = getResources().getStringArray(R.array.eligibility_criteria);
        EligibilityAdapter adapter = new EligibilityAdapter(context, R.layout.list_item_criterion, textArray, flags);
        listView.setAdapter(adapter);

        /*
            Introduce a positive button and check if all the elements of the flags array are 1 and
            accordingly judge the eligibility
         */

        return  dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        switchAIDS.setChecked(flags[0] == 1);
        switchHepatitis.setChecked(flags[1] == 1);
        switchAlcohol.setChecked(flags[2] == 1);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        switchAIDS.setChecked(flags[0] == 1);
        switchHepatitis.setChecked(flags[1] == 1);
        switchAlcohol.setChecked(flags[2] == 1);
    }

    protected interface EligibilityInterface {
        int[] getCheckedFlags();
        Switch getSwitchAIDS();
        Switch getSwitchHepatitis();
        Switch getSwitchAlcohol();
    }

}

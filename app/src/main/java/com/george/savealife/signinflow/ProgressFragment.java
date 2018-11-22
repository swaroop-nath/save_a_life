package com.george.savealife.signinflow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.george.savealife.R;


public class ProgressFragment extends DialogFragment {
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;
    private Context context;
    private String loadingText;
    private TextView loadingTextView;

    public ProgressFragment()
    {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.activity_progress_fragment,null);
        loadingText=getArguments().getString("loading_text_key");

        loadingTextView=view.findViewById(R.id.progressFragmentText);
        loadingTextView.setText(loadingText);

        builder=new AlertDialog.Builder(context);

        builder.setView(view);
        return builder.create();
    }

}

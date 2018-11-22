package com.george.savealife.mainui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

class EligibilityAdapter extends ArrayAdapter {
    private LayoutInflater inflater;
    private String[] textFiller;
    private Context context;
    private int resource;
    private int[] flags;

    public EligibilityAdapter(@NonNull Context context, int resource, @NonNull Object[] objects, int[] flags) {
        super(context, 0, objects);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        textFiller = (String[]) objects;
        this.context = context;
        this.resource = resource;
        this.flags = flags;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        TextView eligibilityText;
        Switch toggleCriteria;
        EligibilityViewHolder eligibilityViewHolder;

        if (view == null) {
            view = inflater.inflate(resource,null);
            eligibilityViewHolder = new EligibilityViewHolder(view);
            eligibilityText = eligibilityViewHolder.getEligibilityText();
            toggleCriteria = eligibilityViewHolder.getToggleCriteria();
            view.setTag(eligibilityViewHolder);
        } else {
            eligibilityViewHolder = (EligibilityViewHolder) view.getTag();
            eligibilityText = eligibilityViewHolder.getEligibilityText();
            toggleCriteria = eligibilityViewHolder.getToggleCriteria();
        }
        toggleCriteria.setChecked(flags[position] == 1);

        eligibilityText.setText(textFiller[position]);
        toggleCriteria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((Switch) view).isChecked())
                    flags[position] = 1;
                else if (!((Switch) view).isChecked())
                    flags[position] = 0;
            }
        });

        return view;
    }
}

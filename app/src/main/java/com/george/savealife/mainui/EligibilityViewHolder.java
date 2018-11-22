package com.george.savealife.mainui;

import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import com.george.savealife.R;

class EligibilityViewHolder {
    private TextView eligibilityText;
    private Switch toggleCriteria;

    public TextView getEligibilityText() {
        return eligibilityText;
    }

    public Switch getToggleCriteria() {
        return toggleCriteria;
    }

    protected EligibilityViewHolder(View listItemLayout) {
        eligibilityText = listItemLayout.findViewById(R.id.listEligibilityCriteriaText);
        toggleCriteria = listItemLayout.findViewById(R.id.switchListItem);
    }
}

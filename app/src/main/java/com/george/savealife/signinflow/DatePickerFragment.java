package com.george.savealife.signinflow;

import android.app.Dialog;
import java.util.Calendar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.DatePickerDialog;
import android.util.Log;
import android.widget.DatePicker;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private Calendar present,dateOfBirth;
    private TransferDatePicked transferDatePicked;
    Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        transferDatePicked= (TransferDatePicked) context;
        this.context=context;
    }

    int year, month, day;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (present==null)
            present=Calendar.getInstance();
        year=present.get(Calendar.YEAR);
        month=present.get(Calendar.MONTH);
        day=present.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(this.context,this,year,month,day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int yyyy, int mm, int dd) {
        dateOfBirth=Calendar.getInstance();
        dateOfBirth.set(Calendar.YEAR,yyyy);
        dateOfBirth.set(Calendar.MONTH,mm);
        dateOfBirth.set(Calendar.DAY_OF_MONTH,dd);

        transferDatePicked.updateLabel(dateOfBirth);
    }

    interface TransferDatePicked
    {
        void updateLabel(Calendar dateOfBirth);
    }
}

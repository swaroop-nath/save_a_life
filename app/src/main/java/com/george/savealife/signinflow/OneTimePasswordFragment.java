package com.george.savealife.signinflow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.george.savealife.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;


public class OneTimePasswordFragment extends DialogFragment {
    private View layout;
    private TextInputEditText phoneNumber, oneTimePass;
    private OneTimePasswordCommunication otpComm;
    private FirebaseAuth firebaseAuth;
    private PhoneAuthCredential phoneAuthCredential;
    private Button verifyPhone, verifyMail, saveCredential;

    private LayoutInflater inflater;
    private Context context;
    private AlertDialog.Builder builder;

    private ProgressFragment progressFragment;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        otpComm = (UserDetail) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        builder = new AlertDialog.Builder(context);

        layout = inflater.inflate(R.layout.verify_phone, null);
        builder.setView(layout).
                setPositiveButton("Verify", null).
                setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        OneTimePasswordFragment.this.getDialog().cancel();
                    }
                });
        phoneNumber = layout.findViewById(R.id.verificationPhoneNumber);
        oneTimePass = layout.findViewById(R.id.oneTimePassword);

        verifyPhone = otpComm.getVerifyPhone();
        verifyMail = otpComm.getVerifyMail();
        saveCredential = otpComm.getSaveCredential();

        phoneNumber.setText(otpComm.getPhoneNumber());
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button verify = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                verify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressFragment = otpComm.getProgressFragment();
                        progressFragment.show(getFragmentManager(), "progress two");
                        firebaseAuth = otpComm.getAuthenticator();
                        phoneAuthCredential = PhoneAuthProvider.getCredential(otpComm.getVerificationID(), oneTimePass.getText().toString());
                        firebaseAuth.getCurrentUser().linkWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressFragment.dismiss();
                                    Toast.makeText(context, "Linking successful", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    verifyPhone.setVisibility(View.GONE);
                                    int TOKEN = otpComm.getToken();
                                    if (TOKEN == 1)
                                        verifyMail.setVisibility(View.VISIBLE);
                                    else
                                        saveCredential.setVisibility(View.VISIBLE);
                                } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    progressFragment.dismiss();
//                                    Log.e("Invalid AUth Exception",task.getException().getMessage());
                                    oneTimePass.setError("Invalid OTP\nPlease enter a valid OTP");
                                } else {
//                                    Log.e("Save a Life", task.getException().getMessage());
                                    progressFragment.dismiss();
                                    Toast.makeText(context, "Sorry, Server Problem\nPlease Try Again Later", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

            }
        });

        return dialog;
    }

    interface OneTimePasswordCommunication {
        String getPhoneNumber();

        FirebaseAuth getAuthenticator();

        ProgressFragment getProgressFragment();

        String getVerificationID();

        Button getVerifyPhone();

        Button getVerifyMail();

        Button getSaveCredential();

        int getToken();
    }
}

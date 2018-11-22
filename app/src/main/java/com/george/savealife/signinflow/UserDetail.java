package com.george.savealife.signinflow;


import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.george.savealife.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UserDetail extends AppCompatActivity implements DatePickerFragment.TransferDatePicked, AdapterView.OnItemSelectedListener,
        OneTimePasswordFragment.OneTimePasswordCommunication {

    public static final String DATE_OF_BIRTH = "dateOfBirth";
    public static final String BLOOD_GROUP = "bloodGroup";
    public static final String GENDER = "gender";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String DISPLAY_NAME = "displayName";
    private static final String AGE = "age";
    private static final String CITY = "city";

    private ConstraintLayout parentView;
    private TextInputLayout passwordLayout;
    private TextInputEditText firstName, lastName, emailId, phoneNumber, dateOfBirthInput, city, passwordText;
    private RadioGroup gender;
    private Spinner bloodGroup;
    private CountryCodePicker countryCode;
    private Button verifyPhone, verifyMail, saveCredential;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String email, name, fname, lname, phnumber, namearray[], providedCity, providedPassword;
    private DatePickerFragment datePickerFragment;
    private ArrayAdapter<CharSequence> adapterBloodGroup;
    private String checkedGender, selectedBloodGroup, selectedDateOfBirth;
    private Map<String, String> userProfile, userProfileMapper;
    private TextView genderTextView;
    private ProgressFragment progressFragment;
    private String countryCodeWithPlus;
    private ArrayList<UserInfo> profile;
    private String providerID;
    private int TOKEN, EMAIL_VERIFY_TOKEN, DATE_OF_BIRTH_FLAG;
    private FirebaseFirestore firebaseFirestore;
    private Bundle progressFragmentData;
    private int age;

    @Override
    protected void onStop() {
        super.onStop();
        unregister(smsReceiver);
    }

    private void unregister(SMSReceiver smsReceiver) {
        if (smsReceiver != null) {
            try {
                unregisterReceiver(smsReceiver);
            } catch (IllegalArgumentException exception) {

            }
            smsReceiver = null;
        }
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneVerifyCallback;
    private OneTimePasswordFragment otpFragment;
    private SMSReceiver smsReceiver;
    private String verificationID;

    int flag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        //Instantiating the relevant objects
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userProfile = new HashMap<>();
        userProfileMapper = new HashMap<>();
        progressFragmentData = new Bundle();

        parentView = findViewById(R.id.userDetailConstraintLayout);
        emailId = findViewById(R.id.emailId);
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        phoneNumber = findViewById(R.id.phoneNumber);
        dateOfBirthInput = findViewById(R.id.dateOfBirth);
        passwordLayout = findViewById(R.id.textInputLayout6);
        passwordText = findViewById(R.id.password);

        gender = findViewById(R.id.radioGroupGender);
        genderTextView = findViewById(R.id.textView);

        countryCode = findViewById(R.id.countryCodeSpinner);
        city = findViewById(R.id.city);
        bloodGroup = findViewById(R.id.bloodGroupSpinner);

        verifyPhone = findViewById(R.id.verifyPhone);
        verifyMail = findViewById(R.id.verifyMail);
        saveCredential = findViewById(R.id.saveCredential);

        EMAIL_VERIFY_TOKEN = 0;

        profile = (ArrayList<UserInfo>) firebaseAuth.getCurrentUser().getProviderData();
        providerID = profile.get(1).getProviderId();
        if (firebaseAuth.getCurrentUser().getPhoneNumber() != null && firebaseAuth.getCurrentUser().getEmail() != null) {
            verifyPhone.setVisibility(View.GONE);
            verifyMail.setVisibility(View.GONE);
            saveCredential.setVisibility(View.VISIBLE);
        }
        if (providerID.equals("phone")) {
            TOKEN = 0;
            verifyPhone.setVisibility(View.GONE);
            passwordLayout.setVisibility(View.VISIBLE);
            verifyMail.setVisibility(View.VISIBLE);
        } else if (providerID.equals("password")) {
            TOKEN = 1;
            verifyPhone.setVisibility(View.VISIBLE);
        } else {
            TOKEN = 2;
            verifyPhone.setVisibility(View.VISIBLE);
        }


        //Creating the callback for phone number verification
        phoneVerifyCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                progressFragment.dismiss();
                if (otpFragment != null)
                    otpFragment.dismiss();
                showProgressFragment("Undergoing Instant Verification...");
                firebaseAuth.getCurrentUser().linkWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressFragment.dismiss();
                            Toast.makeText(UserDetail.this, "Successfully linked phone number with the account", Toast.LENGTH_SHORT).show();
                            if (TOKEN == 1)
                                verifyMail.setVisibility(View.VISIBLE);
                            else if (TOKEN == 2)
                                saveCredential.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(UserDetail.this, task.getException().getMessage() + " onVerificationCompleted", Toast.LENGTH_LONG).show();
                            progressFragment.dismiss();
                        }
                        Log.e("Save A Life", "Unregistering Receiver in OnVerificationCompleted");
                        unregister(UserDetail.this.smsReceiver);
                    }
                });
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressFragment.dismiss();
                Toast.makeText(UserDetail.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("Save A Life", "Unregistering in OnVerificationFailed");
                unregister(UserDetail.this.smsReceiver);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                progressFragment.dismiss();
                showOTPFragment();
                UserDetail.this.verificationID = verificationId;
            }
        };

        //Getting the already known data if any
        email = firebaseUser.getEmail();
        name = firebaseUser.getDisplayName();
        phnumber = firebaseUser.getPhoneNumber();

        //Configuring the spinner indicating blood group and setting the listener
        adapterBloodGroup = ArrayAdapter.createFromResource(this, R.array.blood_group, android.R.layout.simple_spinner_dropdown_item);
        bloodGroup.setAdapter(adapterBloodGroup);
        bloodGroup.setOnItemSelectedListener(this);

        //Setting the already known values if any
        if (email != null)
            emailId.setText(email);
        if (name != null) {
            namearray = name.split(" ");
            fname = namearray[0];
            lname = namearray[1];
            firstName.setText(fname);
            lastName.setText(lname);
        }
        if (phnumber != null) {
            phoneNumber.setText(phnumber);
        }

        countryCodeWithPlus = countryCode.getSelectedCountryCodeWithPlus();


        //Getting user input when the user clicks verify mail
        verifyPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int flag = 1;
                fname = firstName.getText().toString();
                if (TextUtils.isEmpty(fname)) {
                    firstName.setError("First Name complusory");
                    flag = 0;
                }
                lname = lastName.getText().toString();
                if (TextUtils.isEmpty(lname)) {
                    lastName.setError("Last Name compulsory");
                    flag = 0;
                }
                name = fname + " " + lname;
                email = emailId.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    emailId.setError("Email Id compulsory");
                    flag = 0;
                } else if (!isEmailValid(email)) {
                    emailId.setError("Enter a valid email-id");
                    flag = 0;
                }
                phnumber = phoneNumber.getText().toString();
                if (TextUtils.isEmpty(phnumber)) {
                    phoneNumber.setError("Phone Number compulsory");
                    flag = 0;
                }
                UserDetail.this.checkedGender = getGender();
                if (checkedGender == null) {
                    genderTextView.setError("Please select a gender");
                    flag = 0;
                }
                providedCity = city.getText().toString();
                if (TextUtils.isEmpty(providedCity)) {
                    city.setError("Please provide your City/Town");
                    flag = 0;
                }
                if (selectedDateOfBirth == null) {
                    dateOfBirthInput.setError("Please provide your birthday");
                    flag = 0;
                }

                if (flag != 0) {
                    Log.e("Save A Life", "Registering SMS Receiver");
                    smsReceiver = new SMSReceiver();
                    registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

                    phnumber = countryCodeWithPlus + phnumber;

                    //Showing the loading fragment
                    showProgressFragment("Just A Moment...");

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phnumber, 60, TimeUnit.SECONDS, UserDetail.this, phoneVerifyCallback);

                    userProfile.put(DISPLAY_NAME, name);
                    userProfile.put(PHONE_NUMBER, phnumber);
                    userProfile.put(GENDER, checkedGender);
                    userProfile.put(BLOOD_GROUP, selectedBloodGroup);
                }
            }
        });

        verifyMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int flag = 1;
                fname = firstName.getText().toString();
                if (TextUtils.isEmpty(fname)) {
                    firstName.setError("First Name compulsory");
                    flag = 0;
                }
                lname = lastName.getText().toString();
                if (TextUtils.isEmpty(lname)) {
                    lastName.setError("Last Name compulsory");
                    flag = 0;
                }
                name = fname + " " + lname;
                email = emailId.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    emailId.setError("Email Id compulsory");
                    flag = 0;
                } else if (!isEmailValid(email)) {
                    emailId.setError("Enter a valid email-id");
                    flag = 0;
                }
                providedPassword = passwordText.getText().toString();
                if (TextUtils.isEmpty(providedPassword)) {
                    passwordText.setError("Please provide a password for the account");
                }
                phnumber = phoneNumber.getText().toString();
                if (TextUtils.isEmpty(phnumber)) {
                    phoneNumber.setError("Phone Number compulsory");
                    flag = 0;
                }
                UserDetail.this.checkedGender = getGender();
                if (checkedGender == null) {
                    genderTextView.setError("Please select a gender");
                    flag = 0;
                }
                providedCity = city.getText().toString();
                if (TextUtils.isEmpty(providedCity)) {
                    city.setError("Please provide your City/Town");
                    flag = 0;
                }
                if (selectedDateOfBirth == null) {
                    dateOfBirthInput.setError("Please provide your birthday");
                    flag = 0;
                }
                if (flag == 1 && UserDetail.this.TOKEN == 0) {
                    if (EMAIL_VERIFY_TOKEN == 0) {
                        showProgressFragment("Just A Moment...");
                        firebaseUser.updateEmail(emailId.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.e("Email Linking", "Updated email successfully");
                                    firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(UserDetail.this, "Verification link sent to " + emailId.getText().toString(), Toast.LENGTH_SHORT).show();
                                            } else
                                                Log.e("Verification Mail", task.getException().getMessage());
                                            firebaseUser = firebaseAuth.getCurrentUser();
                                            firebaseUser.updatePassword(providedPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(UserDetail.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                                                        Log.e("Password Updation", "Updated");
                                                        verifyMail.setVisibility(View.GONE);
                                                        saveCredential.setVisibility(View.VISIBLE);
                                                    } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                                        progressFragment.dismiss();
                                                        passwordLayout.setError("Weak Password\nShould be atleast 6 characters long");
                                                        EMAIL_VERIFY_TOKEN = 1;
                                                        verifyMail.setText("Validate Password");
                                                        Toast.makeText(UserDetail.this, "Too Short Password", Toast.LENGTH_LONG).show();
                                                    }
                                                    UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder();
                                                    builder.setDisplayName(name);
                                                    UserProfileChangeRequest request = builder.build();
                                                    firebaseUser = firebaseAuth.getCurrentUser();
                                                    firebaseUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Log.e("Profile Updation", "Updated");
                                                            progressFragment.dismiss();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    progressFragment.dismiss();
                                    emailId.setError("Please enter a valid E-mail");
                                } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    progressFragment.dismiss();
                                    emailId.setError("Email Id already in use");
                                } else {
                                    progressFragment.dismiss();
                                    Toast.makeText(UserDetail.this, "Server Error, Please try again later.", Toast.LENGTH_LONG).show();
                                    Log.e("Email Linking", task.getException().getMessage());
                                }
                            }
                        });
                        userProfile.put(DISPLAY_NAME, name);
                        userProfile.put(PHONE_NUMBER, phnumber);
                        userProfile.put(GENDER, checkedGender);
                        userProfile.put(BLOOD_GROUP, selectedBloodGroup);
                    } else if (EMAIL_VERIFY_TOKEN == 1) {
                        showProgressFragment("Just A Moment...");
                        firebaseAuth.getCurrentUser().updatePassword(providedPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressFragment.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(UserDetail.this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
                                    Log.e("Password Updation", "Updated");
                                    verifyMail.setVisibility(View.GONE);
                                    saveCredential.setVisibility(View.VISIBLE);
                                    EMAIL_VERIFY_TOKEN = 1;
                                } else if (task.getException() instanceof FirebaseAuthWeakPasswordException) {
                                    progressFragment.dismiss();
                                    passwordLayout.setError("Weak Password\nShould be atleast 6 characters long");
                                    Toast.makeText(UserDetail.this, "Too Short Password", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                } else if (flag == 1 && UserDetail.this.TOKEN == 1) {
                    showProgressFragment("Just A Moment...");
                    firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressFragment.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(UserDetail.this, "Verification link sent to " + email, Toast.LENGTH_SHORT).show();
                                verifyMail.setVisibility(View.GONE);
                                saveCredential.setVisibility(View.VISIBLE);
                            } else {
                                Log.e("Verification TOKEN=1", task.getException().getMessage());
                                Toast.makeText(UserDetail.this, "Server Error, Please try again later.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


        saveCredential.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DATE_OF_BIRTH_FLAG == 0)
                    setSnackbar(parentView, "Please set a proper Date of Birth");
                else {
                    showProgressFragment("Just A Moment...");
                    userProfile.put(DATE_OF_BIRTH, selectedDateOfBirth);
                    userProfile.put(AGE, Integer.valueOf(age).toString());
                    providedCity = providedCity.toLowerCase();
                    firebaseAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                firebaseFirestore = FirebaseFirestore.getInstance();
                                firebaseFirestore.collection("Cities").document(providedCity).collection("BloodGroup").
                                        document(userProfile.get(BLOOD_GROUP)).collection("Users").document(firebaseAuth.getCurrentUser().getUid()).
                                        set(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            userProfileMapper.put(CITY, providedCity);
                                            userProfileMapper.put(BLOOD_GROUP, selectedBloodGroup);
                                            firebaseFirestore.collection("mapper").document(firebaseAuth.getCurrentUser().getUid()).set(userProfileMapper)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            progressFragment.dismiss();
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(UserDetail.this, "Successfully added data", Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            } else {
                                                                progressFragment.dismiss();
                                                                Log.e("Inside Adding Data", task.getException().getMessage());
                                                            }
                                                        }
                                                    });
                                        } else {
                                            progressFragment.dismiss();
                                            Log.e("Adding data", task.getException().getMessage());
                                        }
                                    }
                                });
                            } else
                                Log.e("Reloading user", task.getException().getMessage());
                        }
                    });
                }
            }
        });

        countryCode.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                UserDetail.this.countryCodeWithPlus = countryCode.getSelectedCountryCodeWithPlus();
            }
        });
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void updateLabel(Calendar dateOfBirth) {
        age = UserDetail.this.computeAge(dateOfBirth);
        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, new Locale("en", "IN"));
        selectedDateOfBirth = simpleDateFormat.format(dateOfBirth.getTime());
        dateOfBirthInput.setText(simpleDateFormat.format(dateOfBirth.getTime()));
    }

    private int computeAge(Calendar dob) {
        int Age = 0;
        Calendar now = Calendar.getInstance();
        if (dob.after(now))
            setSnackbar(parentView, "Please set a proper Date of Birth");
        else {
            UserDetail.this.DATE_OF_BIRTH_FLAG = 1;
            int year1 = now.get(Calendar.YEAR);
            int year2 = dob.get(Calendar.YEAR);
            Age = year1 - year2;
            int month1 = now.get(Calendar.MONTH);
            int month2 = dob.get(Calendar.MONTH);
            if (month2 > month1) {
                Age--;
            } else if (month1 == month2) {
                int day1 = now.get(Calendar.DAY_OF_MONTH);
                int day2 = dob.get(Calendar.DAY_OF_MONTH);
                if (day2 > day1) {
                    Age--;
                }
            }
        }
        return Age;
    }

    private void setSnackbar(View parentView, String snackTitle) {
        Snackbar snackbar = Snackbar.make(parentView, snackTitle, Snackbar.LENGTH_LONG);
        TextView txtv = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        txtv.setGravity(Gravity.CENTER_HORIZONTAL);
        snackbar.show();
    }

    public void showDatePickerDialog(View v) {
        datePickerFragment = new DatePickerFragment();
        datePickerFragment.show(getSupportFragmentManager(), "date picker");
    }

    public String getGender() {
        switch (gender.getCheckedRadioButtonId()) {
            case R.id.maleRadioButton:
                return "Male";
            case R.id.femaleRadioButton:
                return "Female";
            case R.id.otherRadioButton:
                return "Other";
            default:
                return null;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long row) {
        selectedBloodGroup = (String) adapterView.getItemAtPosition(pos);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseAuth.signOut();
    }

    private void showOTPFragment() {
        otpFragment = new OneTimePasswordFragment();
        otpFragment.show(getSupportFragmentManager(), "one time password");
    }

    private void showProgressFragment(String loadingText) {
        progressFragment = new ProgressFragment();
        progressFragmentData.putString("loading_text_key", loadingText);
        progressFragment.setArguments(progressFragmentData);
        progressFragment.show(getSupportFragmentManager(), "progress fragment");
    }

    @Override
    public String getPhoneNumber() {
        return phnumber;
    }

    @Override
    public FirebaseAuth getAuthenticator() {
        return firebaseAuth;
    }

    @Override
    public ProgressFragment getProgressFragment() {
        return progressFragment;
    }

    @Override
    public String getVerificationID() {
        return UserDetail.this.verificationID;
    }

    @Override
    public Button getVerifyPhone() {
        return UserDetail.this.verifyPhone;
    }

    @Override
    public Button getVerifyMail() {
        return UserDetail.this.verifyMail;
    }

    @Override
    public Button getSaveCredential() {
        return UserDetail.this.saveCredential;
    }

    @Override
    public int getToken() {
        return UserDetail.this.TOKEN;
    }

}

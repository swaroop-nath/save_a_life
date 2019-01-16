package com.george.savealife.mainui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.george.savealife.R;
import com.george.savealife.notificationflow.Manager;
import com.george.savealife.signinflow.UserDetail;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity implements EligibilityCriteriaDialog.EligibilityInterface, AdapterView.OnItemClickListener,  CompoundButton.OnCheckedChangeListener{

    private FirebaseAuth mAuth,auth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mUser,user;
    private DocumentReference documentReference;
    private FirebaseFirestore firebaseFirestore;
    private EligibilityCriteriaDialog eligibilityCriteriaDialog;
    private String displayName,emailId,phoneNumber;
    private static final int RC_SIGN_IN = 123;
    private int[] flags;
    private Switch switchAIDS, switchHepatitis, switchAlcohol;
    private Spinner bloodGroup;
    private String selectedBloodGroup;
    private ScrollView parentView;
    private Map<String, Object> userProfile;
    private FirebaseFunctions tempFunc;

    private static final String CITY = "city";
    private static final String BLOOD_GROUP = "bloodGroup";
    private static final String DATE_OF_BIRTH = "dateOfBirth";
    public static final String GENDER = "gender";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String DISPLAY_NAME = "displayName";
    private static final String AGE = "age";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.signInActivityMenuSignOut:
                AuthUI.getInstance().signOut(this);
                startActivityForResult(
                        AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders
                                (Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build())).
                                setTheme(R.style.LoginTheme).setLogo(R.mipmap.logo).
                                setIsSmartLockEnabled(false).build(),
                        RC_SIGN_IN);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
        Manager.getInstance().setContext(getApplicationContext());
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                //Open the main Screen
                auth = FirebaseAuth.getInstance();
                user = auth.getCurrentUser();

                displayName= user.getDisplayName();
                emailId= user.getEmail();
                phoneNumber= user.getPhoneNumber();

                if (displayName==null||emailId==null||phoneNumber==null)
                {
                    Intent userDetailIntent=new Intent(SignInActivity.this,UserDetail.class);
                    startActivity(userDetailIntent);
                }

            } else if (resultCode == RESULT_CANCELED) {
                //Exit the app
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
        setContentView(R.layout.activity_sign_in);

        userProfile = new HashMap<>();
        tempFunc = FirebaseFunctions.getInstance();

        flags = new int[7];
        mAuth = FirebaseAuth.getInstance();
        parentView = findViewById(R.id.signInActivityParentView);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUser = firebaseAuth.getCurrentUser();
                if (mUser != null) {
                    Toast.makeText(SignInActivity.this, "You are logged in", Toast.LENGTH_SHORT).show();
                    loadComponents();
                } else {
                    Toast.makeText(SignInActivity.this, "You are logged out", Toast.LENGTH_SHORT).show();
                    destroyComponents();
                    Log.e("Auth State Listener","Components destroyed, starting activity...");
                    try {
                        startActivityForResult(
                                AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders
                                        (Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build(),
                                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                                new AuthUI.IdpConfig.GoogleBuilder().build())).
                                        setTheme(R.style.LoginTheme).setLogo(R.mipmap.logo).
                                        setIsSmartLockEnabled(false).build(),
                                RC_SIGN_IN);
                    } catch (Exception exp) {
                        Log.e("SIGN_IN_EXCEPTION",exp.getMessage());
                    }
                }
            }
        };
    }

    private void loadComponents() {
        parentView.setVisibility(View.VISIBLE);

        if (userProfile == null)
            userProfile = new HashMap<>();

        switchAIDS = findViewById(R.id.switchAIDS);
        switchHepatitis = findViewById(R.id.switchHepatitis);
        switchAlcohol = findViewById(R.id.switchAlcohol);
        bloodGroup = findViewById(R.id.signInBloodGroupSpinner);

        ArrayAdapter adapterBloodGroup = ArrayAdapter.createFromResource(this, R.array.blood_group, android.R.layout.simple_spinner_dropdown_item);
        bloodGroup.setAdapter(adapterBloodGroup);

        switchAIDS.setOnCheckedChangeListener(this);
        switchHepatitis.setOnCheckedChangeListener(this);
        switchAlcohol.setOnCheckedChangeListener(this);

        if (userProfile.isEmpty()) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            documentReference = firebaseFirestore.collection("mapper").document(mAuth.getCurrentUser().getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshotMapper = task.getResult();
                        Map<String, Object> userProfileMapper = snapshotMapper.getData();
                        documentReference = firebaseFirestore.collection("Cities").document((String) userProfileMapper.get(CITY)).
                                collection("BloodGroup").document((String) userProfileMapper.get(BLOOD_GROUP)).
                                collection("Users").document(mAuth.getCurrentUser().getUid());
                        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot snapshotMain = task.getResult();
                                    userProfile.putAll(snapshotMain.getData());
                                    Toast.makeText(SignInActivity.this, (String) userProfile.get(DISPLAY_NAME), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void destroyComponents() {
        parentView.setVisibility(View.GONE);
        userProfile.clear();
    }

    public void launchMoreCriteriaDialog(View view) {
        eligibilityCriteriaDialog = new EligibilityCriteriaDialog();
        eligibilityCriteriaDialog.show(getSupportFragmentManager(),"eligibility_criteria_dialog");
    }

    @Override
    public int[] getCheckedFlags() {
        return flags;
    }

    @Override
    public Switch getSwitchAIDS() {
        return switchAIDS;
    }

    @Override
    public Switch getSwitchHepatitis() {
        return switchHepatitis;
    }

    @Override
    public Switch getSwitchAlcohol() {
        return switchAlcohol;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long row) {
        selectedBloodGroup = (String) adapterView.getItemAtPosition(pos);
    }

    public void startProcess(View view) {
        Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        notifyUsers("zero").addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignInActivity.this, task.getResult(), Toast.LENGTH_SHORT).show();
                }
                else if (!task.isSuccessful()) {
                    Toast.makeText(SignInActivity.this, "Task Not Successfull", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(SignInActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        int id = compoundButton.getId();

        switch (id) {
            case R.id.switchAIDS:
                if (isChecked)
                    flags[0] = 1;
                else
                    flags[0] = 0;
                break;
            case R.id.switchHepatitis:
                if (isChecked)
                    flags[1] = 1;
                else
                    flags[1] = 0;
                break;
            case R.id.switchAlcohol:
                if (isChecked)
                    flags[2] = 1;
                else
                    flags[2] = 0;
        }
    }
    private Task<String> notifyUsers(String text) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("token", text); //Put Firebase Instance ID token here
        data.put("city", true); //Put the city in which the blood is needed, plus write code for two more fields to incorporate locality and blood group which is needed

        return tempFunc
                .getHttpsCallable("helloWorld")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
    }
}

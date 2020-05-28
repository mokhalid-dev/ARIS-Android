package h.eit.dr.android.aris_project;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class ArisRequestPage extends AppCompatActivity {

    private  Button btn_logout,btn_source,btn_destination,btn_mat,btn_submit;
    private ImageView img_arrow1,img_arrow2;
    private  FirebaseAuth mAuth;
    private  FirebaseAuth.AuthStateListener mAuthListener;
    private  TextView txt_hello;
    private  View toDisplayInDialog;
    private  AlertDialog.Builder sourcePopup,destinationPopup,matPopup;
    private  RadioGroup radioGroupLocation,radioGroupMat;
    private  RadioButton myCheckedMatButton,myCheckedSourceButton,myCheckedDestinationButton;
    private  int radioIndexSource,radioIndexDestination,radioIndexMaterial;
    private  boolean boolS,boolD,boolM;
    String g_Aris_User;
    int g_Aris_cnt;
    int g_Aris_pend;
    private FirebaseDatabase g_Aris_fbDb = FirebaseDatabase.getInstance();
    private DatabaseReference g_Aris_rootRef = g_Aris_fbDb.getReference();
    private DatabaseReference g_Aris_childRef = g_Aris_rootRef.child("ARIS_User_Requests");
    private DatabaseReference g_Aris_childCntRef = g_Aris_rootRef.child("ARIS_Requests_Summary").child("ARIS_Total_Count");
    private DatabaseReference g_Aris_childPendRef = g_Aris_rootRef.child("ARIS_Requests_Summary").child("ARIS_Pending_Count");
    int txtcolor=Color.WHITE;

    public String g_Aris_get_msg_token = "";
    public String g_Aris_set_msg_token;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String G_ARIS_MSG = "visibility";


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        // fetching count and pending requests from database
        g_Aris_childCntRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                g_Aris_cnt = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });

        g_Aris_childPendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                g_Aris_pend = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        });


    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aris_request_page);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        btn_source = (Button) findViewById(R.id.btn_source);
        btn_destination = (Button) findViewById(R.id.btn_destination);
        btn_mat = (Button) findViewById(R.id.btn_mat);
        btn_submit = (Button) findViewById(R.id.btn_submit);
        btn_logout = (Button) findViewById(R.id.btn_logout);
        txt_hello = (TextView) findViewById(R.id.txt_hello);
        img_arrow1= (ImageView) findViewById(R.id.img_arrow1);
        img_arrow2=(ImageView) findViewById(R.id.img_arrow2);

        img_arrow1.setVisibility(View.INVISIBLE);
        img_arrow2.setVisibility(View.INVISIBLE);
        txt_hello.setText("Hello,...");


        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(ArisRequestPage.this,LoginPage.class));
                }
            }
        };



        //Button Onclicks


        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut(); }});

        btn_source.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sourceButtonClicked(); }});

        btn_destination.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                destinationButtonClicked(); }});

        btn_mat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                materialButtonClicked(); }});

        btn_submit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                submitButtonClick();
                submitButtonClickFirebase();

            }
        });


        g_Aris_User = (String) mAuth.getCurrentUser().getDisplayName();
        txt_hello.setText("Hello, " + g_Aris_User);


       // Toast.makeText(getApplicationContext(),"Before condition",Toast.LENGTH_SHORT).show();

        if(g_Aris_get_msg_token.equals("")){
           // Toast.makeText(getApplicationContext(),"Inside condition",Toast.LENGTH_SHORT).show();
            g_Aris_set_msg_token = (String) FirebaseInstanceId.getInstance().getToken();
            saveData();

        }
        loadData();
        if(g_Aris_get_msg_token.equals("")){
            g_Aris_get_msg_token=(String) FirebaseInstanceId.getInstance().getToken();
        }

    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(G_ARIS_MSG ,g_Aris_set_msg_token);
        editor.apply();
        Log.d("SharedPrefCheck","In save Data");
    }
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        g_Aris_get_msg_token = sharedPreferences.getString(G_ARIS_MSG,"");
        Log.d("SharedPrefCheck","In load Data");
    }

    public void submitButtonClickFirebase(){

        // sending values to database
        String l_Aris_key_test = "ARIS_Request_" + (g_Aris_cnt + 1);
        g_Aris_childRef.child(l_Aris_key_test).child("aris_source").setValue(radioIndexSource + 1);
        g_Aris_childRef.child(l_Aris_key_test).child("aris_destination").setValue(radioIndexDestination + 1);
        g_Aris_childRef.child(l_Aris_key_test).child("aris_material").setValue(radioIndexMaterial + 1);
        g_Aris_childRef.child(l_Aris_key_test).child("aris_req_fetched").setValue("0");
        g_Aris_childRef.child(l_Aris_key_test).child("aris_req_status").setValue("0");
        g_Aris_childRef.child(l_Aris_key_test).child("req_id").setValue("request" + (g_Aris_cnt + 1));
        g_Aris_childRef.child(l_Aris_key_test).child("message_token").setValue((String) FirebaseInstanceId.getInstance().getToken());

        g_Aris_childCntRef.setValue(g_Aris_cnt + 1);
        g_Aris_childPendRef.setValue(g_Aris_pend + 1);


    }

    public void submitButtonClick(){
        if(boolS && boolM && boolD)
        {
            if(btn_source.getText()==btn_destination.getText()){
                Toast.makeText(getApplicationContext(),"Source and Destination cannot be same",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getApplicationContext(),"Click Submit",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ArisRequestPage.this, Finalpage.class));}

        }else
        {
            Toast.makeText(getApplicationContext(),"Select the required Source,Destination and Material",Toast.LENGTH_SHORT).show();
        }
    }

    public void sourceButtonClicked(){

        toDisplayInDialog=getLayoutInflater().inflate(R.layout.radiogroup,null);
        sourcePopup = new AlertDialog.Builder(ArisRequestPage.this);
        sourcePopup.setTitle("Source Location");
        sourcePopup.setIcon(R.drawable.logorequest);
        sourcePopup.setView(toDisplayInDialog);
        sourcePopup.setNeutralButton("Cancel",null);
        sourcePopup.setPositiveButton("Set Source", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rGSourceClick();
            }
        });
        sourcePopup.show();



    }

    public void destinationButtonClicked(){

        toDisplayInDialog=getLayoutInflater().inflate(R.layout.radiogroup,null);
        destinationPopup = new AlertDialog.Builder(ArisRequestPage.this);
        destinationPopup.setTitle("Destination Location");
        destinationPopup.setIcon(R.drawable.logorequest);
        destinationPopup.setView(toDisplayInDialog);
        destinationPopup.setNeutralButton("Cancel",null);
        destinationPopup.setPositiveButton("Set Destination", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rGDestinationClick();
            }
        });
        destinationPopup.show();


    }

    public void rGSourceClick(){
        radioGroupLocation = (RadioGroup)
                toDisplayInDialog.findViewById(R.id.radioGroupLocation);
        int radioGroupSourceId = radioGroupLocation.getCheckedRadioButtonId();
        myCheckedSourceButton = (RadioButton)
                toDisplayInDialog.findViewById(radioGroupSourceId);
        int index = radioGroupLocation.indexOfChild(myCheckedSourceButton);
        radioIndexSource=index;
        switch(index) {

            case 0:
                myCheckedSourceButton.isChecked();
                btn_source.setText("Room 1");
                boolS=true;
                btn_source.setBackgroundResource(R.drawable.btn_gradient2);
                btn_source.setTextColor(txtcolor);
                img_arrow1.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(),"Room 1 selected",Toast.LENGTH_SHORT).show();
                break;

            case 1:
                myCheckedSourceButton.isChecked();
                btn_source.setText("Room 2");
                boolS=true;
                btn_source.setBackgroundResource(R.drawable.btn_gradient2);
                btn_source.setTextColor(txtcolor);
                img_arrow1.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(),"Room 2 selected",Toast.LENGTH_SHORT).show();
                break;

            case 2:
                myCheckedSourceButton.isChecked();
                btn_source.setText("Room 3");
                boolS=true;
                btn_source.setBackgroundResource(R.drawable.btn_gradient2);
                btn_source.setTextColor(txtcolor);
                img_arrow1.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(),"Room 3 selected",Toast.LENGTH_SHORT).show();
                break;

        }


    }
    public void rGDestinationClick(){
        radioGroupLocation = (RadioGroup)
                toDisplayInDialog.findViewById(R.id.radioGroupLocation);
        int radioGroupId = radioGroupLocation.getCheckedRadioButtonId();
        myCheckedDestinationButton = (RadioButton)
                toDisplayInDialog.findViewById(radioGroupId);
        int index = radioGroupLocation.indexOfChild(myCheckedDestinationButton);
        radioIndexDestination=index;

        switch(index) {

            case 0:
                myCheckedDestinationButton.isChecked();
                btn_destination.setText("Room 1");
                boolD=true;
                btn_destination.setBackgroundResource(R.drawable.btn_gradient2);
                btn_destination.setTextColor(txtcolor);
                img_arrow2.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(),"Room 1 selected",Toast.LENGTH_SHORT).show();
                break;

            case 1:
                myCheckedDestinationButton.isChecked();
                btn_destination.setText("Room 2");
                boolD=true;
                btn_destination.setBackgroundResource(R.drawable.btn_gradient2);
                btn_destination.setTextColor(txtcolor);
                img_arrow2.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(),"Room 2 selected",Toast.LENGTH_SHORT).show();
                break;

            case 2:
                myCheckedDestinationButton.isChecked();
                btn_destination.setText("Room 3");
                boolD=true;
                btn_destination.setBackgroundResource(R.drawable.btn_gradient2);
                btn_destination.setTextColor(txtcolor);
                img_arrow2.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(),"Room 3 selected",Toast.LENGTH_SHORT).show();
                break;

        }
    }


    public void materialButtonClicked(){

        toDisplayInDialog=getLayoutInflater().inflate(R.layout.radiogroupmaterial,null);
        matPopup = new AlertDialog.Builder(ArisRequestPage.this);
        matPopup.setTitle("Choose Material");
        matPopup.setIcon(R.drawable.logorequest);
        matPopup.setView(toDisplayInDialog);
        matPopup.setNeutralButton("Cancel",null);
        matPopup.setPositiveButton("Set Material", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rGMatClick();
            }
        });
        matPopup.show();


    }


    public void rGMatClick(){
        radioGroupMat = (RadioGroup)
                toDisplayInDialog.findViewById(R.id.radioGroupMat);
        int radioGroupId = radioGroupMat.getCheckedRadioButtonId();
        myCheckedMatButton = (RadioButton)
                toDisplayInDialog.findViewById(radioGroupId);
        int index = radioGroupMat.indexOfChild(myCheckedMatButton);
        radioIndexMaterial=index;

        switch(index) {

            case 0:
                myCheckedMatButton.isChecked();
                boolM=true;
                btn_mat.setBackgroundResource(R.drawable.btn_gradient2);
                btn_mat.setTextColor(txtcolor);
                btn_mat.setText("Material A");
                Toast.makeText(getApplicationContext(),"Material A selected",Toast.LENGTH_SHORT).show();
                break;

            case 1:
                myCheckedMatButton.isChecked();
                boolM=true;
                btn_mat.setBackgroundResource(R.drawable.btn_gradient2);
                btn_mat.setTextColor(txtcolor);
                btn_mat.setText("Material B");
                Toast.makeText(getApplicationContext(),"Material B selected",Toast.LENGTH_SHORT).show();
                break;

            case 2:
                myCheckedMatButton.isChecked();
                boolM=true;
                btn_mat.setBackgroundResource(R.drawable.btn_gradient2);
                btn_mat.setTextColor(txtcolor);
                btn_mat.setText("Material C");
                Toast.makeText(getApplicationContext(),"Material C selected",Toast.LENGTH_SHORT).show();
                break;

        }

    }


}



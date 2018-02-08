package kinneret.shoppinglist.user_interface.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import kinneret.shoppinglist.R;

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private Button googleSignOut;
    private SignInButton googleSignIn;

    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        googleSetUp();

    }

    @SuppressWarnings("EmptyMethod")
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.googleBtnLogin:
                signIn();
                break;
            case R.id.googleBtnLogout:
                if(googleApiClient.isConnected())
                    signOut();
                else
                    Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
                break;
        }

    }
    private void googleSetUp()
    {
        googleSignOut = findViewById(R.id.googleBtnLogout);
        googleSignIn = findViewById(R.id.googleBtnLogin);
        googleSignIn.setOnClickListener(this);
        googleSignOut.setOnClickListener(this);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions).build();
    }


    private void signIn() //check for google login
    {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent,REQ_CODE);
    }

    private void signOut()
    {

        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Toast.makeText(getApplicationContext(), R.string.disconnect, Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQ_CODE) //google signing
        {
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            GoogleSignInAccount account = googleSignInResult.getSignInAccount();

            if(googleSignInResult.isSuccess()) // if got connection go to next activity
            {
                String name = account.getDisplayName();
                String email = account.getEmail();

                Intent intent = new Intent(this, ActivityShoppingList.class); //ActivityShoppingListItem
                intent.putExtra("p_name", name); //send the name to WatchList activity
                intent.putExtra("p_email", email);//send the email to WatchList activity
                startActivity(intent); //start Watch List activity
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}




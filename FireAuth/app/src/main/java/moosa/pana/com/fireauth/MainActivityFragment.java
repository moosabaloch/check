package moosa.pana.com.fireauth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private Handler handler;
    private Firebase firebase;
    private EditText emailInput, passwordInput;
    private Button login, createAccount, logout;
    private Runnable runnable;
    private LoginButton facebookLoginButton;
    private AccessTokenTracker accessTokenTracker;
    private CallbackManager callbackManager;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        firebase = new Firebase("https://fireauthentication.firebaseio.com");
        emailInput = (EditText) view.findViewById(R.id.emailId);
        passwordInput = (EditText) view.findViewById(R.id.password);
        login = (Button) view.findViewById(R.id.loginButton);
        createAccount = (Button) view.findViewById(R.id.createAccountButton);
        logout = (Button) view.findViewById(R.id.logoutButton);
        facebookLoginButton = (LoginButton) view.findViewById(R.id.authButton);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                authResultHandler();
                handler.postDelayed(this, 5000);

            }
        };
        runnable.run();
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();

            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loggedIn();
                authResultHandler();
            }
        });
        //////////////////FACEBOOK////////////////////
        facebookLoginButton.setFragment(this);
        facebookLoginButton.setReadPermissions(Arrays.asList("email"));
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                updateWithToken();
            }
        };
        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker.startTracking();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void updateWithToken() {

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            Log.d("Token From Facebook is=", accessToken.getToken());
            firebase.authWithOAuthToken("facebook", accessToken.getToken(), new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(final AuthData authData) {
                    Log.d("Handler", "UserLoggedIn" + authData.getUid());
                    Log.d("onAuthState", authData.getProviderData().toString());
                    final String uuid = authData.getUid();
                    final String displayname = authData.getProviderData().get("displayName").toString();
                    final String imageURL = authData.getProviderData().get("profileImageURL").toString();
                    final String email = authData.getProviderData().get("email").toString() != null ? authData.getProviderData().get("email").toString() : "Email Not Available";
                    ///////////////////SAVE USER DATA////////////////////////
                    Firebase checkUser = firebase.child("users");

                    checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild(uuid)) {
                                Map<String, String> userData = new HashMap<>();
                                userData.put("id", uuid);
                                userData.put("name", displayname);
                                userData.put("image", imageURL);
                                userData.put("mail", email);
                                firebase.child("users").child(authData.getUid()).setValue(userData);
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
                   ////////////////////////////////////////////////////////


                    Log.d("User Id - ", uuid + "");
                    Log.d("Display Name - ", displayname + "");
                    Log.d("ImageURL - ", imageURL + "");
                    Log.d("Email - ", email + "");
                    Bundle bundle = new Bundle();
                    bundle.putString("uid", uuid);
                    bundle.putString("name", displayname);
                    bundle.putString("dp", imageURL);
                    bundle.putString("email", email);

                    FragmentManager fragmentManager2 = getFragmentManager();
                    FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                    LoggedInFragment fragment2 = LoggedInFragment.newInstance(bundle);
                    fragmentTransaction2.addToBackStack("xyz");
                    fragmentTransaction2.hide(MainActivityFragment.this);
                    fragmentTransaction2.add(android.R.id.content, fragment2);
                    fragmentTransaction2.commit();

                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    Log.d("Handler", "User Not Logged In" + firebaseError.getMessage());

                }
            });
        } else {
            firebase.unauth();
        }
    }

    private void createNewAccount() {
        firebase.createUser(emailInput.getText().toString(), passwordInput.getText().toString(), new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> stringObjectMap) {

                firebase.child("users").child(stringObjectMap.get("uid").toString()).setValue(stringObjectMap.toString());
                Log.d("SIGN-UP Complete", "Created a new User");
                Toast.makeText(getActivity(), "SignUp Succeeded", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onError(FirebaseError firebaseError) {
                Log.d("SIGN-UP Error", "Cannot create a new User");
                Toast.makeText(getActivity(), "Error SignUp", Toast.LENGTH_LONG).show();
            }
        });
    }

    /*
        private void onSessionStateChange(Session session, SessionState state, Exception exception) {
            if (state.isOpened()) {
                Log.i("FACEBOOK", "Logged in...");
            } else if (state.isClosed()) {
                Log.i("FACEBOOK", "Logged out...");
            }
        }
    */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        accessTokenTracker.stopTracking();
        handler.removeCallbacks(runnable);
    }

    private void authResultHandler() {
        firebase.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    Log.d("Handler", "UserLoggedIn" + authData.getUid());
                    Log.d("onAuthState", authData.getProviderData().toString());

                } else {
                    Log.d("Handler", "User Not Logged In");
                }
            }
        });
    }

    private void logout() {
        Log.d("LoggedOut", "Invoked");

        firebase.unauth();
        Toast.makeText(getActivity(), "LoggedOut", Toast.LENGTH_LONG).show();
    }

    private void loggedIn() {
        Log.d("LoggedIn", "Invoked");

        firebase.authWithPassword(emailInput.getText().toString(), passwordInput.getText().toString(), new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                String a = authData.getProvider();
                String b = authData.getUid();
                Log.d("OnAuthenticated", "LoggedIn");
                // Authentication just completed successfully :)
                Map<String, String> map = new HashMap<String, String>();
                map.put("provider", authData.getProvider());
                map.put("details", authData.getProviderData().toString());
                if (authData.getProviderData().containsKey("displayName")) {
                    map.put("displayName", authData.getProviderData().get("displayName").toString());
                }

                firebase.child("users").child(authData.getUid()).setValue(map);

                /////////////////////////////////////////////////////
                Toast.makeText(getActivity(), "Provider=" + a + " UserID=" + b, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.d("OnAuthenticateError", "Error");

                Toast.makeText(getActivity(), firebaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

 /*       FragmentManager fragmentManager2 = getFragmentManager();
        FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
        LoggedInFragment fragment2 = new LoggedInFragment();
        fragmentTransaction2.addToBackStack("xyz");
        fragmentTransaction2.hide(this);
        fragmentTransaction2.add(android.R.id.content, fragment2);
        fragmentTransaction2.commit();
   */
    }

    private void fbLogin() {

    }

}

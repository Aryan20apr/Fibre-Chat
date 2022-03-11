package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseDatabase=FirebaseDatabase.getInstance();//Main access point for our database
        //Uisng this access point we get a reference to a specific part of the database
        mMessagesDatabaseReference=mFirebaseDatabase.getReference()//This gives reference to the root node
                .child("messages");//Interested in messages portion of the database
        //Attach a child event listener object to this reference to listen to have
        //your code triggered whenever changes occur on the messages node.

        mUsername=ANONYMOUS;
        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);
        //Initialize message listView and its adapter
        List<FriendlyMessage> friendlyMessages=new ArrayList<>();
        mMessageAdapter=new MessageAdapter(this,R.layout.item_message,friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
            }
        });
        // Enable Send button when there's text to send
        //TextWatcher prevents to press send button for empty messages
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.i(TAG+"@#","Inside onTextChanged of TextWatcher ");
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                    Log.i(TAG+"@#","Inside onTextChanged of TextWatcher and enabled the button ");
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG+"@#","Inside onClick of ClickListener of Send Button ");
                // TODO: Send messages on click
                //This object has all the keys that we’ll store as a message in the realtime database.
                FriendlyMessage friendlyMessage=new FriendlyMessage(mMessageEditText.getText().toString(),mUsername,null);
                mMessagesDatabaseReference.push().setValue(friendlyMessage);
                // Clear input box
                mMessageEditText.setText("");
            }
        });
        mChildEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) //DataSnapshot object contains data from the Firebase database
            //at the specific location at the exact time the listener is triggered. Here it will always contain the message that has been added.
            {
                Log.i(TAG+"@#","Inside onChildAdded() ");
                /**This method gets called whenever a new message is inserted into
                * the messages list.
                * It is also triggered for every child message in the list when the listener
                * is first attached and for all future children when the listener is still active
                * This means when we attach a listener, for every child message that already exists in the list,
                * the code on the list, the code in the method will be called.*/
                FriendlyMessage friendMessage=snapshot.getValue(FriendlyMessage.class);//By passing the class The code will deserialize the message from the database into our plain FriendlyMessage object
                    //This works because the plain FriendlyMessage object has the exact fiels that matches the fields taht are in our messages object in the database.
                mMessageAdapter.add(friendMessage);
                //With that we added a listener to our messages list.
                //When a new message is adds, it triggers onChildAdded, which in turn takes the newly added message, converts is into a friendly message object and finally adds it to the adapter
                //This will display it in the list view
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            /**Called when the existing message is changed*/
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
/**Called when an existing message is deleted*/
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                /**Called if one of our messages changed position in the list */
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
             /**Indicates that some sort of error occured when you are trying to make changes
              * If it i*/
            }
        };
        mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        //Reference defines what we are listening to
        //This will only trigger when one of the children of the messages node changes.
        //Some other data added outside of messages node would not trigger the listener.

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}



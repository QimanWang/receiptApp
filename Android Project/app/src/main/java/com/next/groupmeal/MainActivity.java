package com.next.groupmeal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

    //Declare all the variable
    private  FirebaseAuth mAuth;
    private LinearLayout createGroupLayout;     //hold the layout of create group
    private Button addMemberButton;             //hold the addMember button
    private EditText groupNameText;             //hold the group name
    private TextView nameGroup;                 //hold the group name to be display in form of textView
    private ListView mListView;                 //hold the list where the contact and phone number will be displayed
    private Cursor mCursor;                     //hold the cursor: need for fetching the contact information
    private LinearLayout HomePage;              //hold the layout of the home page
    ArrayAdapter<Pair<String, String>> adapter ;              //hold the container of the contact
	  private GroupWrap currentGroup;                //hold the current group the user created
	  private PermissionManager permissionManager;//hold the manager to request permission if the user doesn't have it

	ArrayList<Pair<String, String>> listcontact = new ArrayList<>();   //An arraylist that will hold the contact that was fecth from the phone
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        //References all the variable to GUI

        createGroupLayout = (LinearLayout) findViewById(R.id.creategroupAction);
        addMemberButton = (Button) findViewById(R.id.add_member_button);
        groupNameText = (EditText) findViewById(R.id.groupname);
        nameGroup = (TextView) findViewById(R.id.nameGroup);
        mListView = (ListView) findViewById(R.id.list_contact);
        HomePage = (LinearLayout) findViewById(R.id.home_page);

		adapter = new ArrayAdapter<Pair<String, String>>(this, 0)
		{
			@NonNull
			@Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
			{
				final Pair<String, String> contact = getItem(position);

				if (convertView == null)
				{
					LayoutInflater lf = LayoutInflater.from(MainActivity.this);
					convertView = lf.inflate(R.layout.number_list_item, parent, false);
				}

				TextView titleView = convertView.findViewById(R.id.nliTitleView);
				titleView.setText(contact.first);

				TextView textView = convertView.findViewById(R.id.nliTextView);
				textView.setText(contact.second);

				convertView.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						sendMessageTo(contact.second);
					}
				});
				return convertView;
			}
		};
		mListView.setAdapter(adapter);

		StartLoginPage();           //When the user launch the app, it should prompt him/her to login with email and password


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Intent intent = new Intent(MainActivity.this, OcrCaptureActivity.class);
                startActivity(intent);

            }
        });


		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		permissionManager = new PermissionManager(this);

		startService(new Intent(this, ListenerService.class));
	}

	@SuppressLint({"MissingPermission", "HardwareIds"})
	private void sendMessageTo(String second)
	{
		FirebaseDatabase db = FirebaseDatabase.getInstance();

		DatabaseReference ref = db.getReference(properNum(second));

		ref = ref.push();
		ref.setValue(new ListenerService.GroupInvite(currentGroup.groupID, getNumber("Some number...")));
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		permissionManager.onRequestPermissionResult(requestCode, permissions, grantResults);
	}

	//StartLoginPage() method launch the login page when the app is fire up
	private void StartLoginPage()
	{
		Intent intent = new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);


	}

	@Override
	protected void onStart()
	{
		super.onStart();
        //StartLoginPage();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);

	}

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
//--------------------------------------------------------------- CREATE GROUP
	//CreateGroup() method perform all the action that allow the user to create the group

	private void CreateGroup()
	{

		createGroupLayout.setVisibility(View.VISIBLE);

		//if addMemberButton is pressedt

		addMemberButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String groupname = groupNameText.getText().toString().trim();

				if (groupname.isEmpty()) {
					groupNameText.setError(getString(R.string.error_field_required));
					groupNameText.requestFocus();
				} else {
					groupNameText.setVisibility(View.GONE);
					hideKeyboard(MainActivity.this);
					final FirebaseDatabase db = FirebaseDatabase.getInstance();

					DatabaseReference ref = db.getReference("groups").push();
					GroupWrap group = new GroupWrap(ref.getKey(), getNumber("Some Number..."), groupname);
					ref.setValue(group);
					currentGroup = group;
					getContactInfo();
				}
			}
		});
	}

	//The hideKeyboard() hide the keybard when the user enter the group's name

	public static void hideKeyboard(Activity activity) {
		if (activity != null) {
			InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

			if (activity.getCurrentFocus() != null && inputMethodManager != null) {
				inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
				inputMethodManager.hideSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
			}
		}
	}

	//The method getContactInfo() fetch all the contact and display it on the screen
	private void getContactInfo()
	{
		if (permissionManager.hasContactPermission())
		{
			String name;
			String phonenumber;
			mCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

			listcontact.clear();
			while (mCursor.moveToNext())
			{
				name = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				phonenumber = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

				Pair<String, String> contact = new Pair<>(name, phonenumber);
				listcontact.add(contact);
			}
			mCursor.close();
			Collections.sort(listcontact, new Comparator<Pair<String, String>>()
			{
				@Override
				public int compare(Pair<String, String> o1, Pair<String, String> o2)
				{
					return o1.first.compareToIgnoreCase(o2.first);
				}
			});
			adapter.clear();
			adapter.addAll(listcontact);
		} else
		{
			permissionManager.askContactPermission();
		}
	}
	//----------------------------------------------------------------------- END CREATE THE USER

	//----------------------------------------------------------------------LOG OUT THE USER ACTION
	private void LogOutUser()
	{
		FirebaseAuth.getInstance().signOut();
		finish();
		Intent intent = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(intent);
	}
	//-------------------------------------------------------------------------END LOG OUT USER

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public String getNumber(String def)
	{
		String n = getNumber();
		return n == null ? def : n;
	}

	@SuppressLint({"MissingPermission", "HardwareIds"})
	@Nullable
	public String getNumber()
	{
		String from = null;
		if (permissionManager.hasNumberPermission())
		{
			TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			if (tMgr != null) from = tMgr.getLine1Number();
		}
		return from;
	}

	public String properNum(String number)
	{
		return number.replaceAll("[^\\d.]", "");
	}

    //-----------------------------------------------------------------------------------WELCOME PAGE CONTENT
    private void WelcomePageContent() {}

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.create_group) {
            // Handle the creation of the group here

            CreateGroup();
        } else if (id == R.id.join_group) {

        } else if (id == R.id.activity) {

        } else if (id == R.id.help) {

        } else if (id == R.id.setting) {

        } else if (id == R.id.share) {

        }else if (id == R.id.sent){

        }else if (id == R.id.logout)
        {
            LogOutUser();
        }else if (id == R.id.home_page)
        {
            WelcomePageContent();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

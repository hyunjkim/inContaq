package nyc.c4q.jonathancolon.inContaq.contactlist.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nyc.c4q.jonathancolon.inContaq.R;
import nyc.c4q.jonathancolon.inContaq.contactlist.AlertDialogCallback;
import nyc.c4q.jonathancolon.inContaq.contactlist.PicassoHelper;
import nyc.c4q.jonathancolon.inContaq.contactlist.PreCachingLayoutManager;
import nyc.c4q.jonathancolon.inContaq.contactlist.adapters.ContactListAdapter;
import nyc.c4q.jonathancolon.inContaq.contactlist.fragments.SplashScreenFragment;
import nyc.c4q.jonathancolon.inContaq.contactlist.model.Contact;
import nyc.c4q.jonathancolon.inContaq.utlities.DeviceUtils;
import nyc.c4q.jonathancolon.inContaq.utlities.NameSplitter;
import nyc.c4q.jonathancolon.inContaq.utlities.PermissionChecker;
import nyc.c4q.jonathancolon.inContaq.utlities.sqlite.ContactDatabaseHelper;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class ContactListActivity extends AppCompatActivity implements AlertDialogCallback<String>,
        ContactListAdapter.Listener {

    public static final String PARCELLED_CONTACT = "Parcelled Contact";
    private RecyclerView recyclerView;
    private TextView importContactsTV;
    private AlertDialog InputContactDialogObject;
    private List<Contact> contactList;
    private SQLiteDatabase db;
    private String name = "";
    private Context context;
    Handler handler;
    Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        Stetho.initializeWithDefaults(this);



        PermissionChecker permissionChecker = new PermissionChecker(this, getApplicationContext());
        permissionChecker.checkPermissions();



        fragment = new SplashScreenFragment();

        context = getApplicationContext();
        initViews();
        setupRecyclerView();
        refreshRecyclerView();
        buildEnterContactDialog(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.activity_contact_list, fragment)
                .commit();

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                removeSplash();
            }
        }, 4000);
//        checkServiceCreated();
    }

    private void removeSplash() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .remove(fragment)
                .commit();
    }


    private void initViews() {
        importContactsTV = (TextView) findViewById(R.id.import_contacts);
        importContactsTV.setOnClickListener(v -> {
            ImportContacts importContacts = new ImportContacts(context);
            importContacts.getContactsFromContentProvider();
            refreshRecyclerView();
        });

        FloatingActionButton addContactFab = (FloatingActionButton) findViewById(R.id.fab_add_contact);
        addContactFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContactListActivity.this.openEditor();
            }
        });
    }

    private void refreshRecyclerView() {
        ContactDatabaseHelper dbHelper = ContactDatabaseHelper.getInstance(this);
        db = dbHelper.getWritableDatabase();
        contactList = contactArrayList();
        ContactListAdapter adapter = (ContactListAdapter) recyclerView.getAdapter();
        sortContacts();
        adapter.setData(contactList);
    }

    private void openEditor() {
        InputContactDialogObject.show();
    }

    private void setupRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(context));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new ContactListAdapter(this, this));
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setDrawingCacheEnabled(true);
    }

    // POP UP for Entering a new Contact
    private void buildEnterContactDialog(final AlertDialogCallback<String> callback) {

        final EditText input = new EditText(ContactListActivity.this);

        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);
        confirmBuilder.setTitle(R.string.add_contact);
        confirmBuilder.setMessage(R.string.enter_name);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        confirmBuilder.setView(input);


        confirmBuilder.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                name = input.getText().toString();
                callback.alertDialogCallback(name);
            }
        });

        confirmBuilder.setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });
        InputContactDialogObject = confirmBuilder.create();
    }

    private void sortContacts() {
        List<Contact> contacts = contactList;
        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact o1, Contact o2) {
                return o1.getFirstName().compareToIgnoreCase(o2.getFirstName());
            }
        });
    }

    private void preloadContactListImages() {
        PicassoHelper pHelper = new PicassoHelper(context);
        for (int i = 0; i < contactList.size(); i++) {
            if (contactList.get(i).getBackgroundImage() != 0) {
                pHelper.preloadImages(contactList.get(i).getBackgroundImage());
            }
            if (contactList.get(i).getContactImage() != 0) {
                pHelper.preloadImages(contactList.get(i).getContactImage());
            }
        }
    }

    @Override
    public void alertDialogCallback(String userInput) {
        name = userInput;
        if (!isEmptyString(name)) {
            NameSplitter nameSplitter = new NameSplitter();
            String[] splitName = nameSplitter.splitFirstAndLastName(name);

            Contact contact = new Contact();
            contact.setFirstName(splitName[0]);
            contact.setLastName(splitName[1]);
            cupboard().withDatabase(db).put(contact);
            refreshRecyclerView();
        } else {
            Toast.makeText(ContactListActivity.this, R.string.enter_name,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isEmptyString(String string) {
        return string.trim().length() < 0;
    }

    @Override
    public void onContactClicked(Contact contact) {
        Intent intent = new Intent(this, ContactViewPagerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PARCELLED_CONTACT, Parcels.wrap(contact));
        this.startActivity(intent);
    }

    @Override
    public void onContactLongClicked(Contact contact) {
        cupboard().withDatabase(db).delete(contact);
        refreshRecyclerView();
    }

//    public void checkServiceCreated() {
//        if (!ContactNotificationService.hasStarted) {
//            System.out.println("Starting service...");
//            Intent intent = new Intent(getApplicationContext(), ContactNotificationService.class);
//            startService(intent);
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        refreshRecyclerView();
    }

    public static ArrayList<Contact> contactArrayList() {
        ArrayList<Contact> contactDemoList = new ArrayList<>();
        Contact mom = new Contact("Mama", "Colon", R.drawable.flowerbackground, R.drawable.jonsmom);
        Contact romeo = new Contact("Romeo", "Santos", R.drawable.romeobg, R.drawable.romeoprofile);
        Contact ramona = new Contact("Ramona", "Harrison", R.drawable.eightbitcity, R.drawable.ramona);
        Contact rob = new Contact("Robert", "Li", R.drawable.robbackground, R.drawable.robertli);
        Contact john = new Contact("John", "Gomez", R.drawable.johnbg, R.drawable.johngomezprofile);
        Contact erick = new Contact("Erick", "Chang", R.drawable.erickbackground, R.drawable.erickprofile);
        Contact jose = new Contact("Jose", "Garcia", R.drawable.cityblackandwhite, R.drawable.joseprofile);
        Contact david = new Contact("David", "Morant", R.drawable.davidbackground, R.drawable.davidprofile);
        Contact jj = new Contact("Jonathan", "Johnson", R.drawable.jjbackground, R.drawable.jonathanjohnson);
        Contact girlKnewYork = new Contact("Girl", "KnewYork", R.drawable.girlknewbackground, R.drawable.girlknewyork);


        contactDemoList.add(mom);
        contactDemoList.add(romeo);
        contactDemoList.add(ramona);
        contactDemoList.add(rob);
        contactDemoList.add(erick);
        contactDemoList.add(john);
        contactDemoList.add(jose);
        contactDemoList.add(jj);
        contactDemoList.add(david);
        contactDemoList.add(girlKnewYork);

        return contactDemoList;
    }

}



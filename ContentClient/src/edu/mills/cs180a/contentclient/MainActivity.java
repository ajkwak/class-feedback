
package edu.mills.cs180a.contentclient;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 * An {@code Activity} that enables the user to pick a contact, then retrieve
 * comments meant for the contact.
 * 
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class MainActivity extends Activity {
    private static final int PICK_REQUEST = 1;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button fetchOneButton = (Button) findViewById(R.id.fetchOneButton);
        fetchOneButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, 
                        ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(i, PICK_REQUEST);
            }
        });
    }

    // Code adapted from section 11.17 of The Android Cookbook by Ian Darwin (O'Reilly).
    // Returns the empty string if record has no email address.
    private String getContactEmail(Uri contactUri) {
        // Get id of contact.
        Cursor contactCursor = getContentResolver().query(contactUri, null, null, null, null);
        int columnIndexForId = contactCursor.getColumnIndex(ContactsContract.Contacts._ID);
        if (!contactCursor.moveToFirst()) {
            contactCursor.close();
            return "";
        }
        int contactId = contactCursor.getInt(columnIndexForId);

        // Now that we have an id, we can request the email address.
        Cursor emailsCursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, 
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + contactId,
                null, null);

        // Get the (first) email address.
        String email;
        if (emailsCursor.moveToFirst()) {
            email = emailsCursor.getString(emailsCursor.getColumnIndex(
                    ContactsContract.CommonDataKinds.Email.ADDRESS));
            if (emailsCursor.moveToNext()) {
                Log.d(TAG, "Additional emails ignored.");
            }
        } else {
            email = "";
            Log.w(TAG, "No email address found for contact.");
        }
        
        // Clean up before returning.
        contactCursor.close();
        emailsCursor.close();
        return email;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_REQUEST && resultCode == RESULT_OK) {
            // Extract email address.
            String email = getContactEmail(data.getData());
            Toast.makeText(this, "Found email: " + email, Toast.LENGTH_LONG).show();

            // Request comments.
            ContentResolver resolver = getContentResolver();
            Uri uri = Uri.parse(CommentContentProvider.CONTENT_URI + "/" + email);
            String[] projection = { "content" };  // desired columns
            Cursor cursor = resolver.query(uri, projection, null, null, null);
            if (cursor == null) {
                Log.e(TAG, "Cursor returned by content resolver is null.");
                return;
            }
            while (cursor.moveToNext()) {
                String s = cursor.getString(0);
                Log.d(TAG, "Found comment: " + s);
            }
            cursor.close();
        } else {
            Log.w(TAG, "Did not pick contact.");
        }
    }
}

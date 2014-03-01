package edu.mills.cs180a.classfeedback;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Creates and upgrades a database for storing {@link Comment}s.
 * Clients should access the database through {@link CommentsDataSource}.
 *
 * @author ellen.spertus@gmail.com (Ellen Spertus)
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "comments.db";
    private static final int DATABASE_VERSION = 2;

    /**
     * The name of the table storing comments.
     */
    static final String TABLE_COMMENTS = "comments";

    /**
     * The name of the column containing a unique id.
     */
    static final String COLUMN_ID = "_id";

    /**
     * The name of the column containing the index of the recipient in
     * {@link Person#everyone}.
     */
    static final String COLUMN_RECIPIENT = "recipient";

    /**
     * The name of the column containing the comment's content.
     */
    static final String COLUMN_CONTENT = "content";

    /**
     * The index of the column containing a unique id.
     */
    static final int COLUMN_ID_POS = 0;

    /**
     * The index of the column containing the comment recipient.
     */
    static final int COLUMN_RECIPIENT_POS = 1;

    /**
     * The index of the column containing the comment's content.
     */
    static final int COLUMN_CONTENT_POS = 2;

    private static final String DATABASE_CREATE = "create table "
            + TABLE_COMMENTS + "("
            + COLUMN_ID  + " integer primary key autoincrement, "
            + COLUMN_RECIPIENT + " text unique not null, "
            + COLUMN_CONTENT + " text not null);";

    public MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String tmpCommentsTable = TABLE_COMMENTS + "_tmp";
        // Rename the old comments table.
        db.execSQL("alter table " + TABLE_COMMENTS + " rename to " + tmpCommentsTable + ";");

        // Create the new comments table.
        onCreate(db);

        // Populate the new comments table with the contents of the old comments table. If there are
        // multiple comments associated with an e-mail, concatenate them, separating the comments
        // with a single space.
        Cursor cursor = db.query(tmpCommentsTable, new String[] {
                COLUMN_RECIPIENT,
                "Group_Concat(" + COLUMN_CONTENT + ", ' ')" }, null, null,
                COLUMN_RECIPIENT, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ContentValues values = new ContentValues();
            String recipient = cursor.getString(0);
            String content = cursor.getString(1);
            values.put(COLUMN_RECIPIENT, recipient);
            values.put(COLUMN_CONTENT, content);
            db.insert(MySQLiteOpenHelper.TABLE_COMMENTS, null, values);
            cursor.moveToNext();
        }
        cursor.close();

        db.execSQL("DROP TABLE " + tmpCommentsTable + ";");
    }
}

package sqlbrite.demos.yy.com.sqlbrite.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Creator： Chanry
 * Date：2016/12/13
 * Time: 19:39
 * <p/>
 * Description:
 */
public class TestOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    public TestOpenHelper(Context context, String databaseName) {
        super(context, databaseName, null, DATABASE_VERSION);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL(TestA.CREATE_TABLE);
        db.execSQL(TestB.CREATE_TABLE);
    }

    @Override public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

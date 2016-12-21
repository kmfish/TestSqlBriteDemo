package sqlbrite.demos.yy.com.sqlbrite.db;

import android.database.sqlite.SQLiteOpenHelper;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import rx.schedulers.Schedulers;

/**
 * Created by lijun3 on 2016/11/6.
 */

public class SqlBriteFactory {

    private static final String TAG = "SqlBrite";
    private static SqlBriteFactory sInstance;

    private SqlBrite sSqlBrite;

    public static SqlBriteFactory getInstance() {
        if (sInstance == null) {
            synchronized (SqlBriteFactory.class) {
                if (sInstance == null) {
                    sInstance = new SqlBriteFactory();
                }
            }
        }
        return sInstance;
    }

    private SqlBriteFactory() {

    }

    public final SqlBrite getSqlBrite() {
        synchronized (this) {
            if (sSqlBrite == null) {
                sSqlBrite = new SqlBrite.Builder().logger(new SqlBrite.Logger() {
                    @Override
                    public void log(String message) {

                    }
                }).build();

            }

            return sSqlBrite;
        }
    }

    BriteDatabase db;
    public final BriteDatabase wrapDatabase(SQLiteOpenHelper helper) {
        if (db == null) {
            db = getSqlBrite().wrapDatabaseHelper(helper, Schedulers.io());
            db.setLoggingEnabled(false);
        }
        return db;
    }
}

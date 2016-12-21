package sqlbrite.demos.yy.com.sqlbrite;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.QueryObservable;
import com.squareup.sqlbrite.SqlBrite;
import com.squareup.sqldelight.SqlDelightStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import sqlbrite.demos.yy.com.sqlbrite.db.BriteQueryObservableFactory;
import sqlbrite.demos.yy.com.sqlbrite.db.SqlBriteFactory;
import sqlbrite.demos.yy.com.sqlbrite.db.TestA;
import sqlbrite.demos.yy.com.sqlbrite.db.TestAB;
import sqlbrite.demos.yy.com.sqlbrite.db.TestB;
import sqlbrite.demos.yy.com.sqlbrite.db.TestOpenHelper;
import sqlbrite.demos.yy.com.testsqlbrite.R;

import static com.squareup.sqlbrite.SqlBrite.Query.mapToList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SQLiteOpenHelper mTestHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTestHelper = new TestOpenHelper(getBaseContext(), TAG);
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadData();
    }

    public void add(View view) {
        insertA();
    }

    public void load(View view) {
        insertB();
    }

    Subscription subscribe;

    public void loadData() {
        subscribe = queryTestAAndB(BriteQueryObservableFactory.build()
        ).subscribeOn(Schedulers.immediate()
        ).observeOn(AndroidSchedulers.mainThread()
        ).subscribe(new Subscriber<List<TestAB>>() {
            @Override
            public void onCompleted() {
                Log.i(TAG, "onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "onCompleted " + e);
            }

            @Override
            public void onNext(List<TestAB> testABs) {
                Log.i(TAG, "result onNext size:" + testABs.size());
            }
        });

//        queryTestAAndB(QueryObservableFactory.build()
//        ).subscribeOn(Schedulers.immediate()
//        ).observeOn(AndroidSchedulers.mainThread()
//        ).subscribe(new Subscriber<List<TestAB>>() {
//            @Override
//            public void onCompleted() {
//                Log.i("testflag2", "onCompleted");
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.i("testflag2", "onCompleted " + e);
//            }
//
//            @Override
//            public void onNext(List<TestAB> testABs) {
//                Log.i("testflag2", "onNext size:" + testABs.size());
//            }
//        });

//        final QueryObservableFactory factory = QueryObservableFactory.build();
//        queryTestAAndB(factory
//        ).flatMap(new Func1<List<TestAB>, Observable<List<TestAB>>>() {
//            @Override
//            public Observable<List<TestAB>> call(List<TestAB> testABs) {
//
//                List<Long> uids = new ArrayList<>();
//                for (TestAB testAB : testABs) {
//                    uids.add(testAB.id);
//                }
//
//                return getQueryB(factory, uids);
//            }
//        })
//        .subscribeOn(Schedulers.immediate()
//        ).observeOn(AndroidSchedulers.mainThread()
//        ).subscribe(new Subscriber<List<TestAB>>() {
//            @Override
//            public void onCompleted() {
//                Log.i("testflag3", "onCompleted");
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.i("testflag3", "onCompleted " + e);
//            }
//
//            @Override
//            public void onNext(List<TestAB> testABs) {
//                Log.i("testflag3", "onNext size:" + testABs.size());
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTestHelper.close();
        if (subscribe != null) {
            subscribe.unsubscribe();
        }
    }

    private void initData() {
        BriteDatabase db = SqlBriteFactory.getInstance().wrapDatabase(mTestHelper);

        db.delete(TestA.TABLE_NAME, null, null);
        db.delete(TestB.TABLE_NAME, null, null);

        db.insert(TestA.TABLE_NAME, TestA.FACTORY.marshal()
                .uid(100)
                .name("我是100")
                .asContentValues());

        db.insert(TestB.TABLE_NAME, TestB.FACTORY.marshal()
                .id(1)
                .uid(100)
                .name("我是B的1号")
                .asContentValues());
        db.insert(TestB.TABLE_NAME, TestB.FACTORY.marshal()
                .id(2)
                .uid(101)
                .name("我是B的2号")
                .asContentValues());
        db.insert(TestB.TABLE_NAME, TestB.FACTORY.marshal()
                .id(3)
                .uid(105)
                .name("我是B的3号")
                .asContentValues());
    }

    int index = 100;

    private void insertA() {
        BriteDatabase db = SqlBriteFactory.getInstance().wrapDatabase(mTestHelper);
        db.insert(TestA.TABLE_NAME, TestA.FACTORY.marshal()
                .uid(++index)
                .name("我是A的" + index)
                .asContentValues());
    }

    int id = 5;

    private void insertB() {
        BriteDatabase db = SqlBriteFactory.getInstance().wrapDatabase(mTestHelper);
        db.insert(TestB.TABLE_NAME, TestB.FACTORY.marshal()
                .uid(index)
                .id(++id)
                .name("我是B的" + id)
                .asContentValues());
    }

    private Observable<List<TestAB>> queryTestAAndB(final BriteQueryObservableFactory factory) {
        return getQueryA().flatMap(
                new Func1<List<Long>, Observable<List<TestAB>>>() {
                    @Override
                    public Observable<List<TestAB>> call(List<Long> longs) {
                        return getQueryB(factory, longs);

//                        return fillABWrap(this, longs);
//                        return getAllA().flatMap(new Func1<List<TestA>, Observable<List<TestAB>>>() {
//                            @Override
//                            public Observable<List<TestAB>> call(List<TestA> testAs) {
//                                getQueryB(this, null);
//                                return null;
//                            }
//                        });

                    }
                }
        );
    }

    private Observable<List<TestA>> getAllA() {
        Log.i(TAG, "getAllA");
        BriteDatabase db = SqlBriteFactory.getInstance().wrapDatabase(mTestHelper);
        Set<String> tables = new HashSet<>(2);
        tables.add(TestA.TABLE_NAME);
//        tables.add(TestB.TABLE_NAME);
        final QueryObservable observable = db.createQuery(tables, TestA.SELECT_ALL);

        return observable.doOnNext(new Action1<SqlBrite.Query>() {
            @Override
            public void call(SqlBrite.Query query) {
                Log.i(TAG, "A changed " + observable.hashCode());
            }

        }).lift(mapToList(new Func1<Cursor, TestA>() {
            @Override
            public TestA call(Cursor cursor) {
                Log.i(TAG, "A mapToList cursor:" + cursor.hashCode());
                return TestA.FACTORY.select_allMapper().map(cursor);
            }
        }));
    }

    private Observable<List<Long>> getQueryA() {
        return getAllA().flatMap(new Func1<List<TestA>, Observable<List<Long>>>() {
            @Override
            public Observable<List<Long>> call(List<TestA> testAs) {
                Log.i(TAG, "getQueryA " + testAs);
                return getUids(testAs);
            }
        });
    }

    private Observable<List<Long>> getUids(List<TestA> list) {
//        Log.i(TAG, "getUids size:" + list.size());
        return Observable.from(list).map(new Func1<TestA, Long>() {
            @Override
            public Long call(TestA testA) {
                return testA.uid();
            }
        }).toList();
    }

    private Observable<List<TestAB>> fillABWrap(@NonNull final BriteQueryObservableFactory factory, final List<Long> uidList) {
        return Observable.just(2).flatMap(new Func1<Integer, Observable<List<TestAB>>>() {
            @Override
            public Observable<List<TestAB>> call(Integer integer) {
                return getQueryB(factory, uidList);
            }
        });
    }

    private Observable<List<TestAB>> getQueryB(@NonNull BriteQueryObservableFactory factory, List<Long> uidList) {
        long[] uids = new long[uidList.size()];
        for (int i = 0; i < uidList.size(); i++) {
            uids[i] = uidList.get(i);
        }

        BriteDatabase db = SqlBriteFactory.getInstance().wrapDatabase(mTestHelper);
        SqlDelightStatement query = TestB.FACTORY.select_all(uids);

        Set<String> tables = new HashSet<>(2);
//        tables.add(TestA.TABLE_NAME);
        tables.add(TestB.TABLE_NAME);
//        Observable<SqlBrite.Query> records = db.createQuery(
//                tables, query.statement, query.args)
//                ;

        final QueryObservable records = factory.createQuery(db, tables, query.statement, query.args);

        Log.i(TAG, "getQueryB:" + uidList.size());

        return records.doOnNext(new Action1<SqlBrite.Query>() {
            @Override
            public void call(SqlBrite.Query query) {
//                Log.i(TAG, "B changed " + records.hashCode());
            }
        }).lift(mapToList(new Func1<Cursor, TestB>() {
            @Override
            public TestB call(Cursor cursor) {
//                Log.i(TAG, "B mapToList cursor:" + cursor.hashCode());
                return TestB.SELECT_ALL_MAPPER.map(cursor);
            }
        })).map(new Func1<List<TestB>, List<TestAB>>() {
            @Override
            public List<TestAB> call(List<TestB> testBs) {
                List<TestAB> resultList = new ArrayList<>(testBs.size());
                for (TestB b : testBs) {
                    TestAB testAB = new TestAB(b.name(), b.id());
                    resultList.add(testAB);
                }
                return resultList;
            }

        });
    }
}

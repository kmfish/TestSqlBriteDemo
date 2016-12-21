package sqlbrite.demos.yy.com.sqlbrite.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.QueryObservable;
import com.squareup.sqlbrite.SqlBrite;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;

/**
 * Created by lijun3 on 2016/12/16.
 *
 * 该类的主要意义在于，对SqlBrite中BriteDatabase.createQuery()得到的QueryObservable的一个封装和集中管理。
 *
 * 业务背景：
 * SqlBrite 的queryObservable 其实默认是和db建立 了一条连接，这样当多个db查询的queryObservable通过操作符组合的时候，
 * 如A.flatMap(B)，A、B分别和a、b table建立了连接，这样将来table变动时会触发A、B重新执行query，并发射数据。
 * 坑的点在于，每次调用briteDatabase.createQuery 就创建了一个queryObservable的实例，这个实例就和DB有关联了。
 * 多次调用就创建了多个和DB有关联的Observable实例。出现问题的场景：
 *   1、A多次变动，导致下游的B多次执行回调，实则创建了多个Observable和DB关联；
 *   2、当B table变动时，这N个Observable均会重新执行查询（分别以创建时的query 参数去查询），从而会导致多个不同的查询结果发射到最后的订阅者处，从而导致数据混乱，错误。
 *
 * 归纳为：在一个订阅链内，应该只能保持有一个和DB建立连接的Observable。
 * 通过针对一个订阅链使用同一个BriteQueryObservableFactory实例来管理其内部的queryObservable的订阅。
 *
 * Samples：
 *
 * final BriteQueryObservableFactory factory = BriteQueryObservableFactory.build();
 * queryObservableA.flatMap(new Func1<String, Observable<String>>() {
        @Override
        public Observable<String> call(String s) {
            return getQueryB(factory);
        }
   }).flatMap(new Func1<String, Observable<String>>() {
        @Override
        public Observable<String> call(String s) {
            return getQueryC(factory);
        }
    });

    private QueryObservable getQueryB(factory) {
        return factory.createQuery(....);
    }

    private QueryObservable getQueryC(factory) {
        return factory.createQuery(....);
    }


 * 上述代码就是一个典型的多个queryObservable 组合调用的场景，所以使用了同一个BriteQueryObservableFactory实例来管理维护其内部的DB订阅关系。
 * 在组合一个或多个DB queryObservable来实现自己的Observable时，factory参数需要由外部调用者传递进来，因为只有当最终订阅者订阅时创建的BriteQueryObservableFactory
 * 才是正确的，针对这次具体的订阅链。
 */

public class BriteQueryObservableFactory {

    private static final String TAG = "QueryObservableFactory";

    private final Map<Object, Subscription> mObjectSubscriptionMap = new ConcurrentHashMap<>();

    public static BriteQueryObservableFactory build() {
        return new BriteQueryObservableFactory();
    }

    private BriteQueryObservableFactory() {
    }

    public QueryObservable createQuery(@NonNull BriteDatabase briteDatabase,
                                       @NonNull final String table, @NonNull String sql,
                                       @NonNull String... args) {
        final QueryObservable queryObservable = briteDatabase.createQuery(table, sql , args);
        final Object ctx = getLastObservableStackTraceElement();
        return null == ctx ? queryObservable : proxy(ctx, queryObservable);
    }

    public QueryObservable createQuery(@NonNull BriteDatabase database,
                                       @NonNull final Iterable<String> tables, @NonNull String sql,
                                       @NonNull String... args) {
        final QueryObservable queryObservable = database.createQuery(tables, sql , args);
        final Object ctx = getLastObservableStackTraceElement();
        return null == ctx ? queryObservable : proxy(ctx, queryObservable);
    }

    private QueryObservable proxy(@NonNull final Object context, final QueryObservable source) {

        final Observable<SqlBrite.Query> observable = Observable.create(new Observable.OnSubscribe<SqlBrite.Query>() {
            @Override
            public void call(Subscriber<? super SqlBrite.Query> subscriber) {
                removeSubscription(context);

                addSubscription(context, subscriber);

                source.unsafeSubscribe(subscriber);
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                removeSubscription(context);
            }
        });

        return new QueryObservable(new Observable.OnSubscribe<SqlBrite.Query>() {
            @Override
            public void call(Subscriber<? super SqlBrite.Query> subscriber) {
                observable.unsafeSubscribe(subscriber);
            }
        });
    }

    private void unSubscribe(Subscription subscription) {
        if (null != subscription && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    private void removeSubscription(Object context) {
        Subscription subscription = mObjectSubscriptionMap.remove(context);
        unSubscribe(subscription);
    }

    private void addSubscription(Object context, Subscription subscription) {
        mObjectSubscriptionMap.put(context, subscription);
    }

    @Nullable
    private StackTraceElement getLastObservableStackTraceElement() {
//        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
//        final int len = elements.length;
//
//        int lastObservableStackTrackElementIndex = -1;
//        for (int i = 0; i < len; i++) {
//            StackTraceElement element = elements[i];
//            if (element.getClassName().equals(getClass().getName()) && element.getMethodName().equals("createQuery")) {
//                lastObservableStackTrackElementIndex = i + 1;
//                break;
//            }
//        }
//
//        if (lastObservableStackTrackElementIndex < elements.length && lastObservableStackTrackElementIndex >= 0) {
//            return elements[lastObservableStackTrackElementIndex];
//        }

        return null;
    }
}

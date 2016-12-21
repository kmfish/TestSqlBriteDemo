package sqlbrite.demos.yy.com.sqlbrite.db;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import demos.yy.com.sqlbrite.db.TestAModel;

/**
 * Creator： Chanry
 * Date：2016/12/13
 * Time: 17:42
 * <p/>
 * Description:
 */
@AutoValue
public abstract class TestA implements TestAModel {

    public static final TestA.Factory<TestA> FACTORY =
            new Factory<>(new Creator<TestA>() {
                @Override
                public TestA create(long uid, @NonNull String name) {
                    return builder().setUid(uid).setName(name).build();
                }
            });

    public static Builder builder() {
        return new AutoValue_TestA.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract TestA build();

        public abstract TestA.Builder setUid(long uid);

        public abstract TestA.Builder setName(String name);
    }
}

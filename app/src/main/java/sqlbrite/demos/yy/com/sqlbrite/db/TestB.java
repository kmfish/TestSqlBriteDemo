package sqlbrite.demos.yy.com.sqlbrite.db;


import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.squareup.sqldelight.RowMapper;

import demos.yy.com.sqlbrite.db.TestBModel;

/**
 * Creator： Chanry
 * Date：2016/12/13
 * Time: 17:42
 * <p/>
 * Description:
 */
@AutoValue
public abstract class TestB implements TestBModel {

    public static final TestB.Factory<TestB> FACTORY =
            new TestB.Factory<>(new TestB.Creator<TestB>() {
                @Override
                public TestB create(long id, @NonNull String name, long uid) {
                    return builder().setId(id).setName(name).setUid(uid).build();
                }
            });

    public static TestB.Builder builder() {
        return new AutoValue_TestB.Builder();
    }

    public static final RowMapper<TestB> SELECT_ALL_MAPPER =
            FACTORY.select_allMapper();

    @Override
    public String toString() {
        return String.format("id=%d,name=%s,uid=%d", id(), name(), uid());
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract TestB build();

        public abstract TestB.Builder setId(long id);

        public abstract TestB.Builder setUid(long uid);

        public abstract TestB.Builder setName(String name);
    }
}

package sqlbrite.demos.yy.com.sqlbrite.db;

/**
 * Creator： Chanry
 * Date：2016/12/13
 * Time: 18:11
 * <p/>
 * Description:
 */
public class TestAB {
    public String name;
    public long id;

    public TestAB(String name, long id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("name:%s,id=%d", name, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestAB testAB = (TestAB) o;

        if (id != testAB.id) return false;
        return name != null ? name.equals(testAB.name) : testAB.name == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }
}

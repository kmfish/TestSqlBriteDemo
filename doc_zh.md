# �Ķ�����
[SqlBrite](https://github.com/square/sqlbrite)  ��Square��˾�ṩ��һ�����ݿ��������ķ�װ��ܡ��ṩ��RxJava��Observable����DB�����ӿڣ�����һ�������� ��query�����õ���QueryObservable��һֱ���ֶԸôβ�ѯ�ı����������¼��Ķ��ģ��������ͬһ�ţ�����ţ���ı���������ٴη������ݸ�����Subcriber���Ӷ����Է����ʵ�ֽ���ĸ��¡�

�Զ���Ĵʻ㺬�壺
 - ���������¼����� ����ָ�����Observable��Ϻ����ձ�ĳ��Subscriber����ʱ����ȷ����һ�����Ĺ�ϵ����
�磺��
```
 ObservableA.flatMap(ObservableB)
                        .flatMap(ObservableC)
                       .subscribe(new Subscriber<T>() {
...
}
```

# ���ⱳ��
���������һ����Ŀ��ʹ����SqlBrite  + SqlDelight��� ��ʵ�����ǵ����ݿ�㡣ͬʱҲʹ����RxJava����������Ŀ�����У������������ݿ�����Ľӿڶ�����Observable��ʽ���صģ�����Ȼ�ĳ�����һЩ�Ը��ַ�ʽ��϶��QueryObservable�����ṩ������Subscriber���ĵ�������������ӣ�

```java
queryA.flatMap(new Func1<String, Observable<String>>() {
	@Override
	public Observable<String> call(String s) {
		return getQueryB(s);
	}
}).flatMap(new Func1<String, Observable<String>>() {
	@Override
	public Observable<String> call(String s) {
		return getQueryC(s);
	}
}).subcribe(new Action1(String str) {
	  // update UI
});

private QueryObservable getQueryB(String params) {
	return factory.createQuery(....);
}

private QueryObservable getQueryC(String params) {
	return factory.createQuery(....);
}
```
����������е�queryA��queryB��queryC����ʹ��SqlBrite��createQuery�ӿڷ��ص�QueryObservable���������Ǿ�����һֱ�������Լ���ע��ı�������ԣ��������Ƿֱ��ѯ��A��B��C���ű���զ��֮�£���������ƺ�Ҳûʲô���⣬���ǵĴ���һ��ʼҲ��������д���ˡ�����������Щ���ݴ��ҵ����ʱ���Ŷ�λ��������⡣
�������������̣�
1. ����ɶ�����¼����Ķ��ĺ�A������1�α仯����queryA�ᷢ��1�����ݵ������ô��������getQueryB��getQueryC ��������ִ��1�Σ�������createQueryÿ�ζ��ᴴ��QueryOBservable��ʵ��������getQueryBִ��1�Σ��ʹ�����1��QueryObservableB��ʵ���ˣ�getQueryCͬ��Ȼ��subcriber������յ�1�����ݡ�
2. ����1֮�󣬴�ʱB���� ��1�α仯����ôgetQueryC��ִ��һ�Σ�subscriber���յ�1���¼�������������𣿽��ۿ϶���NO�ˡ�ʵ�ʲ��Է��֣���ʱsubcriber���յ�2�����ݡ�

# ׷����Դ
�е�������ͷ���˰ɣ������ٻع�֮ǰ�����̣��ᷢ��getQueryB ����������������ִ����2�飬˵������������QueryObservableB��ʵ������B��仯ʱ������������������queryB��ʵ�����յ���SqlBrite����������ˡ�
�Ǿ���������queryB��˭�������أ������Ҫȥ����RxJava��Դ���ˣ�queryA�� getQueryB ��ʹ����flatMap�����������ӵģ��������ǿ�һ��flatMap��ʵ�֣�
```
public final <R> Observable<R> flatMap(Func1<? super T, ? extends Observable<? extends R>> func) {
	if (getClass() == ScalarSynchronousObservable.class) {
		return ((ScalarSynchronousObservable<T>)this).scalarFlatMap(func);
	}
       // �ص������һ���ˣ����ǿ��Է���flatMap��ʵ������map��Ȼ����merge
	return merge(map(func));   
}

public final <R> Observable<R> map(Func1<? super T, ? extends R> func) {
	return create(new OnSubscribeMap<T, R>(this, func));  //  map��ʵ�־���OnSubscribeMap����ʵ���ǰ�װ��һ���ڲ����������������ε�Observable�����յ����ε�����ʱ��ִ��func��ת���������ͣ��ٷ��������ݸ����ζ����ߡ�
}

public final class OperatorMerge<T> implements Operator<T, Observable<? extends T>> {
  static final class MergeSubscriber<T> extends Subscriber<Observable<? extends T>> {
         .. .
        @Override
        public void onNext(Observable<? extends T> t) {
            if (t == null) {
                return;
            }
            if (t == Observable.empty()) {
                emitEmpty();
            } else
            if (t instanceof ScalarSynchronousObservable) {
                tryEmit(((ScalarSynchronousObservable<? extends T>)t).get());
            } else {
                InnerSubscriber<T> inner = new InnerSubscriber<T>(this, uniqueId++);     // ���Կ��������и�InnerSubscriber
                addInner(inner);
                t.unsafeSubscribe(inner);
                emit();
            }
        }
        
}
```
merge ���������������Ƚ϶�����Щ���ӣ���Ҳֻ�Ǽ򵥷������£����ڲ�ʹ����һ��MergeSubscriber���������ε����ݣ�Ȼ�������ζ�������
MergeSubscriber�ڲ�ʹ����InnerSubscriber�ļ��������Ĵ����ν��յ���ÿ��Observable��Ȼ���ٰѽ��յľ����data���η��䵽���Σ����Բο�
[merge�ĵ���ͼ](https://raw.githubusercontent.com/wiki/ReactiveX/RxJava/images/rx-operators/merge.oo.png) 
[flatMap�ĵ���ͼ](https://raw.githubusercontent.com/wiki/ReactiveX/RxJava/images/rx-operators/flatMap.png)

������flatMap�ķ��������ǿ���֪��queryA�������ݣ�Ȼ��map�������ڲ�������queryA������funcת�������˶��QueryObservableB��merge����������Ϊ���func1�Ǵ�String --> Observable<String>����merge�����������ڲ��ֱ��Ӧ�յ��Ķ��QueryOBservableB����������������������ǵĳ����£�����QueryOBservableB��ʵ�����Ǳ�merge�������������ˣ����Ե�B��仯��SqlBrite�ᷢ�����ݸ���������queryB���Ӷ����մ��ݵ�subscriber�����͵õ���2�����ݡ�

# �������̽��
���������⣬���ǳ����˼��ֽ��˼·��
1. ���������Ķ��QueryObservableͨ��flatMap���ӵ������ֱ��ͨ��sql����������ϲ�ѯ��
�����ǻر������ⳡ������Ҳȷʵ��һЩ�����»��ǻ���Ҫ��϶��QueryObservable�ģ�����queryA��Ȼ���ٵ�Server�ϲ�ѯһ�����b���������bȥ��ΪqueryC�Ĳ�ѯ��������������£�queryA��queryC������Ҫ������һ���¼������ˡ�
2. �����QueryObservable����ʱ���ӵڶ�����ʼ��query�������ټ�����ı仯�������һ�� .first()���������Ļ�����B��C�����仯ʱ�����յ�subcriber�ǲ�����µġ��ֿ��ǣ�������queryA��ȥ���������¼������漰��������DB table�������κ�һ�ű�仯�������¼���������queryһ�顣����һϸ�룬�������Ҳ�ǲ��еģ�queryA�������������յ������¼����ﵽ����Ҫ������Щ����ΪqueryB��queryC��û�б�¶�����Ϣ������ȥ��queryB��queryC��ʵ�֣��������ֶ�ȫ������ˡ�����queryA��queryB��queryC�����ķ�ʽ������һ���ô�����һ��QueryObservable���Ա����ã�������Ҫ���ȿ��Զ���ʹ�ã�Ҳ���Խ��������query���ʹ�ã���2�����Ļ���Ҳ���ƻ���������ˡ�

# ���ǲ��õķ���
ʵ��һ��QueryObservableManager������ͳһ����ÿ���¼����е�QueryObservable�Ķ��ģ����ڲ�ά��һ����ǰ�¼��������е�query���Ĺ�ϵ���ϡ���ԭʼ��createQuery���ɵ�QueryObservable����һ��װ�Σ��ɵ����ߴ���һ��context����������Ψһ��ʶ��һ���¼����е�һ��QueryObservable����DecorationObservable������ʱ������context��ѯ����֮ǰ���ڵĶ��Ĺ�ϵ�����˶���DecorationObservable�ڲ��ٶ���raw query Observable�������������Ķ������������������صı仯��һ����QueryObservableManager��һ����context������
1. ��α�ʶһ��QueryObservable�Ķ���
ͨ�������������Ե���������ʹ��һ��QueryObservableManager����ô��һ���¼����еĲ�ͬQueryObservable��������return ��Observable��Methodջ��Ϣ(StackTraceElement)����context����Ψһ��ʶ�����������ӵ� getQueryB �����������Ϊ֮ǰ�ᵽ��getQueryB�ǿ��Ը��õģ��ڲ�ͬ�Ķ������ж����ܳ��֣������ڲ�ͬ������������£��Ͳ��ܽ����Է�������ջ����ʶ�ˣ���ʱ����ʵ����methodջ��Ϣ��һ���ģ�����ʵ����Ҫͬʱ�����������ĵģ���������һ����������˵������������ͬ��ջʱ����ζ���ж����ͬ��query�������ˣ���ʱ����Ҫ��֮ǰ��query���˶����������һ�ζ��ġ�һ�仰��**һ���������У�ͬһ��getQuery ������Observable����ֻ�ܱ���һ����** ���ԣ����context�Ͳ����ⲿ���ˣ�ֱ����QueryObservableManager �ڲ���ȷ�����ɡ�

2. �����һ���������У�ʹ��һ��QueryObservableManager ��
ͨ�������е����� getQueryB�ķ�����������һ������QueryObservableManager��������Observable������ʱ���ɶ����߹���һ��QueryObservableManager ʵ�����Ӷ���֤����һ���������У�ʹ�õĶ���ͬһ��QueryObservableManager ����

# Demo������Ŀ
[QueryObservableManageʵ��](https://github.com/kmfish/TestSqlBriteDemo/blob/master/app/src/main/java/sqlbrite/demos/yy/com/sqlbrite/db/BriteQueryObservableFactory.java)

[Demo ��Ŀ](https://github.com/kmfish/TestSqlBriteDemo)

# �������
[SqlBrite��Ŀ��ҳ��Ҳ�����������������](https://github.com/square/sqlbrite/issues/102)




























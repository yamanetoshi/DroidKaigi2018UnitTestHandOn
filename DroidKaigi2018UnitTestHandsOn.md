# Unit Test の Hands-on 資料まとめ

以下資料について確認を行い、纏めを作成しています。

- [Android Unit Testing Hands-On](https://github.com/srym/DroidKaigi2018UnitTestHandOn)

初級、中級という形で以下の項目に関する記述が列挙されています。

- ウォーミングアップ
 - はじめてのアサーション
 - はじめての Unit Test
 - Robolectric を使った Unit Test
 - List のアサーション
 - Mockito を使った stubbing
 - Mockito を使った spying
 - 非同期処理の Unit Test

- 実践編
 - MockWebServer を使ってリモートデータソースのテストを書こう
 - Robolectric と Room を使ってローカルデータソースのテストを書こう
 - 各データソースを束ねたレポジトリのテストを書こう
 - ユースケースのテストを書こう
 - Presenter のテストを書こう

<!-- more -->

## assertThat

この hands-on で使っているのは　org.assertj 提供のものとなります。ソース上で以下な `import` 記述があります。

```
import static org.assertj.core.api.Assertions.AssertThat;
```

assert に関する Tutorial も存在しているようです。別途確認の方向。

- [Testing with AssertJ assertions - Tutorial](http://www.vogella.com/tutorials/AssertJ/article.html)

## はじめての Unit Test

ここでは通常の assert による確認以外に例外の発生を確認するための試験についての記載があります。

```
  /**
   * 不正な入力（null）で<code>NullPointerException</code>が上がることを確認するテストケースを書いてみよう。
   * <p>
   * 例: <code>String input = null;</code>
   * <p>
   * ヒント: <code>@Test(expected = 例外.class)</code>
   */
  @Test(expected = NullPointerException.class)
  public void isValid_inputNull_resultsNPE() throws Exception {
    String input = null;
    inputChecker.isValid(input);
  }
```

annotation による切り分けとなっています。

## Robolectric を使った Unit Test

ここは Android の TextUtil というクラスを使っているので以下な形で試験の定義をしてあげれば Android フレームワークのコードが含まれた手続きの試験が可能になります、とのこと。

```
@RunWith(RobolectricTestRunner.class)
public class BetterInputCheckerTest {
  private BetterInputChecker inputChecker;

  @Before
  public void setUp() throws Exception {
    inputChecker = new BetterInputChecker();
  }
```

## List のアサーション

List の検証、ということで以下なメソッドが使われています。

- isNotEmpty
- hasSize
- containsExactly
- isEmpty

## Mockito を使った stubbing

この節で出てくる `TweetRepository` は private 属性として `LocalTweetDataSource` なオブジェクトを持っています。このオブジェクトを `mock` を使って stub してみよう、という例です。setup が以下な形です。

```
  /**
   * {@link TweetRepository} は {@link LocalTweetDataSource} に依存している。
   * テストのやり方は色々あるが、ここでは {@link Mockito#mock(Class)} を利用してみよう。
   * <p>
   * ヒント: {@link Mockito#when(Object)} と <code>thenReturn()</code> を組み合わせると、メソッドのスタブを作ることができる。
   */
  @Before
  public void setUp() throws Exception {
    LocalTweetDataSource mock = mock(LocalTweetDataSource.class);

    tweetRepository = new TweetRepository(mock);

    tweetList = new ArrayList<Tweet>();

    tweetList.add(Tweet.bodyOf(str1));
    tweetList.add(Tweet.bodyOf(str2));

    when(mock.getTimeline()).thenReturn(tweetList);
  }
```

ヒントも一緒に載せておきます。

- `LocalTweetDataSource` の mock を作って
- それを `TweetRepository` のコンストラクタに渡して
- getTimeline というメソッドの stub を設定しています

この例では `LocalTweetDataSource` は interface として定義されているので stubbing している、という形になります。具体的なクラスができてきても unit test はこちらで行っておく方が良いものと思われます。

## Mockito を使った spying

元オブジェクトの一部の機能の書き換えであれば `Mockito#spy` を使ったほうがよいとのことです。以下な記載があります。

```
  @Before
  public void setUp() throws Exception {
    converter = spy(new TweetConverter());

    spy = spy(new LocalTweetDataSource() {
      @NonNull
      @Override
      public List<Tweet> getTimeline() {
        ArrayList<Tweet> ret = new ArrayList<Tweet>();
        ret.add(Tweet.bodyOf("tmp"));
        return ret;
      }
    });

    repository = new TweetRepositoryWithConverter(spy, converter);
  }
```

また、資料では `Spying を利用して、内部でこの変換メソッドが呼ばれていることを検証するのにチャレンジしてみましょう` という記載があります。`TweetRepositoryWithConverter` の定義ですが以下のようになっており

```
  @NonNull
  public List<Tweet> getTimeline() {
    return localDataSource.getTimeline();
  }

  @NonNull
  public List<String> getTimelineBody() {
    return converter.convertList(localDataSource.getTimeline());
  }
```

`getTimeLine` からだと `TweetConverter` のメソッドは呼びませんが、`getTimelineBody` からだと呼び出す形になっています。これを検証するための仕組みとして `veriry` という手続きが用意されています。

- `repository.getTimeline` の呼び出しでは `converter.convertList` は呼び出されない
- `repository.getTimelineBody` の呼び出しでは `converter.convertList` が一度呼び出される

ということが検証できます。また、`convertList` の呼び出しにおいて渡す引数について以下の違いはどこに根拠があるのかなどの情報については

- `verify(converter, never()).convertList(null);`
- `verify(converter, times(1)).convertList(spy.getTimeline());`

下記のドキュメントが参考になります。

- http://y-anz-m.blogspot.jp/2013/06/android-mockito_6954.html

### 追記 (引数について)

`Mockito#verify` についてはメソッドが呼び出されているかどうかをテストするためのもので、引数の値がどんなものであっても良い場合には以下のような書き方ができます。

```
    verify(converter, never()).convertList(anyList());
```    

また、以下の記法であれば

- `verify(converter, never()).convertList(null);`
- `verify(converter, times(1)).convertList(spy.getTimeline());`

引数の値もチェックされる形になります。あるいは引数が複数あるケイスである引数に `any*()` を使う場合には他の引数で実際の値を記載することはできないようです。上記エントリには以下な形で例示されています。

```
    verify(mockResultListener, only()).handleStatus(eq("hoge1"), anyLong());
```

## 非同期処理の Unit Test

非同期処理、以下な手続きがテーマになっています。

```
  void fetch(OnSuccess<T> onSuccess, OnFailure onFailure) {
    new Thread(() -> {
      try {
        Thread.sleep(1000L); // time intensive task
        onSuccess.onSuccess(fetcher.fetch());
      } catch (Exception e) {
        onFailure.onFailure(e);
      }
    }).start();
  }
```

別 thread にて 1 秒 wait して `fetcher.fetch()` を呼び出した戻りを云々、という形。`fetcher` は属性で `AsyncFetcher.java` と同じディレクトリに `Fetcher.java` が投入されています。ここで諸々定義されています。

で、この非同期処理が別 thread で実行されてる間にテストケースを抜けてしまうと成功扱いになってしまうようです。ここでは待機させるために `CountDownLatch` というクラスを使っています。ヒントを確認しましょう。

また、非同期処理が失敗に終わる場合の検証方法ですが `Mockito#doThrow` を使って例外を発生させています。

```
    doThrow(new RuntimeException("NG")).when(fetcher).fetch();
```

これで `fetcher.fetch()` 呼び出し時に RuntimeException を発生させることができるようです。

## MockWebServer を使ってリモートデータソースのテストを書こう

以降は実践編に関する項目となります。

試験については、下層から上に向かって作っていく形になっています。まずは通信部分からとなります。テスト対象は `GitHubRestDataSource` になります。ここでは

- Retrofit
- MockWebServer

というものを使っているようです。`Retrofit` は API 通信の記述に関する面倒な部分を引き受けてくれるようで、`Retrofit` を介して `MockWebServer` に接続する形となるようです。

### Retrofit

不慣れなのでいくつか控えを。この実装では

- GitHubService が API の設定として interface 定義されている
- GithubService のインスタンスは `Retrofit#create` で生成
- Retrofit なオブジェクトは `mockWebServer` なインスタンスとやりとりする形になっている
- 通信に使っている `GitHubRestDataSource` オブジェクトは `GitHubService` のインスタンスを使用

ということになっているようです。そしてこのレイヤが動作確認できているのであればここから上の層は stub で　OK というのも素晴らしいですね。試験に必要なオブジェクトの用意は `setUp` で行われていますのでそのあたりは実装を確認しましょう。

- [GitHubRestDataSource.java](https://github.com/srym/DroidKaigi2018UnitTestHandOn/blob/master/app/src/test/java/us/shiroyama/android/my_repositories/infrastructure/repository/datasource/remote/GitHubRestDataSourceTest.java)

また、Retrofit に関する資料を以下に列挙します。

- [Retrofit 2.0.1 使い方メモ](http://outofmem.hatenablog.com/entry/2016/04/15/005541)

### テスト対象になっているクラスについて

`GitHubRestDataSource` というクラスです。`GitHubRemoteDataSource` というインターフェースで定義されている `getUserRepositories` という手続きを実装しています。以下なあたりは特有の書き方なのだろう、という理解で。

```
    Response<List<RepositoryEntity>> response = gitHubService.getUserRepositories(
        account,
        Sort.UPDATED.value,
        Direction.DESC.value
    ).execute();
```

戻りを super class にて定義されている `stripResult` に渡してその戻りを戻しています。戻り値の型である `List<RepositoryEntity>` ですが、JSON なキーの値で自動的に属性に設定される形になっているものと思われます。定義の一部が以下。

```
public class RepositoryEntity {
  @SerializedName("id")
  private long id;
```

試験についてはテスト対象クラスのメソッドに用意済みのユーザ ID 文字列を渡して

- 結果が空ではない
- 先頭要素の名前のチェック

を行っています。また、未定義のユーザへのアクセスは例外が戻ること、を確認しています。

```
  @Test(expected = ApiException.class)
  public void getUserRepositories_inputInvalidAccount_causedApiException() throws Exception {
    gitHubRestDataSource.getUserRepositories("xxx");
  }
```

例外発生、な検証が簡易なのが有り難いです。

## Robolectric と Room を使ってローカルデータソースのテストを書こう

テスト対象となっているのが以下のクラス

- RoomRepositoryDAO
- GitHubRoomDataSource

確認の前に Room というアーキテクチャに関する確認が必要です。Room に関する資料を以下に列挙します。

- [[Android Architecture Components] - Room 詳解](https://tech.recruit-mp.co.jp/mobile/post-12311/)

基本的な使い方として

- DB の定義：基本的にはこれを i/f として操作を行う
- DAO の定義：テーブルへのアクセスなどについて定義を行う (DB 経由で定義された手続きを呼び出す形となる)
- Entity の定義：テーブルに定義された属性の定義を行う

という三点を定義しておいて操作を行う形となります。今回の実装のように SQLite に cache を行うような場合に使われるアーキテクチャと思われます。

### DAO のテスト

RoomRepositoryDAO の試験から確認します。とりえあず、先頭部分がこうなっているのは

```
@RunWith(RobolectricTestRunner.class)
public class RoomRepositoryDaoTest {
  private AppDatabase db;
```

SQLite を云々するのに Context が必要で、Robolectric だと簡易に取得可能だから、ということだと理解しています。

Context を使う必要があるから、という理解で良いのかな。また、`DatabaseProvider` クラスは非常に参考になりました。こうした形でデータベースが使えるのであればテストの作成も負荷が下がります。ただし、insert な処理に渡す引数については注意が必要です（確認も必要です）。

```
    RoomRepositoryDao roomRepositoryDao = db.repositoryDao();
    roomRepositoryDao.insertAllRepositories(repositoriesYmnder.toArray(new RoomRepositoryEntity[repositoriesYmnder.size()]));
    roomRepositoryDao.insertAllRepositories(repositoriesSrym.toArray(new RoomRepositoryEntity[repositoriesSrym.size()]));
```

基本的にはデータの読み書きが正常にできているか、という確認ができれば良いという形になっています。

### ローカルデータソースのテスト

DAO の試験でデータベースへのアクセスの確認はできているので、`RoomRepositoryDAO` に関する操作については mock する形での実装となっています。試験の記述もですが、このあたりについても確認を行っていきます。まず、`setUp`　の記述から

```
  @Before
  public void setUp() throws Exception {
    AppDatabase db = mock(AppDatabase.class);
    repositoryDao = mock(RoomRepositoryDao.class);

    when(db.repositoryDao()).thenReturn(repositoryDao);

    mapper = spy(RepositoryEntityMapper.Factory.INSTANCE.get());

    gitHubRoomDataSource = new GitHubRoomDataSource(db, mapper);
  }
```

db および repositoryDao にはハリボテなオブジェクトが設定されます。これではまずいので `db.repositoryDao` は repositoryDao を戻すようにしています。また、 mapper にはハリボテではなく、spy を使って RepositoryEntityMapper オブジェクトを設定します。そして最後にこれらを使ってテスト対象となる `GitHubRoomDataSource` オブジェクトを生成しています。

次に `getUserRepositories` のテストを確認してみます。流れとしては stub を用意して

```
    when(repositoryDao.findByAccount(eq("srym"))).thenReturn(Arrays.asList(
            new RepoWithAccount(repositoriesSrym.get(0), srym),
            new RepoWithAccount(repositoriesSrym.get(1), srym)
    ));
```

試験対象の手続きを呼び出して

```
    List<RepositoryEntity> list = gitHubRoomDataSource.getUserRepositories("srym");
```

確認を行う形です。

```
    verify(repositoryDao, times(1)).findByAccount(anyString());
    assertThat(list).hasSize(2);
    verify(mapper, times(1)).convertList(anyList());
```

この手続の定義は以下のようになっており

```
  @Override
  public List<RepositoryEntity> getUserRepositories(@NonNull String account) {
    List<RepoWithAccount> repoWithAccountList = db.repositoryDao().findByAccount(account);
    return repositoryEntityMapper.convertList(repoWithAccountList);
  }
```

db は mock オブジェクトのため

- `db.repositoryDao` の stub が必要
- `findByAccount` の stub が必要

なのですが、mapper は spy しているので stub の定義が不要となっています。また、この試験においては値の精査は行わず

- 戻ったリストのサイズ
- 内部から呼び出されている手続き呼び出しおよび引数の値の精査（必要なもののみ）

と言うかたちになっています。

## 各データソースを束ねたレポジトリのテストを書こう

リポジトリ層では基本的に呼び出されるメソッドを確認することが主な確認となるようです。データソースも mock で定義されています。

```
  /**
   * 各データソースを束ねたレポジトリのテストをする。
   * ここまでくれば、各データソースはすべてモックで良い。
   * <p>
   * レポジトリ層では、各データソースのどこでデータにヒットするかによって呼ばれるメソッドが変わるので、確実に<code>verify()</code>で確かめる。
   * その際、呼ばれないものはきちんと<code>never()</code>しておくとなお良いだろう。
   */
  @Before
  public void setUp() throws Exception {
    remoteDataSource = mock(GitHubRemoteDataSource.class);
    localDataSource = mock(GitHubLocalDataSource.class);
    repository = new GitHubInfraRepository(remoteDataSource, localDataSource);
  }
```

`GitHubInfraRepository#getUserRepositories` については以下のような流れになっており

- `GitHubLocalDataSource#getUserRepositories` の戻りが空でない場合、戻り値を戻す
- local および remote からの戻りが空の場合、空を戻す
- それ以外の場合、remote からの戻りを local に保存する処理を行った後に remote からの戻りを戻す

それぞれの場合について確認を行っています。また、もう一つ定義されている `refreshUserRepositories` についても似たような場合分けがあるために、条件にに応じた試験を書いていることを確認して見てください。

## ユースケースのテストを書こう

ここではリポジトリ層を使うユースケース層に関する試験を書きます。MVP アーキテクチャによればユースケース層は、リポジトリ層とプレゼンター層の中間に位置するもの、とのことです。テスト観点についてコメントから引用したものを以下に列挙してみます。

- `GetRepositories#enqueue` のテスト：実際に処理が行われるま確認するのは困難なので、`TaskQueue#enqueue` を `verify()` する
- `GetRepositories#buildTask` のテスト：これで組み立てた `Task#run` を呼んでやることで、依存関係のライブラリが内部的に呼ばれたかどうかを `verify()` できる
- `GetRepositories#buildTask` のテスト：パラメータが `GetRepositories.Param.newInstance("srym")` で`GitHubRepository#getUserRepositories` でエラーが起きた場合のテスト
- `GetRepositories#buildTask` のテスト：パラメータが `GetRepositories.Param.newInstance("srym")` の場合のテスト
- `GetRepositories#buildTask` のテスト：パラメータが `GetRepositories.Param.newInstance("srym")` で `GitHubRepository#refreshUserRepositories` でエラーが発生した場合のテスト

エラーを発生させる方法についてはエラーが発生する手続きについて以下な方法で例外を投げさせています。

```
    when(repository.refreshUserRepositories("srym")).thenThrow(new ApiException());
```

以下な書き方をしていたのですが、間違いなのかどうか。

```
    doThrow(new ApiException()).when(repository).refreshUserRepositories(eq("srym"));
```

同じことなのかな。。。    

## Presenter のテストを書こう

Github にある解説によれば、

```
PresenterはViewのイベントを受けてビジネスロジックの実行を依頼し、結果をイベントで受け取ってViewに変更通知する役割を担うコンポーネントです。
```

とのことです。アカウント入力画面およびリポジトリ一覧の Presenter が試験の対象とのこと。`AccountInputPresenter` については入力値が

- 空文字列
- 不正な文字列
- 正しい文字列

についてのテストを行っています。`verify` を使ってそれぞれの状態で呼び出されるべき手続きを確認しています。

### RepoListPresenterTest

最後なので実装を確認しつつ、どのような試験が必要なのかを確認しつつで精査してみます。まず、属性の定義が以下。

```
  @NonNull
  private final GetRepositories getRepositories;

  @NonNull
  private final RepoViewModelMapper mapper;

  @Nullable
  TaskTicket ticket;
```

この三点はテスト実装でも保持しておく必要があります。また、`RepoListPresenter` も同様に必要です。これまでにも似たようなケイスがありましたが、`RepoViewModelMapper` は `spy` しておく必要があると思われます。また、上記で列挙した属性についてはオブジェクト生成時に他のオブジェクトが必要、ということはありません。`RepoListPresenter` のスーパークラスにおいて以下の属性が定義されていますので、こちらも用意が必要と思われます。

```
public abstract class BasePresenter<V extends BaseView> {
  protected V view;
```

ちなみに `RepoListPresenter` のクラス定義の先頭は以下な形になっています。

```
public class RepoListPresenter extends BasePresenter<RepoListContract.View> implements RepoListContract.Interaction {
```

ので、V は `RepoListContract.View` となります。この `RepoListContract` は `presentation/view/contract` 配下にて定義されています。該当部分の定義を以下に引用します。

```
public interface RepoListContract {
  interface View extends BaseView {
    void showRepositoryList(@NonNull List<RepoViewModel> repositoryList);

    void showProgressBar();

    void hideProgressBar();

    void showError(@NonNull String message);
  }
```

また、`View` が継承している `BaseView` は `presentation/view/fragment` 配下にて定義されています。先頭部分のみ以下に引用します。

```
public interface BaseView {
  void startActivity(@NonNull Intent intent);

  void startActivityForResult(@NonNull Intent intent, int requestCode);

  void finishActivity();

  @Nullable
  Context getContext();

  @Nullable
  FragmentActivity getActivity();
}
```

これ、何を意味しているかというと `Presenter` は `View` を保持していて、この interface は `Fragment` が継承している、ということです（類推）。で、その `Fragment` はいろいろな手続きを実装しなければならないようですが、この仕組みを使って `Presenter` から `View (Fragment)` への通信などが可能になっているはずです。

引き続き `Presenter` の実装を確認します。コンストラクタは NonNull な属性のオブジェクトを受け取って設定しています。

```
  public RepoListPresenter(@NonNull GetRepositories getRepositories, @NonNull RepoViewModelMapper mapper) {
    this.getRepositories = getRepositories;
    this.mapper = mapper;
  }
```

これ以外に定義されているメソッドとその機能を行かに列挙します。

- getRepositoryList
 - 文字列 account を引数で受け取る
 - view.showProgressBar を呼び出す
 - `GetRepositories#enqueue` を呼び出して戻り値を ticket に格納、引数は `GetRepositories.Param.newInstance(account)`

- refreshRepositoryList
 - 文字列 account を引数で受け取る
 - `GetRepositories#enqueue` を呼び出して戻り値を ticket に格納、引数は `GetRepositories.Param.newInstance(account, true)`

- onDestroyView
 - ticket が null でなくて存在するのであれば ticket.cancel(true) を呼び出す

- onSuccess
 - view.hideProgressBar を呼び出す
 - 引数で受け取った `GetRepositories.OnSuccessGetRepositories` オブジェクトが保持する `List<Repository>` を取得
 - 取得したリストを `mapper.convertList` に渡してその戻りを view.showRepositoryList に渡す

- onError
 - view.hideProgressBar を呼び出す
 - view.showError に `GetRepositories.OnFailureGetRepositories` オブジェクトが保持するメッセージを渡す
 - finishActivity を呼び出す

これがテストの実装とほぼ同一、というのが分かります。

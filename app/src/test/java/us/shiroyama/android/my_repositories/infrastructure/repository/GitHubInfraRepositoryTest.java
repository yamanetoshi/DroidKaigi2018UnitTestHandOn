package us.shiroyama.android.my_repositories.infrastructure.repository;

import org.junit.Before;
import org.junit.Test;
import org.threeten.bp.ZonedDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import us.shiroyama.android.my_repositories.infrastructure.entity.AccountEntity;
import us.shiroyama.android.my_repositories.infrastructure.entity.RepositoryEntity;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local.GitHubLocalDataSource;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local.room.entity.RepoWithAccount;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local.room.entity.RoomAccountEntity;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local.room.entity.RoomRepositoryEntity;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.remote.GitHubRemoteDataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Local Unit Test for {@link GitHubInfraRepository}
 *
 * @author Fumihiko Shiroyama
 */

public class GitHubInfraRepositoryTest {
  private GitHubRemoteDataSource remoteDataSource;
  private GitHubLocalDataSource localDataSource;
  private GitHubInfraRepository repository;
  private List<RoomRepositoryEntity> repositoriesSrym = Arrays.asList(
          new RoomRepositoryEntity(
                  5681402,
                  "dotfiles",
                  "srym/dotfiles",
                  "https://github.com/srym/dotfile",
                  false,
                  "this is description",
                  1192694,
                  ZonedDateTime.parse("2012-09-05T02:17:05Z"),
                  ZonedDateTime.parse("2018-01-26T05:21:03Z"),
                  ZonedDateTime.parse("2018-01-26T05:21:02Z")
          ),
          new RoomRepositoryEntity(
                  64338388,
                  "FirebaseRealTimeChat",
                  "srym/FirebaseRealTimeChat",
                  "https://github.com/srym",
                  false,
                  "Sample real-time chat application using Firebase",
                  1192694,
                  ZonedDateTime.parse("2016-07-27T20:08:52Z"),
                  ZonedDateTime.parse("2018-01-14T02:27:54Z"),
                  ZonedDateTime.parse("2017-02-15T23:52:13Z")
          )
  );
  private RoomAccountEntity srym = new RoomAccountEntity(1192694, "srym", "https://avatars1.githubusercontent.com/u/1192694?v=4", "https://api.github.com/users/srym");

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

  /**
   * {@link GitHubInfraRepository#getUserRepositories(String)}のテスト
   * <p>
   * ローカルデータソースにデータがあった場合のテストを書こう。
   * その際、リモートデータソースの呼び出しは行われないことも同時に検証しよう。
   */
  @Test
  public void getUserRepositories_localDataSource_hit() throws Exception {
    when(localDataSource.getUserRepositories(eq("srym"))).thenReturn(Collections.singletonList(new RepositoryEntity()));
    assertThat(repository.getUserRepositories("srym")).isNotEmpty();

    verify(remoteDataSource, never()).getUserRepositories(anyString());
    verify(localDataSource, never()).insertRepositoriesAndAccounts(anyList(), anyList());
  }

  /**
   * {@link GitHubInfraRepository#getUserRepositories(String)}のテスト
   * <p>
   * ローカルデータソースも空っぽで、リモートデータソースの結果も空っぽの場合のテストを書こう。
   * 同じく、インタラクションが行われないメソッドはそれも検証しよう。
   */
  @Test
  public void getUserRepositories_localDataSource_not_hit_and_remoteDataSource_empty() throws Exception {
    when(localDataSource.getUserRepositories(eq("srym"))).thenReturn(null);
    when(remoteDataSource.getUserRepositories(eq("srym"))).thenReturn(null);
    assertThat(repository.getUserRepositories("srym")).isEmpty();

    verify(localDataSource, never()).insertRepositoriesAndAccounts(anyList(), anyList());
  }

  /**
   * {@link GitHubInfraRepository#getUserRepositories(String)}のテスト
   * <p>
   * ローカルデータソースは空っぽだが、リモートデータソースが結果を返してくれた場合のテストを書こう。
   * リモートデータソースが取得できた場合はローカルデータソースへの書き込みがあるはずなのでそれも検証しよう。
   */
  @Test
  public void getUserRepositories_localDataSource_not_hit_and_remoteDataSource_hit() throws Exception {
    when(localDataSource.getUserRepositories(eq("srym"))).thenReturn(null);
    List<RepositoryEntity> ret = new ArrayList();
    RoomRepositoryEntity tmp = repositoriesSrym.get(0);
    ret.add(new RepositoryEntity(tmp.getId(), tmp.getName(), tmp.getFullName(), tmp.getHtmlUrl(), tmp.isPrivate(),
                    tmp.getDescription(), new AccountEntity(srym.getId(), srym.getLogin(), srym.getAvatarUrl(), srym.getUrl()),
                    tmp.getCreatedAt(), tmp.getUpdatedAt(), tmp.getPushedAt()));
    when(remoteDataSource.getUserRepositories(eq("srym"))).thenReturn(ret);

    assertThat(repository.getUserRepositories("srym")).isNotEmpty();
    verify(localDataSource, times(1)).insertRepositoriesAndAccounts(anyList(), anyList());
  }

  /**
   * {@link GitHubInfraRepository#refreshUserRepositories(String)}のテスト
   * <p>
   * リモートデータソースが空を返した場合のテストを書こう。
   */
  @Test
  public void refreshUserRepositories_remoteDataSource_no_hit() throws Exception {
    when(remoteDataSource.getUserRepositories(eq("srym"))).thenReturn(null);

    assertThat(repository.refreshUserRepositories(eq("srym"))).isEmpty();
    verify(localDataSource, never()).deleteAndInsertRepositoriesAndAccounts(anyString(), anyList(), anyList());
  }

  /**
   * {@link GitHubInfraRepository#refreshUserRepositories(String)}のテスト
   * <p>
   * リモートデータソースが値を返した場合のテストを書こう。
   */
  @Test
  public void refreshUserRepositories_remoteDataSource_hit() throws Exception {
    List<RepositoryEntity> ret = new ArrayList();
    RoomRepositoryEntity tmp = repositoriesSrym.get(0);
    ret.add(new RepositoryEntity(tmp.getId(), tmp.getName(), tmp.getFullName(), tmp.getHtmlUrl(), tmp.isPrivate(),
            tmp.getDescription(), new AccountEntity(srym.getId(), srym.getLogin(), srym.getAvatarUrl(), srym.getUrl()),
            tmp.getCreatedAt(), tmp.getUpdatedAt(), tmp.getPushedAt()));
    when(remoteDataSource.getUserRepositories(eq("srym"))).thenReturn(ret);

    assertThat(repository.refreshUserRepositories(eq("srym"))).isNotEmpty().hasSize(1);
    verify(localDataSource, times(1)).deleteAndInsertRepositoriesAndAccounts(anyString(), anyList(), anyList());
  }

}
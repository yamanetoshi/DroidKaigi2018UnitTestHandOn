package us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.threeten.bp.ZonedDateTime;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import us.shiroyama.android.my_repositories.infrastructure.entity.RepositoryEntity;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local.room.dao.RoomRepositoryDao;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local.room.dao.RoomRepositoryDaoTest;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local.room.db.AppDatabase;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local.room.db.DatabaseProvider;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local.room.entity.RepoWithAccount;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local.room.entity.RoomAccountEntity;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local.room.entity.RoomRepositoryEntity;
import us.shiroyama.android.my_repositories.infrastructure.repository.datasource.local.room.mapper.RepositoryEntityMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Local Unit Test for {@link GitHubRoomDataSource}
 *
 * @author Fumihiko Shiroyama
 */

public class GitHubRoomDataSourceTest {
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

  private RepositoryEntityMapper mapper;
  private RoomRepositoryDao repositoryDao;
  private GitHubRoomDataSource gitHubRoomDataSource;

  /**
   * {@link RoomRepositoryDao} を使ってデータを読み書きするローカルデータソースクラスである。
   * このクラスをテストする方法も色々あるが、DAOのテストは {@link RoomRepositoryDaoTest} でやるので、ここではモックデータと<code>verify()</code>でも充分だろう。
   */
  @Before
  public void setUp() throws Exception {
    AppDatabase db = mock(AppDatabase.class);
    repositoryDao = mock(RoomRepositoryDao.class);

    when(db.repositoryDao()).thenReturn(repositoryDao);

    mapper = spy(RepositoryEntityMapper.Factory.INSTANCE.get());

    gitHubRoomDataSource = new GitHubRoomDataSource(db, mapper);
  }

  /**
   * {@link GitHubRoomDataSource#getUserRepositories(String)} を呼ぶと内部で {@link RoomRepositoryDao#findByAccount(String)} が呼ばれることを<code>verify()</code>しよう。
   * また {@link RepositoryEntityMapper#convertList(List)} が内部で呼ばれることも検証しよう。
   */
  @Test
  public void getUserRepositories() throws Exception {
    when(repositoryDao.findByAccount(eq("srym"))).thenReturn(Arrays.asList(
            new RepoWithAccount(repositoriesSrym.get(0), srym),
            new RepoWithAccount(repositoriesSrym.get(1), srym)
    ));

    List<RepositoryEntity> list = gitHubRoomDataSource.getUserRepositories("srym");

    verify(repositoryDao, times(1)).findByAccount(anyString());
    assertThat(list).hasSize(2);
    verify(mapper, times(1)).convertList(anyList());
  }

  /**
   * {@link GitHubRoomDataSource#insertRepositoriesAndAccounts(List, List)} を呼ぶと内部で {@link RoomRepositoryDao#insertRepositoriesAndAccounts(List, List)} が呼ばれることを<code>verify()</code>しよう。
   */
  @Test
  public void insertRepositoriesAndAccounts() throws Exception {
    gitHubRoomDataSource.insertRepositoriesAndAccounts(repositoriesSrym, Arrays.asList(srym));

    verify(repositoryDao, times(1)).insertRepositoriesAndAccounts(anyList(), anyList());
  }

  /**
   * {@link GitHubRoomDataSource#deleteAndInsertRepositoriesAndAccounts(String, List, List)} を呼ぶと内部で {@link RoomRepositoryDao#deleteAndInsertRepositoriesAndAccounts(String, List, List)} が呼ばれることを<code>verify()</code>しよう。
   */
  @Test
  public void deleteAndInsertRepositoriesAndAccounts() throws Exception {
    gitHubRoomDataSource.deleteAndInsertRepositoriesAndAccounts("srym", repositoriesSrym, Arrays.asList(srym));

    verify(repositoryDao, times(1)).deleteAndInsertRepositoriesAndAccounts(eq("srym"), anyList(), anyList());
  }

}
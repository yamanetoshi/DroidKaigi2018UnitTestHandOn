package us.shiroyama.android.my_repositories.hands_on_beginners.mockito.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.shiroyama.android.my_repositories.hands_on_beginners.mockito.entity.Tweet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Local Unit Test for {@link TweetRepository}
 *
 * @author Fumihiko Shiroyama
 */

public class TweetRepositoryTest {
  private TweetRepository tweetRepository;
  private List<Tweet> tweetList;
  private String str1 = "Alice";
  private String str2 = "Bob";

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

  /**
   * スタブで定義した返り値を検証するテストケースを書いてみよう。
   * Listの検証で学んだアサーションをここでも有効活用しよう。
   */
  @Test
  public void getTimeline() throws Exception {
    assertThat(tweetRepository.getTimeline()).isNotEmpty();
    assertThat(tweetRepository.getTimeline()).hasSize(tweetList.size());

    Tweet tw1 = Tweet.bodyOf(str1);
    Tweet tw2 = Tweet.bodyOf(str2);

    assertThat(tweetRepository.getTimeline()).containsExactly(tw1, tw2);
  }

}
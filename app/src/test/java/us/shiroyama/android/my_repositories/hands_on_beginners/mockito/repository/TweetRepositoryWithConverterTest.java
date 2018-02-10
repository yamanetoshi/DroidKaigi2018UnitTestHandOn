package us.shiroyama.android.my_repositories.hands_on_beginners.mockito.repository;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import java.util.ArrayList;
import java.util.List;

import us.shiroyama.android.my_repositories.hands_on_beginners.mockito.converter.TweetConverter;
import us.shiroyama.android.my_repositories.hands_on_beginners.mockito.entity.Tweet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Local Unit Test for {@link TweetRepositoryWithConverter}
 *
 * @author Fumihiko Shiroyama
 */

public class TweetRepositoryWithConverterTest {
  private TweetConverter converter;
  private TweetRepositoryWithConverter repository;

  private LocalTweetDataSource spy;

  /**
   * {@link Mockito#spy(Object)} を使ってテストスパイを作ってみよう。
   * テストスパイは {@link Mockito#verify(Object)} を使うことで、メソッド呼び出し等のインタラクションを検証することができる。
   */
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

  /**
   * {@link TweetRepositoryWithConverter#getTimeline()} は内部で {@link TweetConverter} を呼ばないはずだ。
   * せっかくなので「インタラクションがない」ことを検証してみよう。
   * <p>
   * ヒント: {@link Mockito#verify(Object, VerificationMode)} に <code>never()</code> を渡すことでインタラクションがないことを検証できる。
   */
  @Test
  public void getTimeline() throws Exception {
    repository.getTimeline();
    verify(converter, never()).convertList(null);
  }

  /**
   * {@link TweetRepositoryWithConverter#getTimelineBody()} では内部で {@link TweetConverter} とのインタラクションがあるはずだ。
   * <p>
   * ヒント: <code>verify(object, times(1))</code> のようにするとインタラクションがあったこととその回数も検証することができる。
   */
  @Test
  public void getTimelineBody() throws Exception {
    repository.getTimelineBody();
    verify(converter, times(1)).convertList(spy.getTimeline());
  }

}
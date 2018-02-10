package us.shiroyama.android.my_repositories.hands_on_beginners.mockito.converter;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import us.shiroyama.android.my_repositories.hands_on_beginners.mockito.entity.Tweet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Local Unit Test for {@link TweetConverter}
 *
 * @author Fumihiko Shiroyama
 */

public class TweetConverterTest {
  private TweetConverter converter;

  @Before
  public void setUp() throws Exception {
    converter = new TweetConverter();
  }

  /**
   * {@link Tweet} を {@link String} に正しく変換できることを検証するテストケースを書こう。
   */
  @Test
  public void convert() throws Exception {
    String str = "Alice";
    assertThat(converter.convert(Tweet.bodyOf(str))).isEqualTo(str);
  }

  /**
   * {@link Tweet#body} が空の場合に変換結果が空文字列であることを検証するテストケースを書こう。
   */
  @Test
  public void convert_inputEmpty_returnsEmpty() throws Exception {
    assertThat(converter.convert(Tweet.bodyOf(""))).isEqualTo("");
  }

  /**
   * {@link List<Tweet>} を {@link List<String>} に正しく変換できることを検証するテストケースを書こう。
   * <p>
   * ヒント: <code>isNotEmpty</code> や <code>hasSize</code> や <code>containsExactly</code> などのList用のアサーションを利用してみよう。
   */
  @Test
  public void convertList() throws Exception {
    String str1 = "Alice";
    String str2 = "Bob";

    List<Tweet> tweetList = new ArrayList<Tweet>();
    tweetList.add(Tweet.bodyOf(str1));
    tweetList.add(Tweet.bodyOf(str2));

    assertThat(converter.convertList(tweetList)).isNotEmpty();
    assertThat(converter.convertList(tweetList)).hasSize(tweetList.size());
    assertThat(converter.convertList(tweetList)).containsExactly(str1, str2);
  }

  /**
   * {@link List<Tweet>} が空リストの場合 {@link List<String>} も空リストになることを検証しよう。
   */
  @Test
  public void convertList_inputEmptyList_returnsEmptyList() throws Exception {
    List<Tweet> tweetList = new ArrayList<Tweet>();

    assertThat(converter.convertList(tweetList)).isEmpty();
  }

}
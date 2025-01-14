package kr.codesqaud.cafe.repository;

import kr.codesqaud.cafe.domain.Article;
import kr.codesqaud.cafe.domain.Reply;
import kr.codesqaud.cafe.domain.User;
import kr.codesqaud.cafe.domain.dto.ReplyWithUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
class MySQLReplyRepositoryTest {

    @Autowired
    private DataSource dataSource;
    private MySQLUserRepository userRepository;
    private H2DBArticleRepository articleRepository;
    private MySQLReplyRepository replyRepository;

    @BeforeEach
    void init() {
        userRepository = new MySQLUserRepository(dataSource);
        articleRepository = new H2DBArticleRepository(dataSource);
        replyRepository = new MySQLReplyRepository(dataSource);

        userRepository.save(new User("Hyun", "1234", "황현", "ghkdgus29@naver.com"));
        userRepository.save(new User("Yoon", "1234", "황윤", "ghkddbs28@naver.com"));

        articleRepository.save(new Article(1, "실화냐?", "미안하다 이거 보여주려고 어그로 끌었다."));
    }

    @AfterEach
    void clear() {
        JdbcTemplate template = new JdbcTemplate(dataSource);

        String sql = "delete from reply;" +
                "alter table reply alter column id restart with 1;" +
                "delete from article;" +
                "alter table article alter column id restart with 1;" +
                "delete from users;" +
                "alter table users alter column id restart with 1;";

        template.update(sql);
    }

    @Test
    @DisplayName("댓글이 제대로 저장된다.")
    void save() {
        Reply reply = new Reply("진짜임?", 2, 1);
        replyRepository.save(reply);

        List<ReplyWithUser> replies = replyRepository.findByArticleId(1);

        assertThat(replies.size()).isEqualTo(1);
        ReplyWithUser replyWithUser = replies.get(0);

        assertThat(replyWithUser.getUserName()).isEqualTo("Yoon");
        assertThat(replyWithUser.getContents()).isEqualTo("진짜임?");
    }

    @Test
    @DisplayName("댓글들을 모두 가져올 수 있다.")
    void findAll() {
        Reply reply1 = new Reply("진짜임?", 2, 1);
        Reply reply2 = new Reply("가짜임ㅋㅋ", 1, 1);

        replyRepository.save(reply1);
        replyRepository.save(reply2);

        List<ReplyWithUser> replies = replyRepository.findByArticleId(1);

        assertThat(replies.size()).isEqualTo(2);
        assertThat(replies.get(0).getUserName()).isEqualTo("Yoon");
        assertThat(replies.get(1).getUserName()).isEqualTo("Hyun");
    }

    @Test
    @DisplayName("댓글 id를 통해 특정 댓글 검색이 가능하다.")
    void findById() {
        Reply reply1 = new Reply("진짜임?", 2, 1);
        Reply reply2 = new Reply("가짜임ㅋㅋ", 1, 1);

        replyRepository.save(reply1);
        replyRepository.save(reply2);

        Reply findReply = replyRepository.findById(1);

        assertThat(findReply.getUserId()).isEqualTo(2);
        assertThat(findReply.getContents()).isEqualTo("진짜임?");
    }

    @Test
    @DisplayName("댓글 id를 통해 특정 댓글 삭제가 가능하다.")
    void delete() {
        Reply reply1 = new Reply("진짜임?", 2, 1);
        Reply reply2 = new Reply("가짜임ㅋㅋ", 1, 1);

        replyRepository.save(reply1);
        replyRepository.save(reply2);

        replyRepository.delete(1);

        assertThat(replyRepository.findByArticleId(1).size()).isEqualTo(1);
    }
}

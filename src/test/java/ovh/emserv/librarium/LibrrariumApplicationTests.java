package ovh.emserv.librarium;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "librarium.jwt.secret=REMOVED_TEST_KEY")
class LibrrariumApplicationTests {

    @Test
    void contextLoads() {
    }

}

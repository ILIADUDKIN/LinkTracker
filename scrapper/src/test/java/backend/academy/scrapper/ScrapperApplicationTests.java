package backend.academy.scrapper;

import backend.academy.scrapper.utils.LinkSourceUtil;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ScrapperApplicationTests {

    private LinkSourceUtil linkSourceUtilMock;
}

package br.com.coffeeandit.transaction.http;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ContextConfiguration(classes = {TransactionHttpService.class})
@TestPropertySource(properties = {"app.urlTransaction=http://server/v1/transaction/",
        "app.urlTransactionById=http://server/v1/transaction/%s"
})
public class TransactionHttpServiceTest {

    @MockBean
    private RestTemplate restTemplate;
    @BeforeEach
    public void setUp() {}

}

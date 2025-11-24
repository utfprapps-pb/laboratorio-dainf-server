package br.com.utfpr.gerenciamento.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ServerApplicationTests {

  @Test
  void contextLoads() {
    // se este teste falhar, a aplicação tem dependência não preenchida
  }
}

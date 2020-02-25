package br.com.utfpr.gerenciamento.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

//    public static void main(String[] args) {
//        System.out.println(new BCryptPasswordEncoder().encode("admin"));
//    }

}

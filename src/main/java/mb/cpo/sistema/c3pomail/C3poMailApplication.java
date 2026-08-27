package mb.cpo.sistema.c3pomail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da API c3pomail.
 * Inicializa o Spring Boot com Tomcat embutido na porta 8080.
 */
@SpringBootApplication
public class C3poMailApplication {

    public static void main(String[] args) {
        SpringApplication.run(C3poMailApplication.class, args);
    }
}

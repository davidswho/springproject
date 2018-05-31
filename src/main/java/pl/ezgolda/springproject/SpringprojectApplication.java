package pl.ezgolda.springproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import pl.ezgolda.springproject.config.AppConfig;
import pl.ezgolda.springproject.config.SecurityConfig;


@SpringBootApplication
@Import({AppConfig.class, SecurityConfig.class})
public class SpringprojectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringprojectApplication.class, args);
    }
}

package fr.ritonquilol.discord.shepard.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "fr.ritonquilol.discord.shepard")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
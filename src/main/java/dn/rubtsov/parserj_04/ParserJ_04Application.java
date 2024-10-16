package dn.rubtsov.parserj_04;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@SpringBootApplication
@EnableScheduling
public class ParserJ_04Application  {

    public static void main(String[] args) {
        SpringApplication.run(ParserJ_04Application.class, args);
    }
}
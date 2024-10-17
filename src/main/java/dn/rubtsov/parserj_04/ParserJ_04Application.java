package dn.rubtsov.parserj_04;

import dn.rubtsov.parserj_04.processor.DBUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@SpringBootApplication
@EnableScheduling
public class ParserJ_04Application  {
    @Autowired
    private DBUtils dbUtils;

    @PostConstruct
    void init(){
        // Удаляем и вновь создаем таблицу БД
        dbUtils.dropTableIfExists("message_db");
        dbUtils.createTableIfNotExists("message_db");
    }

    public static void main(String[] args) {
        SpringApplication.run(ParserJ_04Application.class, args);
    }
}
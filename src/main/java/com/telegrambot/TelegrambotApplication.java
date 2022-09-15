package com.telegrambot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.ApiContextInitializer;
//@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@SpringBootApplication
@Slf4j
public class TelegrambotApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();

        log.info("Api Context init: =================");
        SpringApplication.run(TelegrambotApplication.class);
    }

}

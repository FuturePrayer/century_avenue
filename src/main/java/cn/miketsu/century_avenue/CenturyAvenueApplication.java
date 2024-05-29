package cn.miketsu.century_avenue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.util.Loggers;

/**
 * @author sihuangwlp
 * @date 2024/5/22
 * @since 0.0.1-SNAPSHOT
 */
@SpringBootApplication
public class CenturyAvenueApplication {

    public static void main(String[] args) {
        Loggers.useSl4jLoggers();
        SpringApplication.run(CenturyAvenueApplication.class, args);
    }

}

package com.allinone.DevView.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
public class FlywayRunner implements ApplicationRunner {

    private final DataSource dataSource;

    public FlywayRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Flyway.configure()
                .dataSource(dataSource)               // 순수 DataSource만 사용
                .locations("classpath:db/migration")  // 스크립트 경로
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }
}

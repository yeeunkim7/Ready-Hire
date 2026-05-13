package com.devinterview.api.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Supabase Pooler 환경에서 Flyway 전용 URL로 만든 보조 DataSource는 Hikari 확장 속성을 물려받지 않아
 * 프로토콜 오류가 납니다. 자동 구성을 끄고 애플리케이션 {@link DataSource} 하나로 마이그레이션합니다.
 */
@Configuration
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "false")
public class FlywayConfig {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load();
    }

    @Bean
    public FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway);
    }
}

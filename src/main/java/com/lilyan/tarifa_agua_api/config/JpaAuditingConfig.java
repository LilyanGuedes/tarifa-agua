package com.lilyan.tarifa_agua_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//Ativa o Auditing do jpa
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}

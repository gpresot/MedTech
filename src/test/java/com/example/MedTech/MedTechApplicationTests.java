package com.example.MedTech;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Usa H2 em memória: o teste não pode depender do Supabase nem de variáveis de ambiente.
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:contexto;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MedTechApplicationTests {

    @Test
    void contextLoads() {
    }
}

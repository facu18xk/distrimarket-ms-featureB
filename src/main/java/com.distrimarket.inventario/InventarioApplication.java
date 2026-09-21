package com.distrimarket.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.distrimarket.commons",   // Escanea componentes comunes
        "com.distrimarket.inventario" // Escanea tu microservicio
})
@EntityScan(basePackages = {
        "com.distrimarket.commons.entity",
        //"com.distrimarket.inventario.entity"
})
@EnableJpaRepositories(basePackages = {
        //"com.distrimarket.commons.repository",
        "com.distrimarket.inventario.repository"
})
@EnableFeignClients
public class InventarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventarioApplication.class, args);
    }
}
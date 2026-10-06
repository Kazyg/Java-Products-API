package com.hackerrank.sample.config;

import com.hackerrank.sample.model.Model;
import com.hackerrank.sample.repository.ModelRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
public class DataInitializer {
    @Bean
    public CommandLineRunner loadSampleData(ModelRepository modelRepository) {
        return args -> {
            if (modelRepository.count() > 0) {
                return;
            }

            Model iphone15 = new Model();
            iphone15.setName("iPhone 15");
            iphone15.setUrlImage("https://example.com/images/iphone15.png");
            iphone15.setDescription("Smartphone Apple com tela Super Retina XDR e chip A16 Bionic.");
            iphone15.setPrice(BigDecimal.valueOf(5299L));
            iphone15.setRating(5);
            iphone15.setSpecifications("128GB, 6GB RAM, Câmera 48MP");

            Model galaxyS24 = new Model();
            galaxyS24.setName("Galaxy S24");
            galaxyS24.setUrlImage("https://example.com/images/galaxys24.png");
            galaxyS24.setDescription("Smartphone Samsung com Galaxy AI e tela AMOLED 120Hz.");
            galaxyS24.setPrice(BigDecimal.valueOf(4699L));
            galaxyS24.setRating(4);
            galaxyS24.setSpecifications("256GB, 8GB RAM, Câmera tripla 50MP");

            Model redmiNote13 = new Model();
            redmiNote13.setName("Redmi Note 13");
            redmiNote13.setUrlImage("https://example.com/images/redminote13.png");
            redmiNote13.setDescription("Smartphone Xiaomi com ótimo custo-benefício e bateria duradoura.");
            redmiNote13.setPrice(BigDecimal.valueOf(1899L));
            redmiNote13.setRating(4);
            redmiNote13.setSpecifications("256GB, 8GB RAM, Bateria 5000mAh");

            Model motoG84 = new Model();
            motoG84.setName("Moto G84");
            motoG84.setUrlImage("https://example.com/images/motog84.png");
            motoG84.setDescription("Smartphone Motorola com tela pOLED e som Dolby Atmos.");
            motoG84.setPrice(BigDecimal.valueOf(1599L));
            motoG84.setRating(4);
            motoG84.setSpecifications("256GB, 8GB RAM, Snapdragon 695");

            modelRepository.saveAll(List.of(iphone15, galaxyS24, redmiNote13, motoG84));
        };
    }
}

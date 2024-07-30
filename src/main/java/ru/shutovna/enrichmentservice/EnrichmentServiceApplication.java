package ru.shutovna.enrichmentservice;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.shutovna.enrichmentservice.data.DataService;

@SpringBootApplication
public class EnrichmentServiceApplication {
    private DataService dataService;

    public static void main(String[] args) throws ParseException {
        SpringApplication.run(EnrichmentServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            dataService.addUser("89966366429", 1L, "Nikita", "Perov");
            dataService.addUser("89966366423", 2L, "Nikita", "Shutov");
            dataService.addUser("89966366421", 3L, "Vasya", "Chernov");
            dataService.addUser("89966366422", 4L, "Alex", "Terrible");
            dataService.addUser("88005553535", 5L, "Ilon", "Mask");
        };
    }

    @Autowired
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }
}

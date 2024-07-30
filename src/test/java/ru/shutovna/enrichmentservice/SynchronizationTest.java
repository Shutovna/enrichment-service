package ru.shutovna.enrichmentservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.shutovna.enrichmentservice.data.DataService;
import ru.shutovna.enrichmentservice.movel.Message;
import ru.shutovna.enrichmentservice.movel.User;
import ru.shutovna.enrichmentservice.service.EnrichmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Phaser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@Slf4j
public class SynchronizationTest {
    public static final int NUMBER_OF_WORKERS = 10;
    public static final int NUMBER_OF_PHASES = 10;
    public static final int NUMBER_OF_INSERTS = 10000;
    @Autowired
    private EnrichmentService enrichmentService;

    @Autowired
    private DataService dataService;

    private JSONParser parser;

    private List<Exception> threadExceptions;

    @BeforeEach
    void setUp() {
        parser = new JSONParser();
        threadExceptions = new ArrayList<>();
    }

    @Test
    public void test() {
        Phaser phaser = new Phaser(1); // "1" для регистрации главного потока

        for (int i = 0; i < NUMBER_OF_WORKERS; i++) {
            phaser.register(); // Регистрация нового участника
            new Thread(new Worker(phaser), "Worker-" + i).start();
        }

        for (int phase = 0; phase < NUMBER_OF_PHASES; phase++) {
            // Ожидаем завершения текущей фазы всеми участниками
            phaser.arriveAndAwaitAdvance();
            log.info("Phase " + phase + " completed.");
        }

        phaser.arriveAndDeregister(); // Главный поток завершает свою работу

        if(!threadExceptions.isEmpty()) {
            fail(threadExceptions.size() + " exceptions was thrown");
        }
    }

    class Worker implements Runnable {
        private final Phaser phaser;

        Worker(Phaser phaser) {
            this.phaser = phaser;
        }

        @Override
        public void run() {
            try {
                for (int phase = 0; phase < NUMBER_OF_PHASES; phase++) {
                    log.info(Thread.currentThread().getName() + " working in phase " + phase);
                    // Симуляция работы
                    try {
                        for (int i = 0; i < NUMBER_OF_INSERTS; i++) {
                            String firstName = RandomStringUtils.randomAlphabetic(10);
                            String lastName = RandomStringUtils.randomAlphabetic(10);
                            String msisdn = RandomStringUtils.randomNumeric(11);
                            String field1 = RandomStringUtils.randomAlphabetic(20);
                            String field2 = RandomStringUtils.randomAlphabetic(10);
                            long id = new Random().nextInt(100000);

                            dataService.addUser(msisdn, id, firstName, lastName);

                            String json = getRequestJson().formatted(field1, field2, msisdn);

                            Message msg = new Message(json, Message.EnrichmentType.MSISDN);
                            String res = enrichmentService.enrich(msg);

                            JSONObject jsonResult = getJsonObject(res);

                            String jsonExpected = getExpectedJson().formatted(field1, field2, msisdn, firstName, lastName);
                            JSONObject expectedJsonObject = getJsonObject(jsonExpected);

                            assertEquals(expectedJsonObject, jsonResult);

                            User dbUser = dataService.getUserByMSISDN(msisdn);
                            assertEquals(id, dbUser.getId());
                            assertEquals(firstName, dbUser.getFirstName());
                            assertEquals(lastName, dbUser.getLastName());

                        }

                        Thread.sleep((long) (Math.random() * 10));


                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        threadExceptions.add(e);
                    }
                    // Указываем, что поток завершил фазу и ждем остальных
                    phaser.arriveAndAwaitAdvance();
                }
            } finally {
                phaser.arriveAndDeregister(); // Участник завершает участие в фазах
            }

        }
    }

    private synchronized JSONObject getJsonObject(String str) {
        JSONObject jsonResult = null;
        try {
            jsonResult = (JSONObject) parser.parse(str);
        } catch (Exception e) {
            log.error("Cannot parse json str " + str);
            throw new RuntimeException(e);
        }
        return jsonResult;
    }

    private String getRequestJson() {
        return """
                {
                    "action": "%s",
                    "page": "%s",
                    "msisdn": "%s"
                    }
                """;
    }

    private String getExpectedJson() {
        return """
                {
                    "action": "%s",
                    "page": "%s",
                    "msisdn": "%s",
                    "enrichment": {
                        "firstName": "%s",
                        "lastName": "%s"
                    }
                }""";
    }
}


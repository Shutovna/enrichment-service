package ru.shutovna.enrichmentservice;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.shutovna.enrichmentservice.movel.Message;
import ru.shutovna.enrichmentservice.service.EnrichmentService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
public class EnrichmentServiceIT {
    @Autowired
    private EnrichmentService enrichmentService;
    private JSONParser parser;

    @BeforeEach
    void setUp() {
        parser = new JSONParser();
    }

    @Test
    void testInit() {
        assertNotNull(enrichmentService);
    }

    @Test
    void validData_returnEnrichedMessage() throws ParseException {

        String jsonRequest = """
                {
                    "action": "button_click",
                    "page": "book_card",
                    "msisdn": "88005553535"
                    }
                """;
        JSONObject jsonExpected = getExpectedJsonObject();


        Message message = new Message(jsonRequest, Message.EnrichmentType.MSISDN);
        String result = enrichmentService.enrich(message);
        JSONObject jsonResult = (JSONObject) parser.parse(result);

        assertEquals(jsonExpected, jsonResult);
    }

    private JSONObject getExpectedJsonObject() throws ParseException {
        JSONObject jsonExpected = (JSONObject) parser.parse("""
                {
                    "action": "button_click",
                    "page": "book_card",
                    "msisdn": "88005553535",
                    "enrichment": {
                        "firstName": "Ilon",
                        "lastName": "Mask"
                    }
                }"""
        );
        return jsonExpected;
    }
}

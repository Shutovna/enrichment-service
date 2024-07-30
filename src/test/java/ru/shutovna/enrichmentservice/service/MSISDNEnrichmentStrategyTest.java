package ru.shutovna.enrichmentservice.service;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.shutovna.enrichmentservice.data.DataService;
import ru.shutovna.enrichmentservice.movel.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MSISDNEnrichmentStrategyTest {

    @Mock
    private DataService dataService;

    @InjectMocks
    private MSISDNEnrichmentStrategy enrichmentStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEnrichJSONMessage() {
        // Создание тестовых данных
        String msisdn = "1234567890";
        User user = new User(1L, "John", "Doe");

        // Создание входного JSON объекта
        JSONObject inputJson = new JSONObject();
        inputJson.put("msisdn", msisdn);
        inputJson.put("otherField", "value");

        // Мокирование поведения DataService
        when(dataService.getUserByMSISDN(msisdn)).thenReturn(user);

        // Вызов метода enrichJSONMessage
        JSONObject result = enrichmentStrategy.enrichJSONMessage(inputJson);

        // Проверка результатов
        assertNotNull(result);
        assertEquals(msisdn, result.get("msisdn"));
        assertEquals("value", result.get("otherField"));

        JSONObject enrichment = (JSONObject) result.get("enrichment");
        assertNotNull(enrichment);
        assertEquals("John", enrichment.get("firstName"));
        assertEquals("Doe", enrichment.get("lastName"));

        // Verify that the DataService's getUserByMSISDN method was called once
        verify(dataService, times(1)).getUserByMSISDN(msisdn);
    }

    @Test
    void testEnrichJSONMessage_NoMSISDN() {
        // Создание входного JSON объекта без MSISDN
        JSONObject inputJson = new JSONObject();
        inputJson.put("otherField", "value");

        // Проверка, что метод выбрасывает IllegalArgumentException
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            enrichmentStrategy.enrichJSONMessage(inputJson);
        });

        assertEquals("Type of message is not msisdn", thrown.getMessage());
    }
}

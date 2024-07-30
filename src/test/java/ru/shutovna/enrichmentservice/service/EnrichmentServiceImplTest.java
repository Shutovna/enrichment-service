package ru.shutovna.enrichmentservice.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.shutovna.enrichmentservice.data.DataService;
import ru.shutovna.enrichmentservice.movel.Message;
import ru.shutovna.enrichmentservice.movel.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EnrichmentServiceImplTest {

    @Mock
    private DataService dataService;

    @InjectMocks
    private EnrichmentServiceImpl enrichmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEnrich_Success() throws Exception {
        // Создание тестовых данных
        String jsonContent = "{\"msisdn\": \"1234567890\", \"otherField\": \"value\"}";
        Message message = new Message(jsonContent, Message.EnrichmentType.MSISDN);

        // Мокирование поведения DataService
        User user = new User(1L, "John", "Doe");
        when(dataService.getUserByMSISDN("1234567890")).thenReturn(user);

        // Замена getEnrichmentStrategy на мок-объект
        EnrichmentStrategy mockStrategy = mock(MSISDNEnrichmentStrategy.class);
        JSONObject enrichedJson = new JSONObject();
        enrichedJson.put("msisdn", "1234567890");
        enrichedJson.put("otherField", "value");
        JSONObject enrichment = new JSONObject();
        enrichment.put("firstName", "John");
        enrichment.put("lastName", "Doe");
        enrichedJson.put("enrichment", enrichment);
        when(mockStrategy.enrichJSONMessage(any(JSONObject.class))).thenReturn(enrichedJson);
        enrichmentService = new EnrichmentServiceImpl() {
            @Override
            public EnrichmentStrategy getEnrichmentStrategy(JSONObject jsonObject) {
                return mockStrategy;
            }
        };
        enrichmentService.setDataService(dataService);

        // Вызов метода enrich
        String result = enrichmentService.enrich(message);

        // Проверка результатов
        assertNotNull(result);
        assertTrue(result.contains("enrichment"));
        assertTrue(result.contains("John"));
        assertTrue(result.contains("Doe"));

        // Проверка взаимодействия с dataService
        verify(dataService, times(1)).addEnrichedMessage(message);
        verify(dataService, never()).addNotEnrichedMessage(any(Message.class));
    }

    @Test
    void testEnrich_ParsingException() throws Exception {
        // Создание тестовых данных
        String invalidJsonContent = "{\"msisdn\": \"1234567890\", \"otherField\": \"value\"";
        Message message = new Message(invalidJsonContent, Message.EnrichmentType.MSISDN);

        // Вызов метода enrich
        String result = enrichmentService.enrich(message);

        // Проверка результатов
        assertEquals(invalidJsonContent, result);

        // Проверка взаимодействия с dataService
        verify(dataService, times(1)).addNotEnrichedMessage(message);
        verify(dataService, never()).addEnrichedMessage(any(Message.class));
    }

    @Test
    void testEnrich_NoStrategy() {
        // Создание входного JSON объекта без MSISDN
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("otherField", "value");
        Message message = new Message(jsonObject.toJSONString(), Message.EnrichmentType.MSISDN);

        // Замена getEnrichmentStrategy на мок-объект, который выбрасывает исключение
        enrichmentService = new EnrichmentServiceImpl() {
            @Override
            protected EnrichmentStrategy getEnrichmentStrategy(JSONObject jsonObject) {
                throw new IllegalStateException("No strategy");
            }
        };
        enrichmentService.setDataService(dataService);

        // Вызов метода enrich
        String result = enrichmentService.enrich(message);

        // Проверка результатов
        assertEquals(message.getContent(), result);

        // Проверка взаимодействия с dataService
        verify(dataService, times(1)).addNotEnrichedMessage(message);
        verify(dataService, never()).addEnrichedMessage(any(Message.class));
    }
}

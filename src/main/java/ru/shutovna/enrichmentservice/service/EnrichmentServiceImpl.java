package ru.shutovna.enrichmentservice.service;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.shutovna.enrichmentservice.data.DataService;
import ru.shutovna.enrichmentservice.movel.Message;

@Service
@Slf4j
public class EnrichmentServiceImpl implements EnrichmentService {
    private DataService dataService;

    @Override
    public String enrich(Message message) {
        JSONParser parser = new JSONParser();

        try {
            // Преобразование JSON строки в объект
            JSONObject jsonObject = (JSONObject) parser.parse(message.getContent());

            EnrichmentStrategy strategy = getEnrichmentStrategy(jsonObject);
            JSONObject resultJsonObject = strategy.enrichJSONMessage(jsonObject);
            dataService.addEnrichedMessage(message);

            return resultJsonObject.toJSONString();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            dataService.addNotEnrichedMessage(message);
            return message.getContent();
        }
    }

    protected EnrichmentStrategy getEnrichmentStrategy(JSONObject jsonObject) {
        String msisdn = (String) jsonObject.get("msisdn");
        if (msisdn != null) {
            return new MSISDNEnrichmentStrategy(dataService);
        }
        throw new IllegalStateException("No strategy");
    }

    @Autowired
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }
}

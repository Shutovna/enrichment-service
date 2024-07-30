package ru.shutovna.enrichmentservice.service;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import ru.shutovna.enrichmentservice.data.DataService;
import ru.shutovna.enrichmentservice.movel.User;

@Slf4j
public class MSISDNEnrichmentStrategy implements EnrichmentStrategy {
    private DataService dataService;

    public MSISDNEnrichmentStrategy(DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public JSONObject enrichJSONMessage(JSONObject jsonObject) {
        String msisdn = (String) jsonObject.get("msisdn");
        if (msisdn == null) {
            throw new IllegalArgumentException("Type of message is not msisdn");
        }

        log.debug("Enriching by msisdn=" + msisdn + " object=" + jsonObject + "");

        User user = dataService.getUserByMSISDN(msisdn);

        JSONObject outputJson = new JSONObject();
        outputJson.putAll(jsonObject);
        JSONObject enrichment = new JSONObject();
        enrichment.put("firstName", user.getFirstName());
        enrichment.put("lastName", user.getLastName());
        outputJson.put("enrichment", enrichment);

        log.debug("Result object=" + outputJson.toJSONString());

        return outputJson;
    }
}

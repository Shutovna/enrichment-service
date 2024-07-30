package ru.shutovna.enrichmentservice.service;

import org.json.simple.JSONObject;

public interface EnrichmentStrategy {
    JSONObject enrichJSONMessage(JSONObject jsonObject);
}

package ru.shutovna.enrichmentservice.service;

import ru.shutovna.enrichmentservice.movel.Message;

public interface EnrichmentService {
    String enrich(Message message);
}

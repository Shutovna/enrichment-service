package ru.shutovna.enrichmentservice.data;

import ru.shutovna.enrichmentservice.movel.Message;
import ru.shutovna.enrichmentservice.movel.User;

public interface DataService {
    User getUserByMSISDN(String msisdn);

    void addUser(String msisdn, Long id, String firstname, String lastName);

    void removeUser(String msisdn);

    void addEnrichedMessage(Message message);

    void addNotEnrichedMessage(Message message);
}

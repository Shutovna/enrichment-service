package ru.shutovna.enrichmentservice.data;

import org.springframework.stereotype.Service;
import ru.shutovna.enrichmentservice.movel.Message;
import ru.shutovna.enrichmentservice.movel.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DataServiceImpl implements DataService {
    protected final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    protected final Set<Message> enrichedMessages = Collections.synchronizedSet(new HashSet<>());

    protected final Set<Message> notEnrichedMessages = Collections.synchronizedSet(new HashSet<>());


    @Override
    public User getUserByMSISDN(String msisdn) {
        User user = users.get(msisdn);
        if (user == null) {
            throw new IllegalArgumentException("User " + msisdn + " not found");
        }
        return user;
    }

    @Override
    public void addUser(String msisdn, Long id, String firstname, String lastName) {
        users.put(msisdn, new User(id, firstname, lastName));
    }

    @Override
    public void removeUser(String msisdn) {
        User user = users.remove(msisdn);
        if (user == null) {
            throw new IllegalArgumentException("User " + msisdn + " not found");
        }
    }

    @Override
    public void addEnrichedMessage(Message message) {
        this.enrichedMessages.add(message);
    }

    @Override
    public void addNotEnrichedMessage(Message message) {
        this.notEnrichedMessages.add(message);
    }
}

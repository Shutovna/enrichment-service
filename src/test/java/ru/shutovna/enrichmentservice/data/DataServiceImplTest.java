package ru.shutovna.enrichmentservice.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.shutovna.enrichmentservice.data.DataServiceImpl;
import ru.shutovna.enrichmentservice.movel.Message;
import ru.shutovna.enrichmentservice.movel.User;

import static org.junit.jupiter.api.Assertions.*;

class DataServiceImplTest {

    private DataServiceImpl dataService;

    @BeforeEach
    void setUp() {
        dataService = new DataServiceImpl();
    }

    @Test
    void testAddUserAndGetUserByMSISDN() {
        String msisdn = "1234567890";
        Long id = 1L;
        String firstname = "John";
        String lastName = "Doe";

        dataService.addUser(msisdn, id, firstname, lastName);
        User user = dataService.getUserByMSISDN(msisdn);

        assertNotNull(user);
        assertEquals(id, user.getId());
        assertEquals(firstname, user.getFirstName());
        assertEquals(lastName, user.getLastName());
    }

    @Test
    void testGetUserByMSISDN_UserNotFound() {
        String msisdn = "1234567890";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dataService.getUserByMSISDN(msisdn);
        });

        assertEquals("User " + msisdn + " not found", exception.getMessage());
    }

    @Test
    void testRemoveUser() {
        String msisdn = "1234567890";
        Long id = 1L;
        String firstname = "John";
        String lastName = "Doe";

        dataService.addUser(msisdn, id, firstname, lastName);
        dataService.removeUser(msisdn);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dataService.getUserByMSISDN(msisdn);
        });

        assertEquals("User " + msisdn + " not found", exception.getMessage());
    }

    @Test
    void testRemoveUser_UserNotFound() {
        String msisdn = "1234567890";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            dataService.removeUser(msisdn);
        });

        assertEquals("User " + msisdn + " not found", exception.getMessage());
    }

    @Test
    void testAddEnrichedMessage() {
        Message message = new Message("Hello, world!", Message.EnrichmentType.MSISDN);

        dataService.addEnrichedMessage(message);

        // Add a way to check if the message was added
        assertTrue(dataService.enrichedMessages.contains(message));
    }

    @Test
    void testAddNotEnrichedMessage() {
        Message message = new Message("Hello, world!", Message.EnrichmentType.MSISDN);

        dataService.addNotEnrichedMessage(message);

        // Add a way to check if the message was added
        assertTrue(dataService.notEnrichedMessages.contains(message));
    }
}

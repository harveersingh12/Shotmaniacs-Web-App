package routes;
import jakarta.ws.rs.core.Response;
import nl.utwente.mod4.shotmaniacs.dao.DatabaseConnection;
import nl.utwente.mod4.shotmaniacs.model.Event;
import nl.utwente.mod4.shotmaniacs.model.EventResponse;
import nl.utwente.mod4.shotmaniacs.routes.AuthRoute;
import nl.utwente.mod4.shotmaniacs.routes.EventRoute;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//
//import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class EventTest {

    private EventRoute route;

    @BeforeAll
    public static void init() {
        new DatabaseConnection();
    }

    /**
     *
     */

    @BeforeEach
    public void setUp() {
        route = new EventRoute();
    }

    /**
     *
     */

    @Test
    public void testCreateEvent() {
       Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            Event newEvent = new Event("test", "type1", "2003-06-01T10:00", "ro", 3,
                    "clinet1", "test@gmail.com", "ONGOING", false);
            route.createEvent(newEvent);

            List<EventResponse> listOfEvents = route.getAllEvents();
            EventResponse lastEvent = listOfEvents.stream()
                    .filter(event -> event.getName().equals(newEvent.getEventName()) && event.getDate().equals(newEvent.getEventDate()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Newly created event not found in the list"));
            assertEquals(newEvent.getEventName(), lastEvent.getName());
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database or executing the query.", e);
        }
    }

    /**
     *
     */

    @Test
    public void testUpdateEvent() {
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {

            Event newEvent = new Event("test", "type1", "2003-06-01T10:00", "ro", 3,
                    "clinet1", "cline1@gmail.com", "ONGOING", false);
            route.createEvent(newEvent);

            List<EventResponse> listOfEvents = route.getAllEvents();
            EventResponse lastEvent = listOfEvents.get(3);

            int eventIdToUpdate = lastEvent.getEid();
            HashMap<String, Object> updatedData = new HashMap<>();
            updatedData.put("name", "updated event name");
            updatedData.put("location", "updated location");

            Response response = route.updateEvent(eventIdToUpdate, updatedData);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(), "Event update should return OK");

            List<EventResponse> updatedEvents = route.getAllEvents();
            EventResponse updatedEvent = updatedEvents.stream()
                    .filter(event -> event.getEid() == eventIdToUpdate)
                    .findFirst()
                    .orElse(null);

            assertNotNull(updatedEvent, "Updated event should not be null");
            assertEquals("updated event name", updatedEvent.getName(), "Event name should be updated");
            assertEquals("updated location", updatedEvent.getLocation(), "Event location should be updated");

        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database or executing the query.", e);
        }
    }

    /**
     *
     */
    @Test
    public void testDeleteEvent() {
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {

            Event newEvent = new Event("test3", "type1", "2003-06-01T10:00", "ro", 3,
                    "clinet1", "cline1@gmail.com", "ONGOING", false);
            route.createEvent(newEvent);

            List<EventResponse> listOfEvents = route.getAllEvents();
            EventResponse eventToDelete = listOfEvents.stream()
                    .filter(event -> event.getName().equals(newEvent.getEventName()) && event.getDate().equals(newEvent.getEventDate()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Newly created event not found in the list"));

            int eventIdToDelete = eventToDelete.getEid();

            Response response = route.declineEvent(eventIdToDelete);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(), "Event delete should return OK");

            List<EventResponse> remainingEvents = route.getAllEvents();
            boolean eventExists = remainingEvents.stream()
                    .anyMatch(event -> event.getEid() == eventIdToDelete);

            assertFalse(eventExists, "Deleted event should not exist in the list");

        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database or executing the query.", e);
        }
    }
}

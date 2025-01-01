package routes;

import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import nl.utwente.mod4.shotmaniacs.dao.DatabaseConnection;
import nl.utwente.mod4.shotmaniacs.model.*;
import nl.utwente.mod4.shotmaniacs.routes.AuthRoute;
import nl.utwente.mod4.shotmaniacs.routes.ContractRoute;
import nl.utwente.mod4.shotmaniacs.routes.CrewmembersRoute;
import nl.utwente.mod4.shotmaniacs.routes.EventRoute;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContractTest {


    private ContractRoute route;
    private EventRoute eventRoute;
    private AuthRoute authRoute;
    private CrewmembersRoute crewMemberRoute;
    private int eventIdInContract;
    @BeforeAll
    public static void init() {
        new DatabaseConnection();
    }

    /**
     *
     */

    @BeforeEach
    public void setUp() {
        route = new ContractRoute();
        eventRoute = new EventRoute();
        authRoute = new AuthRoute();
        crewMemberRoute = new CrewmembersRoute();
    }

    /**
     *
     * @return
     */

    private Response createFakeContract() {
        HashMap<String, String> newContract = new HashMap<>();
        Event e = new Event("testevent","festival","2024-08-12T18:00",
                "location",1,
                "hello","hello@gello.com","ONGOING",false);
        eventRoute.createEvent(e);

        List<EventResponse> listOfEvents = eventRoute.getAllEvents();
        EventResponse eventToDelete = listOfEvents.stream()
                .filter(event -> event.getName().equals(e.getEventName()) && event.getDate().equals(e.getEventDate()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Newly created event not found in the list"));

        eventIdInContract = eventToDelete.getEid();

        newContract.put("eid", String.valueOf(eventIdInContract));

        HashMap<String, String> newMember = new HashMap<>();
        newMember.put("name", "testuser" + Math.random()*100);
        newMember.put("email", "testuser@example.com");
        newMember.put("password", "TestPassword1!");
        newMember.put("role", "crewmember");
        newMember.put("job", "jobtitle");
        newMember.put("image", Base64.getEncoder().encodeToString("dummyImageData".getBytes()));

        authRoute.registerCrewMember(newMember);

        String cmid = String.valueOf(crewMemberRoute.getCmidFromName("testuser2"));

        newContract.put("cmid", cmid);
        Response rsp = route.createContract(newContract);
        return rsp;
    }

    /**
     *
     */

    @Test
    public void testCreateContract() {
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {

            Response rsp = createFakeContract();
            assertEquals(200, rsp.getStatus());
            assertEquals(200, route.getEnrolledCrewMembers(eventIdInContract).getStatus() , "Enrolled crew members should return OK");

        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database or executing the query.", e);
        }
    }

    /**
     *
     */

    @Test
    public void testDeleteContract() {
        Response rsp = createFakeContract();
        assertEquals(200, route.getEnrolledCrewMembers(eventIdInContract).getStatus() , "Enrolled crew members should return OK");
        Response rsp2 = route.deleteContract(eventIdInContract);
        assertEquals(200, rsp2.getStatus());
        Object obj = route.getEnrolledCrewMembers(eventIdInContract).getEntity();
        assertEquals("[]", obj.toString());
    }
}

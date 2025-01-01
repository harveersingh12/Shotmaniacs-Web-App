package nl.utwente.mod4.shotmaniacs.routes;

import jakarta.validation.constraints.Email;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import nl.utwente.mod4.shotmaniacs.Utils;
import nl.utwente.mod4.shotmaniacs.dao.DatabaseConnection;
import nl.utwente.mod4.shotmaniacs.model.Client;
import nl.utwente.mod4.shotmaniacs.model.Event;
import nl.utwente.mod4.shotmaniacs.model.EventResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import nl.utwente.mod4.shotmaniacs.routes.EmailRoute;

@Path("/event")
public class EventRoute {
    /**
     * Endpoint to create a new event in the database.
     *
     * @param event The event object containing details of the event to be created.
     * @return The created event object.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Event createEvent(Event event) {
        Client client = new Client(event.getClientName(), event.getClientEmail());

        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            String checkClientQuery = "SELECT cid FROM client WHERE name = ? AND email = ?";
            PreparedStatement checkClientStmt = connection.prepareStatement(checkClientQuery);
            checkClientStmt.setString(1, client.getName());
            checkClientStmt.setString(2, client.getEmail());
            ResultSet checkClientResult = checkClientStmt.executeQuery();

            int clientCid;
            if (checkClientResult.next()) {
                clientCid = checkClientResult.getInt("cid");
            } else {
                String getLastCidQuery = "SELECT MAX(cid) AS last_cid FROM client;";
                PreparedStatement getLastCidStmt = connection.prepareStatement(getLastCidQuery);
                ResultSet getLastCidResult = getLastCidStmt.executeQuery();

                int lastCid = 0;
                if (getLastCidResult.next()) {
                    lastCid = getLastCidResult.getInt("last_cid");
                }
                int newCid = lastCid + 1;

                String insertClientQuery = "INSERT INTO client (cid, name, email) VALUES (?, ?, ?)";
                PreparedStatement insertClientStmt = connection.prepareStatement(insertClientQuery);
                insertClientStmt.setInt(1, newCid);
                insertClientStmt.setString(2, client.getName());
                insertClientStmt.setString(3, client.getEmail());
                insertClientStmt.executeUpdate();
                clientCid = newCid;
            }

            String getLastEidQuery = "SELECT MAX(eid) AS last_eid FROM event;";
            PreparedStatement getLastEidStmt = connection.prepareStatement(getLastEidQuery);
            ResultSet getLastEidResult = getLastEidStmt.executeQuery();

            int lastEid = 0;
            if (getLastEidResult.next()) {
                lastEid = getLastEidResult.getInt("last_eid");
            }
            int newEid = lastEid + 1;

            String insertEventQuery = "INSERT INTO event(eid, cid, name," +
                    " type, date, location, status, bookingtype, duration)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertEventStmt = connection.prepareStatement(insertEventQuery);
            insertEventStmt.setInt(1, newEid);
            insertEventStmt.setInt(2, clientCid);
            insertEventStmt.setString(3, event.getEventName());
            insertEventStmt.setString(4, event.getEventType());
            insertEventStmt.setString(5, event.getEventDate());
            insertEventStmt.setString(6, event.getEventLocation());
            insertEventStmt.setString(7, "ONGOING");
            insertEventStmt.setString(8, "NOTSET");
            insertEventStmt.setInt(9, event.getEventDuration());
            int rowsUpdated = insertEventStmt.executeUpdate();
        } catch (SQLException sqlError) {
            System.err.println("Error connecting: " + sqlError);
        }

        return event;
    }

    /**
     * Retrieves all event objects in the database.
     *
     * @return a list of type EventResponse
     * (includes more attributes than can be set when creating an event object),
     * it contains all details of every event object in the database.
     */

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<EventResponse> getAllEvents() {
        return getEvents(
                "SELECT *, c.email as client_email," + " c.name as client_name FROM event e JOIN client c ON e.cid = c.cid");
    }

    /**
     * Queries the database for all events of a crew member by extracting the name
     * from the authorization header.
     *
     * @param authorizationHeader contains a token which is used to identify the crew member
     * @return list of type EventResponse containing all event details for
     * every event object linked to a crew member
     */
    @GET
    @Path("/crew")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EventResponse> getEventsOfCrewMember(@HeaderParam("Authorization") String authorizationHeader) {
        String crewMemberName = Utils.extractNameFromHeader(authorizationHeader);
        List<EventResponse> response = new ArrayList<>();
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            String query = "SELECT *, c.email as client_email, " +
                    "c.name as client_name FROM event e JOIN client c ON e.cid = c.cid" +
                    " JOIN contract co ON co.eid = e.eid JOIN crewmember cm ON cm.cmid = co.cmid"
                    + " WHERE cm.name=? ORDER BY e.date DESC;";

            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, crewMemberName);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                EventResponse eventResponse = new EventResponse();
                eventResponse.setEid(resultSet.getInt("eid"));
                eventResponse.setClientName(resultSet.getString("client_name"));
                eventResponse.setClientEmail(resultSet.getString("client_email"));
                eventResponse.setName(resultSet.getString("name"));
                eventResponse.setType(resultSet.getString("type"));
                eventResponse.setDate(resultSet.getString("date"));
                eventResponse.setLocation(resultSet.getString("location"));
                eventResponse.setStatus(resultSet.getString("status"));
                eventResponse.setBookingtype(resultSet.getString("bookingtype"));
                eventResponse.setDuration(resultSet.getInt("duration"));
                eventResponse.setIsaccepted(resultSet.getBoolean("isaccepted"));

                int cmid = resultSet.getInt("productionmanager");
                String name = cmid == 0 ? "unset" : CrewmembersRoute.getNameFromCmid(cmid);

                eventResponse.setProductionmanager(name);
                eventResponse.setMaxmembers(resultSet.getInt("maxmembers"));
                eventResponse.setCurrentmembers(resultSet.getInt("currentmembers"));

                response.add(eventResponse);
            }
        } catch (SQLException sqlError) {
            System.err.println("Error connecting: " + sqlError);
        }

        return response;
    }

    /**
     * Retrieves all event objects in the database ordered by date in descending order.
     *
     * @return a list of type EventResponse with all events.
     */
    @GET
    @Path("/desc-by-date")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EventResponse> getAllEventsDescByDate() {
        return getEvents(
                "SELECT *, c.email as client_email, c.name as" +
                        " client_name FROM event e JOIN client c ON e.cid =" +
                        " c.cid ORDER BY e.date DESC;");
    }

    /**
     * A reusable method to get any number of events and
     * their details based on a query to the database.
     *
     * @param query the SQL query that retrieves the specific event objects.
     * @return a list of type EventResponse containing all events that match the entered conditions.
     */

    private List<EventResponse> getEvents(String query) {
        List<EventResponse> eventList = new ArrayList<>();
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                EventResponse eventResponse = new EventResponse();
                eventResponse.setEid(resultSet.getInt("eid"));
                eventResponse.setClientName(resultSet.getString("client_name"));
                eventResponse.setClientEmail(resultSet.getString("client_email"));
                eventResponse.setName(resultSet.getString("name"));
                eventResponse.setType(resultSet.getString("type"));
                eventResponse.setDate(resultSet.getString("date"));
                eventResponse.setLocation(resultSet.getString("location"));
                eventResponse.setStatus(resultSet.getString("status"));
                eventResponse.setBookingtype(resultSet.getString("bookingtype"));
                eventResponse.setDuration(resultSet.getInt("duration"));
                eventResponse.setIsaccepted(resultSet.getBoolean("isaccepted"));

                int cmid = resultSet.getInt("productionmanager");
                String name = cmid == 0 ? "unset" : CrewmembersRoute.getNameFromCmid(cmid);

                eventResponse.setProductionmanager(name);
                eventResponse.setMaxmembers(resultSet.getInt("maxmembers"));
                eventResponse.setCurrentmembers(resultSet.getInt("currentmembers"));

                eventList.add(eventResponse);
            }
        } catch (SQLException sqlError) {
            System.err.println("Error connecting: " + sqlError);
        }
        return eventList;
    }

    /**
     * Updates an event object in the database based on its ID.
     *
     * @param eid the unique identifier of the event to be updated.
     * @param receivedData A hashmap containing the event details that need
     *                     updating and their new values.
     * @return a response code indicating the success or failure of the operation.
     */

    @PUT
    @Path("/{eid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateEvent(@PathParam("eid") int eid, HashMap<String, Object> receivedData) {
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            connection.setAutoCommit(false);
            StringBuilder query = new StringBuilder("UPDATE event SET ");

            ArrayList<Object> values = new ArrayList<>();
            for (String key : receivedData.keySet()) {
                query.append(key).append("=?, ");
                values.add(receivedData.get(key));
            }
            query.setLength(query.length() - 2);

            query.append(" WHERE eid=?");
            values.add(eid);

            if (receivedData.containsKey("isaccepted")) {
                if ((boolean) receivedData.get("isaccepted")) {
                    List<EventResponse> events = getAllEvents();
                    for (EventResponse event : events) {
                        if (event.getEid() == eid) {
                            if (!event.isIsaccepted()) {
                                event.setIsaccepted(true);
                                EmailRoute.bookingAccepted(eid, "accepted");
                            }
                        }
                    }
                }


            }

            String queryString = query.toString();
            PreparedStatement statement = connection.prepareStatement(queryString);

            for (int i = 0; i < values.size(); i++) {
                Object value = values.get(i);
                if (value instanceof String) {
                    statement.setString(i + 1, (String) value);
                } else if (value instanceof Integer) {
                    statement.setInt(i + 1, (Integer) value);
                } else if (value instanceof Boolean) {
                    statement.setBoolean(i + 1, (Boolean) value);
                }
            }

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                connection.commit();
                return Response.ok().entity("Event updated successfully").build();
            } else {
                connection.rollback();
                return Response.status(Response.Status.NOT_FOUND).entity("Event not found").build();
            }
        } catch (SQLException | ClassCastException e) {
            System.out.println("Database error: " + e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to update event").build();
        }
    }

    /**
     * Administrators have the option to accept or decline an event requested by a client.
     * Upon denial, the event object is deleted.
     *
     * @param eid is the unique identifier of the event object that will be removed from the database.
     * @return the response code that verifies whether the deletion was successful or not.
     */

    @DELETE
    @Path("/{eid}")
    public Response declineEvent(@PathParam("eid") int eid) {
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            connection.setAutoCommit(false);
            String query = "DELETE FROM event WHERE eid=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, eid);
            EmailRoute.bookingAccepted(eid, "declined");

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                connection.commit();
                return Response.ok().entity("Event deleted successfully").build();
            } else {
                connection.rollback();
                return Response.status(Response.Status.NOT_FOUND).entity("Event not found").build();
            }
        } catch (SQLException e) {
            System.out.println("Error when declining event: " + e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to delete event").build();
        }
    }

    /**
     * Assesses whether a row is empty or not by checking for all blank/null cells.
     *
     * @param row is the row in question for emptiness.
     * @return true if the row is empty, otherwise false.
     */

    private boolean isRowEmpty(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the String value of a given cell.
     *
     * @param cell the cell from which the value is being retrieved.
     * @return the String value or null in case of an empty cell.
     */
    private String getCellStringValue(Cell cell) {
        return cell == null ? "" : cell.getStringCellValue();
    }

    /**
     * Gets the numerical value of a given cell.
     *
     * @param cell the cell from which the value is being retrieved.
     * @return the numerical value or 0 in case of an empty cell.
     */
    private double getCellNumericValue(Cell cell) {
        return cell == null ? 0 : cell.getNumericCellValue();
    }

}

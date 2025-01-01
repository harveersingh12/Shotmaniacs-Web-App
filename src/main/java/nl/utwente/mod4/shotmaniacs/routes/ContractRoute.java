package nl.utwente.mod4.shotmaniacs.routes;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.Response;
import nl.utwente.mod4.shotmaniacs.Utils;
import nl.utwente.mod4.shotmaniacs.dao.DatabaseConnection;
import nl.utwente.mod4.shotmaniacs.model.Contract;
import nl.utwente.mod4.shotmaniacs.model.Crewmember;

@Path("/contract")
public class ContractRoute {
    /**
     * Retrieves all crew members from the database and returns their contracts.
     * @return a list of contracts
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Contract> getAllCrewmembers() {
        List<Contract> contracts = new ArrayList<>();
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            String getAllCrewMembersQuery =
                    "SELECT c.cmid AS crew_cmid, c.eid AS crew_eid FROM contract c";
            PreparedStatement getAllCrewMembersStmt = connection.prepareStatement(
                    getAllCrewMembersQuery);
            ResultSet resultSet = getAllCrewMembersStmt.executeQuery();
            //iterate through the result set, create new contract obj and add it to the contracts list
            while (resultSet.next()) {
                Contract contract = new Contract(resultSet.getInt("crew_cmid"),
                                                 resultSet.getInt("crew_eid"));
                contracts.add(contract);
            }
        } catch (SQLException sqlError) {
            System.err.println("Error connecting: " + sqlError);
        }
        return contracts;
    }

    /**
     * Creates a new contract in the database with the received data.
     * @param receivedData HashMap containing event ID and crew member ID
     * @return Response indicating the success or failure of the operation
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createContract(HashMap<String, String> receivedData) {
        int eid = Integer.parseInt(receivedData.get("eid"));
        int cmid = Integer.parseInt(receivedData.get("cmid"));

        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            String query = "INSERT INTO contract (cmid, eid) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, cmid);
            preparedStatement.setInt(2, eid);
            //get the number of rows updated by the query
            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                return Response.ok().entity("Contract created successfully").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Contract could not be created").build();
            }
        } catch (SQLException e) {
            System.out.println(
                    "Error while creating contract for event: " + eid + " and crew: " + cmid);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create contract").build();
        }
    }

    /**
     * Retrieves the crew members enrolled in an event form the database.
     * @param eid event ID
     * @return Response indicating the success or failure of the operation
     */
    @GET
    @Path("/{eid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEnrolledCrewMembers(@PathParam("eid") int eid) {
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        List<Map<String, Object>> crewMembers = new ArrayList<>();

        try (connection) {
            String query = "SELECT c.cmid, m.name, m.role, m.job " +
                    "FROM contract c " +
                    "JOIN crewmember m ON c.cmid = m.cmid " +
                    "WHERE c.eid = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, eid);

            ResultSet resultSet = preparedStatement.executeQuery();
            //go through each row in the result set and add crew member details to the HashMap
            while (resultSet.next()) {
                int cmid = resultSet.getInt("cmid");
                String name = resultSet.getString("name");
                String role = resultSet.getString("role");
                String job = resultSet.getString("job");

                Map<String, Object> crewMember = new HashMap<>();
                crewMember.put("cmid", cmid);
                crewMember.put("name", name);
                crewMember.put("role", role);
                crewMember.put("job", job);

                crewMembers.add(crewMember);
            }

            return Response.ok(crewMembers).build();
        } catch (SQLException e) {
            System.out.println("Error retrieving crew members for event: " + eid);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to retrieve assigned crewmembers").build();
        }
    }

    /**
     * Deletes from the database a specific contract based on the provided event ID.
     * @param eid event ID
     * @return Response indicating the success or failure of the operation
     */
    @DELETE
    @Path("/{eid}")
    public Response deleteContract(@PathParam("eid") int eid) {
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            // delete contract with that eid
            String query = "DELETE FROM contract WHERE eid = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, eid);
            preparedStatement.executeUpdate();
            return Response.ok().entity("Contract deleted successfully").build();
        } catch (SQLException e) {
            System.out.println("Error while deleting contract for event: " + eid);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to delete contract").build();
        }
    }
    /**
     *Enroll a new crew member into an event if the member is not already enrolled.
     * @param eid event ID
     * @param authorizationHeader the Authorization header containing credentials
     * @return Response indicating the success or failure of the operation
     */
    @POST
    @Path("/{eid}")
    public Response enroll(@PathParam("eid") int eid, @HeaderParam("Authorization") String authorizationHeader) {
        String name = Utils.extractNameFromHeader(authorizationHeader);
        String role = Utils.extractRoleFromHeader(authorizationHeader);

        //check if crew member is already enrolled
        if (isAlreadyEnrolled(eid, name)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to enroll - crew member already enrolled").build();
        }

        //check if there is currently a production manager
        //if there is already one, don't add a new one
        if (hasProductionManager(eid) && role.equals("productionmanager")) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("You are not allowed to have more than 1 production manager.").build();
        }

        if (name.isEmpty()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to extract name from token.").build();
        }

        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            String query = "SELECT cmid FROM crewmember WHERE name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);

            ResultSet resultSet = preparedStatement.executeQuery();

            int cmid;
            if (resultSet.next()) {
                cmid = resultSet.getInt("cmid");
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Crew member not found for name: " + name).build();
            }
            //insert the new contract into the database
            query = "INSERT INTO contract (cmid, eid) VALUES (?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, cmid);
            preparedStatement.setInt(2, eid);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 1) {
                //increment the members count
                String q = "SELECT currentmembers FROM event e WHERE e.eid=?";
                PreparedStatement s = connection.prepareStatement(q);
                s.setInt(1, eid);
                ResultSet res = s.executeQuery();
                int currentmembers = -1;
                if (res.next()) {
                    currentmembers = res.getInt("currentmembers");
                }

                if (currentmembers != -1) {
                    currentmembers++;
                    q = "UPDATE event SET currentmembers=? WHERE eid = ?";
                    s = connection.prepareStatement(q);
                    s.setInt(1, currentmembers);
                    s.setInt(2, eid);
                    int rowsUpdated = s.executeUpdate();
                    if (rowsUpdated == 0) {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .entity("Failed to increment currentmembers").build();
                    }
                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Failed to increment currentmembers").build();
                }

                return Response.status(Response.Status.CREATED)
                        .entity("Contract created successfully").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Failed to create contract").build();
            }
        } catch (SQLException e) {
            System.out.println("Error while creating contract for: " + eid);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create contract").build();
        }
    }

    /**
     * Checks if an event has at least one production manager assigned to it.
     * @param eid event ID
     * @return true if the event has at least one production manager, false otherwise
     */
    private boolean hasProductionManager(int eid) {
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            //retrieve crew member IDs associated with an event
            String query = "SELECT cmid FROM contract WHERE eid=?";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            List<Integer> eventCmids = new ArrayList<>();
            while (resultSet.next()) {
                eventCmids.add(resultSet.getInt("cmid"));
            }
            //retrieve all crew member IDs that have the role production manager
            String cmidQuery = "SELECT cmid FROM crewmember WHERE role = 'productionmanager'";
            statement = connection.prepareStatement(cmidQuery);
            resultSet = statement.executeQuery();

            List<Integer> productionManagerCmids = new ArrayList<>();
            while (resultSet.next()) {
                productionManagerCmids.add(resultSet.getInt("cmid"));
            }

            eventCmids.retainAll(productionManagerCmids);
            //return true if there is at least one common cmid
            return !eventCmids.isEmpty();
        } catch (SQLException e) {
            System.out.println(
                    "Error occurred while checking if event has production manager assigned to it");

            return false;
        }
    }

    /**
     * Extracts the name of a crew member from the authorization header.
     * Returns the result of method isAlreadyEnrolled.
     * @param authorizationHeader the Authorization header containing credentials
     * @param eid event ID
     * @return true if the crew member is already enrolled in the event, false otherwise
     */
    @GET
    @Path("/status/{eid}")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean isCrewMemberEnrolled(@HeaderParam("Authorization")
                                            String authorizationHeader, @PathParam("eid") int eid) {
        String name = Utils.extractNameFromHeader(authorizationHeader);
        System.out.println("Extracted name is " + authorizationHeader);
        return isAlreadyEnrolled(eid, name);
    }

    /**
     * Checks if a crew member is already enrolled in a given event.
     * @param eid event ID
     * @param name the name of the crew member
     * @return true if the crew member is already enrolled in the event, false otherwise
     */
    private boolean isAlreadyEnrolled(int eid, String name) {
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            String query = "SELECT c.cmid, m.name, m.role, m.job " + "FROM contract c " +
                    "JOIN crewmember m ON c.cmid = m.cmid " + "WHERE c.eid = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, eid);

            ResultSet resultSet = preparedStatement.executeQuery();

            List<String> enrolledNames = new ArrayList<>();
            while (resultSet.next()) {
                enrolledNames.add(resultSet.getString("name"));
            }
            //check if name is in the list of enrolled crew members
            return enrolledNames.contains(name);
        } catch (SQLException e) {
            System.out.println(
                    "Error checking if crew members is already enrolled in event: " + eid);
        }

        return false;
    }
}

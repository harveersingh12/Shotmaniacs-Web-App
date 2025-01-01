package nl.utwente.mod4.shotmaniacs.routes;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import nl.utwente.mod4.shotmaniacs.dao.DatabaseConnection;

import nl.utwente.mod4.shotmaniacs.model.Crewmember;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import nl.utwente.mod4.shotmaniacs.model.Event;
import nl.utwente.mod4.shotmaniacs.model.EventResponse;
import nl.utwente.mod4.shotmaniacs.model.Image;

@Path("/crewmembers")
public class CrewmembersRoute {
    /**
     *Retrieves all crew members along with their associated images from the database.
     * @return a list of crew members
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Crewmember> getAllCrewmembers() {
        List<Crewmember> crewmembers = new ArrayList<>();
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            String getAllCrewMembersQuery = "SELECT c.name AS member_name, c.email AS" +
                    " member_email, c.role AS member_role, c.cmid AS member_cmid, c.job AS" +
                    " member_job, i.image_data AS image_data, i.image_name as image_name " +
                    "FROM crewmember c, image_store i WHERE c.cmid = i.cmid";
            PreparedStatement getAllCrewMembersStmt =
                    connection.prepareStatement(getAllCrewMembersQuery);
            ResultSet resultSet = getAllCrewMembersStmt.executeQuery();


            while (resultSet.next()) {
                Crewmember crewmember = new Crewmember(
                        resultSet.getString("member_name"),
                        resultSet.getString("member_email"),
                        resultSet.getString("member_role"),
                        resultSet.getInt("member_cmid"),
                        resultSet.getString("member_job"),
                        new Image(resultSet.getString("image_name"),
                                  Base64.getEncoder().encodeToString
                                          (resultSet.getBytes("image_data")),
                                  resultSet.getInt("member_cmid"))
                );
                crewmembers.add(crewmember);
            }


        } catch (SQLException sqlError) {
            System.err.println("Error connecting: " + sqlError);
        }
        return crewmembers;
    }

    /**
     * Retrieves a list of events corresponding to a specific crew member.
     * @param cmid the ID of the crew member whose events will be retrieved
     * @return a list of EventResponse that contain information about the events
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{cmid}/events")
    public List<EventResponse> getCorrespondingBookings(@PathParam("cmid") int cmid) {
        List<EventResponse> correspondingBookings = new ArrayList<>();
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            //retrieve events associated with a specific crew member
            String getAllBookingsQuery =
                 "SELECT DISTINCT e.name AS event_name, e.type AS event_type, e.date AS event_date," +
                     " e.location AS event_location, e.status AS " +
                      "event_status, e.bookingType AS booking_type," +
                       " e.duration AS event_duration, e.isAccepted AS event_isAccepted," +
                      " e.productionManager AS event_prodmanager, e.maxMembers AS event_maxMembers," +
                            " e.currentMembers AS event_curMember, e.eid AS event_eid "+
                            "FROM crewmember c " +
                            "JOIN contract co ON c.cmid = co.cmid " +
                            "JOIN event e ON e.eid = co.eid " +
                            "WHERE c.cmid = ?";
            PreparedStatement getAllCrewMembersStmt = connection.prepareStatement(getAllBookingsQuery);
            getAllCrewMembersStmt.setInt(1, cmid);
            ResultSet resultSet = getAllCrewMembersStmt.executeQuery();

            while (resultSet.next()) {
                EventResponse event = new EventResponse(
                        resultSet.getString("event_name"),
                        resultSet.getString("event_type"),
                        resultSet.getString("event_date"),
                        resultSet.getString("event_location"),
                        resultSet.getString("event_status"),
                        resultSet.getString("booking_type"),
                        resultSet.getInt("event_duration"),
                        resultSet.getBoolean("event_isAccepted"),
                        resultSet.getString("event_prodmanager"),
                        resultSet.getInt("event_maxMembers"),
                        resultSet.getInt("event_curMember"),
                        resultSet.getInt("event_eid")
                );
                correspondingBookings.add(event);
            }
        }catch (SQLException sqlError) {
            System.err.println("Error connecting: " + sqlError);
        }
        return correspondingBookings;
    }

    /**
     * Update the details(such as name, email) from the database of a specific crew member.
     * @param crewmember
     * @param cmid
     */
    @PUT
    @Path("/{cmid}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateCrewMember(HashMap<String, Object> crewmember, @PathParam("cmid") int cmid) {
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try {
            //extract crew member details from the HashMap
            String memberName = (String) crewmember.get("memberName");
            String memberEmail = (String) crewmember.get("memberEmail");
            String memberJob = (String) crewmember.get("memberJob");
            String memberRole = (String) crewmember.get("memberRole");
            String memberImage = crewmember.get("memberImage").toString();


            String updateQuery =
                    "UPDATE crewmember SET name = ?, email = ?, job = ?, role = ? WHERE cmid = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setString(1, memberName);
            updateStmt.setString(2, memberEmail);
            updateStmt.setString(3, memberJob);
            updateStmt.setString(4, memberRole);
            updateStmt.setInt(5, cmid);
            updateStmt.executeUpdate();

            //check if there is also an image update
            if (memberImage != null && !memberImage.isEmpty()) {
                String checkImageQuery = "SELECT COUNT(*) AS img_count FROM image_store WHERE cmid = ?";
                PreparedStatement checkImageStmt = connection.prepareStatement(checkImageQuery);
                checkImageStmt.setInt(1, cmid);
                ResultSet imgResultSet = checkImageStmt.executeQuery();
                imgResultSet.next();
                int imgCount = imgResultSet.getInt("img_count");

                //update the image if there was already one or add a new image
                // if here was no image for that crew member
                if (imgCount > 0) {
                    String updateImageQuery = "UPDATE image_store SET image_data = ? WHERE cmid = ?";
                    PreparedStatement updateImageStmt = connection.prepareStatement(updateImageQuery);
                    byte[] imageBytes = Base64.getDecoder().decode(memberImage);
                    updateImageStmt.setBytes(1, imageBytes);
                    updateImageStmt.setInt(2, cmid);
                    updateImageStmt.executeUpdate();
                } else {
                    String insertImageQuery = "INSERT INTO image_store (image_name, image_data, cmid) VALUES (?, ?, ?)";
                    PreparedStatement insertImageStmt = connection.prepareStatement(insertImageQuery);
                    insertImageStmt.setString(1, "profile_picture_" + cmid);
                    byte[] imageBytes = Base64.getDecoder().decode(memberImage);
                    insertImageStmt.setBytes(2, imageBytes);
                    insertImageStmt.setInt(3, cmid);
                    insertImageStmt.executeUpdate();
                }
            }

        } catch (SQLException sqlError) {
            System.err.println("Error connecting: " + sqlError);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates a crew member's job in the database.
     * @param crewmember HashMap containing the crew member's name and the new job
     * @return a response indicating if the update was successful of not
     */
    @PUT
    @Path("/jobs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCrewmemberJob(HashMap<String, String> crewmember) {
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            String updateJobQuery = "UPDATE crewmember SET job = ? WHERE name = ?";
            PreparedStatement updateJobStmt = connection.prepareStatement(updateJobQuery);
            updateJobStmt.setString(1, crewmember.get("job"));
            updateJobStmt.setString(2, crewmember.get("name"));
            int rowsUpdated = updateJobStmt.executeUpdate();
            //check if any rows were updated
            if (rowsUpdated > 0) {
                return Response.ok().entity("Crewmember job updated successfully").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Crewmember not found").build();
            }
        } catch (SQLException sqlError) {
            System.err.println("Error updating job: " + sqlError);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity("Error updating job").build();
        }
    }

    /**
     * Retrieves the name of a crew member based on their ID.
     * @param cmid the ID of the crew member
     * @return the name of the crew member
     */
    public static String getNameFromCmid(int cmid) {
        String name = "";
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            String query = "SELECT name FROM crewmember WHERE cmid = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, cmid);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getString("name");
            }
        }
        catch (SQLException e) {
            System.out.println("Error occurred while matching cmid to name: " + e);
        }

        return name;
    }

    /**
     * Retrieves the crew member ID based on the name of a crew member.
     * @param name the name of the crew member whose ID we want to retrive
     * @return the ID of the crew member
     */
    public static int getCmidFromName(String name) {
        int cmid = 0;
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            String query = "SELECT cmid FROM crewmember WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                cmid = resultSet.getInt("cmid");
            }
        }
        catch (SQLException e) {
            System.out.println("Error occurred while matching name to cmid: " + e);
        }

        return cmid;
    }


}
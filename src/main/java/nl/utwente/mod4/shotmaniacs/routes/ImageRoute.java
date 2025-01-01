package nl.utwente.mod4.shotmaniacs.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import nl.utwente.mod4.shotmaniacs.dao.DatabaseConnection;
import nl.utwente.mod4.shotmaniacs.model.Crewmember;
import nl.utwente.mod4.shotmaniacs.model.Image;


@Path("/images")
public class ImageRoute {
    /**
     * Upload an image to the database.
     * @param jsonString string containing the image data
     * @return HTTP response
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadImage(String jsonString) {
        try {
            //parse the JSON string into an image obj
            ObjectMapper mapper = new ObjectMapper();
            Image image = mapper.readValue(jsonString, Image.class);
            //decode the base64 data of the image
            byte[] imageData = Base64.getDecoder().decode(image.getData());

            try (Connection connection = DatabaseConnection.INSTANCE.getConnection()) {
                //SQL query to insert image data and name to database
                String insertImageQuery = "INSERT INTO image_store (image_name, image_data) VALUES (?, ?)";
                PreparedStatement insertImageStmt = connection.prepareStatement(insertImageQuery);
                insertImageStmt.setString(1, image.getImageName());
                insertImageStmt.setBytes(2, imageData);
                insertImageStmt.executeUpdate();
                return Response.ok().entity("{\"success\":true}").build();
            } catch (SQLException sqlError) {
                System.err.println("Error connecting: " + sqlError);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"success\":false}").build();
            }
        } catch (IOException ioError) {
            System.err.println("Error reading JSON data: " + ioError);
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"success\":false}").build();
        }
    }

    /**
     * Retrieve all images associated with the given user.
     * @param authorizationHeader the authorization header that contains the token
     * @return the images or null if there are none
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Image getAllImages(@HeaderParam("Authorization") String authorizationHeader) {
        String name = extractNameFromToken(authorizationHeader);

        int cmid;

        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try (connection) {
            String checkClientQuery = "SELECT cmid FROM crewmember WHERE name = ?";
            PreparedStatement checkClientStmt = connection.prepareStatement(checkClientQuery);
            checkClientStmt.setString(1, name);
            ResultSet checkClientResult = checkClientStmt.executeQuery();
            if (checkClientResult.next()) {
                cmid = checkClientResult.getInt("cmid");
                //get the image associated with the cmid
                String getImageQuery = "SELECT image_name, image_data FROM image_store WHERE cmid = ?";
                PreparedStatement getImageStmt = connection.prepareStatement(getImageQuery);
                getImageStmt.setInt(1, cmid);
                ResultSet resultSet = getImageStmt.executeQuery();
                if (resultSet.next()) {
                    //create and return the image that was encoded in base64
                    Image image = new Image(resultSet.getString("image_name"), Base64.getEncoder().encodeToString(resultSet.getBytes("image_data")),
                                            cmid);
                    return image;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Extracts the user's name from the token provided in the authorization header.
     * @param header the authorization header containing the header
     * @return the user's name or an empty string if the extraction failed
     */
    private String extractNameFromToken(String header) {
        String token = "";
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        try {
            Claims claims = Jwts.parser().verifyWith(AuthRoute.key).build().parseSignedClaims(token)
                    .getPayload();
            //return the name from the claim
            return claims.getSubject();
        } catch (JwtException e) {
            System.out.println("Could not extract name for calendar from token: " + e);
        }

        return "";
    }

    /**
     * Retrieve a list of images from the database for a given list of crew members.
     * @param jsonPayload the JSON payload containing crew member IDs
     * @return a list of images corresponding to the list of crew members
     */
    @POST
    @Path("/all")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Image> getAllImagesFromCmidList(String jsonPayload) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            //deserialize the JSON payload
            JsonNode jsonNode = objectMapper.readTree(jsonPayload);
            //extract the list of crewmember IDs from the JSON node
            JsonNode cmidListNode = jsonNode.get("cmidList");
            List<Integer> cmidList =
                    objectMapper.convertValue(cmidListNode, new TypeReference<List<Integer>>() { });
            //establish connection to the database
            Connection connection = DatabaseConnection.INSTANCE.getConnection();
            List<Image> images = new ArrayList<>();
            try {
                for (int i = 0; i < cmidList.size(); i++) {
                    //retrieve the image data for the ID
                    String getImageQuery = "SELECT image_name, image_data " +
                                    "FROM image_store WHERE cmid = ? AND image_data IS NOT NULL";
                    PreparedStatement getImageStmt = connection.prepareStatement(getImageQuery);
                    getImageStmt.setInt(1, cmidList.get(i));
                    ResultSet resultSet = getImageStmt.executeQuery();
                    if (resultSet.next()) {
                        Image image = new Image(resultSet.getString("image_name"),
                 Base64.getEncoder().encodeToString(resultSet.getBytes("image_data")),
                                                cmidList.get(i));
                        images.add(image);
                    }
                }


            } catch (SQLException sqlError) {
                System.err.println("Error connecting: " + sqlError);
            }
            return images;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
package nl.utwente.mod4.shotmaniacs.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import nl.utwente.mod4.shotmaniacs.dao.DatabaseConnection;
import nl.utwente.mod4.shotmaniacs.model.Image;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * Class for handling authentication and registration of crew members.
 */
@Path("/auth")
public class AuthRoute {
    public static final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * Deals with logging in a member to the dashboard.
     * @param member HashMap containing "name" and "password" for authentication
     * @return Response indicating the success or failure of the operation
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginCrewMember(HashMap<String, String> member) {
        boolean authenticated = authenticate(member.get("name"), member.get("password"));

        Map<String, String> response = new HashMap<>();
        response.put("status", authenticated ? AuthCode.LOGIN_SUCCESS.toString() : AuthCode.LOGIN_INVALID.toString());

        if(authenticated) {
            String role = getUserRole(member.get("name"));
            String token = generateToken(member.get("name"), role);
            response.put("token", token);
            response.put("role", role);
        }

        return Response.ok(response).build();
    }

    /**
     * Registers a new crew member and adds him to the dashboard.
     * @param newMember HashMap containing "name", "email", "password", "role", "job",
     * and (optionally) "image" for registration
     * @return
     */
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerCrewMember(HashMap<String, String> newMember) {
        Map<String, String> response = new HashMap<>();

        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try(connection) {
            //check if username exists or password format is invalid
            String checkName = "SELECT name FROM crewmember WHERE name = ?";
            PreparedStatement checkNameStmt = connection.prepareStatement(checkName);
            checkNameStmt.setString(1, newMember.get("name"));
            ResultSet checkNameResult = checkNameStmt.executeQuery();
            //if username already exists or password format is invalid return failure response
            if(checkNameResult.next() || !checkPasswordFormat(newMember.get("password"))){
                System.out.println("Register failed: name is already used or password is not strong enough");
                response.put("status", AuthCode.REGISTER_INVALID.toString());
                return Response.ok(response).build();
            }
            // determine what the next cmid for the new crew member should be
            String getCmid = "SELECT MAX(cmid) AS last_cmid FROM crewmember";
            PreparedStatement getCmidStatement = connection.prepareStatement(getCmid);
            ResultSet lastCmidResult = getCmidStatement.executeQuery();

            int lastCmid = 0;
            if (lastCmidResult.next()) {
                lastCmid = lastCmidResult.getInt("last_cmid");
            }
            int newCmid = lastCmid + 1;
            //insert the new crew member details into the crewmember table
            String insertCrewMemberQuery = "INSERT INTO crewmember (cmid, name, email, password, role, job) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertCrewMemberStmt = connection.prepareStatement(insertCrewMemberQuery);
            insertCrewMemberStmt.setInt(1, newCmid);
            insertCrewMemberStmt.setString(2, newMember.get("name"));
            insertCrewMemberStmt.setString(3, newMember.get("email"));
            insertCrewMemberStmt.setString(4, hashPassword(newMember.get("password")));
            insertCrewMemberStmt.setString(5, newMember.get("role"));
            insertCrewMemberStmt.setString(6, newMember.get("job"));
            insertCrewMemberStmt.executeUpdate();
            ObjectMapper mapper = new ObjectMapper();

            //insert image if provided
            try {
                String imageDataBase64 = newMember.get("image");
                byte[] imageData = Base64.getDecoder().decode(imageDataBase64);

                String insertImageQuery = "INSERT INTO image_store (image_name, image_data, cmid) VALUES (?, ?, ?)";
                PreparedStatement insertImageStmt = connection.prepareStatement(insertImageQuery);
                insertImageStmt.setString(1, "profile_picture_" + newCmid);
                insertImageStmt.setBytes(2, imageData);
                insertImageStmt.setInt(3, newCmid);
                insertImageStmt.executeUpdate();
            }
            catch (Exception e) {
                System.out.println("Error while inserting image: " + e);
            }
        }
        catch (SQLException e) {
            System.err.println("Error logging in: " + e);
            response.put("status", AuthCode.REGISTER_INVALID.toString());
            return Response.ok(response).build();
        }

        response.put("status", AuthCode.REGISTER_SUCCESS.toString());
        return Response.ok(response).build();
    }

    /**
     * Hashes the given password using the SHA-256 algorithm.
     * @param password the password that needs to be hashed
     * @return the encoded password or null if an error occurred
     */
    public String hashPassword(String password) {
        try{
            byte[] inputData = password.getBytes();
            MessageDigest md;
            md = MessageDigest.getInstance("SHA-256");
            md.update(inputData);
            byte[] digest = md.digest();

            return Hex.encodeHexString(digest);
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("Error while hashing the password: " + e);
        }

        return null;
    }

    /**
     * Check if the provided password meets the format criteria.
     * The password need to have at least one upper case, lower case, digit and special character.
     * @param password the password that needs to be checked
     * @return true if the password meets the criteria, false otherwise
     */
    public boolean checkPasswordFormat(String password) {
        if (password == null || password.length() < 8 || password.contains(" ")) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }

            if (hasUppercase && hasLowercase && hasDigit && hasSpecialChar) {
                break;
            }
        }

        boolean result = hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
        return result;
    }

    /**
     * Authenticate a user based on name and password.
     * @param name the name of the user who attempts to log in
     * @param password the password of the user who attempts to log in
     * @return true if the user was successfully authenticated, false otherwise
     */
    public boolean authenticate(String name, String password) {
        boolean result = false;

        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try(connection) {
            String checkPasswordQuery = "SELECT password FROM crewmember WHERE name = ?";
            PreparedStatement checkPasswordStmt = connection.prepareStatement(checkPasswordQuery);
            checkPasswordStmt.setString(1, name);
            ResultSet checkPasswordResult = checkPasswordStmt.executeQuery();
            //if a password is found compare it to the hashed password
            if(checkPasswordResult.next()) {
                result = checkPasswordResult.getString("password").equals(hashPassword(password));
            }
            else{
                return false;
            }
        }
        catch (SQLException e) {
            System.err.println("Error logging in: " + e);
        }

        return result;
    }

    /**
     * Generates a token for a user.
     * @param username the username of the user
     * @param role the role of the user
     * @return a token as a string
     */
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    /**
     * Retrieves the role of a user based on their name.
     * @param name the name of the crew member
     * @return the role of the user
     */
    public String getUserRole(String name) {
        String role = "";
        Connection connection = DatabaseConnection.INSTANCE.getConnection();
        try(connection) {
            String query = "SELECT role FROM crewmember WHERE name = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                role = resultSet.getString("role");
            }
        }
        catch (SQLException e) {
            System.out.println("Error retrieving the user role: " + e);
        }

        return role;
    }
}

package routes;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.ws.rs.core.Response;
import nl.utwente.mod4.shotmaniacs.dao.DatabaseConnection;
import nl.utwente.mod4.shotmaniacs.routes.AuthCode;
import nl.utwente.mod4.shotmaniacs.routes.AuthRoute;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthTest {

    /**
     * Unit tests for AuthRoute functionality including password hashing, format validation,
     * JWT token generation, and user registration/login.
     */

    private AuthRoute route;

    /**
     * Connects to the database
     */
    @BeforeAll
    public static void init() {
        new DatabaseConnection();
    }

    /**
     * Sets up the connection to the route required to test the following methods
     */

    @BeforeEach
    public void setUp() {
       route = new AuthRoute();
    }

    /**
     * Tests that unique passwords result in unique hashes and identical ones
     * remain the same.
     */

    @Test
    public void testPasswordHashing() {
        String password1 = "password";
        String password2 = "password2";
        String hash1 = route.hashPassword(password1);
        String hash2 = route.hashPassword(password2);
        assertNotEquals(hash1, hash2);

        password2 = "password";
        hash2 = route.hashPassword(password2);
        assertEquals(hash2, hash1);
    }

    /**
     * Tests if the password conforms to the following conditions:
     * At least 1 lower case and 1 upper case character
     * No white spaces
     * At least 1 special character
     * At least 1 digit
     */

    @Test
    public void testPasswordFormat() {
        String password = "testpassword";
        assertFalse(route.checkPasswordFormat(password));
        password = "testPassword";
        assertFalse(route.checkPasswordFormat(password));
        password = "testPassword1";
        assertFalse(route.checkPasswordFormat(password));
        password = "testPassword1!";
        assertTrue(route.checkPasswordFormat(password));
        password = "test Password1!";
        assertFalse(route.checkPasswordFormat(password));
    }

    /**
     * Tests JWT token generation based on name and role.
     * Verifies token content including subject, role claim, issued and expiration.
     */

    @Test
    public void testJsonTokenGeneration() {
        String name = "tudor";
        String role = "crewmember";
        String token = route.generateToken(name, role);

        Claims claims = Jwts.parser()
                .setSigningKey(AuthRoute.key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(name, claims.getSubject());
        assertEquals(role, claims.get("role"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    /**
     * Tests registering a new crew member and then attempting to log in with those
     * credentials. Asserts that log in was successful through checking the response
     * code and the generated token.
     */

    @Test
    public void testRegisterAndLogIn() {
        HashMap<String, String> newMember = new HashMap<>();
        newMember.put("name", "testuser2");
        newMember.put("email", "testuser@example.com");
        newMember.put("password", "TestPassword1!");
        newMember.put("role", "crewmember");
        newMember.put("job", "jobtitle");
        newMember.put("image", Base64.getEncoder().encodeToString("dummyImageData".getBytes()));

        Response registerResponse = route.registerCrewMember(newMember);
        assertEquals(Response.Status.OK.getStatusCode(), registerResponse.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, String> registerResponseBody = (Map<String, String>) registerResponse.getEntity();
        assertEquals(AuthCode.REGISTER_SUCCESS.toString(), registerResponseBody.get("status"));

        HashMap<String, String> loginMember = new HashMap<>();
        loginMember.put("name", "testuser");
        loginMember.put("password", "TestPassword1!");

        Response loginResponse = route.loginCrewMember(loginMember);
        assertEquals(Response.Status.OK.getStatusCode(), loginResponse.getStatus());

        @SuppressWarnings("unchecked")
        Map<String, String> loginResponseBody = (Map<String, String>) loginResponse.getEntity();
        assertEquals(AuthCode.LOGIN_SUCCESS.toString(), loginResponseBody.get("status"));
        assertNotNull(loginResponseBody.get("token"));
        assertEquals("crewmember", loginResponseBody.get("role"));
    }

}

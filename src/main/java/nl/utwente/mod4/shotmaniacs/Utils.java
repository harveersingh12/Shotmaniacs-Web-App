package nl.utwente.mod4.shotmaniacs;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import nl.utwente.mod4.shotmaniacs.routes.AuthRoute;

public class Utils {
    /**
     * Extracts the name from the Authorization header token.
     * @param header the Authorization header containing the token
     * @return the extracted name, or empty string if error occurs
     */
    public static String extractNameFromHeader(String header) {
        String token = extractTokenFromHeader(header);

        try{
            Claims claims = Jwts.parser()
                    .verifyWith(AuthRoute.key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        }
        catch (JwtException e) {
            System.out.println("Could not extract name from token: " + e);
        }

        return "";
    }

    /**
     * Extracts the role from the Authorization header token.
     * @param header the Authorization header containing the token
     * @return the extracted role, or empty string if error occurs
     */
    public static String extractRoleFromHeader(String header) {
        String token = extractTokenFromHeader(header);

        try{
            Claims claims = Jwts.parser()
                    .verifyWith(AuthRoute.key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return (String) claims.get("role");
        }
        catch (JwtException e) {
            System.out.println("Could not extract role from token: " + e);
        }

        return "";
    }

    /**
     * Extracts the token from the Authorization header token.
     * @param header the Authorization header containing the token
     * @return the extracted token, or empty string if no token was found
     */
    public static String extractTokenFromHeader(String header) {
        String token = "";
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }

        return token;
    }
}

package nl.utwente.mod4.shotmaniacs;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import nl.utwente.mod4.shotmaniacs.model.ProtectedRoute;
import nl.utwente.mod4.shotmaniacs.routes.AuthRoute;

import java.util.*;

@Provider
public class RequestFilter implements ContainerRequestFilter {

    //static map to define protected routes and the roles that can acces them
    private static final Map<String, ProtectedRoute> PROTECTED_ROUTES = new HashMap<>();

    static {
        // auth
        PROTECTED_ROUTES.put("auth/register", new ProtectedRoute("auth/register", "admin"));

        // announcement
        PROTECTED_ROUTES.put("announcement",
                             new ProtectedRoute("announcement", "POST", "admin"));
        PROTECTED_ROUTES.put("announcement",
                             new ProtectedRoute("announcement", "DELETE", "admin"));
        PROTECTED_ROUTES.put("announcement", new ProtectedRoute
                ("announcement", "GET", "crewmember"));

        // calendar
        PROTECTED_ROUTES.put("email", new ProtectedRoute("email", "crewmember"));

        // contract
        PROTECTED_ROUTES.put("contract", new ProtectedRoute("contract", "crewmember"));

        // crewmembers
        PROTECTED_ROUTES.put("crewmembers",
                             new ProtectedRoute("crewmembers", "crewmember"));

        // event
        PROTECTED_ROUTES.
                put("event", new ProtectedRoute("event", "GET", "crewmember"));
        PROTECTED_ROUTES.
                put("event/crew", new ProtectedRoute("event/crew", "crewmember"));
        PROTECTED_ROUTES.put("event/desc-by-date",
                             new ProtectedRoute("event/desc-by-date", "crewmember"));

        // images
        PROTECTED_ROUTES.put("/images", new ProtectedRoute("/images", "crewmember"));
        PROTECTED_ROUTES.put("/images/all",
                             new ProtectedRoute("/images/all", "crewmember"));
    }

    /**
     * Filters incoming HTTP requests for access control based on the protected routes.
     * @param requestContext the context of the incoming HTTP request
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String route = requestContext.getUriInfo().getPath();
        if(isProtected(route, requestContext.getMethod())) {
            String authHeader = extractTokenFromRequest(requestContext.getHeaderString("Authorization"));
            if (authHeader == null || !isValidAuthorizationHeader(route, authHeader)) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("<html><body><h1>403 Unauthorized</h1><p>You are not" +
                                        " allowed to access this api endpoint.</p></body></html>")
                        .build());
            }
        }
    }

    /**
     * Checks if a given route and HTTP method combination is protected.
     * @param route the route to be checked
     * @param method method of the HTTP request
     * @return true if route is protected and matches the HTTP method, false otherwise
     */
    private boolean isProtected(String route, String method) {
        ProtectedRoute protectedRoute = PROTECTED_ROUTES.get(route);
        if(protectedRoute == null) {
            return false;
        }

        if(protectedRoute.getMethod().equals("all")) {
            return true;
        }
        else {
            return protectedRoute.getMethod().equals(method);
        }
    }

    private boolean isValidAuthorizationHeader(String route, String token) {
        System.out.println("Chcking authheader is valid");
        // get role for the path
        ProtectedRoute protectedRoute = PROTECTED_ROUTES.get(route);
        String requiredRole = protectedRoute.getRoles();

        // crewmember includes admin
        // admin is just admin

        // first extract role from authHeader
        try{
            Claims claims = Jwts.parser()
                    .verifyWith(AuthRoute.key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String extractedRole = (String) claims.get("role");
            System.out.println("Extracted role is: " + extractedRole + " required role is: " + requiredRole);

            if(requiredRole.equals("crewmember")) {
                return true;
            }
            else {
                return extractedRole.equals("admin");
            }
        }
        catch (JwtException e) {
            System.out.println("Error happened while checking if authorization header is valid.");
            return false;
        }
    }

    private String extractTokenFromRequest(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }
}

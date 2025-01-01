package nl.utwente.mod4.shotmaniacs.model;

public class ProtectedRoute {
    private String route;
    private String method;
    private String roles;

    // crewmember includes also admin
    // admin is always just admin

    public ProtectedRoute(String route, String method, String roles) {
        this.route = route;
        this.method = method;
        this.roles = roles;
    }

    public ProtectedRoute(String route, String roles) {
        this.route = route;
        this.method = "all";
        this.roles = roles;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}

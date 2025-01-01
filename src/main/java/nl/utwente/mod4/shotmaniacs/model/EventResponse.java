package nl.utwente.mod4.shotmaniacs.model;

/**
 * Represents a response object for an event, containing detailed information about an event.
 */
public class EventResponse {
    private String clientName;
    private String clientEmail;
    private int eid;
    private String name;
    private String type;
    private String date;
    private String location;
    private String status;
    private String bookingtype;
    private int duration;
    private boolean isaccepted;
    private String productionmanager;
    private int maxmembers;
    private int currentmembers;

    /**
     * Default constructor for the EventResponse class.
     */
    public EventResponse() {
    }

    /**
     * Parameterized constructor for initializing all fields of the EventResponse class.
     *
     * @param name             Name of the event
     * @param type             Type of the event
     * @param date             Date of the event
     * @param location         Location of the event
     * @param status           Status of the event
     * @param bookingtype      Booking type of the event
     * @param duration         Duration of the event
     * @param isaccepted       Flag indicating acceptance status of the event
     * @param productionmanager Name of the production manager overseeing the event
     * @param maxmembers       Maximum number of members allowed for the event
     * @param currentmembers   Current number of members registered for the event
     * @param eid              Unique identifier of the event
     */
    public EventResponse(String name, String type, String date, String location, String status, String bookingtype,
                         int duration, boolean isaccepted, String productionmanager, int maxmembers, int currentmembers,
                         int eid) {
        this.name = name;
        this.type = type;
        this.date = date;
        this.location = location;
        this.status = status;
        this.bookingtype = bookingtype;
        this.duration = duration;
        this.isaccepted = isaccepted;
        this.productionmanager = productionmanager;
        this.maxmembers = maxmembers;
        this.currentmembers = currentmembers;
        this.eid = eid;
    }

    /**
     * Getter method for retrieving the event ID.
     *
     * @return The event ID
     */
    public int getEid() {
        return eid;
    }

    /**
     * Setter method for setting the event ID.
     *
     * @param eid The event ID to set
     */
    public void setEid(int eid) {
        this.eid = eid;
    }

    /**
     * Getter method for retrieving the client name associated with the event.
     *
     * @return The client name
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Setter method for setting the client name associated with the event.
     *
     * @param clientName The client name to set
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * Getter method for retrieving the client email associated with the event.
     *
     * @return The client email
     */
    public String getClientEmail() {
        return clientEmail;
    }

    /**
     * Setter method for setting the client email associated with the event.
     *
     * @param clientEmail The client email to set
     */
    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    /**
     * Getter method for retrieving the name of the event.
     *
     * @return The event name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for setting the name of the event.
     *
     * @param name The event name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for retrieving the type of the event.
     *
     * @return The event type
     */
    public String getType() {
        return type;
    }

    /**
     * Setter method for setting the type of the event.
     *
     * @param type The event type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Getter method for retrieving the date of the event.
     *
     * @return The event date
     */
    public String getDate() {
        return date;
    }

    /**
     * Setter method for setting the date of the event.
     *
     * @param date The event date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Getter method for retrieving the location of the event.
     *
     * @return The event location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Setter method for setting the location of the event.
     *
     * @param location The event location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Getter method for retrieving the status of the event.
     *
     * @return The event status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter method for setting the status of the event.
     *
     * @param status The event status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Getter method for retrieving the booking type of the event.
     *
     * @return The event booking type
     */
    public String getBookingtype() {
        return bookingtype;
    }

    /**
     * Setter method for setting the booking type of the event.
     *
     * @param bookingtype The event booking type to set
     */
    public void setBookingtype(String bookingtype) {
        this.bookingtype = bookingtype;
    }

    /**
     * Getter method for retrieving the duration of the event.
     *
     * @return The event duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Setter method for setting the duration of the event.
     *
     * @param duration The event duration to set
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Getter method for retrieving the acceptance status of the event.
     *
     * @return True if the event is accepted, false otherwise
     */
    public boolean isIsaccepted() {
        return isaccepted;
    }

    /**
     * Setter method for setting the acceptance status of the event.
     *
     * @param isaccepted The acceptance status to set
     */
    public void setIsaccepted(boolean isaccepted) {
        this.isaccepted = isaccepted;
    }

    /**
     * Getter method for retrieving the production manager overseeing the event.
     *
     * @return The name of the production manager
     */
    public String getProductionmanager() {
        return productionmanager;
    }

    /**
     * Setter method for setting the production manager overseeing the event.
     *
     * @param productionmanager The name of the production manager to set
     */
    public void setProductionmanager(String productionmanager) {
        this.productionmanager = productionmanager;
    }

    /**
     * Getter method for retrieving the maximum number of members allowed for the event.
     *
     * @return The maximum number of members
     */
    public int getMaxmembers() {
        return maxmembers;
    }

    /**
     * Setter method for setting the maximum number of members allowed for the event.
     *
     * @param maxmembers The maximum number of members to set
     */
    public void setMaxmembers(int maxmembers) {
        this.maxmembers = maxmembers;
    }

    /**
     * Getter method for retrieving the current number of members registered for the event.
     *
     * @return The current number of members
     */
    public int getCurrentmembers() {
        return currentmembers;
    }

    /**
     * Setter method for setting the current number of members registered for the event.
     *
     * @param currentmembers The current number of members to set
     */
    public void setCurrentmembers(int currentmembers) {
        this.currentmembers = currentmembers;
    }
}

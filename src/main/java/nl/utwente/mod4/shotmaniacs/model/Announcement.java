package nl.utwente.mod4.shotmaniacs.model;

import java.util.List;

public class Announcement {
    private int announcementID;
    private String announcementName;
    private String announcementMessage;
    private int announcementCMID;
    private String announcementDate;
    private List<String> crewMembers;
    private int urgency;
    public Announcement(){

    }

    public Announcement(String Name, String Message, int cmid, String date, int announcementID, int urgency){
        this.announcementName = Name;
        this.announcementMessage = Message;
        this.announcementCMID = cmid;
        this.announcementDate = date;
        this.announcementID = announcementID;
        this.urgency = urgency;
    }


    public int getAnnouncementID() {
        return announcementID;
    }

    public void setAnnouncementID(int announcementID) {
        this.announcementID = announcementID;
    }

    public String getAnnouncementName() {
        return announcementName;
    }

    public void setAnnouncementName(String announcementName) {
        this.announcementName = announcementName;
    }

    public String getAnnouncementMessage() {
        return announcementMessage;
    }

    public void setAnnouncementMessage(String announcementMessage) {
        this.announcementMessage = announcementMessage;
    }

    public int getAnnouncementCMID() {
        return announcementCMID;
    }

    public void setAnnouncementCMID(int announcementCMID) {
        this.announcementCMID = announcementCMID;
    }

    public String getAnnouncementDate() {
        return announcementDate;
    }

    public void setAnnouncementDate(String announcementDate) {
        this.announcementDate = announcementDate;
    }

    public List<String> getCrewMembers() {
        return crewMembers;
    }

    public void setCrewMembers(List<String> crewMembers) {
        this.crewMembers = crewMembers;
    }
    public int getUrgency() {
        return urgency;
    }

    public void setUrgency(int urgency) {
        this.urgency = urgency;
    }
}

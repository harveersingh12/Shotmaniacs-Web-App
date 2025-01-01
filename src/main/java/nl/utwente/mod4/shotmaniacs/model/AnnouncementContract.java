package nl.utwente.mod4.shotmaniacs.model;

public class AnnouncementContract {
    private int announcementconractCMID;
    private int getAnnouncementconractID;

    public AnnouncementContract(){

    }

    public AnnouncementContract(int cmid, int id){
        this.announcementconractCMID = cmid;
        this.getAnnouncementconractID = id;
    }

    public int getAnnouncementconractCMID() {
        return announcementconractCMID;
    }

    public void setAnnouncementconractCMID(int announcementconractCMID) {
        this.announcementconractCMID = announcementconractCMID;
    }

    public int getGetAnnouncementconractID() {
        return getAnnouncementconractID;
    }

    public void setGetAnnouncementconractID(int getAnnouncementconractID) {
        this.getAnnouncementconractID = getAnnouncementconractID;
    }
}

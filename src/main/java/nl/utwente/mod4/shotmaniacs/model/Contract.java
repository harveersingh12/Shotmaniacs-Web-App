package nl.utwente.mod4.shotmaniacs.model;

public class Contract {
    private int cmid;
    private int eid;

    public Contract() {

    }

    public Contract(int cmid, int eid) {
        this.cmid = cmid;
        this.eid = eid;
    }

    public int getCmid() {
        return cmid;
    }

    public void setCmid(int cmid) {
        this.cmid = cmid;
    }

    public int getEid() {
        return eid;
    }

    public void setEid(int eventID) {
        this.eid = eid;
    }
}

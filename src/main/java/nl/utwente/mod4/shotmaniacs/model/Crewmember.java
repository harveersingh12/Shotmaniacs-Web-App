package nl.utwente.mod4.shotmaniacs.model;


public class Crewmember {
    private String memberName;
    //private String memberEmail;
    private String memberRole;
    private int memberCmid;
    private String memberJob;
    private String memberEmail;
    private Image memberImage;

    public Crewmember() {

    }
    public Crewmember(String memberName, String memberEmail, String memberRole, int memberCmid, String memberJob, Image memberImage) {
        this.memberName = memberName;
        this.memberEmail = memberEmail;
        this.memberRole = memberRole;
        this.memberCmid = memberCmid;
        this.memberJob = memberJob;
        this.memberImage = memberImage;
    }

    public String getMemberEmail() {return memberEmail;}
    public void setMemberEmail(String memberEmail) {this.memberEmail = memberEmail;}
    public int getMemberCmid() {
        return memberCmid;
    }

    public void setMemberCmid(int memberCmid) {
        this.memberCmid = memberCmid;
    }

    public String getMemberJob() { return memberJob; }

    public void setMemberJob(String memberJob) { this.memberJob = memberJob; }

    public String getMemberName(){
        return memberName;
    }

    public String getMemberRole(){
        return memberRole;
    }

    public void setMemberRole(String memberRole){
        this.memberRole=memberRole;
    }

    public void setMemberName(String memberName){
        this.memberName=memberName;
    }

    public void setMemberImage(Image memberImage) {
        this.memberImage = memberImage;
    }
    public Image getMemberImage() {
        return memberImage;
    }

    @Override
    public String toString() {
        return "Crewmember{" +
                "memberName='" + memberName + '\'' +
                ", memberRole='" + memberRole + '\'' +
                ", memberImage='" + memberImage + '\'' +
                '}';
    }
}
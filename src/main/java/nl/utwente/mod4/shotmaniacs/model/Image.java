package nl.utwente.mod4.shotmaniacs.model;

public class Image {
    private int id;
    private String imageName;
    private String data;
    private int cmid;
    public Image() { }
    public Image(String imageName, String imageData, int cmid) {
        this.imageName = imageName;
        this.data = imageData;
        this.cmid = cmid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCmid() {
        return cmid;
    }

    public void setCmid(int cmid) {
        this.cmid = cmid;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
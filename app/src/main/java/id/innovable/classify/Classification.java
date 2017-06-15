package id.innovable.classify;

/**
 * Created by Qoharu on 4/11/17.
 */

public class Classification {
    private String className;
    private Double percentage;

    public Classification(String kelas, Double percent){
        className = kelas;
        percentage = percent;
    }

    public Double getPercentage() {
        return percentage;
    }

    public String getClassName() { return className;}

    public void setClassName(String className) {
        this.className = className;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

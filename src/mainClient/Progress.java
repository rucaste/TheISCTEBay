package mainClient;

public class Progress {

    private static Progress instance;

    public Progress(){
        instance = this;
    }

    public static Progress getInstance(){
        return instance;
    }

    private float fractionDone;

    public void setFractionDone(float fractionDone) {
        this.fractionDone = fractionDone;
    }

    public float getFractionDone() {
        return fractionDone;
    }
}

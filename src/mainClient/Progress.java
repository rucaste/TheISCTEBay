package mainClient;


public class Progress {

    private static Progress instance;

    private float fractionDone = 0;

    public Progress(){
        instance = this;
    }

    public static Progress getInstance(){
        return instance;
    }

    public void setFractionDone(float fractionDone) {
        this.fractionDone = fractionDone;
    }

    public float getFractionDone() {
        return fractionDone;
    }


}

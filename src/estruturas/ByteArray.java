package estruturas;

import java.io.Serializable;
import java.util.Arrays;

public class ByteArray implements Serializable {

    private byte[] array;

    public ByteArray(byte[] array) {
        this.array = array;
    }

    public byte[] getArray() {
        return array;
    }

    @Override
    public String toString() {
        return "ByteArray{" +
                "array=" + Arrays.toString(array) +
                '}';
    }
}

package info.spotcomms.extirpater;

/**
 * Created using IntelliJ IDEA
 * User: Tad
 * Date: 9/1/2014
 * Time; 9:07 PM
 */
public class ByteArray {

    private byte value;
    private int length;
    private byte[] byteArray;

    public ByteArray(GUI gui, byte value, int length) {
        this.value = value;
        this.length = length;
        this.byteArray = generateByteArray(value, length);
        gui.byteArrays.add(this);
    }

    private byte[] generateByteArray(int b, int length) {
        byte[] bytes = new byte[length];
        for (int x = 0; x < length; x++) {
            bytes[x] = (byte) b;
        }
        return bytes;
    }

    public byte getValue() {
        return this.value;
    }

    public int getLength() {
        return this.length;
    }

    public byte[] getByteArray() {
        return this.byteArray;
    }

}

/*
 * Copyright (c) 2015. Spot Communications
 */

package info.spotcomms.extirpater;

/**
 * Created using IntelliJ IDEA
 * User: Tad
 * Date: 9/1/2014
 * Time; 9:07 PM
 */
class ByteArray {

    private final byte value;
    private final int length;
    private final byte[] byteArray;

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

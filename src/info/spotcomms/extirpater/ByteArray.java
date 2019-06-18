/*
Copyright (c) 2014 Divested Computing Group

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
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

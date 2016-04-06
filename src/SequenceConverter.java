import java.lang.*;

/**
 * Author: Logan Jahnke
 * 810616891
 *
 * Class to convert bytes to ints and ints to bytes  
 */
public class SequenceConverter {
    
    // Empty Constructor
    public SequenceConverter() {}
    
    
    /**
     * Converts an integer to a byte array
     *
     * @param i - the integer to convert
     * @return byte[] - the byte array to return
     */
    public byte[] intToByte(int i) {
        byte[] b = new byte[4];

        b[0] = (byte) (i >> 24);
        b[1] = (byte) (i >> 16);
        b[2] = (byte) (i >> 8);
        b[3] = (byte) (i /*>> 0*/);

        return b;
    }
    
    
    /**
     * Converts a byte array to an integer
     *
     * @param b - the byte array to convert
     * @return int - the integer to return
     */
    public int byteToInt(byte[] b) {
        return b[0] << 24 | (b[1] & 0xFF) << 16 | (b[2] & 0xFF) << 8 | (b[3] & 0xFF);
    }

}
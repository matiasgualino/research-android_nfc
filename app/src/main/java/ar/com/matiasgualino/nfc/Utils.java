package ar.com.matiasgualino.nfc;

import android.nfc.tech.MifareClassic;

/**
 * Created by mgualino on 1/5/16.
 */
public class Utils {

    public static byte[] stringToWrite(String str) {

        byte[] value = str.getBytes();
        byte[] toWrite = new byte[MifareClassic.BLOCK_SIZE];

        for (int i = 0; i < MifareClassic.BLOCK_SIZE; i++) {
            if (i < value.length)
                toWrite[i] = value[i];
            else
                toWrite[i] = 0;
        }
        return toWrite;
    }

    public static String unHex(String hex){

        StringBuilder sb = new StringBuilder();

        for( int i=0; i<hex.length()-1; i+=2 ){

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);

            if (decimal != 0) {
                sb.append((char)decimal);
            }

        }

        return sb.toString();
    }

    public static String bytesToHex(byte[] bytes) {

        final char[] hexArray = "0123456789ABCDEF".toCharArray();


        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}

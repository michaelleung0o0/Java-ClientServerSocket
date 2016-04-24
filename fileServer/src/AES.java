/**
 * Created by michaelleung on 9/4/16.
 */

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES
{
    private IvParameterSpec ivSpec;
    private SecretKeySpec keySpec;

    public AES( String key )
    {
        try {
            byte[] keyBytes = key.getBytes();
            byte[] buf = new byte[ 16 ];

            for ( int i = 0; i < keyBytes.length && i < buf.length; i++ ) {
                buf[ i ] = keyBytes[ i ];
            }

            this.keySpec = new SecretKeySpec( buf, "AES" );
            this.ivSpec = new IvParameterSpec( keyBytes );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public String encrypt( String origData )
    {
        try {
            Cipher cipher = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
            cipher.init( Cipher.ENCRYPT_MODE, this.keySpec, this.ivSpec );
            return bytesToHex( cipher.doFinal( origData.getBytes() ) );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

    public String decrypt( String crypted )
    {
        try {
            Cipher cipher = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
            cipher.init( Cipher.DECRYPT_MODE, this.keySpec, this.ivSpec );
            return new String( cipher.doFinal( toByte( crypted ) ), "UTF-8" );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

    public String bytesToHex( byte[] bytes )
    {
        int length = bytes.length;
        StringBuffer sb = new StringBuffer();
        for ( int i = 0; i < length; i++ ) {
            sb.append( Integer.toString( ( bytes[ i ] & 0xff ) + 0x100, 16 ).substring( 1 ) );
        }
        return sb.toString();
    }

    public static byte[] toByte( String hexString )
    {
        int len = hexString.length() / 2;
        byte[] result = new byte[ len ];
        for ( int i = 0; i < len; i++ )
            result[ i ] = Integer.valueOf( hexString.substring( 2 * i, 2 * i + 2 ), 16 ).byteValue();
        return result;
    }

    /**
     * get default encrypt key with aes encrypt tools
     * @return AES instance
     */
//    public static AES getDefaultInstance(){
//        return  new AES( MainApplication.getInstance().getString( R.string.drop_call_jp ) );
//    }

}


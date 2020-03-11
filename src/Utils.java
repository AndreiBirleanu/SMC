import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

public class Utils {
	
	public static final int SERVER_PORT = 4000;
	public static final int BANK_PORT = 4001;
	
	
	public static byte[] sign(PrivateKey privateKey, byte[] value) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(value);
        return sign.sign();
    }
	

    public static boolean verify(PublicKey publicKey,byte[] value , byte[] signedValue) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(publicKey);
        sign.update(value);
        return sign.verify(signedValue);
    }
}
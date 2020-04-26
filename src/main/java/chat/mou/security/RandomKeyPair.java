package chat.mou.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Component
@Scope("prototype")
public class RandomKeyPair
{
    private final KeyPair keyPair;
    private final Cipher cipher;

    public RandomKeyPair() throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        final var provider = new BouncyCastleProvider();

        final var keyPairGenerator = KeyPairGenerator.getInstance("RSA", provider);
        keyPairGenerator.initialize(2048);

        keyPair = keyPairGenerator.generateKeyPair();
        cipher = Cipher.getInstance("RSA", provider);

        // PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
        // PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec
        // (privateKeyBytes));
    }

    private RSAPublicKey getPublicKey()
    {
        return (RSAPublicKey) keyPair.getPublic();
    }

    private RSAPrivateKey getPrivateKey()
    {
        return (RSAPrivateKey) keyPair.getPrivate();
    }

    public byte[] encryptWithPublicKey(byte[] input) throws
        InvalidKeyException,
        IllegalBlockSizeException,
        BadPaddingException
    {
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
        return cipher.doFinal(input);
    }

    public byte[] encryptWithPrivateKey(byte[] input) throws
        InvalidKeyException,
        IllegalBlockSizeException,
        BadPaddingException
    {
        cipher.init(Cipher.ENCRYPT_MODE, getPrivateKey());
        return cipher.doFinal(input);
    }

    public byte[] decryptWithPublicKey(byte[] input) throws
        InvalidKeyException,
        IllegalBlockSizeException,
        BadPaddingException
    {
        cipher.init(Cipher.DECRYPT_MODE, getPublicKey());
        return cipher.doFinal(input);
    }

    public byte[] decryptWithPrivateKey(byte[] input) throws
        InvalidKeyException,
        IllegalBlockSizeException,
        BadPaddingException
    {
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
        return cipher.doFinal(input);
    }
}

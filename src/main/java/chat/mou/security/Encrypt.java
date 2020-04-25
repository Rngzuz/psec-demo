package chat.mou.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class Encrypt
{
    private final KeyPairGenerator keyPairGenerator;
    private final KeyPair keyPair;
    private final Cipher cipher;

    public Encrypt() throws Exception
    {
        final var provider = new BouncyCastleProvider();

        keyPairGenerator = KeyPairGenerator.getInstance("RSA", provider);
        keyPairGenerator.initialize(2048);

        keyPair = keyPairGenerator.generateKeyPair();

        cipher = Cipher.getInstance("RSA", provider);

        // new RSAPublicKeySpec();
        // new RSAPrivateKeySpec();
    }

    public RSAPublicKey getPublicKey()
    {
        return (RSAPublicKey) keyPair.getPublic();
    }

    public RSAPrivateKey getPrivateKey()
    {
        return (RSAPrivateKey) keyPair.getPrivate();
    }

    public byte[] encrypt(byte[] input) throws Exception
    {
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
        return cipher.doFinal(input);
    }

    public byte[] decrypt(byte[] input) throws Exception
    {
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
        return cipher.doFinal(input);
    }
}

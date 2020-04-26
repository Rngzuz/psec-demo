package chat.mou.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

@Component
@Scope("singleton")
public class KeyStore
{
    public final static int KEY_SIZE = 2048;

    private final KeyPairGenerator generator;
    private final Cipher cipher;

    private RSAPublicKey ownPublicKey;
    private RSAPrivateKey ownPrivateKey;
    private RSAPublicKey externalPublicKey;

    public KeyStore() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException
    {
        java.security.Security.addProvider(new BouncyCastleProvider());
        generator = KeyPairGenerator.getInstance("RSA", "BC");
        generator.initialize(KEY_SIZE);

        final var keyPair = generator.generateKeyPair();
        ownPublicKey = (RSAPublicKey) keyPair.getPublic();
        ownPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

        cipher = Cipher.getInstance("RSA", "BC");
    }

    public void refreshOwnKeys()
    {
        generator.initialize(KEY_SIZE);

        final var keyPair = generator.generateKeyPair();
        ownPublicKey = (RSAPublicKey) keyPair.getPublic();
        ownPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

    public byte[] getEncodedOwnPublicKey()
    {
        return ownPublicKey.getEncoded();
    }

    public void setAndDecodeExternalPublicKey(byte[] bytes) throws
        NoSuchProviderException,
        NoSuchAlgorithmException,
        InvalidKeySpecException
    {
        final var key = new X509EncodedKeySpec(bytes);
        externalPublicKey = (RSAPublicKey) KeyFactory.getInstance("RSA", "BC").generatePublic(key);
    }

    public boolean hasExternalPublicKey()
    {
        return externalPublicKey != null;
    }

    public byte[] encryptMessage(String message) throws
        InvalidKeyException,
        IllegalBlockSizeException,
        BadPaddingException
    {
        cipher.init(Cipher.ENCRYPT_MODE, externalPublicKey);
        final var encryptWithExternalPublicKey = cipher.doFinal(message.getBytes());

        cipher.init(Cipher.ENCRYPT_MODE, ownPrivateKey);
        return cipher.doFinal(encryptWithExternalPublicKey);
    }

    public byte[] decryptMessage(byte[] bytes) throws
        InvalidKeyException,
        IllegalBlockSizeException,
        BadPaddingException
    {
        cipher.init(Cipher.DECRYPT_MODE, externalPublicKey);
        final var decryptedWithExternalPublicKey = cipher.doFinal(bytes);

        cipher.init(Cipher.DECRYPT_MODE, ownPrivateKey);
        return cipher.doFinal(decryptedWithExternalPublicKey);
    }

}

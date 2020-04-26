//package chat.mou.testing;
//
//import java.util.Base64;
//
//public class EncryptTest
//{
//    public static void main(String[] args) throws Exception
//    {
//        final var enc = new Encrypt();
//
//        final var data = "Hello World!";
//        System.out.println("Original:\t" + data);
//        System.out.print("\n");
//
//        final var encryptedBytes = enc.encrypt(data.getBytes());
//        System.out.println("Encrypted:\t" + new String(Base64.getEncoder().encode(encryptedBytes)));
//        System.out.println(enc.getPrivateKey()); System.out.print("\n");
//
//        final var decryptedBytes = enc.decrypt(encryptedBytes);
//        System.out.println("Decrypted:\t" + new String(decryptedBytes));
//        System.out.println(enc.getPublicKey());
//    }
//
//    public static void main(String[] args) throws Exception
//    {
//        final var provider = new BouncyCastleProvider();
//
//        byte[] input = "Hello World!".getBytes();
//
//        final var cipher = Cipher.getInstance("RSA", provider);
//        final var keyFactory = KeyFactory.getInstance("RSA", provider);
//
//        // Create public key
//        final var pubKeySpec = new RSAPublicKeySpec(new BigInteger("d46f473a2d746537de2056ae3092c451", 16),
//            new BigInteger("11", 16)
//        );
//
//        // Create private key
//        final var privKeySpec = new RSAPrivateKeySpec(new BigInteger("d46f473a2d746537de2056ae3092c451", 16),
//            new BigInteger("57791d5430d593164082036ad8b29fb1", 16)
//        );
//
//        final var pubKey = (RSAPublicKey) keyFactory.generatePublic(pubKeySpec);
//        final var privKey = (RSAPrivateKey) keyFactory.generatePrivate(privKeySpec);
//
//        System.out.println("Original:\t" + new String(input) + "\n");
//
//        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
//        byte[] cipherText = cipher.doFinal(input);
//        System.out.println("Encrypted:\t" + new String(cipherText));
//        System.out.print(pubKey + "\n");
//
//        cipher.init(Cipher.DECRYPT_MODE, privKey);
//        byte[] plainText = cipher.doFinal(cipherText);
//        System.out.println("Decrypted:\t" + new String(plainText));
//        System.out.print(privKey + "\n");
//    }
//}

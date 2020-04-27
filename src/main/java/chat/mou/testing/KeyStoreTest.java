package chat.mou.testing;

import chat.mou.security.KeyStore;

public class KeyStoreTest
{
    public static void main(String[] args) throws Exception
    {
        final var clientKeyStore = new KeyStore();
        final var hostKeyStore = new KeyStore();

        hostKeyStore.setAndDecodeExternalPublicKey(clientKeyStore.getEncodedOwnPublicKey());
        clientKeyStore.setAndDecodeExternalPublicKey(hostKeyStore.getEncodedOwnPublicKey());

        final var originalMessage = "Lorem ipsum dolor sit amet, consectetur adipiscing elit";
        System.out.println("Original:\t" + originalMessage);

        final var encryptedMessage = clientKeyStore.encryptMessage(originalMessage);
        System.out.println("Encrypted:\t" + new String(encryptedMessage));
        System.out.println(encryptedMessage.length);

        final var decryptedMessage = hostKeyStore.decryptMessage(encryptedMessage);
        System.out.println("Decrypted:\t" + new String(decryptedMessage));
    }
}

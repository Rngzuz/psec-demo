package chat.mou.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class KeyStore
{
    private final RandomKeyPair keyPair;

    @Autowired
    public KeyStore(RandomKeyPair keyPair)
    {
        this.keyPair = keyPair;
    }
}

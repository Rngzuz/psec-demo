package chat.mou.client;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class MessageCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
    @Override
    public void completed(AsynchronousSocketChannel result, Object attachment) {

    }

    @Override
    public void failed(Throwable exc, Object attachment) {

    }
}

package chat.mou.client;

import org.springframework.context.ApplicationEvent;

public class ViewEvent extends ApplicationEvent {
    private String previousViewName;
    private String nextViewName;

    public ViewEvent(String previousViewName, String nextViewName) {
        super(previousViewName);
        this.previousViewName = previousViewName;
        this.nextViewName = nextViewName;
    }

    public String getPreviousViewName() {
        return previousViewName;
    }

    public String getNextViewName() {
        return nextViewName;
    }
}

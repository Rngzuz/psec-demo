package chat.mou.events;

import javafx.scene.Parent;
import org.springframework.context.ApplicationEvent;

public class ViewEvent extends ApplicationEvent
{
    private final Class<? extends Parent> view;

    public ViewEvent(Parent source, Class<? extends Parent> view)
    {
        super(source);
        this.view = view;
    }

    public Class<? extends Parent> getView()
    {
        return view;
    }
}

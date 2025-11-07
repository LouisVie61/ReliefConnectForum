package demo.reliefconnectforum.service.event;

import org.springframework.context.ApplicationEvent;

public class UserAllTokensRevokedEvent extends ApplicationEvent {
    private final String userId;

    public UserAllTokensRevokedEvent(Object source, String userId) {
        super(source);
        this.userId = userId;
    }

    public String getUserId() { return userId; }
}

package demo.reliefconnectforum.service.event;

import org.springframework.context.ApplicationEvent;

public class TokenRevokedEvent extends ApplicationEvent {
    private final String userId;
    private final String token;

    public TokenRevokedEvent(Object source, String userId, String token) {
        super(source);
        this.userId = userId;
        this.token = token;
    }

    public String getUserId() { return userId; }
    public String getToken() { return token; }
}


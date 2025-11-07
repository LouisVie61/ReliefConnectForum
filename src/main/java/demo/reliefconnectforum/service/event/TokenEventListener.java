package demo.reliefconnectforum.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenEventListener {

    @Async
    @EventListener
    public void handleTokenRevoked(TokenRevokedEvent event) {
        log.info("[Event] Token revoked event received for user {} - token {}", event.getUserId(), event.getToken());
        // Future: save to audit log / UI notify
    }

    @Async
    @EventListener
    public void handleUserAllTokensRevoked(UserAllTokensRevokedEvent event) {
        log.info("[Event] All tokens revoked for user {}", event.getUserId());
    }
}

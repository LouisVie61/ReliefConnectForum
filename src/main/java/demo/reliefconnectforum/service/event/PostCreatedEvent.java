package demo.reliefconnectforum.service.event;

import java.util.UUID;

public class PostCreatedEvent {
    private final UUID postId;

    public PostCreatedEvent(UUID postId) {
        this.postId = postId;
    }

    public UUID getPostId() {
        return postId;
    }
}
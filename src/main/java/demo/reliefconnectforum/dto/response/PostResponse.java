package demo.reliefconnectforum.dto.response;

import demo.reliefconnectforum.Enum.PostType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostResponse {
    private UUID id;
    private String title;
    private String content;
    private String location;
    private String imageUrl;
    private UUID userId;
    private String username;
    private PostType postType;
    private BigDecimal totalDonations;
    private LocalDateTime createdAt;
}

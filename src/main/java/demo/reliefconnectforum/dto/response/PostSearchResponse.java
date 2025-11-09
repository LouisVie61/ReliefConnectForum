package demo.reliefconnectforum.dto.response;

import demo.reliefconnectforum.Enum.PostType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchResponse {
    private String id;
    private String title;
    private String content;
    private PostType postType;
    private String location;
    private String authorId;
    private String authorUsername;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDateTime createdAt;
}
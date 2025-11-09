package demo.reliefconnectforum.dto.response;

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
public class DonationResponse {
    private UUID id;
    private BigDecimal amount;
    private String currency;
    private String place;
    private String message;
    private UUID postId;
    private String postTitle;
    private UUID userId;
    private String username;
    private LocalDateTime createdAt;
}

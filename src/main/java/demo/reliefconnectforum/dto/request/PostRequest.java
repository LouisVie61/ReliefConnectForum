package demo.reliefconnectforum.dto.request;

import demo.reliefconnectforum.Enum.PostType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostRequest {
    private String title;
    private String content;
    private UUID userId;
    private String location;
    private String contactName;
    private String contactPhone;
    private BigDecimal targetAmount;
}

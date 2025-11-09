package demo.reliefconnectforum.dto.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class DonationStatistic {
    private UUID postId;
    private BigDecimal totalAmount;

    public UUID getPostId() {
        return postId;
    }

    public void setPostId(UUID postId) {
        this.postId = postId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}

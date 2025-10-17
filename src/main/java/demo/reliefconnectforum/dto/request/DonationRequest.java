package demo.reliefconnectforum.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class DonationRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String currency;

    @NotBlank(message = "location is required")
    private String location;

    @NotNull(message = "Post ID is required")
    private UUID postId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String message;

    public @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than 0") BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(@NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than 0") BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public @NotBlank(message = "location is required") String getLocation() {
        return location;
    }

    public void setLocation(@NotBlank(message = "location is required") String location) {
        this.location = location;
    }

    public @NotNull(message = "Post ID is required") UUID getPostId() {
        return postId;
    }

    public void setPostId(@NotNull(message = "Post ID is required") UUID postId) {
        this.postId = postId;
    }

    public @NotNull(message = "User ID is required") UUID getUserId() {
        return userId;
    }

    public void setUserId(@NotNull(message = "User ID is required") UUID userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

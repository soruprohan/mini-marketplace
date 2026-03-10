package com.marketplace.mini_marketplace.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReviewDTO {

    // No @NotNull here — productId is always set from the URL path variable
    // in ReviewController, never from the form body. Validating it here
    // causes @Valid to fail before the controller can assign it.
    private Long productId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @NotBlank(message = "Comment is required")
    private String comment;

    public Long getProductId()                 { return productId; }
    public void setProductId(Long productId)   { this.productId = productId; }
    public Integer getRating()                 { return rating; }
    public void setRating(Integer rating)      { this.rating = rating; }
    public String getComment()                 { return comment; }
    public void setComment(String comment)     { this.comment = comment; }
}
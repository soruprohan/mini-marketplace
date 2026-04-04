package com.marketplace.mini_marketplace.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private Long sellerId;

    public Long getId()                          { return id; }
    public void setId(Long id)                   { this.id = id; }
    public String getName()                      { return name; }
    public void setName(String name)             { this.name = name; }
    public String getDescription()               { return description; }
    public void setDescription(String desc)      { this.description = desc; }
    public BigDecimal getPrice()                 { return price; }
    public void setPrice(BigDecimal price)       { this.price = price; }
    public Integer getStock()                    { return stock; }
    public void setStock(Integer stock)          { this.stock = stock; }
    public Long getCategoryId()                  { return categoryId; }
    public void setCategoryId(Long categoryId)   { this.categoryId = categoryId; }
    public Long getSellerId()                    { return sellerId; }
    public void setSellerId(Long sellerId)       { this.sellerId = sellerId; }
}
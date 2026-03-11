package com.marketplace.mini_marketplace.service;

import com.marketplace.mini_marketplace.dto.ReviewDTO;
import com.marketplace.mini_marketplace.model.Product;
import com.marketplace.mini_marketplace.model.Review;
import com.marketplace.mini_marketplace.model.User;
import com.marketplace.mini_marketplace.repository.ProductRepository;
import com.marketplace.mini_marketplace.repository.ReviewRepository;
import com.marketplace.mini_marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ReviewService reviewService;

    private User buyer;
    private Product product;
    private ReviewDTO reviewDTO;

    @BeforeEach
    void setUp() {
        buyer = new User("buyer1", "buyer1@test.com", "pass");
        buyer.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setName("Gadget");
        product.setPrice(BigDecimal.valueOf(15.00));
        product.setStock(5);

        reviewDTO = new ReviewDTO();
        reviewDTO.setProductId(10L);
        reviewDTO.setRating(4);
        reviewDTO.setComment("Pretty good!");
    }

    @Test
    void addReview_shouldSaveAndReturnReview() {
        when(userRepository.findByUsername("buyer1")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(reviewRepository.findByBuyerIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        Review savedReview = new Review();
        savedReview.setId(1L);
        savedReview.setBuyer(buyer);
        savedReview.setProduct(product);
        savedReview.setRating(4);
        savedReview.setComment("Pretty good!");
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        Review result = reviewService.addReview(reviewDTO, "buyer1");

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(4);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void addReview_shouldThrowWhenDuplicateReview() {
        Review existing = new Review();
        existing.setId(99L);

        when(userRepository.findByUsername("buyer1")).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(reviewRepository.findByBuyerIdAndProductId(1L, 10L)).thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class,
                () -> reviewService.addReview(reviewDTO, "buyer1"));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void getReviewsByProduct_shouldReturnListOfReviews() {
        Review r1 = new Review();
        r1.setId(1L);
        Review r2 = new Review();
        r2.setId(2L);

        when(productRepository.existsById(10L)).thenReturn(true);
        when(reviewRepository.findByProductId(10L)).thenReturn(List.of(r1, r2));

        List<Review> result = reviewService.getReviewsByProduct(10L);

        assertThat(result).hasSize(2);
    }
}
package com.marketplace.mini_marketplace.service;

import com.marketplace.mini_marketplace.dto.ReviewDTO;
import com.marketplace.mini_marketplace.exception.ResourceNotFoundException;
import com.marketplace.mini_marketplace.model.Product;
import com.marketplace.mini_marketplace.model.Review;
import com.marketplace.mini_marketplace.model.User;
import com.marketplace.mini_marketplace.repository.ProductRepository;
import com.marketplace.mini_marketplace.repository.ReviewRepository;
import com.marketplace.mini_marketplace.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository) {
        this.reviewRepository  = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository    = userRepository;
    }

    @Transactional
    public Review addReview(ReviewDTO dto, String buyerUsername) {
        User buyer = userRepository.findByUsername(buyerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + buyerUsername));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + dto.getProductId()));

        // One review per buyer per product
        reviewRepository.findByBuyerIdAndProductId(buyer.getId(), product.getId())
                .ifPresent(existing -> {
                    throw new IllegalStateException("You have already reviewed this product");
                });

        Review review = new Review();
        review.setBuyer(buyer);
        review.setProduct(product);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByProduct(Long productId) {
        // Verify product exists first
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        return reviewRepository.findByProductId(productId);
    }

    @Transactional
    public void deleteReview(Long reviewId, String currentUsername) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"));
        boolean isOwner = review.getBuyer().getUsername().equals(currentUsername);

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete this review");
        }

        reviewRepository.delete(review);
    }
}
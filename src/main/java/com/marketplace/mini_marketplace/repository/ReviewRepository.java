package com.marketplace.mini_marketplace.repository;

import com.marketplace.mini_marketplace.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
    Optional<Review> findByBuyerIdAndProductId(Long buyerId, Long productId);
}
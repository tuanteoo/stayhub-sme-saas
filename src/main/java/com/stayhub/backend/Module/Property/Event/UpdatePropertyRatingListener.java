package com.stayhub.backend.Module.Property.Event;

import com.stayhub.backend.Module.Property.Model.Property;
import com.stayhub.backend.Module.Property.Repository.PropertyRepository;
import com.stayhub.backend.Module.Review.Event.ReviewCreatedEvent;
import com.stayhub.backend.Module.Review.Model.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdatePropertyRatingListener {

    private final PropertyRepository propertyRepository;

    @EventListener
    @Transactional
    public void onReviewCreated(ReviewCreatedEvent event) {
        Review review = event.getReview();
        Property property = review.getProperty();
        int rating = review.getRating();
        
        log.info("Observer Pattern: Updating property rating for property ID {}", property.getId());

        Property latestProperty = propertyRepository.findById(property.getId())
                .orElse(property);

        int currentReviewCount = latestProperty.getReviewCount();
        double currentRatingAvg = latestProperty.getRatingAvg();

        double newTotalScore = (currentRatingAvg * currentReviewCount) + rating;
        int newReviewCount = currentReviewCount + 1;
        double newRatingAvg = newTotalScore / newReviewCount;

        newRatingAvg = Math.round(newRatingAvg * 10.0) / 10.0;

        latestProperty.setReviewCount(newReviewCount);
        latestProperty.setRatingAvg(newRatingAvg);

        propertyRepository.save(latestProperty);
    }
}

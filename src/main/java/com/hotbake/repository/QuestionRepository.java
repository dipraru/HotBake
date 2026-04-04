package com.hotbake.repository;

import com.hotbake.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByProductIdOrderByAskedAtDesc(Long productId);
    List<Question> findByAnswerTextIsNullAndProductSellerProfileId(Long sellerId);
}

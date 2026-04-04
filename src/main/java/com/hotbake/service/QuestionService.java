package com.hotbake.service;

import com.hotbake.exception.ResourceNotFoundException;
import com.hotbake.exception.UnauthorizedException;
import com.hotbake.model.Product;
import com.hotbake.model.Question;
import com.hotbake.model.SellerProfile;
import com.hotbake.model.User;
import com.hotbake.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class QuestionService {

    @Autowired private QuestionRepository questionRepository;

    public Question askQuestion(String questionText, Product product, User asker) {
        Question q = new Question();
        q.setProduct(product);
        q.setAsker(asker);
        q.setQuestionText(questionText);
        return questionRepository.save(q);
    }

    public Question answerQuestion(Long questionId, String answerText, SellerProfile seller) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        if (!q.getProduct().getSellerProfile().getId().equals(seller.getId())) {
            throw new UnauthorizedException("You can only answer questions for your own products.");
        }
        q.setAnswerText(answerText);
        q.setAnsweredAt(LocalDateTime.now());
        return questionRepository.save(q);
    }

    public List<Question> getProductQuestions(Long productId) {
        return questionRepository.findByProductIdOrderByAskedAtDesc(productId);
    }

    public List<Question> getUnansweredQuestionsForSeller(Long sellerId) {
        return questionRepository.findByAnswerTextIsNullAndProductSellerProfileId(sellerId);
    }
}

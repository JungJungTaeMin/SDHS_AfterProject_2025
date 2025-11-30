package com.example.afterproject.repository;

import com.example.afterproject.entity.SurveyResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponseEntity, Long> {
        boolean existsByQuestion_Survey_SurveyIdAndRespondent_UserId(Long surveyId, Long userId);
}

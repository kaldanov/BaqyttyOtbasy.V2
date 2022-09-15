package com.telegrambot.repository;

import com.telegrambot.entity.custom.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findAllByConsultationNameIdAndLangId(int consultationId, int langId);

    List<Consultation> findAllByConsultationTeacherIdAndLangId(Long consultationTeacherId, int langId);

    Consultation findByConsultationTeacherIdAndLangId(Long consultationTeacherId, int langId);
    void deleteById(int id);

    Consultation findByIdAndLangId(int id, int langId);
    List<Consultation> findAllByIdAndLangId(int id, int langId);
    Consultation findBySecondAndLangId(int second, int langId);
    Consultation findById(int id);
}


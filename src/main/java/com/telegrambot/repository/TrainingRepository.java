//package baliviya.com.github.bodistrict.telegrambot.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface TrainingRepository extends JpaRepository<Training, Long> {
//    List<Training> findAllByLangId(int langId);
//
//    List<Training> findAllByTrainingNameIdAndLangId(int trainingId, int langId);
//
//    List<Training> findAllByTrainingTeacherIdAndLangId(int trainingTeacher, int langId);
//
//    Training findByTrainingNameIdAndLangId(int trainingNameId, int langId);
//    Training findByTrainingTeacherIdAndLangId(int trainingTeacherId, int langId);
//    Training findByIdAndLangId(int id, int langId);
//    Training findBySecondAndLangId(int second, int langId);
//    Training findById(int id);
//
//    void deleteById(int id);
//}

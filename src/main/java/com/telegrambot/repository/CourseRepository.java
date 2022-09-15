//package baliviya.com.github.bodistrict.telegrambot.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface CourseRepository extends JpaRepository<Course, Long> {
//    Course findByCourseTeacherIdAndLangId(long chatId, int langId);
//    List<Course> findAllByCourseNameIdAndLangId(int courseId, int langId);
//    void deleteById(int id);
//    Course findByIdAndLangId(int id, int langId);
//    Course findBySecondAndLangId(int second, int langId);
//    Course findById(int id);
//}

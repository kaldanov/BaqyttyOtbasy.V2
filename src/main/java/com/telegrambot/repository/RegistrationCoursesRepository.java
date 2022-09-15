//package baliviya.com.github.bodistrict.telegrambot.repository;
//
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.Date;
//import java.util.List;
//
//@Repository
//public interface RegistrationCoursesRepository extends JpaRepository<RegistrationCourses, Integer> {
//    List<RegistrationCourses> findAllByRegistrationDateBeforeAndRegistrationDateAfter(Date start, Date end);
//    List<RegistrationCourses> findAllByRegistrationDateBeforeAndRegistrationDateAfterAndId(Date start, Date end, int id);
//    List<RegistrationCourses> findAllByRegistrationDateBetweenAndIdHandling(Date start, Date end, int id);
//    RegistrationCourses findById(int id);
//    List<RegistrationCourses> findAllByRegistrationDateBeforeAndRegistrationDateAfterAndIin(Date registrationDate, Date registrationDate2, String iin);
//    List<RegistrationCourses> findAllByRegistrationDateBetween(Date start, Date end);
//
//    List<RegistrationCourses> findAllByRegistrationDateBetweenAndIin(Date registrationDate, Date registrationDate2, String iin);
//
//    RegistrationCourses findByChatId(long chatId);
//
//}

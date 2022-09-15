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
//public interface RegistrationTrainingRepository extends JpaRepository<RegistrationTraining, Integer> {
//    List<RegistrationTraining> findAllByRegistrationDateBeforeAndRegistrationDateAfter(Date start, Date end);
//    RegistrationTraining findById(int id);
//
//    List<RegistrationTraining> findAllByRegistrationDateBeforeAndRegistrationDateAfterAndId(Date start, Date end, int id);
//    List<RegistrationTraining> findAllByRegistrationDateBetween(Date start, Date end);
//    List<RegistrationTraining> findAllByRegistrationDateBetweenAndIdHandling(Date start, Date end, int id);
//
//}

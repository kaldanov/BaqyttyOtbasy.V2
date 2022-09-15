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
//public interface RegistrationConsultationRepository extends JpaRepository<RegistrationConsultation, Integer> {
//    List<RegistrationConsultation> findAllByRegistrationDateBeforeAndRegistrationDateAfterAndId(Date start, Date end, int id);
//
//    List<RegistrationConsultation> findAllByRegistrationDateBeforeAndRegistrationDateAfter(Date start, Date end);
//
//    List<RegistrationConsultation> findAllByRegistrationDateBeforeAndRegistrationDateAfterAndIdHandling(Date start, Date end, int id);
//    List<RegistrationConsultation> findAllByRegistrationDateBetweenAndIdHandling(Date start, Date end, int id);
//    List<RegistrationConsultation> findAllByRegistrationDateBetween(Date start, Date end);
//    RegistrationConsultation findById(int id);
//}

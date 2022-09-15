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
//public interface RegistrationSeviceRepository extends JpaRepository<RegistrationService, Integer> {
//    RegistrationService findByChatId(long id);
//    RegistrationService findById(int id);
//    List<RegistrationService> findAllByRegistrationDateBeforeAndRegistrationDateAfterAndId(Date start, Date end, int id);
//    List<RegistrationService> findAllByRegistrationDateBetweenAndIdHandling(Date start, Date end, int id);
//
//    RegistrationService findByServiceIdAndChatId(int id, long chatId);
//}

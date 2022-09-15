package com.telegrambot.repository;

import com.telegrambot.entity.custom.Recipient;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RecipientRepository extends CrudRepository<Recipient, Integer> {

//    Recipient findByChatId(long chatId);
    List<Recipient> findAllByOrderById();
    List<Recipient> findAllByOrderByIdDesc();
//    int                 countByChatId(long chatId);
    int                 countByIin(String iin);
    List<Recipient> findAllByRegistrationDateBeforeAndRegistrationDateAfterOrderById(Date start, Date end);
    List<Recipient> findAllByRegistrationDateBetweenOrderById(Date start, Date end);
    List<Recipient> findAllByRegistrationDateBetweenOrderByRegistrationDateDesc(Date start, Date end);
    List<Recipient> findAllByDistrict(String schema);
    Recipient findByIin(String iin);
    List<Recipient>  findAllByIinOrderByRegistrationDateDesc(String iin);
//    List<Recipient>  findAllByChatIdOrderByRegistrationDate(long chatId);

    Recipient save(Recipient r);
    Recipient findById(long id);
}

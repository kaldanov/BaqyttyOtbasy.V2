package com.telegrambot.service;

import com.telegrambot.entity.LanguageUser;
import com.telegrambot.enums.Language;
import com.telegrambot.repository.LanguageUserRepository;
import com.telegrambot.repository.TelegramBotRepositoryProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LanguageService {

    private static Map<Long, Language>      languageMap         = new HashMap<>();
    private static LanguageUserRepository   languageUserRepo    = TelegramBotRepositoryProvider.getLanguageUserRepository();

    public  static  Language    getLanguage(long chatId) {
        return Optional.ofNullable(languageMap.get(chatId)).orElseGet(() -> languageUserRepo.findByChatId(chatId).map(languageUser -> {
            Language language = Language.getById(languageUser.getLanguageId());
            languageMap.put(chatId, language);
            return language;
        }).orElseGet(() -> { return setLanguage(chatId, Language.ru); }));
//        Language language = languageMap.get(chatId);
//        if (language == null) {
//            LanguageUser languageUser = languageUserRepo.findByChatId(chatId);
//            if (languageUser != null) {
//                language = Language.getById(languageUser.getLanguageId());
//                languageMap.put(chatId, language);
//            }
////            else{
////                language = Language.getById(2);
////                languageMap.put(chatId, language);
////            }
//        }
//        return language;
    }

//    public  static  void        setLanguage(long chatId, Language language) {
//        languageMap.put(chatId, language);
//        LanguageUser languageUser = languageUserRepo.findByChatId(chatId);
//        if (languageUser == null) {
//            languageUserRepo.save(new LanguageUser().setChatId(chatId).setLanguageId(language.getId()));
//        } else {
////            if(languageUserRepo.existsByChatId(chatId)){
////                languageUserRepo.delete(languageUser);
////            }
//            languageUserRepo.save(languageUser.setLanguageId(language.getId()));
//        }
//    }



    public  static  Language    setLanguage(long chatId, Language language) {
        languageMap.put(chatId, language);
        return Language.getById(languageUserRepo.save(languageUserRepo.findByChatId(chatId).orElse(new LanguageUser()).setLanguageId(language.getId()).setChatId(chatId)).getLanguageId());
    }
}

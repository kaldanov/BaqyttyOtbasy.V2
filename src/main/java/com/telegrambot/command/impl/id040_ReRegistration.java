//package baliviya.com.github.bodistrict.telegrambot.command.impl;
//
//import baliviya.com.github.bodistrict.telegrambot.command.Command;
//import baliviya.com.github.bodistrict.telegrambot.entity.User;
////import baliviya.com.github.bodistrict.telegrambot.service.RegistrationService;
//import baliviya.com.github.bodistrict.telegrambot.util.Const;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//
//public class id040_ReRegistration extends Command {
////    private RegistrationService registration = new RegistrationService(chatId);
//
//    @Override
//    public boolean execute() throws TelegramApiException {
//        deleteMessage(updateMessageId);
////        userRepository.deleteByChatId(chatId);
//
//        User user = userRepository.findByChatId(chatId);
//        if (user!= null){
//            user.setEmail(Const.TABLE_NAME);
//            userRepository.save(user);
//        }
//        if (!isRegistered()) {
//
////            if (!registration.isRegistration(update, botUtils)) {
////                return COMEBACK;
////            } else {
////                userRepository.save(registration.getUser());
//////                sendMessageWithAddition();
////                sendMessage(4);
////                return EXIT;
////            }
//        }
//
//        return EXIT;
//    }
//}
//
//

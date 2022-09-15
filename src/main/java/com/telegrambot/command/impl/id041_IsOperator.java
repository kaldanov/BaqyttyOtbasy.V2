//package baliviya.com.github.bodistrict.telegrambot.command.impl;
//
//import baliviya.com.github.bodistrict.telegrambot.command.Command;
//import baliviya.com.github.bodistrict.telegrambot.entity.Message;
//import baliviya.com.github.bodistrict.telegrambot.util.Const;
//import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
//import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
//import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//
//public class id041_IsOperator  extends Command {
//    @Override
//    public boolean execute() throws TelegramApiException {
//        if (!isOper()) {
//            sendMessage(Const.NO_ACCESS);
//            return EXIT;
//        }
//        deleteMessage(updateMessageId);
//        Message message = messageRepository.findByIdAndLangId(messageId, getLanguage().getId());
//        sendMessage(messageId, chatId, null, message.getPhoto());
//        if (message.getFile() != null) {
//            switch (message.getTypeFile()) {
//                case "audio":
//                    bot.execute(new SendAudio().setAudio(message.getFile()).setChatId(chatId));
//                case "video":
//                    bot.execute(new SendVideo().setVideo(message.getFile()).setChatId(chatId));
//                case "document":
//                    bot.execute(new SendDocument().setDocument(message.getFile()).setChatId(chatId));
//            }
//        }
//        return EXIT;
//    }
//}

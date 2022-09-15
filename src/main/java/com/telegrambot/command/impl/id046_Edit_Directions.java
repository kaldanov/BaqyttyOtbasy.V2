//package baliviya.com.github.bodistrict.telegrambot.command.impl;
//
//import baliviya.com.github.bodistrict.telegrambot.command.Command;
//import baliviya.com.github.bodistrict.telegrambot.command.CommandFactory;
//import baliviya.com.github.bodistrict.telegrambot.config.Conversation;
//import baliviya.com.github.bodistrict.telegrambot.entity.custom.*;
//import baliviya.com.github.bodistrict.telegrambot.entity.enums.WaitingType;
//import baliviya.com.github.bodistrict.telegrambot.util.ButtonsLeaf;
//import baliviya.com.github.bodistrict.telegrambot.util.Const;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class id046_Edit_Directions extends Command {
//
//
//    public  int deleteMessId;
//    public  int secondDeleteMessId;
//    public  long currentServiceId;
//
//    private List<Direction> directions;
//    private StringBuilder directionsInfo = new StringBuilder();
//
//    private ButtonsLeaf buttonsLeaf;
//
//    private Direction newDirection = new Direction();
//    private Direction currentDirection;
//    private long currentDirectionId;
//
//    private String newNameDirKaz;
//    private String newNameDirRus;
//
//    @Override
//    public boolean execute() throws TelegramApiException {
//       try{
//           if (!isAdmin() && !isMainAdmin()) {
//               sendMessage(Const.NO_ACCESS);
//               return EXIT;
//           }
//           switch (waitingType){
//
//
//               default: return EXIT;
//           }
//       }
//       catch (Exception e){
//           e.printStackTrace();
//           sendMessageWithKeyboard("Добро пожаловать!", 1);
//           return EXIT;
//       }
//    }
//
//
//
//
//    private void deleteMess() {
//
//            deleteMessage(updateMessageId);
//            deleteMessage(deleteMessId );
//            deleteMessage(secondDeleteMessId);
//    }
//
//
//    public String getText(int messageIdFromDb) {
//        return messageRepository.getMessageText(messageIdFromDb, getLanguage().getId());
//    }
//
//
//}

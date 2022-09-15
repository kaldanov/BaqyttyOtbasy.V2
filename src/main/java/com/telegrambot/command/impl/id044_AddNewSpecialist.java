package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.custom.Specialist;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.Const;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public class id044_AddNewSpecialist extends Command {


    public  int deleteMessId;
    public  int secondDeleteMessId;
    public  int thirdDeleteMessId;

    private Specialist newSpecialist = new Specialist();



    @Override
    public boolean execute() throws TelegramApiException {
       try{
           if(userRepository.findByChatId(chatId) == null){
               sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
               return EXIT;
           }

           if (!isAdmin() && !isMainAdmin()) {
               sendMessage(Const.NO_ACCESS);
               return EXIT;
           }
           switch (waitingType){
               case START:
                   deleteMess();
                   deleteMessId = sendMessageWithKeyboard(getText(64), 47);
                   waitingType = WaitingType.SET_CONTACT_SPEC;
                    return COMEBACK;
               case SET_CONTACT_SPEC:
                   deleteUpdateMess();
                   if (isButton(96)){ // cancel
                       deleteMess();
                       deleteMessId = sendMessageWithKeyboard(getText(12), 11); // 12-редактирование 11 - клавиатоура редактирование
                       waitingType = WaitingType.START;
                   }
                   if (hasContact()){

                       String phone = update.getMessage().getContact().getPhoneNumber();
                       if(phone.charAt(0) == '8')
                           phone = phone.replaceFirst("8" , "+7");
                       else if(phone.charAt(0) == '7')
                           phone = phone.replaceFirst("7", "+7");


                       try {
                           if (userRepository.findByPhone(phone) == null) {
                               deleteMessage(thirdDeleteMessId);
                               thirdDeleteMessId = sendMessage("Пользователь не зарегестрирован в данном боте!\nОтправьте зарегистрированного пользователя!");
                           }

                           else {
                               deleteMess();
                               long userChatId = userRepository.findByPhone(phone).getChatId();
                               newSpecialist.setChatId(userChatId);

                               deleteMessId = sendMessageWithKeyboard(getText(60), 47); // отменить добавление косу
                               waitingType = WaitingType.SET_NAME_SPEC;
                           }

                       }
                       catch (Exception e) {
                           e.printStackTrace();
                           sendMessage("Пользователь ранее удалил аккаунт, из-за этого в базе 2 экземпляра этого аккаунта!");
                       }
                   }
                   else {
                       deleteMessId = sendMessage(getText(65));
                       waitingType = WaitingType.SET_CONTACT_SPEC;
                   }
                   return COMEBACK;
               case SET_NAME_SPEC:
                   deleteMess();
                   if (hasMessageText()){
                       if (isButton(96)){ //cancel
                           sendMessageWithKeyboard(getText(12), 11); // 12-редактирование 11 - клавиатоура редактирование
                           waitingType = WaitingType.START;
                       }
                       else{
                           newSpecialist.setFullName(updateMessageText);

                           deleteMessId = sendMessageWithKeyboard(getText(61),47); // отменить добавление косу
                           waitingType = WaitingType.SET_DESC_SPEC_KAZ;
                       }
                   }
                   else{
                       sendMessageWithKeyboard(getText(1002), 11);
                       deleteMessId = sendMessageWithKeyboard(getText(60),47); // отменить добавление косу
                       waitingType = WaitingType.SET_NAME_SPEC;
                   }

                   return COMEBACK;

               case SET_DESC_SPEC_KAZ:
                   deleteMess();
                   if (hasMessageText()) {
                       if (isButton(96)) { //cancel
                           sendMessageWithKeyboard(getText(12), 11); // 12-редактирование 11 - клавиатоура редактирование
                           waitingType = WaitingType.START;
                       } else {
                           newSpecialist.setDescriptionKaz(updateMessageText);

                           deleteMessId = sendMessageWithKeyboard(getText(63), 47); // отменить добавление косу
                           waitingType = WaitingType.SET_DESC_SPEC_RUS;
                       }
                   }
                   else {
                       sendMessageWithKeyboard(getText(1002), 11);
                       deleteMessId = sendMessageWithKeyboard(getText(61),47); // отменить добавление косу
                       waitingType = WaitingType.SET_DESC_SPEC_KAZ;                   }
                   return COMEBACK;

               case SET_DESC_SPEC_RUS:
                   deleteMess();
                   if (hasMessageText()) {
                       if (isButton(96)) { //cancel
                           sendMessageWithKeyboard(getText(12), 11); // 12-редактирование 11 - клавиатоура редактирование
                           waitingType = WaitingType.START;
                       } else {
                           newSpecialist.setDescriptionRus(updateMessageText);

                           deleteMessId = sendMessageWithKeyboard(getText(1140), 47); // отменить добавление косу
                           waitingType = WaitingType.SET_PHOTO_SPEC;
                       }
                   } else {
                       sendMessageWithKeyboard(getText(1002), 11);
                       deleteMessId = sendMessageWithKeyboard(getText(63), 47); // отменить добавление косу
                       waitingType = WaitingType.SET_DESC_SPEC_RUS;
                   }
                   return COMEBACK;

               case SET_PHOTO_SPEC:
                   deleteMess();
                   if (isButton(96)){ //cancel
                       sendMessageWithKeyboard(getText(12), 11); // 12-редактирование 11 - клавиатоура редактирование
                       waitingType = WaitingType.START;
                   }
                   else{
                       if (hasPhoto()){
                           newSpecialist.setPhoto(updateMessagePhoto);
                           newSpecialist.setActive(true);

                           specialistRepository.save(newSpecialist);

                           sendMessageWithKeyboard(getText(62), 11); // 62 - спец добавлен 11 - клавиатоура редактирование
                           waitingType = WaitingType.START;
                       }
                       else {
                           sendMessageWithKeyboard(getText(1002), 11);
                           deleteMessId = sendMessageWithKeyboard(getText(1140), 47); // отменить добавление косу
                       }
                   }
                   return COMEBACK;

               default: return EXIT;
           }
       }
       catch (Exception e){
           e.printStackTrace();
           sendMessageWithKeyboard("Добро пожаловать!", 1);
           return EXIT;
       }
    }

    private void deleteMess() {

            deleteMessage(updateMessageId);
            deleteMessage(deleteMessId );
            deleteMessage(secondDeleteMessId);
            deleteMessage(thirdDeleteMessId);
    }


    private void deleteUpdateMess() {

            deleteMessage(updateMessageId);
    }


    public String getText(int messageIdFromDb) {
        return messageRepository.getMessageText(messageIdFromDb, getLanguage().getId());
    }


}

//package baliviya.com.github.bodistrict.telegrambot.service;
//
//import baliviya.com.github.bodistrict.telegrambot.entity.User;
//import baliviya.com.github.bodistrict.telegrambot.entity.enums.WaitingType;
//import baliviya.com.github.bodistrict.telegrambot.enums.Language;
//import baliviya.com.github.bodistrict.telegrambot.repository.ButtonRepository;
//import baliviya.com.github.bodistrict.telegrambot.repository.MessageRepository;
//import baliviya.com.github.bodistrict.telegrambot.repository.TelegramBotRepositoryProvider;
//import baliviya.com.github.bodistrict.telegrambot.repository.UserRepository;
//import baliviya.com.github.bodistrict.telegrambot.util.BotUtil;
//import baliviya.com.github.bodistrict.telegrambot.util.ButtonsLeaf;
//import baliviya.com.github.bodistrict.telegrambot.util.Const;
//import baliviya.com.github.bodistrict.telegrambot.util.UpdateUtil;
//import org.springframework.stereotype.Service;
//import org.telegram.telegrambots.meta.api.objects.Update;
//import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//public class RegistrationService {
//
//    private User user;
//    private long chatId;
//    private BotUtil botUtil;
//    private List<String> list;
//    private ButtonsLeaf buttonsLeaf;
//
//    private WaitingType waitingType = WaitingType.START;
//    private MessageRepository messageRepository = TelegramBotRepositoryProvider.getMessageRepository();
//    private UserRepository userRepository = TelegramBotRepositoryProvider.getUserRepository();
//    private ButtonRepository buttonRepository = TelegramBotRepositoryProvider.getButtonRepository();
//
//    private boolean COMEBACK = false;
//    private boolean EXIT = true;
//
//    private int deleteMessageId;
//    private int secDeleteMessageId;
//
//    public RegistrationService(long chatId) {
//        this.chatId = chatId;
//    }
//
//    public boolean isRegistration(Update update, BotUtil botUtil) throws TelegramApiException {
//        if (botUtil == null || chatId == 0) {
//            chatId = UpdateUtil.getChatId(update);
//            this.botUtil = botUtil;
//        }
//        switch (waitingType) {
//            case START:
//                delete();
//                user = new User();
//                user.setChatId(chatId);
//                deleteMessageId = botUtil.sendMessage(Const.SET_FULL_NAME_MESSAGE, chatId);
//                waitingType = WaitingType.SET_FULL_NAME;
//                return COMEBACK;
//            case SET_FULL_NAME:
//
//                delete();
//                if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().length() <= 50) {
//                    user.setFullName(update.getMessage().getText());
//                    secDeleteMessageId = botUtil.sendMessage(Const.SEND_CONTACT_MESSAGE, chatId);
//                    waitingType = WaitingType.SET_PHONE_NUMBER;
//                } else {
//                    secDeleteMessageId = botUtil.sendMessage(Const.WRONG_DATA_TEXT, chatId);
//                    deleteMessageId = botUtil.sendMessage(Const.SET_FULL_NAME_MESSAGE, chatId);
//                }
//                return COMEBACK;
//            case SET_PHONE_NUMBER:
//                delete();
//                if (update.getMessage().hasContact()) {
//                    String phone = update.getMessage().getContact().getPhoneNumber();
//
//                    if (phone.charAt(0) == '8') {
//                        phone = phone.replaceFirst("8", "+7");
//                    } else if (phone.charAt(0) == '7') {
//                        phone = phone.replaceFirst("7", "+7");
//                    }
//
//                    if (userRepository.findByPhone(phone) != null) {
//                        user.setId(userRepository.findByPhone(phone).getId());
//                    }
//
//                    user.setPhone(phone);
//                    user.setUserName(UpdateUtil.getFrom(update));
//                    getIin();
//                    waitingType = WaitingType.SET_IIN;
//
//                    return COMEBACK;
//                } else {
//                    secDeleteMessageId = botUtil.sendMessage(Const.WRONG_DATA_TEXT, chatId);
//                    deleteMessageId = botUtil.sendMessage(Const.SEND_CONTACT_MESSAGE, chatId);
//                    return COMEBACK;
//                }
//
//            case SET_IIN:
//                delete();
//                if (update.getMessage().hasText() && isIin(update.getMessage().getText())) {
//                    List<User> userList = userRepository.findAllByIin(update.getMessage().getText());
//                    if (userList.size() != 0) {
//                        botUtil.sendMessage(getText(93), chatId);
//                        getIin();
//                        waitingType = WaitingType.SET_IIN;
//                    } else {
//                        user.setIin(update.getMessage().getText());
//                        asd
//                    }
//                } else {
//                    wrongData();
//                    getIin();
//                    waitingType = WaitingType.SET_IIN;
//
//                }
//                return COMEBACK;
//            case CONFIRM:
//                if(update.hasCallbackQuery()){
//                    if(isButton(update,89)){//confirm
//                        userRepository.save(user);
//                        getStatus();
//                        waitingType = WaitingType.SET_STATUS;
//                    }
//                    else if(isButton(update,1005)){ //back
//                        botUtil.sendMessage(Const.SET_IIN_MESSAGE, chatId);
//                        waitingType = WaitingType.SET_IIN;
//                    }
//                    else{
//                        botUtil.sendMessage(Const.WRONG_DATA_TEXT, chatId);
//                        botUtil.sendMessageWithKeyboard(getText(94), 55);
//                    }
//
//                }
//                else{
//                    deleteMessId=  sendMessage(Const.WRONG_DATA_TEXT, chatId);
//                    deleteMessId = sendMessageWithKeyboard(getText(94), 55);
//                }
//                return COMEBACK;
//            case SET_STATUS:
//                if (update.hasCallbackQuery()) {
//                    if (list.get(Integer.parseInt(update.getCallbackQuery().getData())).equals(getText(Const.OTHERS_MESSAGE))) {
//                        getOther();
//                        waitingType = WaitingType.OTHER_STATUS;
//                    } else {
//
//                        user.setStatus(list.get(Integer.parseInt(update.getCallbackQuery().getData())));
//                        user.setEmail(Const.TABLE_NAME);
//                        userRepository.save(user);
//                        botUtil.sendMessage(4, chatId);
//                        return EXIT;
//                    }
//                } else {
//                    wrongData();
//                    getStatus();
//                }
//                return COMEBACK;
//            case OTHER_STATUS:
//                if (update.hasMessage() && update.getMessage().hasText()) {
//                    user.setStatus(update.getMessage().getText());
//                    user.setEmail(Const.TABLE_NAME);
//                    userRepository.save(user);
//                    botUtil.sendMessage(4, chatId);
//                    return EXIT;
//                } else {
//                    wrongData();
//                    getOther();
//                }
//                return COMEBACK;
//        }
//        return EXIT;
//    }
//
//    private boolean isIin(String text) {
//        try {
//            Long.parseLong(text);
//            return text.length() == 12;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    private void wrongData() throws TelegramApiException {
//        botUtil.sendMessage(Const.WRONG_DATA_TEXT, chatId);
//    }
//
//    private void getName() throws TelegramApiException {
//        botUtil.sendMessage(Const.SET_FULL_NAME_MESSAGE, chatId);
//    }
//
//    private void getPhone() throws TelegramApiException {
//        botUtil.sendMessage(Const.SEND_CONTACT_MESSAGE, chatId);
//    }
//
//    private int getIin() throws TelegramApiException {
//        return botUtil.sendMessage(Const.SET_IIN_MESSAGE, chatId);
//    }
//
//    private int wrongIinNotNumber() throws TelegramApiException {
//        return botUtil.sendMessage(Const.IIN_WRONG_MESSAGE, chatId);
//    }
//
//    private void getStatus() throws TelegramApiException {
//        list = new ArrayList<>();
//        Arrays.asList(getText(Const.STATUS_TYPE_MESSAGE).split(Const.SPLIT)).forEach((e) -> list.add(e));
//        list.add(getText(Const.OTHERS_MESSAGE));
//        buttonsLeaf = new ButtonsLeaf(list);
//        botUtil.sendMessageWithKeyboard(getText(Const.STATUS_MESSAGE), buttonsLeaf.getListButton(), chatId);
//    }
//
//    private int getOther() throws TelegramApiException {
//        return botUtil.sendMessage(Const.SET_YOUR_OPTION_MESSAGE, chatId);
//    }
//
//    private String getText(int messageIdFromDb) {
//        return messageRepository.getMessageText(messageIdFromDb, getLanguage().getId());
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    private Language getLanguage() {
//        if (chatId == 0) return Language.ru;
//        return LanguageService.getLanguage(chatId);
//    }
//
//
//    private void delete() {
////        botUtil.deleteMessage(updateMessageId);
//        botUtil.deleteMessage(chatId, deleteMessageId);
//        botUtil.deleteMessage(chatId, secDeleteMessageId);
//    }
//    protected boolean isButton(Update update ,int buttonId) {
//        if (update.getMessage().hasText()){
//
//            return update.getMessage().getText().equals(buttonRepository.getButtonText(buttonId,  getLanguage().getId()));
//        }
//    }
//}

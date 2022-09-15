package com.telegrambot.config;

import com.telegrambot.command.Command;
import com.telegrambot.command.impl.id054_GroupManager;
import com.telegrambot.enums.Language;
import com.telegrambot.exception.CommandNotFoundException;
import com.telegrambot.repository.KeyboardRepository;
import com.telegrambot.repository.MessageRepository;
import com.telegrambot.repository.TelegramBotRepositoryProvider;
import com.telegrambot.repository.UserRepository;
import com.telegrambot.service.CommandService;
import com.telegrambot.service.KeyboardMarkUpService;
import com.telegrambot.service.LanguageService;
import com.telegrambot.util.DateUtil;
import com.telegrambot.util.SetDeleteMessages;
import com.telegrambot.util.UpdateUtil;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.Date;

@Slf4j
public class Conversation {

    // уникальный идентификатор Telegram-a
    private         Long                chatId;

    // уникальный идентификатор для LanguageService
    private          long                currentChatId;
    public Command command;
    private CommandService commandService      = new CommandService();
    private         String              empty               = "";

    private MessageRepository messageRepository   = TelegramBotRepositoryProvider.getMessageRepository();
    private KeyboardRepository keyboardRepository   = TelegramBotRepositoryProvider.getKeyboardRepository();
    private UserRepository userRepository = TelegramBotRepositoryProvider.getUserRepository();
    protected KeyboardMarkUpService keyboardMarkUpService = new KeyboardMarkUpService();

    public          void        handleUpdate(Update update, DefaultAbsSender bot) throws TelegramApiException, SQLException {
        printUpdate(update);
        chatId          = UpdateUtil.getChatId(update);
        currentChatId   = chatId;
        checkLanguage(chatId);

        try {
            if (chatId < 0) {
                command = new id054_GroupManager();
                if (command.isInitNormal(update, bot)) {
                    clear();
                    return;
                }
                command.execute();
                clear();
                return;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            command     = commandService.getCommand(update).map(command1 -> {
                SetDeleteMessages.deleteKeyboard(chatId, bot);
                SetDeleteMessages.deleteMessage(chatId, bot);
                return command1;
            }).orElse(null);

        } catch (CommandNotFoundException e) {
            if (chatId < 0) return;
            if (command == null) {
                SetDeleteMessages.deleteKeyboard(chatId, bot);
                SetDeleteMessages.deleteMessage(chatId, bot);
                ReplyKeyboard replyKeyboard = keyboardMarkUpService.select(1 ,chatId)
                        .orElseThrow(() -> new TelegramApiException("keyboard id" + 1 + "not found"));

                if (userRepository.findByChatId(chatId) == null){
                    bot.execute(new SendMessage().setChatId(chatId).setText("Вы не зарегистрированы!\nНажмите на -> /start"));
                }
                else {
                    bot.execute(new SendMessage().setChatId(chatId).setText(messageRepository.findByIdAndLangId(2, getLanguage().getId()).getName()).setReplyMarkup(replyKeyboard));
                }
            }
        }
        if (command != null) {
            if (command.isInitNormal(update, bot)) {
                clear();
                return;
            }
            boolean commandFinished = command.execute();
            if (commandFinished) clear();
        }
    }

    private         void        printUpdate(Update update) {
        String dataMessage = empty;
        if (update.hasMessage()) dataMessage = DateUtil.getDbMmYyyyHhMmSs(new Date((long) update.getMessage().getDate() * 1000));
        log.info("New update get {} -> send response {}", dataMessage, DateUtil.getDbMmYyyyHhMmSs(new Date()));
        log.info(UpdateUtil.toString(update));
    }

    public    long        getCurrentChatId() { return currentChatId; }

//    private         void        checkLanguage(long chatId) { LanguageService.getLanguage(chatId); }

    private void checkLanguage(long chatId) {
        LanguageService.getLanguage(chatId);
//        if (LanguageService.getLanguage(chatId) == null) LanguageService.setLanguage(chatId, Language.ru);
    }

    private Language getLanguage() {
        if (chatId == 0) return Language.ru;
        return LanguageService.getLanguage(chatId);
    }

    private         void        clear() {
        command.clear();
        command         = null;
    }
}

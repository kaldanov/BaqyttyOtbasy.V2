package com.telegrambot.config;

import com.telegrambot.util.Const;
import com.telegrambot.util.UpdateUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


@Component
@Slf4j
@Getter
@Setter
public class Bot extends TelegramLongPollingBot {
    @Value("${telegram-bot-username}")
    private String                  botUsername;

   // @Value("${table-schema}")
    private String     tableSchema = Const.TABLE_SCHEMA.toUpperCase();


    @Value("${telegram-bot-token}")
    private String                  botToken;
    //private Map<Long, Conversation> conversations = new HashMap<>();
    private Map<Long, Conversation> conversations = new HashMap<>();

    @PostConstruct
    public  void            initIt() throws TelegramApiRequestException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        telegramBotsApi.registerBot(this);
        log.info("Bot was registered : " + botUsername);
    }
//    private ButtonRepository buttonRepository  = TelegramBotRepositoryProvider.getButtonRepository();
    @Override
    public void onUpdateReceived(Update update) {
        //System.out.println(update);
        //Long chatId               = UpdateUtil.getChatId(update);
        //List<Button> buttons = buttonRepository.findAll();
        //System.out.println(buttons);
        Conversation conversation = getConversation(update);
        try {

            conversation.handleUpdate(update, this);
        } catch (TelegramApiException | SQLException e) {
            log.error("Error in conversation handleUpdate" + e);
        }
    }
    public String getTableSchema(){

        return tableSchema.toUpperCase();
    }
    private Conversation    getConversation(Update update) {
        Long chatId                 = UpdateUtil.getChatId(update);
        Conversation conversation   = conversations.get(chatId);
        if (conversation == null) {
            log.info("InitNormal new conversation for '{}'", chatId);
            conversation            = new Conversation();
            conversations.put(chatId, conversation);
        }
        return conversation;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
    @Override
    public String getBotToken() {
        return botToken;
    }
}

package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.util.UpdateUtil;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.sql.SQLException;
import java.util.HashSet;

public class id054_GroupManager extends Command {

    private static HashSet<Integer> formRegistrMessages = new HashSet<>();
    private Chat chat;

    private void groupInit() {
        chat = UpdateUtil.getChat(update);
        updateMessage = UpdateUtil.getMessage(update);
        if (updateMessage != null) {
            updateMessageId = updateMessage.getMessageId();
            // init updateText
            if (update.hasCallbackQuery()) {//это врядли тут появится, но на всякий случай
                updateMessageText = update.getCallbackQuery().getData();
            } else if (update.hasMessage()) {
                if (updateMessage.hasText()) {
                    updateMessageText = updateMessage.getText();
                } else if (updateMessage.getCaption() != null) {
                    updateMessageText = update.getMessage().getCaption();
                }
            } else {
                updateMessageText = null;
            }
        }
    }

    @Override
    public boolean execute() throws SQLException, TelegramApiException {
        groupInit();
        if (chat == null || !chat.isSuperGroupChat()) {
            return COMEBACK;
        }

        if (!isUserRegistered()) {
            deleteMessage(updateMessage.getMessageId());
            sendRegistrationMessage();
            return COMEBACK;
        }

        return COMEBACK;
    }

    private String getLinkT(String link, String text) {
        return new StringBuilder().append("<a href = \"https://t.me/").append(link).append("\">").append(text).append("</a>\n").toString();
    }

    private void sendRegistrationMessage() throws TelegramApiException {
        for (Integer formRegistrMessage : formRegistrMessages) {
            deleteMessage(formRegistrMessage);
        }
        StringBuilder groups = new StringBuilder();
        User user = UpdateUtil.getUser(update);
        String from = (user.getFirstName() != null ? user.getFirstName() + " " : "") + (user.getLastName() != null ? user.getLastName() : "");
        if (from.isEmpty()) {
            from = user.getUserName();
        }
        groups.append(getLinkForUser(user.getId(), from));
        groups.append(" чтобы писать в группе, необходимо пройти регистрацию").append("\n");
        groups.append(getLinkT(bot.getMe().getUserName(), "Регистрация")).append("\n");
        formRegistrMessages.add(sendMessage(groups.toString()));

//        + "?start=" + group.getUserName()
    }

    private boolean isUserRegistered() {
        if(UpdateUtil.getUser(update) != null)
            return userRepository.findByChatId(UpdateUtil.getUser(update).getId()) != null;
        return false;
    }
}

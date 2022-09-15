package com.telegrambot.command.impl;

import com.telegrambot.command.Command;
import com.telegrambot.entity.custom.Category;
import com.telegrambot.entity.custom.CategoryGroup;
import com.telegrambot.entity.custom.Group;
import com.telegrambot.entity.enums.WaitingType;
import com.telegrambot.util.Const;
import com.telegrambot.util.ParserMessageEntity;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class id026_EditGroup extends Command {
    private String              commChangeRegGroup;
    private String              commSetting;
    private String              commWithoutTag;
    private String              commLink;
    private String              commSticker;
    private String              commPhoto;
    private String              commVideo;
    private String              commAudio;
    private String              commFile;
    private String              commHash;
    private String              commEditMessage;
    private String              commShowMessage;
    private String              changeTag;
    private String              commBack;
    private ParserMessageEntity parserMessageEntity = new ParserMessageEntity();
    private int                 deleteMessageId;
    private Group               group;
    private CategoryGroup categoryGroup;

    @Override
    public boolean  execute()       throws TelegramApiException {
        if(userRepository.findByChatId(chatId) == null){
            sendMessageWithKeyboard("Вы не прошли регистрацию" , 57);
            return EXIT;
        }
        if (!isAdmin() && !isMainAdmin()) {
            sendMessage(Const.NO_ACCESS);
            return EXIT;
        }
        if (commChangeRegGroup == null) initCommand();
        switch (waitingType) {
            case START:
                deleteMessage(updateMessageId);
                group = groupRepository.findById(propertiesRepository.findById(Const.GROUP_ID_FROM_PROPERTIES).getId());
                sendListGroup();
                return COMEBACK;
            case CHOOSE_GROUP:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasMessageText()) {
                    if (isCommand(commChangeRegGroup)) {
                        group   .setRegistered(!group.isRegistered());
                        groupRepository.save(group);
                        sendListGroup();
                    } else if (isCommand(commSetting)) {
                        sendInfoGroup();
                    }
                }
                return COMEBACK;
            case EDITION:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasMessageText()) {
                    if (isCommand(commBack)) {
                        sendListGroup();
                    } else if (isCommand(commWithoutTag)) {
                        group.setCanWithoutTag(!group   .isCanWithoutTag());
                    } else if (isCommand(commLink)) {
                        group.setCanLink(!group         .isCanLink());
                    } else if (isCommand(commSticker)) {
                        group.setCanSticker(!group      .isCanSticker());
                    } else if (isCommand(commPhoto)) {
                        group.setCanPhoto(!group        .isCanPhoto());
                    } else if (isCommand(commVideo)) {
                        group.setCanVideo(!group        .isCanVideo());
                    } else if (isCommand(commAudio)) {
                        group.setCanAudio(!group        .isCanAudio());
                    } else if (isCommand(commFile)) {
                        group.setCanFile(!group         .isCanFile());
                    } else if (isCommand(commHash)) {
                        sendTags();
                        return COMEBACK;
                    } else if (isCommand(commEditMessage)) {
                        sendMessage(getText(Const.WELCOME_GROUP_TEXT_EDIT_MESSAGE) + next + "Назад - " + commBack);
                        waitingType = WaitingType.EDITION_MESSAGE;
                        return COMEBACK;
                    } else if (isCommand(commShowMessage)) {
                        sendMessage(group.getMessage() == null ? "Не задано" : group.getMessage());
                        sendInfoGroup();
                        return COMEBACK;
                    }
                    groupRepository.save(group);
                    sendInfoGroup();
                }
                return COMEBACK;
            case EDIT_TAG:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasMessageText()) {
                    if (isCommand(commBack)) {
                        sendInfoGroup();
                        return COMEBACK;
                    } else if (isCommand(changeTag)) {
                        List<CategoryGroup> categoryGroups = categoryGroupRepository.findAllByIdAndGroupChatId(getInt(), group.getChatId());
                        if (categoryGroups != null && categoryGroups.size() > 0) {
                            categoryGroupRepository.deleteByIdAndGroupChatId(getInt(), group.getChatId());
                        } else {
                            categoryGroup = new CategoryGroup();
                            categoryGroup.setGroupChatId(group.getChatId());
                            categoryGroupRepository.save(categoryGroup);
                        }
                    }
                    sendTags();
                }
                return COMEBACK;
            case EDITION_MESSAGE:
                deleteMessage(updateMessageId);
                deleteMessage(deleteMessageId);
                if (hasMessageText()) {
                    if (isCommand(commBack)) {
                        sendInfoGroup();
                        return COMEBACK;
                    }
                    if (updateMessageText.equalsIgnoreCase("удалить")) {
                        group.setMessage(null);
                    } else {
                        group.setMessage(parserMessageEntity.parseEntityToStringTag(updateMessage));
                    }
                    groupRepository.save(group);
                    sendInfoGroup();
                }
                return COMEBACK;
        }
        return COMEBACK;
    }

    private void    initCommand() {
        commChangeRegGroup  = buttonRepository.findByIdAndLangId(Const.TAG_GR_BUTTON, getLanguage().getId()).getName();
        commSetting         = buttonRepository.findByIdAndLangId(Const.TAG_ST_BUTTON, getLanguage().getId()).getName();
        commWithoutTag      = buttonRepository.findByIdAndLangId(Const.TAG_ED0_BUTTON, getLanguage().getId()).getName();
        commLink            = buttonRepository.findByIdAndLangId(Const.TAG_ED1_BUTTON, getLanguage().getId()).getName();
        commSticker         = buttonRepository.findByIdAndLangId(Const.TAG_ED2_BUTTON, getLanguage().getId()).getName();
        commPhoto           = buttonRepository.findByIdAndLangId(Const.TAG_ED3_BUTTON, getLanguage().getId()).getName();
        commVideo           = buttonRepository.findByIdAndLangId(Const.TAG_ED4_BUTTON, getLanguage().getId()).getName();
        commAudio           = buttonRepository.findByIdAndLangId(Const.TAG_ED5_BUTTON, getLanguage().getId()).getName();
        commFile            = buttonRepository.findByIdAndLangId(Const.TAG_ED6_BUTTON, getLanguage().getId()).getName();
        commHash            = buttonRepository.findByIdAndLangId(Const.TAG_TAG_BUTTON, getLanguage().getId()).getName();
        commEditMessage     = buttonRepository.findByIdAndLangId(Const.TAG_ED7_BUTTON, getLanguage().getId()).getName();
        commShowMessage     = buttonRepository.findByIdAndLangId(Const.TAG_MSG_BUTTON, getLanguage().getId()).getName();
        changeTag           = buttonRepository.findByIdAndLangId(Const.TAG_H_BUTTON, getLanguage().getId()).getName();
        commBack            = buttonRepository.findByIdAndLangId(Const.TAG_BACK_BUTTON, getLanguage().getId()).getName();
    }

    private String  yesOrNot(boolean b) { return b ? Const.YES : Const.NO; }

    private String  getLinkGroup(Group group) { return "<a href = \"https://t.me/" + group.getUserName() + "/" + "\">" + group.getNames() + "</a>"; }

    private boolean isCommand(String command) { return updateMessageText.startsWith(command); }

    private void    sendListGroup() throws TelegramApiException {
        String formatMessage        = getText(Const.GROUP_CHANGE_INFO_MESSAGE);
        StringBuilder infoByGroups  = new StringBuilder();
        String format               = getText(Const.GROUP_EDIT_MESSAGE);
        infoByGroups.append(String.format(format, yesOrNot(group.isRegistered()), commChangeRegGroup, commSetting, getLinkGroup(group)));
        deleteMessageId             = sendMessage(String.format(formatMessage, infoByGroups.toString()));
        waitingType                 = WaitingType.CHOOSE_GROUP;
    }

    private void    sendInfoGroup() throws TelegramApiException {
        String format                       = getText(Const.GROUP_EDIT_INFO_MESSAGE);
        StringBuilder listCategory          = new StringBuilder();
        List<CategoryGroup> categoryGroups  = categoryGroupRepository.findByGroupChatId(group.getChatId());
        if (categoryGroups != null && categoryGroups.size() > 0) {
            categoryGroups.forEach((category) -> listCategory.append(categoryRepository.findById(category.getId()).getName()).append(", "));
            listCategory  .deleteCharAt(listCategory.length() - 2);
            listCategory  .append(next);
        }
        String result = String.format(format,
                        yesOrNot(group.isRegistered()),
                        getLinkGroup(group),
                        yesOrNot(group.isCanWithoutTag())   + space + commWithoutTag,
                        yesOrNot(group.isCanLink())         + space + commLink,
                        yesOrNot(group.isCanSticker())      + space + commSticker,
                        yesOrNot(group.isCanPhoto())        + space + commPhoto,
                        yesOrNot(group.isCanVideo())        + space + commVideo,
                        yesOrNot(group.isCanAudio())        + space + commAudio,
                        yesOrNot(group.isCanFile())         + space + commFile,
                        commHash,
                        listCategory.toString(),
                        group.getMessage() == null ? "Не задано" : StringUtils.abbreviate(group.getMessage(), 100),
                        commEditMessage,
                        commShowMessage,
                        commBack
                );
        deleteMessageId = sendMessage(result);
        waitingType     = WaitingType.EDITION;
    }

    private void    sendTags()      throws TelegramApiException {
        List<Category>      include             = new ArrayList<>();
        List<Category>      exclude             = new ArrayList<>();
        List<Category>      categories          = categoryRepository.findAll();
        List<CategoryGroup> categoryGroups      = categoryGroupRepository.findByGroupChatId(group.getChatId());
        if (categoryGroups.size() > 0) {
            for (Category category : categories) {
                for (CategoryGroup group : categoryGroups) {
                    if (category.getId() == group.getId()) {
                        include.add(category);
                    } else {
                        exclude.add(category);
                    }
                }
            }
        } else {
            categories.forEach(category -> exclude.add(category));
        }
        StringBuilder info = new StringBuilder();
        info.append("Группа ").append(getBolt(group.getNames())).append(next).append(next);
        info.append("Теги включенные в группе: ").append(next);
        include.forEach(category -> info.append(changeTag).append(category.getId()).append(" - ").append(category.getName()).append(next));
        info.append(next).append("Доступные теги:").append(next);
        exclude.forEach(category -> info.append(changeTag).append(category.getId()).append(" - ").append(category.getName()).append(next));
        info.append(next).append("Назад: ").append(commBack);
        deleteMessageId = sendMessage(info.toString());
        waitingType     = WaitingType.EDIT_TAG;
    }

    private int     getInt() { return Integer.parseInt(updateMessageText.replaceAll("[^0-9]", "")); }
}

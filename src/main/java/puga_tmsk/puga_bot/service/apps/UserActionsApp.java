package puga_tmsk.puga_bot.service.apps;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import puga_tmsk.puga_bot.config.BotStatus;
import puga_tmsk.puga_bot.model.User;
import puga_tmsk.puga_bot.service.TelegramBot;

import java.sql.Timestamp;

@Slf4j
public class UserActionsApp {

    private final TelegramBot telegramBot;

    public UserActionsApp(TelegramBot tgb) {
        telegramBot = tgb;
    }

    public void registerUser(Message message) {

        telegramBot.setBotStatus(message, BotStatus.MAIN);

        if(telegramBot.getUserRepository().findById(message.getChatId()).isEmpty()) {

            Long chatId = message.getChatId();
            Chat chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisterTime(new Timestamp(System.currentTimeMillis()));

            telegramBot.getUserRepository().save(user);

            log.info("User saved: " + user);
        }
    }
}

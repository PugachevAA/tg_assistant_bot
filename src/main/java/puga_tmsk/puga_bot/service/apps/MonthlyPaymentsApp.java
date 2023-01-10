package puga_tmsk.puga_bot.service.apps;

import puga_tmsk.puga_bot.model.MonthlyPayments;
import puga_tmsk.puga_bot.service.TelegramBot;

import javax.persistence.Id;
import java.math.BigDecimal;

public class MonthlyPaymentsApp {
    private final TelegramBot telegramBot;

    public MonthlyPaymentsApp(TelegramBot tgb) {
        telegramBot = tgb;
    }

    public void addItemName(Long chatId, String messageText) {
        MonthlyPayments mpForAdd = new MonthlyPayments();
        mpForAdd.setUserId(chatId);
        mpForAdd.setTitle(messageText);
        mpForAdd.setPrice(BigDecimal.valueOf(0));
        mpForAdd.setAddFinish(false);
        telegramBot.getMonthlyPaymentsRepository().save(mpForAdd);
    }
    public void addItemPrice(Long chatId, String messageText) {
        MonthlyPayments mpForAdd = telegramBot.getMonthlyPaymentsRepository().findByUserIdAndAddFinish(chatId, false);
        Double price;
        try {
            price = new Double(messageText);
            mpForAdd.setPrice(BigDecimal.valueOf(price));
            mpForAdd.setAddFinish(true);
            telegramBot.getMonthlyPaymentsRepository().save(mpForAdd);
        } catch (NumberFormatException e) {
            price = null; // не-а, не double
            telegramBot.sendMessage(chatId, "Неверно введена цена, попробуй еще раз", "", telegramBot.getInLineKeyboards().getMonthlyPaymentsAdd());
        }

    }

    public void cancelAddItem(long chatId) {
        MonthlyPayments mp = telegramBot.getMonthlyPaymentsRepository().findByUserIdAndAddFinish(chatId, false);
        if (mp != null) {
            telegramBot.getMonthlyPaymentsRepository().delete(telegramBot.getMonthlyPaymentsRepository().findByUserIdAndAddFinish(chatId, false));
        }
    }

    public void deleteItem(long id) {
        telegramBot.getMonthlyPaymentsRepository().deleteById(id);
    }
}

package puga_tmsk.puga_bot.service.apps;

import org.telegram.telegrambots.meta.api.objects.Message;
import puga_tmsk.puga_bot.config.BotStatus;
import puga_tmsk.puga_bot.model.MonthlyPayments;
import puga_tmsk.puga_bot.service.TelegramBot;
import java.math.BigDecimal;

public class MonthlyPaymentsApp {
    private final TelegramBot telegramBot;

    public MonthlyPaymentsApp(TelegramBot tgb) {
        telegramBot = tgb;
    }

    public void addItemName(Message msg) {
        MonthlyPayments mpForAdd = new MonthlyPayments();
        mpForAdd.setUserId(msg.getChatId());
        mpForAdd.setTitle(msg.getText());
        mpForAdd.setPrice(BigDecimal.valueOf(0));
        mpForAdd.setAddFinish(false);
        telegramBot.getMonthlyPaymentsRepository().save(mpForAdd);
        telegramBot.sendMessage(msg, "Введи сумму платежа:", BotStatus.MONTHLY_PAYMENTS_ADD_PRICE,
                telegramBot.getInLineKeyboards().getMonthlyPaymentsAdd());
    }

    public void addItemPrice(Message msg) {
        MonthlyPayments mpForAdd = telegramBot.getMonthlyPaymentsRepository().findByUserIdAndAddFinish(msg.getFrom().getId(), false);
        Double price;
        try {
            price = new Double(msg.getText());
            mpForAdd.setPrice(BigDecimal.valueOf(price));
            mpForAdd.setAddFinish(true);
            telegramBot.getMonthlyPaymentsRepository().save(mpForAdd);

            telegramBot.sendMessage(msg, "Ежемесячные платежи",
                    BotStatus.MONTHLY_PAYMENTS, telegramBot.getInLineKeyboards().getMonthlyPayments(msg.getChatId()));
        } catch (NumberFormatException e) {
            telegramBot.sendMessage(msg,"Неверно введена цена, попробуй еще раз", BotStatus.MONTHLY_PAYMENTS_ADD_PRICE,
                    telegramBot.getInLineKeyboards().getMonthlyPaymentsAdd());
        }

    }

    public void cancelAddItem(Message msg) {
        MonthlyPayments mp = telegramBot.getMonthlyPaymentsRepository().findByUserIdAndAddFinish(msg.getChatId(), false);
        if (mp != null) {
            telegramBot.getMonthlyPaymentsRepository().delete(telegramBot.getMonthlyPaymentsRepository().findByUserIdAndAddFinish(msg.getChatId(), false));
        }
    }

    public void deleteItem(long id) {
        telegramBot.getMonthlyPaymentsRepository().deleteById(id);
    }
}

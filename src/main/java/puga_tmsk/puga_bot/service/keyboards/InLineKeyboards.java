package puga_tmsk.puga_bot.service.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import puga_tmsk.puga_bot.model.MonthlyPayments;
import puga_tmsk.puga_bot.model.MonthlyPaymentsRepository;
import puga_tmsk.puga_bot.model.ShoppingList;
import puga_tmsk.puga_bot.model.ShoppingListRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InLineKeyboards {

    public InlineKeyboardMarkup getMain() {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.add(row1btn(button("Списки", "/lists")));
        keyboardRows.add(row1btn(button("Ежемесячные платежи","/monthly_payments")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    public InlineKeyboardMarkup getLists() {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.add(row1btn(button("Сходить в магазин","/shoppinglist")));
        //keyboardRows.add(row1btn(button("Вишлист (в разработке)", "/wishlist")));
        //keyboardRows.add(row1btn(button("Подписки (в разработке)","/subscriptions")));
        keyboardRows.add(row1btn(button("Назад", "/main")));

        return new InlineKeyboardMarkup(keyboardRows);
    }


    public InlineKeyboardMarkup getShoppingList(Long chatId, String message, ShoppingListRepository shoppingListRepository) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.addAll(generateShoppingListButtons(chatId, message, shoppingListRepository));
        keyboardRows.add(row2btns(button("Заполнить", "/shoplistadditems"), button("Назад", "/lists")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    public InlineKeyboardMarkup getShoppingListAdd(Long chatId, String message, ShoppingListRepository shoppingListRepository) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.addAll(generateShoppingListButtons(chatId, message, shoppingListRepository));
        keyboardRows.add(row1btn(button("Закончить", "/shoplistendadd")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    public InlineKeyboardMarkup getMonthlyPayments(Long chatId, String message, MonthlyPaymentsRepository mpr) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        keyboardRows.addAll(generateMonthlyPaymentsButtons(chatId, message, mpr));
        keyboardRows.add(row2btns(button("Добавить", "/monthly_payments_add"), button("Назад", "/main")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    public InlineKeyboardMarkup getMonthlyPaymentsAdd() {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        //keyboardRows.addAll(generateMonthlyPaymentsButtons(chatId, message, mpr));
        keyboardRows.add(row1btn(button("Отменить", "/monthly_payments_add_cancel")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    public InlineKeyboardMarkup getMonthlyPaymentsEdit(long id) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        //keyboardRows.add(row2btns(button("Изменить название", "/monthly_payments_item_edittitle_" + id),
        //                            button("Изменить платеж", "/monthly_payments_item_editprice_" + id)));
        keyboardRows.add(row1btn(button("Удалить","/monthly_payments_item_delete_" + id)));
        keyboardRows.add(row1btn(button("Назад", "/monthly_payments")));

        return new InlineKeyboardMarkup(keyboardRows);
    }

    private Collection<? extends List<InlineKeyboardButton>> generateMonthlyPaymentsButtons(Long userId, String message, MonthlyPaymentsRepository mpr) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        if (mpr.count() > 0) {
            for (MonthlyPayments mp : mpr.findAll()) {
                if (mp.getUserId() == userId) {
//                    keyboardRows.add(row2btns(button(mp.getTitle() + ", " + mp.getPrice(), "/monthly_payments_" + mp.getId()),
//                                                button(mp.getTitle(), "/monthly_payments_" + mp.getId() + "_payed")));
                    keyboardRows.add(row1btn(button(mp.getTitle() + ", " + mp.getPrice(), "/monthly_payments_item_view_" + mp.getId())));
                }
            }
        }
        return keyboardRows;
    }


    private List<List<InlineKeyboardButton>> generateShoppingListButtons(Long chatId, String message, ShoppingListRepository shoppingListRepository) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        if (shoppingListRepository.count() > 0) {
            for (ShoppingList shoppingList1 : shoppingListRepository.findAll()) {
                if (shoppingList1.getChatId().equals(chatId)) {
                    keyboardRows.add(row1btn(button(shoppingList1.getProduct(), "/shoppinglist_" + shoppingList1.getId())));
                }
            }
        }
        return keyboardRows;
    }


    private List<InlineKeyboardButton> row1btn(InlineKeyboardButton btn) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(btn);
        return row;
    }
    private List<InlineKeyboardButton> row2btns(InlineKeyboardButton btn1,InlineKeyboardButton btn2) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(btn1);
        row.add(btn2);
        return row;
    }

    private InlineKeyboardButton button(String text, String data) {
        InlineKeyboardButton checkButton = new InlineKeyboardButton();
        checkButton.setText(text);
        checkButton.setCallbackData(data);
        return checkButton;
    }

}

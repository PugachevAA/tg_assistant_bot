package puga_tmsk.puga_bot.service.keyboards;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import puga_tmsk.puga_bot.model.ShoppingList;
import puga_tmsk.puga_bot.model.ShoppingListRepository;
import java.util.ArrayList;
import java.util.List;

public class InLineKeyboards {

    public InlineKeyboardMarkup getMain() {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        List<InlineKeyboardButton> keyboardRow1 = new ArrayList<>();
        InlineKeyboardButton lists = new InlineKeyboardButton();
        lists.setText("Списки");
        lists.setCallbackData("/lists");
        keyboardRow1.add(lists);

        List<InlineKeyboardButton> keyboardRow2 = new ArrayList<>();
        InlineKeyboardButton monthlyPayments = new InlineKeyboardButton();
        monthlyPayments.setText("Ежемесячные платежи (в разработке)");
        monthlyPayments.setCallbackData("/monthly_payments");
        keyboardRow2.add(monthlyPayments);

        keyboardRows.add(keyboardRow1);
        keyboardRows.add(keyboardRow2);

        final InlineKeyboardMarkup mainInLineKeyboard = new InlineKeyboardMarkup();
        mainInLineKeyboard.setKeyboard(keyboardRows);

        return mainInLineKeyboard;
    }

    public InlineKeyboardMarkup getLists() {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        List<InlineKeyboardButton> keyboardRow1 = new ArrayList<>();
        InlineKeyboardButton shoppingList = new InlineKeyboardButton();
        shoppingList.setText("Сходить в магазин");
        shoppingList.setCallbackData("/shoppinglist");
        keyboardRow1.add(shoppingList);

        List<InlineKeyboardButton> keyboardRow2 = new ArrayList<>();
        InlineKeyboardButton wishList = new InlineKeyboardButton();
        wishList.setText("Вишлист (в разработке)");
        wishList.setCallbackData("/wishlist");
        keyboardRow2.add(wishList);

        List<InlineKeyboardButton> keyboardRow3 = new ArrayList<>();
        InlineKeyboardButton subscriptions = new InlineKeyboardButton();
        subscriptions.setText("Подписки (в разработке)");
        subscriptions.setCallbackData("/subscriptions");
        keyboardRow3.add(subscriptions);

        List<InlineKeyboardButton> keyboardRow4 = new ArrayList<>();
        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("Назад");
        back.setCallbackData("/main");
        keyboardRow4.add(back);

        keyboardRows.add(keyboardRow1);
        keyboardRows.add(keyboardRow2);
        keyboardRows.add(keyboardRow3);
        keyboardRows.add(keyboardRow4);

        final InlineKeyboardMarkup mainInLineKeyboard = new InlineKeyboardMarkup();
        mainInLineKeyboard.setKeyboard(keyboardRows);

        return mainInLineKeyboard;
    }

    public InlineKeyboardMarkup getShoppingList(Long chatId, String message, ShoppingListRepository shoppingListRepository) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        for (List<InlineKeyboardButton> row : generateShoppingListButtons(chatId, message, shoppingListRepository)) {
            keyboardRows.add(row);
        }

        List<InlineKeyboardButton> keyboardRow1 = new ArrayList<>();

        InlineKeyboardButton additems = new InlineKeyboardButton();
        additems.setText("Заполнить");
        additems.setCallbackData("/shoplistadditems");
        keyboardRow1.add(additems);

//        InlineKeyboardButton clearall = new InlineKeyboardButton();
//        clearall.setText("Удалить все");
//        clearall.setCallbackData("/shoplistclear");
//        keyboardRow1.add(clearall);

        List<InlineKeyboardButton> keyboardRow2 = new ArrayList<>();

        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText("Назад");
        back.setCallbackData("/lists");
        keyboardRow1.add(back);

        keyboardRows.add(keyboardRow1);
        keyboardRows.add(keyboardRow2);

        final InlineKeyboardMarkup mainInLineKeyboard = new InlineKeyboardMarkup();
        mainInLineKeyboard.setKeyboard(keyboardRows);

        return mainInLineKeyboard;
    }

    public InlineKeyboardMarkup getShoppingListAdd(Long chatId, String message, ShoppingListRepository shoppingListRepository) {

        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        for (List<InlineKeyboardButton> row : generateShoppingListButtons(chatId, message, shoppingListRepository)) {
            keyboardRows.add(row);
        }

        List<InlineKeyboardButton> keyboardRow1 = new ArrayList<>();

        InlineKeyboardButton additems = new InlineKeyboardButton();
        additems.setText("Закончить");
        additems.setCallbackData("/shoplistendadd");
        keyboardRow1.add(additems);

        keyboardRows.add(keyboardRow1);

        final InlineKeyboardMarkup mainInLineKeyboard = new InlineKeyboardMarkup();
        mainInLineKeyboard.setKeyboard(keyboardRows);

        return mainInLineKeyboard;
    }

    private List<List<InlineKeyboardButton>> generateShoppingListButtons(Long chatId, String message, ShoppingListRepository shoppingListRepository) {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        if (shoppingListRepository.count() > 0) {
            for (ShoppingList shoppingList1 : shoppingListRepository.findAll()) {
                if (shoppingList1.getChatId().equals(chatId)) {

                    List<InlineKeyboardButton> keyboardRowTemp = new ArrayList<>();

                    InlineKeyboardButton itemButton = new InlineKeyboardButton();
                    itemButton.setText(shoppingList1.getProduct());
                    itemButton.setCallbackData("/shoppinglist_" + shoppingList1.getId());
                    keyboardRowTemp.add(itemButton);
                    keyboardRows.add(keyboardRowTemp);
                }
            }
        }
        return keyboardRows;
    }


}

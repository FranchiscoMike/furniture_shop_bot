package uz.pdp.furniture_shop_bot.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.pdp.furniture_shop_bot.entity.Basket;
import uz.pdp.furniture_shop_bot.entity.BotUser;
import uz.pdp.furniture_shop_bot.entity.Category;
import uz.pdp.furniture_shop_bot.entity.SavedProduct;
import uz.pdp.furniture_shop_bot.entity.enums.Language;
import uz.pdp.furniture_shop_bot.repository.BasketRepository;
import uz.pdp.furniture_shop_bot.repository.BotUserRepository;
import uz.pdp.furniture_shop_bot.repository.CategoryRepository;
import uz.pdp.furniture_shop_bot.repository.SavedProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BotService {

    public SendMessage selectLanguage(Update update) {
        Long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        KeyboardButton uzbek = new KeyboardButton(Buttons.UZBEK);
        KeyboardButton rus = new KeyboardButton(Buttons.RUS);
        KeyboardButton krill = new KeyboardButton(Buttons.KRILL);


        row.add(uzbek);
        row.add(rus);
        row.add(krill);

        sendMessage.setText("Select your language");
        replyKeyboardMarkup.setKeyboard(List.of(row));
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }

    public SendMessage defaultMessage(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());

        sendMessage.setText("tushunmadim to'g'ri ma'lumot kiriting!");
        return sendMessage;
    }

    private final BotUserRepository botUserRepository;


    public SendMessage setLanguage(Update update) {
        String language = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        BotUser user = botUserRepository.findByChatId(chatId.toString()).get();


        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        if (language.equals(Buttons.UZBEK)) {
            user.setLanguage(Language.UZBEK);
            botUserRepository.save(user);
            sendMessage.setText("siz o'zbek tilini tanladingiz!\nTelefon raqamingizni ulashing:");
        } else if (language.equals(Buttons.KRILL)) {
            user.setLanguage(Language.KRILL);
            botUserRepository.save(user);
            sendMessage.setText("сиз ўзбек тилини танладингиз!\nТелефон рақамингизни улашинг:");
        } else if (language.equals(Buttons.RUS)) {
            user.setLanguage(Language.RUS);
            botUserRepository.save(user);
            sendMessage.setText("Вы выбрали русский язык!\nПоделитесь своим номером телефона:");
        } else {
            sendMessage.setText("\uD83C\uDD98 No to'g'ri ma'alumot kiritdingiz ⁉️");
            Optional<BotUser> byChatId = botUserRepository.findByChatId(update.getMessage().getChatId().toString());
            BotUser botUser = byChatId.get();
            botUser.setState(States.START);
            botUserRepository.save(botUser);
            return sendMessage;
        }


        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        KeyboardButton sharePhoneNumber = new KeyboardButton(Buttons.SHARE_PHONE_NUMBER);
        sharePhoneNumber.setRequestContact(true);
        row.add(sharePhoneNumber);

        rows.add(row);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        replyKeyboardMarkup.setKeyboard(rows);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        return sendMessage;
    }

    public SendMessage login(Update update) {
        Long chatId = update.getMessage().getChatId();

        BotUser user = botUserRepository.findByChatId(chatId.toString()).get();


        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasContact()) {
                Contact contact = message.getContact();
                user.setPhoneNumber(contact.getPhoneNumber());
                user.setFullName(contact.getFirstName() + " " + contact.getLastName());
                user.setState(States.SELECT_FROM_MENU);
                botUserRepository.save(user);
                sendMessage.setText("Thanks for your share!\nSelect from menu!");
            } else {
                sendMessage.setText("Please share your contact!");
            }
            // buttons for menu
            List<KeyboardRow> rowList = new ArrayList<>();

            KeyboardRow row1 = new KeyboardRow();
            KeyboardRow row2 = new KeyboardRow();
            KeyboardRow row3 = new KeyboardRow();

            KeyboardButton categories = new KeyboardButton(Buttons.PARENT_CATEGORIES);
            KeyboardButton orders = new KeyboardButton(Buttons.ORDERS);
            KeyboardButton basket = new KeyboardButton(Buttons.BASKET);
            KeyboardButton profile = new KeyboardButton(Buttons.PROFILE);
            KeyboardButton settings = new KeyboardButton(Buttons.SETTINGS);


            row1.add(categories);
            row1.add(orders);

            row2.add(basket);

            row3.add(profile);
            row3.add(settings);

            rowList.add(row1);
            rowList.add(row2);
            rowList.add(row3);


            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(rowList);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }

        sendMessage.setProtectContent(true);
        sendMessage.setAllowSendingWithoutReply(false);
        return sendMessage;
    }

    private final BasketRepository basketRepository;
    private final SavedProductRepository savedProductRepository;

    public SendMessage basket(Update update) {
        String chat_id = update.getMessage().getChatId().toString();
        // user found
        Optional<BotUser> byChatId = botUserRepository.findByChatId(chat_id);
        BotUser user = byChatId.get();

        // send message
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat_id);

        Optional<Basket> byUser_id = basketRepository.findByUser_Id(user.getId());
        if (byUser_id.isPresent()) {
            Basket basket = byUser_id.get();
            List<SavedProduct> all_saved_products = savedProductRepository.findAllByBasket_Id(basket.getId());

            String text = "";

            if (all_saved_products.isEmpty()) {
                text = "your basket is empty!";
                sendMessage.setText(text);
            } else {
                Double all_money = 0.0d;
                for (SavedProduct savedProduct : all_saved_products) {
                    double money = savedProduct.getProduct().getPrice() * savedProduct.getCount();
                    all_money += money;
                    String s = savedProduct.getProduct().getName() + " * " + savedProduct.getCount() + " = " + money;
                    text = text + s + "\n";
                }
                text = text + "jami summa : " + all_money + " !";

                sendMessage.setText(text);


                List<KeyboardRow> rowList = new ArrayList<>();
                KeyboardRow row = new KeyboardRow();
                // button adding
                KeyboardButton submit = new KeyboardButton("submit");
                KeyboardButton back = new KeyboardButton("back");

                row.add(submit);
                row.add(back);

                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(rowList);
                replyKeyboardMarkup.setResizeKeyboard(true);
                replyKeyboardMarkup.setSelective(true);
                replyKeyboardMarkup.setOneTimeKeyboard(true);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);

            }
        }

        return sendMessage;
    }



    public SendMessage profile(Update update) {
        return null;
    }

    public SendMessage settings(Update update) {
        return null;
    }

    public SendMessage orders(Update update) {
        return null;
    }

    private final CategoryRepository categoryRepository;

    public SendMessage select_parent_categories(Update update) {

        Long chatId = update.getMessage().getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId + "");
        List<Category> all = categoryRepository.findAll();

        if (all.isEmpty()){
            sendMessage.setText("no categories found");
            return sendMessage;
        }

        List<KeyboardRow> rowList = new ArrayList<>();

        for (Category category : all) {
            KeyboardRow row = new KeyboardRow();
            KeyboardButton button = new KeyboardButton(category.getName());
            row.add(button);
            rowList.add(row);
        }


        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(rowList);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        Optional<BotUser> byChatId = botUserRepository.findByChatId(chatId.toString());
        BotUser user = byChatId.get();
        user.setState(States.SELECT_CATEGORY);
        botUserRepository.save(user);

        sendMessage.setText(" which category do you want ?");
        return sendMessage;
    }


    public SendMessage select_categories(Update update) {
        SendMessage sendMessage = new SendMessage();
        Long chatId = update.getMessage().getChatId();
        sendMessage.setChatId(chatId.toString());

        Optional<BotUser> byChatId = botUserRepository.findByChatId(chatId.toString());
        BotUser user = byChatId.get();
        // hali state o'zgaradi

        String category_name = update.getMessage().getText();

        Optional<Category> byName = categoryRepository.findByName(category_name);
        if (byName.isPresent()) {
            Category category = byName.get();


            List<Category> sub_categories = categoryRepository.findAllByParentCategory_Id(category.getId());

            if (sub_categories.isEmpty()) {
                sendMessage.setText("Bu category bo'yicha sub categorylar topilmadi");
                return sendMessage;
            }

            List<KeyboardRow> rowList = new ArrayList<>();

            for (Category sub_category : sub_categories) {
                KeyboardRow row = new KeyboardRow();
                KeyboardButton button = new KeyboardButton(sub_category.getName());
                row.add(button);
                rowList.add(row);
            }


            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(rowList);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setSelective(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);

            user.setState(States.SELECT_PRODUCT);
            sendMessage.setText("Click once for get more products");
        }

        return sendMessage;
    }

    public SendMessage select_product(Update update) {
        return null;
    }
}

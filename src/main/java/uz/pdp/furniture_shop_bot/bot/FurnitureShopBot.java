package uz.pdp.furniture_shop_bot.bot;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import uz.pdp.furniture_shop_bot.entity.Basket;
import uz.pdp.furniture_shop_bot.entity.BotUser;
import uz.pdp.furniture_shop_bot.entity.enums.Language;
import uz.pdp.furniture_shop_bot.repository.BasketRepository;
import uz.pdp.furniture_shop_bot.repository.BotUserRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FurnitureShopBot extends TelegramLongPollingBot {
    @Value("${telegram_bot_username}")
    String username;
    @Value("${telegram_bot_botToken}")
    String botToken;

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }


    /**
     * repositories
     *
     * @param update
     */

    private final BotUserRepository botUserRepository;
    private final BasketRepository basketRepository;
    private final BotService botService;

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            BotUser currentUser = null;

            if (message.hasText()) {
                String text = message.getText();

                if (text.equals("/start")) {
                    Optional<BotUser> byChatId = botUserRepository.findByChatId(chatId.toString());

                    SendMessage sendMessage1 = new SendMessage();
                    sendMessage1.setChatId(String.valueOf(chatId));

                    if (byChatId.isPresent()) {
                        BotUser user = byChatId.get();
                        user.setState(States.START);
                        botUserRepository.save(user);

                        Optional<Basket> byUser_id = basketRepository.findByUser_Id(user.getId());
                        if (byUser_id.isEmpty()) {
                            Basket basket = new Basket();
                            basket.setUser(user);
                            basketRepository.save(basket);
                        }

                    } else {

                        // yangi user oq'shilganda unga doim basket generate qilinadi!
                        BotUser botUser = new BotUser();
                        botUser.setChatId(chatId.toString());
                        botUserRepository.save(botUser);

                        Basket basket = new Basket();
                        basket.setUser(botUser);
                       basketRepository.save(basket);

                    }
                    sendMessage1.setText("select language");
                    sendMessage1 = botService.selectLanguage(update);
                    execute(sendMessage1);
                    return;
                }
            }
            BotUser user = botUserRepository.findByChatId(chatId + "").get();
            currentUser = user;

            String state = currentUser.getState();

            switch (state) {
                case States.START -> {
                    if (update.hasMessage()){
                        if (update.getMessage().equals("/start")) {
                            currentUser.setState(States.START);
                            botUserRepository.save(currentUser);
                            sendMessage = botService.selectLanguage(update);
                        }
                    }
                    currentUser.setState(States.LOGIN);
                    botUserRepository.save(currentUser);
                    sendMessage = botService.setLanguage(update);
                }
                case States.LOGIN -> {
                    sendMessage = botService.login(update);
                }
                case States.SELECT_FROM_MENU -> {
                    switch (update.getMessage().getText()) {
                        case Buttons.BASKET -> sendMessage = botService.basket(update);
                        case Buttons.PARENT_CATEGORIES -> sendMessage = botService.select_parent_categories(update);
                        case Buttons.PROFILE -> sendMessage = botService.profile(update);
                        case Buttons.SETTINGS -> sendMessage = botService.settings(update);
                        case Buttons.ORDERS -> sendMessage = botService.orders(update);
                        default -> sendMessage = botService.defaultMessage(update);
                    }
                }
                case States.SELECT_CATEGORY  -> {
                    sendMessage = botService.select_categories(update);
                }

                case States.SELECT_PRODUCT  -> {
                    sendMessage = botService.select_product(update);
                }


                default -> {
                    throw new IllegalStateException("Unexpected value: " + state);
                }
            }

            execute(sendMessage);
        }


    }




}


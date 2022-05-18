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
import uz.pdp.furniture_shop_bot.entity.BotUser;
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
    private final BotService botService;

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();


            if (message.hasText()) {
                String text = message.getText();
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(String.valueOf(chatId));

                if (text.equals("/start")) {
                    Optional<BotUser> byChatId = botUserRepository.findByChatId(String.valueOf(chatId));

                    if (byChatId.isPresent()) {
                        BotUser currentUser = byChatId.get();
                        currentUser.setState(States.START);
                        botUserRepository.save(currentUser);
                    } else {
                        BotUser newUser = new BotUser();
                        newUser.setChatId(String.valueOf(chatId));
                        botUserRepository.save(newUser);
                    }

                     sendMessage = botService.selectLanguage(update);

                } else {

                    sendMessage = botService.defaultMessage(update);

                }

                execute(sendMessage);

            }
        }
    }
}

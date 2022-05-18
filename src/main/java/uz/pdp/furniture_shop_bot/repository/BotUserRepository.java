package uz.pdp.furniture_shop_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.furniture_shop_bot.entity.BotUser;

import java.util.Optional;

public interface BotUserRepository extends JpaRepository<BotUser, Long> {

    Optional<BotUser> findByChatId(String chatId);
}
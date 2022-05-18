package uz.pdp.furniture_shop_bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.furniture_shop_bot.entity.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {
}
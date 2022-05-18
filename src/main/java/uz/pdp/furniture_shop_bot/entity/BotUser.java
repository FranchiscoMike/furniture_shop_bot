package uz.pdp.furniture_shop_bot.entity;

import lombok.*;
import uz.pdp.furniture_shop_bot.bot.States;
import uz.pdp.furniture_shop_bot.entity.enums.Language;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class BotUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatId;

    private String state = States.START;

    private String email;

    private String fullName;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Language language = Language.UZBEK;

    /**
     * location uchun
     */
    private float lat;

    private float lon;

    @OneToOne
    private Attachment passportPhoto;

    @OneToOne
    private Attachment photoWithFace;

    /**
     * for confirmation
     */

    private String companyName;

    private String position;

    private Integer experience = 0;

    private Double salary = 0.0d;

    private String cardNumber;

    private String extraNumber;

}

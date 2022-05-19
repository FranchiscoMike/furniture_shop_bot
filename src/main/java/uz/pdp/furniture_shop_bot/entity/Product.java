package uz.pdp.furniture_shop_bot.entity;

import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Category category;

    private String name;

    private Double price;

    @OneToOne
    private Attachment photo;

    @Column(columnDefinition = "TEXT")
    private String description; // default 255 varchar

    private String descriptionOfPrices;

}


//Yumshoq Mebelni
//rasmi, mebel haqida
//malumot va rasrochka
//narxlari description
//sifatida yoziladi
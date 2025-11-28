package utils;

import com.github.javafaker.Faker;
import models.Courier;
import models.Order;
import java.util.Arrays;
import java.util.List;

public class TestDataGenerator {
    private static final Faker faker = new Faker();

    public static String generateUniqueLogin() {
        return "courier_" + System.currentTimeMillis() + "_" + faker.number().randomNumber(4, true);
    }

    public static String generateUniquePassword() {
        return "pass_" + System.currentTimeMillis() + "_" + faker.internet().password(8, 10);
    }

    public static String generateFirstName() { return faker.name().firstName(); }
    public static String generateLastName() { return faker.name().lastName(); }
    public static String generateAddress() { return faker.address().fullAddress(); }
    public static String generatePhone() { return faker.phoneNumber().phoneNumber(); }
    public static String generateComment() { return faker.lorem().sentence(); }

    public static Courier generateUniqueCourier() {
        return new Courier(generateUniqueLogin(), generateUniquePassword(), generateFirstName());
    }

    public static Order generateOrderWithColors(List<String> colors) {
        return new Order(
                generateFirstName(),
                generateLastName(),
                generateAddress(),
                String.valueOf(faker.number().numberBetween(1, 10)),
                generatePhone(),
                faker.number().numberBetween(1, 7),
                "2024-06-06",
                generateComment(),
                colors
        );
    }

    public static Order generateDefaultOrder() {
        return generateOrderWithColors(null);
    }

    public static Order generateOrderWithAllFields() {
        return new Order(
                "Naruto",
                "Uchiha",
                "Konoha, 142 apt.",
                "4",
                "+7 800 355 35 35",
                5,
                "2024-06-06",
                "Saske, come back to Konoha",
                Arrays.asList("BLACK")
        );
    }

    public static String generateNonExistentCourierId() {
        return "99999" + faker.number().numberBetween(1000, 9999);
    }

    public static String generateNonExistentOrderId() {
        return "88888" + faker.number().numberBetween(1000, 9999);
    }

    public static Integer generateNonExistentTrackNumber() {
        return faker.number().numberBetween(1000000, 9999999);
    }

    public static String generateStationSearchQuery() {
        List<String> popularStations = Arrays.asList("Сокол", "Парк", "Киев", "Курск", "Белорус");
        return popularStations.get(faker.number().numberBetween(0, popularStations.size() - 1));
    }

    public static String generateNonExistentStationQuery() {
        return "NonexistentStation" + faker.number().randomNumber(6, true);
    }

    public static List<String> generateStationNumbers() {
        return Arrays.asList(
                String.valueOf(faker.number().numberBetween(1, 10)),
                String.valueOf(faker.number().numberBetween(11, 20))
        );
    }

    public static Integer generateValidLimit() { return faker.number().numberBetween(1, 30); }
    public static Integer generatePageNumber() { return faker.number().numberBetween(0, 5); }
}
package order;

import helpers.OrderTestHelper;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.Order;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import utils.TestDataGenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class OrderCreationTest {
    private final OrderTestHelper orderHelper = new OrderTestHelper();

    private final List<String> colors;
    private final String testDescription;

    public OrderCreationTest(List<String> colors, String testDescription) {
        this.colors = colors;
        this.testDescription = testDescription;
    }

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {Arrays.asList("BLACK"), "Создание заказа с цветом BLACK"},
                {Arrays.asList("GREY"), "Создание заказа с цветом GREY"},
                {Arrays.asList("BLACK", "GREY"), "Создание заказа с обоими цветами"},
                {null, "Создание заказа без указания цвета"}
        });
    }

    @After
    public void tearDown() {
        orderHelper.cancelAllTestOrders();
    }

    @Test
    @DisplayName("Создание заказа с разными цветами")
    @Description("Проверка создания заказа с разными вариантами цветов")
    public void createOrder_WithDifferentColors_ShouldReturnValidTrack() {
        Order order = orderHelper.createTestOrder(colors);
        Integer track = orderHelper.getCurrentOrderTrack();

        assertThat("Track number should be positive for " + testDescription,
                track, greaterThan(0));
    }

    @Test
    @DisplayName("Создание заказа со всеми обязательными полями")
    @Description("Проверка создания заказа со всеми обязательными полями")
    public void createOrder_WithAllRequiredFields_ShouldReturn201AndValidTrack() {
        Order order = orderHelper.createDefaultTestOrder();
        Integer track = orderHelper.getCurrentOrderTrack();

        assertThat("Track number should be positive integer", track, greaterThan(0));
    }

    @Test
    @DisplayName("Создание заказа с примерными данными")
    @Description("Проверка создания заказа с примерными тестовыми данными")
    public void createOrder_WithExampleData_ShouldReturnValidTrack() {
        Order order = TestDataGenerator.generateOrderWithAllFields();

        Response response = orderHelper.getOrderClient().createOrder(order);
        orderHelper.getOrderClient().validateSuccessfulCreation(response);

        int trackNumber = response.jsonPath().getInt("track");
        assertThat("Track number should be positive", trackNumber, greaterThan(0));

        // Регистрируем заказ для cleanup
        orderHelper.registerOrderForCleanup(trackNumber, null);
    }

    @Test
    @DisplayName("Получение заказа по track номеру")
    @Description("Проверка что созданный заказ может быть получен по track номеру")
    public void createOrder_ThenGetByTrack_ShouldReturnMatchingData() {
        Order originalOrder = orderHelper.createDefaultTestOrder();
        Integer track = orderHelper.getCurrentOrderTrack();

        Response getResponse = orderHelper.getOrderClient().getOrderByTrack(track);
        orderHelper.getOrderClient().validateSuccessfulGetOrderByTrack(getResponse, track);

        String responseFirstName = getResponse.jsonPath().getString("order.firstName");
        assertThat("First name should match created order",
                responseFirstName, equalTo(originalOrder.getFirstName()));
    }

    @Test
    @DisplayName("Проверка уникальности track номеров")
    @Description("Проверка что track номера уникальны для разных заказов")
    public void createOrder_TrackNumbersShouldBeUnique() {
        // Создаем два заказа подряд
        orderHelper.createDefaultTestOrder();
        Integer firstTrack = orderHelper.getCurrentOrderTrack();

        orderHelper.createDefaultTestOrder();
        Integer secondTrack = orderHelper.getCurrentOrderTrack();

        // Проверяем что треки разные
        assertThat("Track numbers should be different",
                firstTrack, not(equalTo(secondTrack)));
    }

    @Test
    @DisplayName("Создание нескольких заказов подряд")
    @Description("Проверка создания нескольких заказов подряд")
    public void createMultipleOrders_ShouldAllBeManaged() {
        orderHelper.createTestOrder(Arrays.asList("BLACK"));
        orderHelper.createTestOrder(Arrays.asList("GREY"));
        orderHelper.createTestOrder(Arrays.asList("BLACK", "GREY"));

        Integer track1 = orderHelper.getCurrentOrderTrack();
        String orderId1 = orderHelper.getCurrentOrderId();

        assertThat("Should create multiple orders successfully",
                orderHelper.getCreatedOrderTracks().size(), equalTo(3));
        assertThat("First track should be valid", track1, greaterThan(0));
        assertThat("First order ID should not be null", orderId1, notNullValue());
    }
}
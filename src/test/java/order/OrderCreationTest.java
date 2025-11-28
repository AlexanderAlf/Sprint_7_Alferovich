package order;

import helpers.OrderTestHelper;
import io.restassured.response.Response;
import models.Order;
import org.junit.After;
import org.junit.Test;
import utils.TestDataGenerator;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class OrderCreationTest {
    private final OrderTestHelper orderHelper = new OrderTestHelper();

    @After
    public void tearDown() {
        orderHelper.cancelAllTestOrders();
    }

    @Test
    public void createOrder_WithAllRequiredFields_ShouldReturn201AndValidTrack() {
        Order order = orderHelper.createDefaultTestOrder();
        Integer track = orderHelper.getCreatedOrderTrack(); // Используем исправленный метод

        assertThat("Track number should be positive integer", track, greaterThan(0));
    }

    @Test
    public void createOrder_WithBlackColor_ShouldReturnValidTrack() {
        createAndValidateOrderWithColors(Collections.singletonList("BLACK"));
    }

    @Test
    public void createOrder_WithGreyColor_ShouldReturnValidTrack() {
        createAndValidateOrderWithColors(Collections.singletonList("GREY"));
    }

    @Test
    public void createOrder_WithBothColors_ShouldReturnValidTrack() {
        createAndValidateOrderWithColors(Arrays.asList("BLACK", "GREY"));
    }

    @Test
    public void createOrder_WithoutColor_ShouldReturnValidTrack() {
        createAndValidateOrderWithColors(null);
    }

    @Test
    public void createOrder_WithExampleData_ShouldReturnValidTrack() {
        Order order = TestDataGenerator.generateOrderWithAllFields();

        Response response = orderHelper.getOrderClient().createOrder(order);

        orderHelper.getOrderClient().validateSuccessfulCreation(response);

        int trackNumber = response.jsonPath().getInt("track");
        assertThat("Track number should be positive", trackNumber, greaterThan(0));
    }

    @Test
    public void createOrder_ThenGetByTrack_ShouldReturnMatchingData() {
        Order originalOrder = orderHelper.createDefaultTestOrder();
        Integer track = orderHelper.getCreatedOrderTrack(); // Используем исправленный метод

        Response getResponse = orderHelper.getOrderClient().getOrderByTrack(track);

        orderHelper.getOrderClient().validateSuccessfulGetOrderByTrack(getResponse, track);

        String responseFirstName = getResponse.jsonPath().getString("order.firstName");
        assertThat("First name should match created order",
                responseFirstName, equalTo(originalOrder.getFirstName()));
    }

    @Test
    public void createOrder_TrackNumbersShouldBeUnique() {
        OrderTestHelper helper1 = new OrderTestHelper();
        OrderTestHelper helper2 = new OrderTestHelper();

        try {
            helper1.createDefaultTestOrder();
            helper2.createDefaultTestOrder();

            Integer track1 = helper1.getCurrentOrderTrack();
            Integer track2 = helper2.getCurrentOrderTrack();

            assertThat("Track numbers should be unique across orders",
                    track1, not(equalTo(track2)));
        } finally {
            helper1.cancelAllTestOrders();
            helper2.cancelAllTestOrders();
        }
    }

    @Test
    public void createMultipleOrders_ShouldAllBeManaged() {
        Order order1 = orderHelper.createTestOrder(Collections.singletonList("BLACK"));
        Order order2 = orderHelper.createTestOrder(Collections.singletonList("GREY"));
        Order order3 = orderHelper.createTestOrder(Arrays.asList("BLACK", "GREY"));

        Integer track1 = orderHelper.getCurrentOrderTrack();
        String orderId1 = orderHelper.getCurrentOrderId();

        assertThat("Should create multiple orders successfully",
                orderHelper.getCreatedOrderTracks().size(), equalTo(3));
        assertThat("First track should be valid", track1, greaterThan(0));
        assertThat("First order ID should not be null", orderId1, notNullValue());
    }

    private void createAndValidateOrderWithColors(java.util.List<String> colors) {
        Order order = orderHelper.createTestOrder(colors);
        Integer track = orderHelper.getCreatedOrderTrack(); // Используем исправленный метод

        assertThat("Track number should be positive for order with colors: " + colors,
                track, greaterThan(0));
    }
}
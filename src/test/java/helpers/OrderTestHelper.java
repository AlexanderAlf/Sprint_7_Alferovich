package helpers;

import clients.OrderClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Order;
import utils.TestDataGenerator;

import java.util.ArrayList;
import java.util.List;

public class OrderTestHelper {
    private final OrderClient orderClient = new OrderClient();
    private final List<Integer> createdOrderTracks = new ArrayList<>();
    private final List<String> createdOrderIds = new ArrayList<>();

    @Step("Create test order with colors: {colors}")
    public Order createTestOrder(List<String> colors) {
        Order order = TestDataGenerator.generateOrderWithColors(colors);
        Response response = orderClient.createOrder(order);
        orderClient.validateSuccessfulCreation(response);

        Integer track = response.jsonPath().getInt("track");
        if (track == null || track <= 0) {
            throw new AssertionError("Order track should be a positive number");
        }

        createdOrderTracks.add(track);
        return order;
    }

    @Step("Create default test order")
    public Order createDefaultTestOrder() {
        return createTestOrder(null);
    }

    @Step("Get created order track")
    public Integer getCreatedOrderTrack() {
        return getCurrentOrderTrack();
    }

    @Step("Get order ID from track with validation")
    public String getCurrentOrderId() {
        if (createdOrderTracks.isEmpty()) {
            throw new IllegalStateException("No order track available - order may not have been created");
        }

        Integer currentTrack = createdOrderTracks.get(createdOrderTracks.size() - 1);
        Response response = orderClient.getOrderByTrack(currentTrack);
        orderClient.validateSuccessfulGetOrderByTrack(response, currentTrack);

        String orderId = response.jsonPath().getString("order.id");
        if (orderId == null) {
            throw new IllegalStateException("Order ID is not available in the response");
        }

        if (!createdOrderIds.contains(orderId)) {
            createdOrderIds.add(orderId);
        }

        return orderId;
    }

    @Step("Cancel all test orders")
    public void cancelAllTestOrders() {
        for (Integer track : createdOrderTracks) {
            if (track != null) {
                try {
                    Response response = orderClient.cancelOrder(String.valueOf(track));
                    if (response.statusCode() != 200) {
                        System.out.println("Order cancellation returned status: " + response.statusCode());
                    }
                } catch (Exception e) {
                    System.out.println("Order cancellation failed for track " + track + ": " + e.getMessage());
                }
            }
        }
        createdOrderTracks.clear();
        createdOrderIds.clear();
    }

    @Step("Get current order track with validation")
    public Integer getCurrentOrderTrack() {
        if (createdOrderTracks.isEmpty()) {
            throw new IllegalStateException("No order track available - order may not have been created");
        }
        return createdOrderTracks.get(createdOrderTracks.size() - 1);
    }

    @Step("Register existing order for cleanup")
    public void registerOrderForCleanup(Integer track, String orderId) {
        if (track != null) {
            createdOrderTracks.add(track);
        }
        if (orderId != null && !orderId.isEmpty() && !createdOrderIds.contains(orderId)) {
            createdOrderIds.add(orderId);
        }
    }

    public OrderClient getOrderClient() {
        return orderClient;
    }

    public List<Integer> getCreatedOrderTracks() {
        return new ArrayList<>(createdOrderTracks);
    }
}
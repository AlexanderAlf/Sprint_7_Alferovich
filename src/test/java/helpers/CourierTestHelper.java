package helpers;

import clients.CourierClient;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import models.Courier;
import utils.TestDataGenerator;

import java.util.ArrayList;
import java.util.List;

public class CourierTestHelper {
    private final CourierClient courierClient = new CourierClient();
    private final List<String> createdCourierIds = new ArrayList<>();
    private Courier currentTestCourier;

    @Step("Setup test courier with cleanup tracking")
    public Courier setupTestCourier() {
        currentTestCourier = TestDataGenerator.generateUniqueCourier();
        Response createResponse = courierClient.createCourier(currentTestCourier);
        courierClient.validateSuccessfulCreation(createResponse);

        String courierId = courierClient.loginAndGetCourierId(currentTestCourier);
        createdCourierIds.add(courierId);
        return currentTestCourier;
    }

    @Step("Create additional test courier")
    public Courier createAdditionalCourier() {
        Courier additionalCourier = TestDataGenerator.generateUniqueCourier();
        Response response = courierClient.createCourier(additionalCourier);
        courierClient.validateSuccessfulCreation(response);
        String courierId = courierClient.loginAndGetCourierId(additionalCourier);
        createdCourierIds.add(courierId);
        return additionalCourier;
    }

    @Step("Cleanup all test couriers")
    public void cleanupAllTestCouriers() {
        for (String courierId : createdCourierIds) {
            try {
                Response deleteResponse = courierClient.deleteCourier(courierId);
                if (deleteResponse.statusCode() == 200) {
                    System.out.println("Successfully deleted courier: " + courierId);
                } else {
                    System.out.println("Failed to delete courier " + courierId + ": " + deleteResponse.statusCode());
                }
            } catch (Exception e) {
                System.out.println("Exception deleting courier " + courierId + ": " + e.getMessage());
            }
        }
        createdCourierIds.clear();
        currentTestCourier = null;
    }

    @Step("Cleanup test courier")
    public void cleanupTestCourier() {
        cleanupAllTestCouriers();
    }

    @Step("Get created courier ID with validation")
    public String getCreatedCourierId() {
        if (createdCourierIds.isEmpty()) {
            throw new IllegalStateException("No courier ID available - courier may not have been created");
        }
        return createdCourierIds.get(createdCourierIds.size() - 1);
    }

    @Step("Get current test courier with validation")
    public Courier getCurrentTestCourier() {
        if (currentTestCourier == null) {
            throw new IllegalStateException("Test courier is not available - courier may not have been created");
        }
        return currentTestCourier;
    }

    @Step("Register existing courier for cleanup")
    public void registerCourierForCleanup(String courierId) {
        if (courierId != null && !courierId.isEmpty()) {
            createdCourierIds.add(courierId);
        }
    }
}
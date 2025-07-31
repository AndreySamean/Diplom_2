package client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import model.Ingredients;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static model.constants.SpecificationTemplates.REQUEST_SPECIFICATION_TEMPLATE;
import static model.constants.SpecificationTemplates.RESPONSE_SPECIFICATION_TEMPLATE;
import static model.constants.UrlPath.*;

public class SBOrderClient {

    @Step("Создать заказ")
    public ValidatableResponse createOrder(Ingredients ingredients,  String token){
        ValidatableResponse response;
        RequestSpecification request = given().spec(REQUEST_SPECIFICATION_TEMPLATE);
        if (token != null && !token.isEmpty()) {
            request.header("Authorization", token);
        }
        if (ingredients != null && !ingredients.getIngredients().isEmpty()) {
            request.body(ingredients);
        }
        response = request
                .post(CREATE_ORDER_ENDPOINT)
                .then()
                .spec(RESPONSE_SPECIFICATION_TEMPLATE);
        return response;
    }

    @Step("Создать список с хешами ингредиентов")
    public Ingredients createIngredientsByIndexes(List<String> ingredientsData, int [] indexes) {
        List<String> selectedIds = new ArrayList<>();
        for (int index : indexes) {
            if (index >= 0 && index < ingredientsData.size()) {
                selectedIds.add(ingredientsData.get(index));
            } else {
                throw new IllegalArgumentException("Индекс " + index + " выходит за границы списка");
            }
        }
        return new Ingredients(selectedIds);
    }

    @Step("Получить заказы пользователя")
    public ValidatableResponse getUsersOrders(String token){
        ValidatableResponse response;
        RequestSpecification request = given()
                .spec(REQUEST_SPECIFICATION_TEMPLATE);
        if (token != null && !token.isEmpty()){
            request.header("Authorization", token);
        }
        response = request.get(GET_USERS_ORDERS_ENDPOINT)
                .then().spec(RESPONSE_SPECIFICATION_TEMPLATE);
        return response;
    }

    public List<String> getIngredients() {
        ValidatableResponse response =
                given()
                        .log()
                        .all()
                        .baseUri(BASE_URL)
                        .get(GET_INGREDIENTS_ENDPOINT)
                        .then()
                        .log()
                        .all();

        return response.extract().jsonPath().getList("data._id");
    }
}

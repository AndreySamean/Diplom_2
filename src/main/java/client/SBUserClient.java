package client;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import model.User;

import static io.restassured.RestAssured.given;
import static model.constants.SpecificationTemplates.REQUEST_SPECIFICATION_TEMPLATE;
import static model.constants.SpecificationTemplates.RESPONSE_SPECIFICATION_TEMPLATE;
import static model.constants.UrlPath.*;

public class SBUserClient {

    @Step("Создать пользователя")
    public ValidatableResponse createUser(User user){
        return given()
                .spec(REQUEST_SPECIFICATION_TEMPLATE)
                .body(user)
                .post(CREATE_USER_ENDPOINT)
                .then()
                .spec(RESPONSE_SPECIFICATION_TEMPLATE);
    }

    @Step("Удалить пользователя")
    public void deleteUser(String bearerToken){
        given().baseUri(BASE_URL).header("Authorization", bearerToken)
                .delete("/api/auth/user").then().log().all();
    }

    @Step("Авторизация пользователя")
    public ValidatableResponse loginUser(User user, String bearerToken){
        return given()
                .spec(REQUEST_SPECIFICATION_TEMPLATE)
                .header("Authorization", bearerToken)
                .and().body(user)
                .post(LOGIN_USER_ENDPOINT)
                .then()
                .spec(RESPONSE_SPECIFICATION_TEMPLATE);
    }

    @Step("Изменить данные пользователя")
    public ValidatableResponse changeUserCredentials(User user, String token){
        ValidatableResponse response;
        if (token != null && !token.isEmpty()){
            response = given().spec(REQUEST_SPECIFICATION_TEMPLATE)
                    .header("Authorization", token)
                    .and().body(user)
                    .patch(PATCH_USER_ENDPOINT)
                    .then()
                    .spec(RESPONSE_SPECIFICATION_TEMPLATE);;
        } else {
            response = given().spec(REQUEST_SPECIFICATION_TEMPLATE)
                    .and().body(user)
                    .patch(PATCH_USER_ENDPOINT)
                    .then()
                    .spec(RESPONSE_SPECIFICATION_TEMPLATE);}
        return response;
    }

    public String getBearerToken(ValidatableResponse response){
        return response.extract().jsonPath().getString("accessToken");
    }
}

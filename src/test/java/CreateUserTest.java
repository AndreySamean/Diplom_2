import client.SBUserClient;
import io.restassured.response.ValidatableResponse;
import model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static model.constants.UserCredentials.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Тесты на создание пользователя")
public class CreateUserTest {

    private static SBUserClient client;
    private static String bearerToken;

    @BeforeAll
    public static void setUp(){
        client =  new SBUserClient();
    }

    @AfterEach
    public void cleanUp(){
       try {
           client.deleteUser(bearerToken);
       } catch (IllegalArgumentException e) {
           System.out.println("Такого пользователя не существует");
       }
    }

    @Test
    @DisplayName("Создать уникального пользователя - успех")
    public void createUser_success(){
        User user = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);

        ValidatableResponse response = client.createUser(user);
        bearerToken = client.getBearerToken(response);

        response.assertThat().statusCode(200)
                .and().body("success", equalTo(true))
                .and().body("accessToken", notNullValue())
                .and().body("refreshToken", notNullValue())
                .and().body("user", hasKey("email"))
                .and().body("user.email", equalTo(USER_EMAIL))
                .and().body("user", hasKey("name"))
                .and().body("user.name", equalTo(USER_NAME));
    }

    @Test
    @DisplayName("Создать пользователя, который уже зарегистрирован - ошибка")
    public void createTwoIdenticalUsers_expectError(){
        User user = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);

        ValidatableResponse firstUser =  client.createUser(user);
        bearerToken = client.getBearerToken(firstUser);
        ValidatableResponse secondUser = client.createUser(user);

        secondUser.assertThat().statusCode(403)
                .and().body("success", equalTo(false))
                .and().body("message", equalTo("User already exists"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataProvider")
    @DisplayName("Создать пользователя и не заполнить одно из обязательных полей - ошибка")
    public void createUser_OneFieldIsEmpty_expectError(String description, String email, String password, String name){
        User user = new User(email, password, name);

        ValidatableResponse response = client.createUser(user);
        bearerToken = client.getBearerToken(response);

        response.assertThat().statusCode(403)
                .and().body("success", equalTo(false))
                .and().body("message", equalTo("Email, password and name are required fields"));
    }

    static Stream<Arguments> dataProvider(){
        return Stream.of(
                Arguments.of("Без email", USER_EMPTY_FIELD, USER_PASSWORD, USER_NAME),
                Arguments.of("Без password", USER_EMAIL, USER_EMPTY_FIELD, USER_NAME),
                Arguments.of("Без name", USER_EMAIL, USER_PASSWORD, USER_EMPTY_FIELD)
        );
    }
}

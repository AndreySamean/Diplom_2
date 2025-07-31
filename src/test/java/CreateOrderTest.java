import client.SBOrderClient;
import client.SBUserClient;
import io.restassured.response.ValidatableResponse;
import model.Ingredients;
import model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static model.constants.UserCredentials.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Тесты на создание заказа")
public class CreateOrderTest {

    private  SBOrderClient client;
    private  SBUserClient userClient;
    private static List<String> data;
    private Ingredients ingredients;
    private static String bearerToken;

    @BeforeEach
    public void setUp() {
        client = new SBOrderClient();
        data = client.getIngredients();
        userClient = new SBUserClient();
        User user = new User(USER_EMAIL, USER_PASSWORD, USER_NAME);
        bearerToken = userClient.getBearerToken(userClient.createUser(user));
    }


    @AfterEach
    public void cleanUp(){
        userClient.deleteUser(bearerToken);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataProviderIngredients")
    @DisplayName("Создание заказа с авторизацией - успех")
    public void createOrder_withAuth_success(String description, int[] indexes){
        ingredients = client.createIngredientsByIndexes(data, indexes);
        ValidatableResponse response = client.createOrder(ingredients, bearerToken);

        response.assertThat().statusCode(200)
                .and().body("success", equalTo(true))
                .and().body("name", notNullValue())
                .and().body("order", notNullValue())
                .and().body("order.number", instanceOf(Integer.class));
        for (String expectedId : ingredients.getIngredients()) {
            response.assertThat()
                    .body("order.ingredients._id", hasItem(expectedId));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataProviderIngredients")
    @DisplayName("Создание заказа без авторизации - успех")
    public void createOrder_withoutAuth_success(String description, int[] indexes){
        ingredients = client.createIngredientsByIndexes(data, indexes);
        ValidatableResponse response = client.createOrder(ingredients, null);

        response.assertThat().statusCode(200)
                .and().body("success", equalTo(true))
                .and().body("name", not(empty()))
                .and().body("order", hasKey("number"))
                .and().body("order.number", instanceOf(Integer.class));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataProviderToken")
    @DisplayName("Создание заказа без ингредиентов - ошибка")
    public void createOrder_withoutIngredients_expectError(String description,
                                                                         String token){
        ValidatableResponse response = client.createOrder(ingredients, token);

        response.assertThat().statusCode(400)
                .and().body("success", equalTo(false))
                .and().body("message", equalTo("Ingredient ids must be provided"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataProviderToken")
    @DisplayName("Создание заказа с неверным хешем ингредиентов - ошибка")
    public void createOrder_invalidIngredientId_expectError(String description,
                                                            String token){
        ingredients = client.createIngredientsByIndexes(data, new int[]{0});
        String originalId = data.get(0);
        String modifiedId = originalId + "1225d";

        List<String> modifiedIngredients = List.of(modifiedId);;

        Ingredients invalidIds = new Ingredients(modifiedIngredients);

        ValidatableResponse response = client.createOrder(invalidIds, token);

        response.assertThat().statusCode(500)
                .and().body(containsString("Internal Server Error"));
    }

    @Test
    @DisplayName("Получение заказов конкретного пользователя с авторизацией")
    public void getOrders_withAuth_success(){
        ValidatableResponse response = client.getUsersOrders(bearerToken);

        response.assertThat().statusCode(200)
                .and().body("success", equalTo(true))
                .and().body("orders", notNullValue())
                .and().body("total", instanceOf(Integer.class))
                .and().body("totalToday", instanceOf(Integer.class));
    }

    @Test
    @DisplayName("Получение заказов конкретного пользователя без авторизации")
    public void getOrders_withoutAuth_expectError(){
        ValidatableResponse response = client.getUsersOrders(null);

        response.assertThat().statusCode(401)
                .and().body("success", equalTo(false))
                .and().body("message", equalTo("You should be authorised"));
    }

    static Stream<Arguments> dataProviderIngredients(){
        List<String> ingredients = new SBOrderClient().getIngredients();
        int size = ingredients.size() - 1;
        Random random = new Random();

        int[] oneIngredient = { random.nextInt(size) };
        int[] twoIngredients = { random.nextInt(size), random.nextInt(size) };
        int[] threeIngredients = {
                random.nextInt(size),
                random.nextInt(size),
                random.nextInt(size)
        };
        return Stream.of(
                Arguments.of("Заказ из одного ингредиента", oneIngredient),
                Arguments.of("Заказ из двух ингредиентов", twoIngredients),
                Arguments.of("Заказ из трёх ингредиентов", threeIngredients)
        );
    }

    static Stream<Arguments> dataProviderToken(){
        return Stream.of(
                Arguments.of("С авторизацией", bearerToken),
                Arguments.of("Без авторизации", null)
        );
    }
}

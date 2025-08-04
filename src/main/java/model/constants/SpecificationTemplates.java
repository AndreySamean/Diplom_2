package model.constants;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static model.constants.UrlPath.BASE_URL;

public class SpecificationTemplates {

    public static final RequestSpecification REQUEST_SPECIFICATION_TEMPLATE =
            new RequestSpecBuilder()
                    .setBaseUri(BASE_URL)
                    .addHeader("Content-Type", "application/json")
                    .log(LogDetail.ALL)
                    .addFilter(new AllureRestAssured())
                    .build();
    public static final ResponseSpecification RESPONSE_SPECIFICATION_TEMPLATE =
            new ResponseSpecBuilder()
                    .log(LogDetail.ALL)
                    .build();
}

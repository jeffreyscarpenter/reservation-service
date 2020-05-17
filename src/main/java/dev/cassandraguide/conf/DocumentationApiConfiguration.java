/*
 * Copyright (C) 2017-2020 Jeff Carpenter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.cassandraguide.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.cassandraguide.ReservationServiceApp;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Documentation of the API
 *
 * @author Cedrick Lunven
 */
@Configuration
@EnableSwagger2
public class DocumentationApiConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("ReservationService")
            .select()
            .apis(RequestHandlerSelectors.basePackage("dev.cassandraguide.controller"))
            .paths(PathSelectors.regex("/api/v1.*"))
            .build()
            .apiInfo(apiInfo())
            .useDefaultResponseMessages(false);
    }
    
    /**
     * Initialization of documentation
     *
     * @return static infos
     */
    private ApiInfo apiInfo() {
        ApiInfoBuilder builder = new ApiInfoBuilder();
        builder.title("Reservation Service RESTful API");
        builder.description("Provides the ability to create, read, update and delete reservations.");
        builder.version(ReservationServiceApp.class.getPackage().getImplementationVersion());
        builder.license("Apache License, Version 2.0");
        builder.licenseUrl("http://www.apache.org/licenses/LICENSE-2.0");
        return builder.build();
    }
}

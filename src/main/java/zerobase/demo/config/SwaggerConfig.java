package zerobase.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo())
            .select()
            .apis(RequestHandlerSelectors.basePackage("zerobase.demo")) // controller 패키지 경로
            .paths(PathSelectors.ant("/read/**"))
            .build();
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("날씨 일기 프로젝트") // API 문서 제목
            .description("날씨와 일기를 같이 저장하는 프로그램") // API 문서 설명
            .version("2.0") // API 버전
            .build();
    }

}

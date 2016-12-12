package com.feiyangedu.springcloud.petstore.common.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.feiyangedu.springcloud.petstore.common.filter.UserContextFilter;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class CustomWebConfig {

	@Bean
	public Docket createSwaggerDocket() {
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select()
				.paths(PathSelectors.regex("^/api/.*$")).build();
	}

	ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("Petstore")
				.contact(new Contact("Liao Xuefeng", "http://www.liaoxuefeng.com", "askxuefeng@gmail.com"))
				.license("Apache 2.0").licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
				.description("Petstore Sample Application on Spring Cloud").build();
	}

	@Bean
	public FilterRegistrationBean userContextFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new UserContextFilter());
		registration.addUrlPatterns("/*");
		registration.setName("userContextFilter");
		registration.setOrder(1);
		return registration;
	}

	@Bean
	public ObjectMapper objectMapper() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.registerModule(new JavaTimeModule());
		return mapper;
	}

	@Autowired
	ObjectMapper objectMapper;

	@Bean
	public WebMvcConfigurer webMvcConfigurer() {
		return new WebMvcConfigurerAdapter() {
			/**
			 * Spring默认把静态资源文件/static/abc.js映射到/abc.js，不利于配置反向代理。配置为保留/static/前缀
			 */
			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				super.addResourceHandlers(registry);
				registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
			}

			/**
			 * Json默认序列化设置，增加Java8 Time支持
			 */
			@Override
			public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
				final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
				converter.setObjectMapper(objectMapper);
				converters.add(converter);
				super.configureMessageConverters(converters);
			}
		};
	}
}
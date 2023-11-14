package com.itgrail.grail.swagger;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author xujin
 * @date 2019/6/3
 */
@Profile("!pro")
@Configuration
@EnableSwagger2
@EnableConfigurationProperties(SwaggerProperties.class)
@ConditionalOnProperty(prefix = "grail.swagger", name = "enabled", matchIfMissing = true)
public class SwaggerConfig implements ApplicationContextAware {

    private static Logger log = LoggerFactory.getLogger(SwaggerConfig.class);

    private static final String SEPARATOR = ",";
    private static final Pattern PACKAGES_PATTERN = Pattern.compile("^([a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*\\.[a-zA-Z]+[0-9a-zA-Z_]*)+(\\,([a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*\\.[a-zA-Z]+[0-9a-zA-Z_]*)+){0,100}$");

    private ApplicationContext applicationContext;

    @Autowired
    private SwaggerProperties swaggerProperties;


    @Bean
    public Docket autoEnableSwagger() {
        // api信息
        ApiInfo apiInfo = new ApiInfoBuilder().title(swaggerProperties.getTitle())
                .description(swaggerProperties.getDescription())
                .version(swaggerProperties.getVersion())
                .contact(swaggerProperties.contact())
                .build();


        // 忽略路径
        String excludePaths = swaggerProperties.getExcludePaths();
        List<Predicate<String>> exclude = new ArrayList<>();
        if (!StringUtils.isEmpty(excludePaths)) {
            exclude = Arrays.stream(excludePaths.split(SEPARATOR))
                    .map(PathSelectors::ant)
                    .collect(Collectors.toList());
        }
        // 扫描路径，未配置则设置默认值
        ArrayList<String> basePackage = new ArrayList<>();
        String swaggerBasePackages = swaggerProperties.getBasePackages();
        if (StringUtils.isEmpty(swaggerBasePackages)) {
            //List<String> baseScanPackages= GrailEnvironment.getBaseScanPackages();
            //swaggerBasePackages = basePackage.stream().collect(Collectors.joining(SEPARATOR));
        } else {
            // 校验格式
            if (!PACKAGES_PATTERN.matcher(swaggerBasePackages).matches()) {
                log.error("配置->grail.swagger.api.basePackages格式有误，请配置正确的包路径，多个用英文逗号隔开");
                System.exit(-1);
            }
        }

        Docket docket=  new Docket(DocumentationType.SWAGGER_2).groupName(swaggerProperties.getName())
                .apiInfo(apiInfo)
                .select()
                .apis(basePackage(swaggerBasePackages))
                .paths(Predicates.not(Predicates.or(exclude)))
                .build();

        if(null==swaggerProperties.getGlobalOperationParameters()){
            return docket;
        }

        List<Parameter> parameterList = buildGlobalOperationParameters(
                swaggerProperties.getGlobalOperationParameters());
        if(null!=parameterList&&parameterList.size()>0){
            docket.globalOperationParameters(parameterList);
        }
        return docket;

    }

    public static Predicate<RequestHandler> basePackage(final String basePackage) {
        return input -> declaringClass(input).transform(handlerPackage(basePackage)).or(true);
    }

    private static Function<Class<?>, Boolean> handlerPackage(final String basePackage) {
        return input -> {
            for (String strPackage : basePackage.split(SEPARATOR)) {
                boolean isMatch = input.getPackage().getName().startsWith(strPackage);
                if (isMatch) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Optional<? extends Class<?>> declaringClass(RequestHandler input) {
        return Optional.fromNullable(input.declaringClass());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 添加额外参数
     *
     * @return
     */
    private List<Parameter> buildGlobalOperationParameters(List<SwaggerProperties.GlobalOperationParameter> globalOperationParameters) {
        if (CollectionUtils.isEmpty(globalOperationParameters)) {
            return null;
        }
        List<Parameter> parameters = new ArrayList<>();
        globalOperationParameters.forEach(parameter -> {
            // 设置自定义参数
            parameters.add(new ParameterBuilder()
                    .name(parameter.getName())
                    .description(parameter.getDescription())
                    .modelRef(new ModelRef(parameter.getDataType()))
                    .parameterType(parameter.getParameterType())
                    .required(parameter.isRequired())
                    .defaultValue(parameter.getDefaultValue())
                    .build());
        });
        return parameters;
    }

    /**
     *  Fix Bug Failed to start bean 'documentationPluginsBootstrapper'; nested exception is java.lang.NullPointerException
     *  https://github.com/springfox/springfox/issues/3462
     * @param webEndpointsSupplier
     * @param servletEndpointsSupplier
     * @param controllerEndpointsSupplier
     * @param endpointMediaTypes
     * @param corsProperties
     * @param webEndpointProperties
     * @param environment
     * @return
     */
    /**
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier, ServletEndpointsSupplier servletEndpointsSupplier,
                                                                         ControllerEndpointsSupplier controllerEndpointsSupplier, EndpointMediaTypes endpointMediaTypes,
                                                                         CorsEndpointProperties corsProperties, WebEndpointProperties webEndpointProperties, Environment environment) {
        List<ExposableEndpoint<?>> allEndpoints = new ArrayList();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment, basePath);
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes, corsProperties.toCorsConfiguration(), new EndpointLinksResolver(allEndpoints, basePath), shouldRegisterLinksMapping, null);
    }

    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment, String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath) || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    }**/

    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                List<T> copy = mappings.stream()
                        .filter(mapping -> mapping.getPatternParser() == null)
                        .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            @SuppressWarnings("unchecked")
            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                try {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    field.setAccessible(true);
                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}

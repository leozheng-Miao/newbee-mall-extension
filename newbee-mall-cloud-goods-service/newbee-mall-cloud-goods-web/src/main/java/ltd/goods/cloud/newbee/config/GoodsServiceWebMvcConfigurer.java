/**
 * 严肃声明：
 * 开源版本请务必保留此注释头信息，若删除我方将保留所有法律责任追究！
 * 本软件已申请软件著作权，受国家版权局知识产权以及国家计算机软件著作权保护！
 * 可正常分享和学习源码，不得用于违法犯罪活动，违者必究！
 * Copyright (c) 2022 程序员十三 all rights reserved.
 * 版权所有，侵权必究！
 */
package ltd.goods.cloud.newbee.config;

import com.alibaba.cloud.seata.web.SeataHandlerInterceptor;
import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.SentinelWebInterceptor;
import ltd.goods.cloud.newbee.config.handler.TokenToAdminUserMethodArgumentResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;
import java.util.Optional;

@Configuration
public class GoodsServiceWebMvcConfigurer extends WebMvcConfigurationSupport {

    private static final Logger log = LoggerFactory.getLogger(GoodsServiceWebMvcConfigurer.class);

    @Autowired
    private TokenToAdminUserMethodArgumentResolver tokenToAdminUserMethodArgumentResolver;

    @Autowired
    private SentinelProperties sentinelProperties;

    @Autowired
    private Optional<SentinelWebInterceptor> sentinelWebInterceptorOptional;

    /**
     * @param argumentResolvers
     * @tip @TokenToAdminUser 注解处理方法
     */
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(tokenToAdminUserMethodArgumentResolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.
                addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                .resourceChain(false);
    }


    /**
     * 拦截seata上游发送的xid， 否则seata事务回滚会失效
     * 拦截sentinel
     * @param registry
     */
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SeataHandlerInterceptor()).addPathPatterns("/**");

        //sentinel拦截器
        if (this.sentinelWebInterceptorOptional.isPresent()) {
            SentinelProperties.Filter filter = this.sentinelProperties.getFilter();
            registry.addInterceptor(
                            (HandlerInterceptor) this.sentinelWebInterceptorOptional.get())
                    .order(filter.getOrder())
                    .addPathPatterns(filter.getUrlPatterns());
            log.info("[Sentinel Starter] register SentinelWebInterceptor with urlPatterns: {}.", filter.getUrlPatterns());
        }
    }
}
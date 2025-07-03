package ltd.shopcart.cloud.newbee.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: newbee-mall-cloud-dev-step07
 * @description:
 * @author: Miao Zheng
 * @date: 2025-07-02 17:14
 **/
@Configuration
public class OpenFeignConfiguration {

    @Bean
    public Logger.Level openFeignLogLevel() {
        return Logger.Level.FULL;
    }
}

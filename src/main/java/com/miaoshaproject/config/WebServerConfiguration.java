package com.miaoshaproject.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * @Author:CR7-source
 * @Date: 2022/02/11/ 16:27
 * @Description
 */
//当Spring容器没有TomcatEmbeddedServletContainerFactory这个bean时，会把此bean加载到spring中
@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        //通过对应的工厂提供给我们的接口定制化我的tomcat connector
        ((TomcatServletWebServerFactory)factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                //定制化keep-alive timeout
                protocol.setKeepAliveTimeout(30000);
                //发送超过10000个请求后自动断开
                protocol.setMaxKeepAliveRequests(10000);
            }
        });
    }
}

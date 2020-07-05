package client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.client.support.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.soap.security.support.KeyStoreFactoryBean;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;
import org.springframework.ws.soap.security.xwss.XwsSecurityInterceptor;
import org.springframework.ws.soap.security.xwss.callback.KeyStoreCallbackHandler;

import java.io.IOException;

@Configuration
@Slf4j
public class XwsClientConfiguration {
    private Resource requestSchema = new ClassPathResource("course-details.xsd");

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.in28minutes.courses");
        return marshaller;
    }

//    @Bean
//    public KeyStoreCallbackHandler securityCallbackHandler(){
//        KeyStoreCallbackHandler callbackHandler = new KeyStoreCallbackHandler();
//        callbackHandler.setKeyStore(getKeyStoreFactoryBean().getObject());
//        callbackHandler.setPrivateKeyPassword("keyStorePassword");
//        return callbackHandler;
//    }

    @Bean
    public KeyStoreCallbackHandler keyStoreHandler() {
        KeyStoreCallbackHandler keyStoreHandler = new KeyStoreCallbackHandler();
        keyStoreHandler.setTrustStore(truststore().getObject());
        keyStoreHandler.setKeyStore(keystore().getObject());
        keyStoreHandler.setPrivateKeyPassword("123456");
        keyStoreHandler.setDefaultAlias("mycert");
        return keyStoreHandler;
    }

    @Bean
    KeyStoreFactoryBean keystore() {
        KeyStoreFactoryBean bean = new KeyStoreFactoryBean();
        bean.setLocation(new ClassPathResource("keystore/server.keystore"));
        bean.setPassword("123456");
        return bean;
    }

    @Bean
    KeyStoreFactoryBean truststore() {
        KeyStoreFactoryBean bean = new KeyStoreFactoryBean();
        bean.setLocation(new ClassPathResource("keystore/truststore"));
        bean.setPassword("123456");
        return bean;
    }

//    @Bean
//    public KeyStoreFactoryBean getKeyStoreFactoryBean(){
//        KeyStoreFactoryBean keyStoreFactoryBean = new KeyStoreFactoryBean();
//        keyStoreFactoryBean.setLocation(new ClassPathResource("privatestore.jks"));
//        keyStoreFactoryBean.setPassword("keyStorePassword");
//        return keyStoreFactoryBean;
//    }

    @Bean
    public XwsSecurityInterceptor securityInterceptor(){
        XwsSecurityInterceptor securityInterceptor = new XwsSecurityInterceptor();
        securityInterceptor.setPolicyConfiguration(new ClassPathResource("clientSecurityPolicy.xml"));
        securityInterceptor.setCallbackHandler(keyStoreHandler());
        return securityInterceptor;
    }


    @Bean
     public PayloadValidatingInterceptor payloadValidatingInterceptor() {
        // schema validation the request payload
        PayloadValidatingInterceptor payloadValidatingInterceptor = new PayloadValidatingInterceptor();
        payloadValidatingInterceptor.setValidateRequest(true);
        payloadValidatingInterceptor.setSchema(requestSchema);
        return payloadValidatingInterceptor;
    }


    @Bean
    public CourseClient createClient(Jaxb2Marshaller marshaller) {
        log.info("Building WSS Client");
        CourseClient client = new CourseClient();
        ClientInterceptor[] interceptors = new ClientInterceptor[]{securityInterceptor(), payloadValidatingInterceptor()};
        client.setInterceptors(interceptors);
        client.setDefaultUri("http://localhost:8081/ws");
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }

}

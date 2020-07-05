package client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.client.support.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;

import java.io.IOException;

@Configuration
@Slf4j
public class Wss4jClientConfiguration {
    private final String clientKsAlias = "mycert";
    private final String clientKsPwd = "123456";

    private final Resource clientKs = new ClassPathResource("keystore/server.keystore");

    private Resource requestSchema = new ClassPathResource("course-details.xsd");

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.in28minutes.courses");
        return marshaller;
    }

    @Bean
    public Wss4jSecurityInterceptor wss4jSecurityInterceptor() throws Exception {
        Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();

        // set security actions: Timestamp Signature SAMLTokenSigned SAMLTokenUnsigned
        securityInterceptor.setSecurementActions("Timestamp Signature");

        // sign the request
        securityInterceptor.setSecurementUsername(clientKsAlias);
        securityInterceptor.setSecurementPassword(clientKsPwd);

        securityInterceptor.setSecurementSignatureCrypto(getCryptoFactoryBean().getObject());
        securityInterceptor.setSecurementSignatureParts(
                "{Element}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp;" +
                        "{Element}{http://schemas.xmlsoap.org/soap/envelope/}Body"
        );

//        securityInterceptor.setValidateResponse(false);
//        securityInterceptor.setValidationActions("NoSecurity");
        // X509KeyIdentifier, DirectReference
//        securityInterceptor.setSecurementSignatureKeyIdentifier("DirectReference");
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
    public CryptoFactoryBean getCryptoFactoryBean() throws IOException {
        CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
        cryptoFactoryBean.setKeyStorePassword(clientKsPwd);
        cryptoFactoryBean.setKeyStoreLocation(clientKs);
        return cryptoFactoryBean;
    }

    @Bean
    public CourseClient createClient(Wss4jSecurityInterceptor interceptor,
                                     PayloadValidatingInterceptor payloadValidatingInterceptor,
                                     Jaxb2Marshaller marshaller) throws Exception {
        log.info("Building WSS Client");
        CourseClient client = new CourseClient();
        ClientInterceptor[] interceptors = new ClientInterceptor[]{interceptor, payloadValidatingInterceptor};
        client.setInterceptors(interceptors);
        client.setDefaultUri("http://localhost:8081/ws");
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }

}

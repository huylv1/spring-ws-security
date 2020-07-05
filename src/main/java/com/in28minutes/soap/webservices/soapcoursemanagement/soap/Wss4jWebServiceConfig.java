package com.in28minutes.soap.webservices.soapcoursemanagement.soap;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.io.IOException;
import java.util.List;

//Enable Spring Web Services
@EnableWs
// Spring Configuration
@Configuration
public class Wss4jWebServiceConfig extends WsConfigurerAdapter {
	// MessageDispatcherServlet
	// ApplicationContext
	// url -> /ws/*

	@Bean
	public ServletRegistrationBean messageDispatcherServlet(ApplicationContext context) {
		MessageDispatcherServlet messageDispatcherServlet = new MessageDispatcherServlet();
		messageDispatcherServlet.setApplicationContext(context);
		messageDispatcherServlet.setTransformWsdlLocations(true);
		return new ServletRegistrationBean(messageDispatcherServlet, "/ws/*");
	}

	// /ws/courses.wsdl
	// course-details.xsd
	@Bean(name = "courses")
	public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema coursesSchema) {
		DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
		definition.setPortTypeName("CoursePort");
		definition.setTargetNamespace("http://in28minutes.com/courses");
		definition.setLocationUri("/ws");
		definition.setSchema(coursesSchema);
		return definition;
	}

	@Bean
	public XsdSchema coursesSchema() {
		return new SimpleXsdSchema(new ClassPathResource("course-details.xsd"));
	}

	@Bean
	public Wss4jSecurityInterceptor securityInterceptor() throws Exception {
		Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();

		// set security actions
		securityInterceptor.setSecurementActions("Timestamp Signature");

		// sign the request
		securityInterceptor.setSecurementUsername("mycert");
		securityInterceptor.setSecurementPassword("123456");
		securityInterceptor.setSecurementSignatureCrypto(getCryptoFactoryBean().getObject());

		// sign the response
//		securityInterceptor.setValidationActions("Timestamp Signature");
//		securityInterceptor.setValidationSignatureCrypto(getCryptoFactoryBean().getObject());
//		securityInterceptor.setValidationDecryptionCrypto(getCryptoFactoryBean().getObject());
//		securityInterceptor.setValidationCallbackHandler(keystoreCallbackHandler());


//		securityInterceptor.setSecureResponse(false);
//		securityInterceptor.setValidateResponse(false);
		return securityInterceptor;
	}

//	KeyStoreCallbackHandler keystoreCallbackHandler() {
//		KeyStoreCallbackHandler handler = new KeyStoreCallbackHandler();
//		handler.setPrivateKeyPassword("myAliasPassword");
//		return handler;
//	}

	@Bean
	public CryptoFactoryBean getCryptoFactoryBean() throws IOException {
		CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
		cryptoFactoryBean.setKeyStorePassword("123456");
		cryptoFactoryBean.setKeyStoreLocation(new ClassPathResource("keystore/server.keystore"));
		return cryptoFactoryBean;
	}

	//Interceptors.add -> XwsSecurityInterceptor
	@Override
	public void addInterceptors(List<EndpointInterceptor> interceptors) {
		try {
			interceptors.add(securityInterceptor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		interceptors.add(payloadLoggingInterceptor());
		interceptors.add(payloadValidatingInterceptor());
	}

	PayloadLoggingInterceptor payloadLoggingInterceptor() {
		return new PayloadLoggingInterceptor();
	}

	PayloadValidatingInterceptor payloadValidatingInterceptor() {
		PayloadValidatingInterceptor validatingInterceptor = new PayloadValidatingInterceptor();
		validatingInterceptor.setValidateRequest(true);
		validatingInterceptor.setValidateResponse(true);
		validatingInterceptor.setXsdSchema(coursesSchema());
		return validatingInterceptor;
	}

}

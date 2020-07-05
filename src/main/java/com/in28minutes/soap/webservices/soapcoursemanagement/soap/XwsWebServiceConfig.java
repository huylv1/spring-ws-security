package com.in28minutes.soap.webservices.soapcoursemanagement.soap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.interceptor.PayloadLoggingInterceptor;
import org.springframework.ws.soap.security.support.KeyStoreFactoryBean;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;
import org.springframework.ws.soap.security.x509.X509AuthenticationProvider;
import org.springframework.ws.soap.security.x509.populator.DaoX509AuthoritiesPopulator;
import org.springframework.ws.soap.security.xwss.XwsSecurityInterceptor;
import org.springframework.ws.soap.security.xwss.callback.KeyStoreCallbackHandler;
import org.springframework.ws.soap.security.xwss.callback.SpringCertificateValidationCallbackHandler;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import javax.security.auth.callback.CallbackHandler;
import java.io.IOException;
import java.util.List;

//Enable Spring Web Services
//@EnableWs
// Spring Configuration
//@Configuration
public class XwsWebServiceConfig extends WsConfigurerAdapter {
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

	//XwsSecurityInterceptor
	@Bean
	public XwsSecurityInterceptor securityInterceptor(){
		XwsSecurityInterceptor securityInterceptor = new XwsSecurityInterceptor();

		securityInterceptor.setCallbackHandlers(new CallbackHandler[] {
				keyStoreHandler(),
				certificateHandler()
		});
		//Security Policy -> securityPolicy.xml
		securityInterceptor.setPolicyConfiguration(new ClassPathResource("securityPolicy.xml"));
		return securityInterceptor;
	}


	public KeyStoreCallbackHandler keyStoreHandler() {
		KeyStoreCallbackHandler keyStoreHandler = new KeyStoreCallbackHandler();
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
	SpringCertificateValidationCallbackHandler certificateHandler() {
		SpringCertificateValidationCallbackHandler callbackHandler = new SpringCertificateValidationCallbackHandler();
		callbackHandler.setAuthenticationManager(authenticationManager());
		return callbackHandler;
	}

	@Bean
	ProviderManager authenticationManager() {
		return new ProviderManager(authenticationProvider());
	}

	@Bean
	DaoX509AuthoritiesPopulator authPopulator() {
		DaoX509AuthoritiesPopulator authPopulator = new DaoX509AuthoritiesPopulator();
		authPopulator.setUserDetailsService(userDetailsService);
		return authPopulator;
	}

	@Bean
	X509AuthenticationProvider authenticationProvider() {
		X509AuthenticationProvider authenticationProvider = new X509AuthenticationProvider();
		authenticationProvider.setX509AuthoritiesPopulator(authPopulator());
		return authenticationProvider;
	}

	@Autowired
	private UserDetailsService userDetailsService;

	//Interceptors.add -> XwsSecurityInterceptor
	@Override
	public void addInterceptors(List<EndpointInterceptor> interceptors) {
		interceptors.add(securityInterceptor());
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

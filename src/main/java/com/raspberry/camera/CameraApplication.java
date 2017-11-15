package com.raspberry.camera;

import com.raspberry.camera.service.AutoDiscoveryListener;
import com.raspberry.camera.service.RabbitReceiver;
import org.apache.log4j.Logger;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
@EnableAutoConfiguration
public class CameraApplication {

	private final static Logger logger = Logger.getLogger(CameraApplication.class);

	@Bean
	public Queue queue() {
		return new Queue("test");
	}

	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
		connectionFactory.setUsername("pi");
		connectionFactory.setPassword("raspberry");
		return connectionFactory;
	}

	@Bean
	public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames("test2");
		RabbitReceiver receiver = new RabbitReceiver();
		container.setMessageListener(new MessageListenerAdapter(receiver, "receive"));
		container.setAcknowledgeMode(AcknowledgeMode.AUTO);
		return container;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Runtime.getRuntime().exec("v4l2-ctl --overlay=1").waitFor();
		System.load("/usr/lib/jni/libopencv_java249.so");
		SpringApplication.run(CameraApplication.class, args);
		logger.info("Spring framework uruchomiony");
		logger.info("Rozpoczynanie nasłuchiwania pakietów...");
		Thread thread = new Thread(new AutoDiscoveryListener());
		thread.start();
		//blinkingRunnable.finishBlibking();
	}

}

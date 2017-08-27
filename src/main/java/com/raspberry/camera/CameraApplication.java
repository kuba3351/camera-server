package com.raspberry.camera;

import org.apache.log4j.Logger;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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

	public static void main(String[] args) {
		System.load("/usr/lib/jni/libopencv_java249.so");
		SpringApplication.run(CameraApplication.class, args);
		logger.info("Spring framework uruchomiony");
		logger.info("Rozpoczynanie nasłuchiwania pakietów...");
		Thread thread = new Thread(new AutoDiscoveryListener());
		thread.start();
		logger.info("Rozpoczynam test kamery...");
		PhotoService.takeFirstPhoto();
		logger.info("Test kamery zakończony.");
	}

}

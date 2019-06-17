package com.kafka.producer.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class Application {

	public static void main(String[] args) throws  Exception{

		ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

		MsgProducer producer = context.getBean(MsgProducer.class);
		MsgConsumer consumer = context.getBean(MsgConsumer.class);
		Scanner scan = new Scanner(System.in);
		while (true) {

			String msg = scan.nextLine();
			if (msg.compareToIgnoreCase("Exit")== 0 )
				break;

			producer.sendMsg(msg);
			consumer.latch.await(10, TimeUnit.SECONDS);
		}
		context.close();


	}
	@Bean
	public MsgProducer msgProducer() {
		return new MsgProducer();
	}

	@Bean
	public MsgConsumer msgConsumer() {
		return new MsgConsumer();
	}


	public static class MsgProducer {

		@Autowired
		private KafkaTemplate<String,String> kafkaTemplate;

		@Value(value="${topic.name}")
		private String topic;

		public void sendMsg(String msg)
		{
			  ListenableFuture<SendResult<String,String>>  future = kafkaTemplate.send(topic,msg);

			  future.addCallback(new ListenableFutureCallback<SendResult<String,String>> () {

			  	@Override
				  public void onSuccess(SendResult<String,String> result) {

			  		System.out.println("Sent Msg Successfully : " + msg);
				}
				@Override
				  public  void onFailure(Throwable ex){
					System.out.println("Msg failed to be deliverd " + msg);

				}
			  });
		}
	}

	public static class MsgConsumer {

		private CountDownLatch latch  = new CountDownLatch(3);
		@KafkaListener(topics = "${topic.name}" ,groupId = "foo" , containerFactory = "kafkaListenerContainerFactory")
		public void reciveMsg(String msg)
		{

			System.out.println("Recived Msg : " + msg);
			latch.countDown();
		}

	}

}

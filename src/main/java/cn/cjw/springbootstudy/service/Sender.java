package cn.cjw.springbootstudy.service;

import java.util.Date;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Sender {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	public void send(int index) {
		String context = index + " " + new Date();
		System.out.println("Sender: " + context);
		this.rabbitTemplate.convertAndSend("hello", context);
	}
}

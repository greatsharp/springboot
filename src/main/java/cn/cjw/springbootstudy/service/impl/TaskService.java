package cn.cjw.springbootstudy.service.impl;

import java.util.Random;
import java.util.concurrent.Future;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

	public static Random random = new Random();

	public void doTaskOne() throws Exception {
		System.out.println("开始做任务一");
		long start = System.currentTimeMillis();
		Thread.sleep(random.nextInt(10000));
		long end = System.currentTimeMillis();
		System.out.println("完成任务一，耗时：" + (end - start) + "毫秒");
	}

	public void doTaskTwo() throws Exception {
		System.out.println("开始做任务二");
		long start = System.currentTimeMillis();
		Thread.sleep(random.nextInt(10000));
		long end = System.currentTimeMillis();
		System.out.println("完成任务二，耗时：" + (end - start) + "毫秒");
	}

	public void doTaskThree() throws Exception {
		System.out.println("开始做任务三");
		long start = System.currentTimeMillis();
		Thread.sleep(random.nextInt(10000));
		long end = System.currentTimeMillis();
		System.out.println("完成任务三，耗时：" + (end - start) + "毫秒");
	}
	
	@Async
	public Future<String> doAsyncTaskOne() throws Exception {
		doTaskOne();
		return new AsyncResult<>("任务一完成");
	}
	
	@Async
	public Future<String> doAsyncTaskTwo() throws Exception {
		doTaskTwo();
		return new AsyncResult<>("任务二完成");
	}
	
	@Async
	public Future<String> doAsyncTaskThree() throws Exception {
		doTaskThree();
		return new AsyncResult<>("任务三完成");
	}
}

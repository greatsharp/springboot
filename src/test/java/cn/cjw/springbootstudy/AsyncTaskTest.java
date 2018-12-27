package cn.cjw.springbootstudy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.cjw.springbootstudy.service.impl.TaskService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AsyncTaskTest {
	
	@Autowired
	private TaskService ts;

//	@Test
	public void runInSync() throws Exception {
		ts.doTaskOne();
		ts.doTaskTwo();
		ts.doTaskThree();
	}
	
	@Test
	public void runInAsync() throws Exception {
		long start = System.currentTimeMillis();
		
		Future<String> task1 = ts.doAsyncTaskOne();
		Future<String> task2 = ts.doAsyncTaskTwo();
		Future<String> task3 = ts.doAsyncTaskThree();
		
		while(true) {
			if(task1.isDone() && task2.isDone() && task3.isDone()) {
				// 三个任务都调用完成，退出循环等待
				break;
			}
			Thread.sleep(1000);
		}

		long end = System.currentTimeMillis();

		System.out.println("任务全部完成，总耗时：" + (end - start) + "毫秒");
		
		System.out.println(task1.get());
		System.out.println(task2.get());
		System.out.println(task3.get());
	}
}

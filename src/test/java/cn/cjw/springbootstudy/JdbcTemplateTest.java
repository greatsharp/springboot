package cn.cjw.springbootstudy;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.cjw.springbootstudy.model.User;
import cn.cjw.springbootstudy.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JdbcTemplateTest {

	@Autowired
	private UserService userService;
	
	@Test
	public void test() {
		User user = new User();
		user.setName("zhangshan");
		user.setAge(10);
		userService.createUser(user);
		
		List<User> list = userService.getAllUser();
		list.stream().forEach(u -> System.out.println(u.getId() + "=" + u.getName() + "," + u.getAge()));
	}
}

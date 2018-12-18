package cn.cjw.springbootstudy;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cn.cjw.springbootstudy.web.HelloController;
import cn.cjw.springbootstudy.web.UserController;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ControlllerTests {

	private MockMvc mvc;

	@Before
	public void setUp() {
		mvc = MockMvcBuilders.standaloneSetup(new HelloController(), new UserController()).build();
	}

	@Test
	public void testHello() throws Exception {
		mvc.perform(get("/hello").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().string(equalTo("Hello World")));
	}

	@Test
	public void testUser() throws Exception {
		RequestBuilder request = null;

		request = get("/users/");
		mvc.perform(request).andExpect(status().isOk()).andExpect(content().string(equalTo("[]")));

		request = post("/users/").param("id", "1").param("name", "rapheal").param("age", "20");
		mvc.perform(request).andExpect(status().isOk()).andExpect(content().string(equalTo("success")));

		// 3、get获取user列表，应该有刚才插入的数据
		request = get("/users/");
		mvc.perform(request).andExpect(status().isOk())
				.andExpect(content().string(equalTo("[{\"id\":1,\"name\":\"rapheal\",\"age\":20}]")));

		// 4、put修改id为1的user
		request = put("/users/1").param("name", "rapheal new").param("age", "30");
		mvc.perform(request).andExpect(content().string(equalTo("success")));

		// 5、get一个id为1的user
		request = get("/users/1");
		mvc.perform(request).andExpect(content().string(equalTo("{\"id\":1,\"name\":\"rapheal new\",\"age\":30}")));

		// 6、del删除id为1的user
		request = delete("/users/1");
		mvc.perform(request).andExpect(content().string(equalTo("success")));

		// 7、get查一下user列表，应该为空
		request = get("/users/");
		mvc.perform(request).andExpect(status().isOk()).andExpect(content().string(equalTo("[]")));

	}
}

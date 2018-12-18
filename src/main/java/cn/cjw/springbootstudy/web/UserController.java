package cn.cjw.springbootstudy.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.cjw.springbootstudy.model.User;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/users")
public class UserController {

	static Map<Long, User> users = Collections.synchronizedMap(new HashMap<Long, User>());

	@ApiOperation(value = "获取用户列表", notes = "")
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public List<User> getUserList() {
		return new ArrayList<User>(users.values());
	}

	@ApiOperation(value = "创建用户", notes = "根据User对象创建用户")
	@ApiImplicitParam(name = "user", value = "用户详细实体user", required = true, dataType = "User")
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public String addUser(@RequestBody User user) {
		users.put(user.getId(), user);
		return "success";
	}

	@ApiOperation(value = "创建用户并以xml格式返回用户信息", notes = "根据User对象创建用户，并以xml格式返回用户信息")
	@ApiImplicitParam(name = "user", value = "用户详细实体user", required = true, dataType = "User")
	@PostMapping(value = "/testXml", consumes=MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_XML_VALUE)
	@ResponseBody
	public User create(@RequestBody User user) {
		user.setName("new name : " + user.getName());
		user.setAge(user.getAge() + 100);
		users.put(user.getId(), user);
		return user;
	}

	@ApiOperation(value = "获取用户详细信息", notes = "根据url的id来获取用户详细信息")
	@ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long")
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public User getUser(@PathVariable Long id) {
		return users.get(id);
	}

	@ApiOperation(value = "更新用户详细信息", notes = "根据url的id来指定更新对象，并根据传过来的user信息来更新用户详细信息")
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long"),
			@ApiImplicitParam(name = "user", value = "用户详细实体user", required = true, dataType = "User") })
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public String updateUser(@PathVariable Long id, @RequestBody User user) {
		// 处理"/users/{id}"的PUT请求，用来更新User信息
		User u = users.get(id);
		u.setName(user.getName());
		u.setAge(user.getAge());
		users.put(id, u);
		return "success";
	}

	@ApiOperation(value = "删除用户", notes = "根据url的id来指定删除对象")
	@ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "Long")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public String deleteUser(@PathVariable Long id) {
		// 处理"/users/{id}"的DELETE请求，用来删除User
		users.remove(id);
		return "success";
	}
}

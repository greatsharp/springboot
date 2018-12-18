package cn.cjw.springbootstudy.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.cjw.springbootstudy.model.BlogProperties;

@Controller
public class HelloController {
	
	@Autowired
	private BlogProperties blogProps;

	@RequestMapping("/hello")
	@ResponseBody
	public String hello() throws Exception {
		return "Hello World";
	}
	
	@RequestMapping("/test_error")
	@ResponseBody
	public String error(@RequestParam(required=false) String error) throws Exception {
		if(!StringUtils.isEmpty(error)) {
			throw new Exception(error);
		}
		return "Some error happened";
	}
	
	@RequestMapping("/blog")
	@ResponseBody
	public String blog() {
		return blogProps.getDesc();
	}
	
	@RequestMapping("/")
	public String index(ModelMap map) {
		map.put("host", "https://start.spring.io/");
		return "index.html";
	}
}

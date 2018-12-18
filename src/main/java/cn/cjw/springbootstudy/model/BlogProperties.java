package cn.cjw.springbootstudy.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BlogProperties {

	@Value("${cn.cjw.springbootstudy.blog.author}")
	private String author;
	
	@Value("${cn.cjw.springbootstudy.blog.description}")
	private String desc;

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}

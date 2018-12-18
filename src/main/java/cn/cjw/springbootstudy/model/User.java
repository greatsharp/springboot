package cn.cjw.springbootstudy.model;

import java.io.Serializable;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "User")
public class User implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@JacksonXmlProperty(localName = "id")
	private Long id;
	@JacksonXmlProperty(localName = "name")
	private String name;
	@JacksonXmlProperty(localName = "age")
	private Integer age;
}

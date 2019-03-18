package cn.cjw.springbootstudy.elasticsearch;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasitcsearchUtils {
	
	public static final String HOST = "10.223.32.231";
	public static final int PORT = 9200;

	@Bean
	public RestHighLevelClient getRestHighLevelClient() {
		return new RestHighLevelClient(
				RestClient.builder(new HttpHost(ElasitcsearchUtils.HOST, 9200, "http")));
	}
}

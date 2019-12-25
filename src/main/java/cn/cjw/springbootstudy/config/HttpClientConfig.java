package cn.cjw.springbootstudy.config;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * Apache HttpClient 配置类
 *
 */
@Configuration
public class HttpClientConfig {

	@Value("${httpclient.maxTotal}")
    private Integer maxTotal;

    @Value("${httpclient.defaultMaxPerRoute}")
    private Integer defaultMaxPerRoute;

    @Value("${httpclient.connectTimeout}")
    private Integer connectTimeout;

    @Value("${httpclient.connectionRequestTimeout}")
    private Integer connectionRequestTimeout;

    @Value("${httpclient.socketTimeout}")
    private Integer socketTimeout;

    @Value("${httpclient.staleConnectionCheckEnabled}")
    private boolean staleConnectionCheckEnabled;
    
    /**
     * HttpClient 连接池
     * @return
     */
    @Bean
    public HttpClientConnectionManager httpClientConnectionManager() {
    	PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    	connectionManager.setMaxTotal(maxTotal);
    	connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
    	
    	return connectionManager;
    }

    /**
     * 注册RequestConfig
     * @return
     */
    @Bean
	public RequestConfig requestConfig() {
		return RequestConfig.custom().setConnectTimeout(connectTimeout)
				.setConnectionRequestTimeout(connectionRequestTimeout).setSocketTimeout(socketTimeout).build();
	}
    
    /**
     * 注册HttpClient
     * @param manager
     * @param config
     * @return
     */
    @Bean
    public HttpClient httpClient(HttpClientConnectionManager manager, RequestConfig config) {
    	return HttpClientBuilder.create().setConnectionManager(manager).setDefaultRequestConfig(config).build();
    }
    
    /**
     * 注册RestTemplate, 使用UTF-8编码
     * @param httpClient
     * @return
     */
    @Bean
    public RestTemplate restTemplate(HttpClient httpClient) {
    	ClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
    	RestTemplate restTemplate = new RestTemplate(factory);
    	
    	List<HttpMessageConverter<?>> list = restTemplate.getMessageConverters();
    	for (HttpMessageConverter<?> mc : list) {
    		if (mc instanceof StringHttpMessageConverter) {
    			((StringHttpMessageConverter) mc).setDefaultCharset(Charset.forName("UTF-8"));
    		}
    	}
    	
    	return restTemplate;
    }
    
    
}

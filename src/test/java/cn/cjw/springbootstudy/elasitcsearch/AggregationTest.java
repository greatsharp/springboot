package cn.cjw.springbootstudy.elasitcsearch;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.cjw.springbootstudy.elasticsearch.ElasitcsearchUtils;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class AggregationTest {
	
	private static final String AGGS_POPULAR_COLOR = "popular_colors";
	private static final String AGGS_MADEBY = "madeby";
	
	private void setColorAggs(SearchSourceBuilder builder) {
		builder.aggregation(AggregationBuilders.terms(AGGS_POPULAR_COLOR).field("color")
				.subAggregation(AggregationBuilders.terms(AGGS_MADEBY).field("make")));
	}

	@Test
	public void testAggregation() {
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost(ElasitcsearchUtils.HOST, 9200, "http")));
		
		SearchRequest request = new SearchRequest("cars");
		SearchSourceBuilder builder = new SearchSourceBuilder();
		setColorAggs(builder);
		request.source(builder);
		
		try {
			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			log.info("---------------------------------aggregation response ------------------------------------");
			log.info("total shards: {}", response.getTotalShards());
			log.info("success shards: {}", response.getSuccessfulShards());
			log.info("failed shards: {}", response.getFailedShards());
			
			SearchHits hits = response.getHits();
			log.info("total hits: {}", hits.getTotalHits());
			log.info("max score: {}", hits.getMaxScore());
			
			Terms terms = response.getAggregations().get(AGGS_POPULAR_COLOR);
			for(Terms.Bucket entry : terms.getBuckets()) {
				log.info("color: {}, doc-count: {}", entry.getKeyAsString(), entry.getDocCount());

				Terms makes = entry.getAggregations().get(AGGS_MADEBY);
				for(Terms.Bucket madeby : makes.getBuckets()) {
					log.info("|______madeby: {}, doc-count: {}", madeby.getKeyAsString(), madeby.getDocCount());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void setHistogramByPrice(SearchSourceBuilder builder) {
		builder.aggregation(AggregationBuilders.histogram("price").field("price").interval(20000)
				.subAggregation(AggregationBuilders.sum("revenue").field("price")));
	}
	
	@Test
	public void testHistogram() {
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost(ElasitcsearchUtils.HOST, 9200, "http")));
		
		SearchRequest request = new SearchRequest("cars");
		SearchSourceBuilder builder = new SearchSourceBuilder();
		setHistogramByPrice(builder);
		request.source(builder);
		
		try {
			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			log.info("---------------------------------histogram response ------------------------------------");
			log.info("total shards: {}", response.getTotalShards());
			log.info("success shards: {}", response.getSuccessfulShards());
			log.info("failed shards: {}", response.getFailedShards());
			
			SearchHits hits = response.getHits();
			log.info("total hits: {}", hits.getTotalHits());
			log.info("max score: {}", hits.getMaxScore());
			
			Histogram historgram = response.getAggregations().get("price");
			for(Histogram.Bucket entry : historgram.getBuckets()) {
				log.info("price: {}->, doc-count: {}", entry.getKeyAsString(), entry.getDocCount());
				Sum revenue = entry.getAggregations().get("revenue");
				log.info("|_______ revenue: {}", revenue.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

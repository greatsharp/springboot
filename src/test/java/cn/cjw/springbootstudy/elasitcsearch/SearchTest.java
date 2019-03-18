package cn.cjw.springbootstudy.elasitcsearch;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.cjw.springbootstudy.elasticsearch.ElasitcsearchUtils;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SearchTest {
	
	private void setMatchAllQuery(SearchSourceBuilder builder) {
		builder.query(QueryBuilders.matchAllQuery());
	}
	
	private void setMatchQuery(SearchSourceBuilder builder) {
		builder.query(QueryBuilders.matchQuery("make", "toyota"));
	}
	
	private void setRangeQuery(SearchSourceBuilder builder) {
		builder.query(QueryBuilders.rangeQuery("price").gte(20000));
		sortBy(builder, "price", SortOrder.ASC);
	}
	
	private void setMultiFieldQuery(SearchSourceBuilder builder) {
		builder.query(QueryBuilders.multiMatchQuery("bmw", "make", "color").minimumShouldMatch("0.3").tieBreaker(0.3f));
	}
	
	private void setMatchPhaseQuery(SearchSourceBuilder builder) {
		builder.query(QueryBuilders.matchPhraseQuery("make", "bmw").slop(1));
	}
	
	
	
	private void setFilterQuery(SearchSourceBuilder builder) {
		builder.query(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("make", "honda")).filter(QueryBuilders.matchQuery("color", "red")));
	}
	
	private void sortBy(SearchSourceBuilder builder, String field, SortOrder order) {
		builder.sort(field, order);
	}

	@Test
	public void testSearch() {
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost(ElasitcsearchUtils.HOST, 9200, "http")));
		
		SearchRequest request = new SearchRequest("cars");
		SearchSourceBuilder builder = new SearchSourceBuilder();
//		setMatchAllQuery(builder);
//		setMatchQuery(builder);
//		setRangeQuery(builder);
		setFilterQuery(builder);
		request.source(builder);
		
		try {
			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			log.info("---------------------------------search response ------------------------------------");
			log.info("total shards: {}", response.getTotalShards());
			log.info("success shards: {}", response.getSuccessfulShards());
			log.info("failed shards: {}", response.getFailedShards());
			
			SearchHits hits = response.getHits();
			log.info("total hits: {}", hits.getTotalHits());
			log.info("max score: {}", hits.getMaxScore());
			
			for(SearchHit hit: hits.getHits()) {
				log.info("");
				log.info("_index: {}", hit.getIndex());
				log.info("_type: {}", hit.getType());
				log.info("_id: {}", hit.getId());
				log.info("_score: {}", hit.getScore());
				log.info("_source: {}", hit.getSourceAsString());
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

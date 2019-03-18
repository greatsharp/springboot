package cn.cjw.springbootstudy.elasitcsearch;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.rest.RestStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.cjw.springbootstudy.elasticsearch.ElasitcsearchUtils;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class IndexTest {

//	@Test
	public void testIndex() {
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost(ElasitcsearchUtils.HOST, 9200, "http")));

//		IndexRequest indexRequest = new IndexRequest("posts", "doc", "1")
//		        .source("user", "kimchy",
//		                "postDate", new Date(),
//		                "message", "trying out Elasticsearch");
		
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("user", "kimchy");
		jsonMap.put("postDate", new Date());
		jsonMap.put("message", "trying out Elasticsearch");
		IndexRequest indexRequest = new IndexRequest("posts", "doc", "1")
		        .source(jsonMap);
		
		log.info("---------------------------index doc---------------------------");
		try {
			IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
			log.info("index name : {}", indexResponse.getIndex());
			log.info("doc type : {}", indexResponse.getType());
			log.info("doc id : {}", indexResponse.getId());
			log.info("doc version : {}", indexResponse.getVersion());
			if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
			    log.info("index created!");
			} else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
				log.info("index updated!");
			}
			ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
			if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
			    log.info("shard errors");
			}
			if (shardInfo.getFailed() > 0) {
			    for (ReplicationResponse.ShardInfo.Failure failure :
			            shardInfo.getFailures()) {
			        log.info("shard errors: {}", failure.reason());
			    }
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch(ElasticsearchException e) {
			if (e.status() == RestStatus.CONFLICT) {
		        log.info("version confilict");
		    }
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
//	@Test
	public void testIndexAsync() {
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost(ElasitcsearchUtils.HOST, 9200, "http")));
		
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("user", "kimchy");
		jsonMap.put("postDate", new Date());
		jsonMap.put("message", "trying out Elasticsearch");
		IndexRequest indexRequest = new IndexRequest("posts", "doc", "1")
		        .source(jsonMap);
		
		log.info("--------------------------index doc async----------------------------");
		client.indexAsync(indexRequest, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {

			@Override
			public void onResponse(IndexResponse response) {
				log.info("index name : {}", response.getIndex());
				log.info("doc type : {}", response.getType());
				log.info("doc id : {}", response.getId());
				log.info("doc version : {}", response.getVersion());
				
				if (response.getResult() == DocWriteResponse.Result.CREATED) {
				    log.info("index created!");
				} else if (response.getResult() == DocWriteResponse.Result.UPDATED) {
					log.info("index updated!");
				}
				
				ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
				if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
				    log.info("shard errors");
				}
				if (shardInfo.getFailed() > 0) {
				    for (ReplicationResponse.ShardInfo.Failure failure :
				            shardInfo.getFailures()) {
				        log.info("shard errors: {}", failure.reason());
				    }
				}
				
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Exception e) {
				log.info("index async failed: {}", e.getMessage());
				
				try {
					client.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			
		});
	}
	
//	@Test
	public void testGet() {
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost(ElasitcsearchUtils.HOST, 9200, "http")));
		
		GetRequest request = new GetRequest("posts", "doc", "1");
		log.info("------------------------get doc------------------------------");
		try {
			GetResponse response = client.get(request, RequestOptions.DEFAULT);
			log.info("index name : {}", response.getIndex());
			log.info("doc type : {}", response.getType());
			log.info("doc id : {}", response.getId());
			if(response.isExists()) {
				log.info("doc version : {}", response.getVersion());
				log.info("doc source: {}", response.getSourceAsString());
			} else {
				log.info("doc isn't found!");
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
	
//	@Test
	public void testDelete() {
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost(ElasitcsearchUtils.HOST, 9200, "http")));
		
		//only works if current version is 2
//		DeleteRequest request = new DeleteRequest("posts", "doc", "1").version(2);
		DeleteRequest request = new DeleteRequest("posts", "doc", "1");
		log.info("------------------------delete doc------------------------------");
		try {
			DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
			log.info("index name : {}", response.getIndex());
			log.info("doc type : {}", response.getType());
			log.info("doc id : {}", response.getId());
			log.info("doc version: {}", response.getVersion());
			ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
			if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
				log.info("shard errors");
			}
			if (shardInfo.getFailed() > 0) {
			    for (ReplicationResponse.ShardInfo.Failure failure :
			            shardInfo.getFailures()) {
			        log.info("shard errors: {}", failure.reason());
			    }
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch(ElasticsearchException e) {
			e.printStackTrace();
			if (e.status() == RestStatus.CONFLICT) {
		        log.info("version conflict");
		    }
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
//	@Test
	public void testUpdate() {
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost(ElasitcsearchUtils.HOST, 9200, "http")));
		
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("updated", new Date());
		jsonMap.put("reason", "daily update");
		UpdateRequest request = new UpdateRequest("posts", "doc", "1").doc(jsonMap);
		request.retryOnConflict(5);
		request.fetchSource(true);
		
		log.info("----------------------------update doc--------------------------");
		try {
			UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
			log.info("index name : {}", response.getIndex());
			log.info("doc type : {}", response.getType());
			log.info("doc id : {}", response.getId());
			log.info("doc version: {}", response.getVersion());
			
			ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
			if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
				log.info("shard errors");
			}
			if (shardInfo.getFailed() > 0) {
			    for (ReplicationResponse.ShardInfo.Failure failure :
			            shardInfo.getFailures()) {
			        log.info("shard errors: {}", failure.reason());
			    }
			}
			
			GetResult result = response.getGetResult();
			if (result!=null && result.isExists()) {
			    log.info("doc source: {}", result.sourceAsString());
			} else {
				log.info("doc isn't found!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch(ElasticsearchException e) {
			e.printStackTrace();
			if (e.status() == RestStatus.CONFLICT) {
		        log.info("version conflict");
		    }
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	@Test
	public void testBulk() {
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost(ElasitcsearchUtils.HOST, 9200, "http")));
		
		log.info("----------------------------bulk doc--------------------------");
		
		BulkRequest request = new BulkRequest();
		for(int i=0; i<10000; i++) {
			request.add(new IndexRequest("posts", "doc", ""+i).source(XContentType.JSON, "key", "value_"+i));
		}
		
		try {
			BulkResponse bulkResponse = client.bulk(request, RequestOptions.DEFAULT);
			for (BulkItemResponse bulkItemResponse : bulkResponse) { 
				if(bulkItemResponse.isFailed()) {
					log.error("error happended in bulk : {}", bulkItemResponse.getFailure());
					continue;
				}
				
			    DocWriteResponse itemResponse = bulkItemResponse.getResponse();
			    switch (bulkItemResponse.getOpType()) {
			    case INDEX:    
			    case CREATE:
			        IndexResponse indexResponse = (IndexResponse) itemResponse;
			        log.info("index name : {}, type: {}, id: {}, version: {}", indexResponse.getIndex(), indexResponse.getType(), indexResponse.getId(), indexResponse.getVersion());
			        break;
			    case UPDATE:
			        UpdateResponse updateResponse = (UpdateResponse) itemResponse;
			        break;
			    case DELETE:   
			        DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
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
}

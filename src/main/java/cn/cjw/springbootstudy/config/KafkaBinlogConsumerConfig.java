package cn.cjw.springbootstudy.config;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * kafka配置
 */
@Configuration
@EnableKafka
public class KafkaBinlogConsumerConfig {

    @Value("${spring.kafka.binlog-consumer.bootstrap-servers}")
    private String servers;

    @Value("${spring.kafka.binlog-consumer.enable-auto-commit}")
    private boolean enableAutoCommit;

    @Value("${spring.kafka.binlog-consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${spring.kafka.binlog-consumer.max-poll-records}")
    private int maxPollRecords;

    @Value("${spring.kafka.binlog-consumer.group-id}")
    private String groupId;
    
    @Value("${spring.kafka.binlog-consumer.max-partition-fetch-bytes:52428800}")
    private int maxPartitionFetchBytes;
    
    @Value("${spring.kafka.binlog-consumer.concurrency:5}")
    private int concurrency;

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> binlogKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfigs(groupId)));
        factory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL);
        factory.getContainerProperties().setPollTimeout(15000);
        return factory;
    }

    /**
    * 使用方式：@KafkaListener(topics = {"topic_name"}, containerFactory = "binlogBatchFactory")
    */
    @Bean
    public KafkaListenerContainerFactory<?> binlogBatchFactory() {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfigs(groupId)));
        factory.setBatchListener(true);
        //设置并发量，小于或等于Topic的分区数
        factory.setConcurrency(this.concurrency);
        factory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL);
        return factory;
    }

    private Map<String, Object> consumerConfigs(String groupId) {
        Map<String, Object> propsMap = new HashMap<>();
        propsMap.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        propsMap.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        propsMap.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        propsMap.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        propsMap.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        propsMap.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        propsMap.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes);
        
        return propsMap;
    }

}

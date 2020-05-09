/*
 * Copyright (C) 2017-2020 Jeff Carpenter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.cassandraguide.conf;

import dev.cassandraguide.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;

/**
 * Import Configuration from Configuration File
 *
 * @author Jeff Carpenter
 */
@Configuration
public class KafkaConfiguration {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(ReservationRepository.class);

    // Bootstrap servers
    @Value("${kafka.bootstrap-servers:localhost:9092}")
    protected String bootstrapServers;

    // Kafka Client ID
    @Value("${kafka.client-id:ReservationService}")
    protected String clientId = "ReservationService";

    // Topic Name
    @Value("${kafka.topicName:reservation}")
    public String topicName = "reservation";

    /**
     * Default configuration.
     */
    public KafkaConfiguration() {}

    /**
     * Initialization of Configuration.
     *
     * @param bootstrapServers
     * @param clientId
     * @param topicName
     */
    public KafkaConfiguration(
            String bootstrapServers, String clientId, String topicName) {
        super();
        this.bootstrapServers = bootstrapServers;
        this.clientId = clientId;
        this.topicName = topicName;
    }

    @Bean
    public KafkaProducer<String, String> kafkaProducer() {
        logger.info("Creating Kafka Producer.");

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    /**
     * Getter accessor for attribute 'bootstrapServers'.
     *
     * @return
     *       current value of 'bootstrapServers'
     */
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    /**
     * Setter accessor for attribute 'bootstrapServers'.
     * @param bootstrapServers
     * 		new value for 'bootstrapServers'
     */
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    /**
     * Getter accessor for attribute 'clientId'.
     *
     * @return
     *       current value of 'clientId'
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Setter accessor for attribute 'clientId'.
     * @param clientId
     * 		new value for 'clientId '
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Getter accessor for attribute 'topicName'.
     *
     * @return
     *       current value of 'topicName'
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Setter accessor for attribute 'topicName'.
     * @param topicName
     * 		new value for 'topicName '
     */
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

}

/*
 * Copyright (C) 2017-2019 Jeff Carpenter
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

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createKeyspace;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.dropKeyspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.cassandraguide.repository.ReservationRepository;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;

/**
 * Import Configuration from Configuration File
 *
 * @author Cedrick Lunven
 */
@Configuration
public class CassandraConfiguration {
    
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(ReservationRepository.class);

    // Keyspace Name
    @Value("${cassandra.keyspaceName:reservation}")
    public String keyspaceName = "reservation";
    
    // Option to drop schema and generate table again at startup
    @Value("${cassandra.dropSchema:true}")
    public boolean dropSchema;

    /**
     * Default configuration.
     */
    public CassandraConfiguration() {}
            
    /**
     * Initialization of Configuration.
     *
     * @param keyspaceName
     * @param dropSchema
     */
    public CassandraConfiguration(
            String keyspaceName, boolean dropSchema) {
        super();
        this.keyspaceName        = keyspaceName;
        this.dropSchema          = dropSchema;
    }
    
    /**
     * Returns the keyspace to connect to. The keyspace specified here must exist.
     *
     * @return The {@linkplain CqlIdentifier keyspace} bean.
     */
    @Bean
    public CqlIdentifier keyspace() {
      return CqlIdentifier.fromCql(keyspaceName);
    }
    
    @Bean
    public CqlSession cqlSession() {
        return CqlSession.builder()
                //.addContactPoint(new InetSocketAddress(getCassandraUsername(), getCassandraPassword()))
                .withKeyspace(keyspace())
                //.withLocalDatacenter(getLocalDataCenterName())
                .build();
    }

    /**
     * Getter accessor for attribute 'keyspaceName'.
     *
     * @return
     *       current value of 'keyspaceName'
     */
    public String getKeyspaceName() {
        return keyspaceName;
    }

    /**
     * Setter accessor for attribute 'keyspaceName'.
     * @param keyspaceName
     * 		new value for 'keyspaceName '
     */
    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }

    /**
     * Getter accessor for attribute 'dropSchema'.
     *
     * @return
     *       current value of 'dropSchema'
     */
    public boolean isDropSchema() {
        return dropSchema;
    }

    /**
     * Setter accessor for attribute 'dropSchema'.
     * @param dropSchema
     * 		new value for 'dropSchema '
     */
    public void setDropSchema(boolean dropSchema) {
        this.dropSchema = dropSchema;
    }
    
    

}

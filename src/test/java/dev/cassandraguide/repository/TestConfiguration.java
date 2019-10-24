package dev.cassandraguide.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Import Configuration from Configuration File
 *
 * @author Jeff Carpenter
 */
@Configuration
public class TestConfiguration {

    // Use Container
    @Value("${test.useContainer}")
    public boolean useContainer = true;

    /**
     * Default configuration.
     */
    public TestConfiguration() {}

    /**
     * Initialization of Configuration.
     *
     * @param useContainer
     */
    public TestConfiguration(boolean useContainer) {
        super();
        this.useContainer = useContainer;
    }

    /**
     * Getter accessor for attribute 'useContainer'.
     *
     * @return
     *       current value of 'useContainer'
     */
    public boolean getUseContainer() {
        return useContainer;
        }

        /**
         * * Setter accessor for attribute 'keyspaceName'.
         * @param useContainer
         * 		new value for 'useContainer'
         */
        public void setUseContainer(boolean useContainer) {

            this.useContainer = useContainer;
        }


}

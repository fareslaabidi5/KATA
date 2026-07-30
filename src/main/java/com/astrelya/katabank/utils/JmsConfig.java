package com.astrelya.katabank.utils;

import com.ibm.mq.jakarta.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import jakarta.jms.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

@Configuration
@EnableJms
public class JmsConfig {

    @Value("${ibm.mq.queue-manager}")
    private String queueManager;

    @Value("${ibm.mq.channel}")
    private String channel;

    @Value("${ibm.mq.conn-name}")
    private String connName; // format: host(port)

    @Value("${ibm.mq.user}")
    private String user;

    @Value("${ibm.mq.password}")
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        MQQueueConnectionFactory mqFactory = new MQQueueConnectionFactory();
        try {
            String host = connName.substring(0, connName.indexOf('('));
            int port = Integer.parseInt(
                    connName.substring(connName.indexOf('(') + 1, connName.indexOf(')'))
            );

            mqFactory.setHostName(host);
            mqFactory.setPort(port);
            mqFactory.setQueueManager(queueManager);
            mqFactory.setChannel(channel);
            mqFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);

            mqFactory.setStringProperty(WMQConstants.USERID, user);
            mqFactory.setStringProperty(WMQConstants.PASSWORD, password);
            mqFactory.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);

        } catch (Exception e) {
            throw new RuntimeException("Failed to configure MQQueueConnectionFactory", e);
        }
        return mqFactory; // implements jakarta.jms.ConnectionFactory directly
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionTransacted(true);
        return factory;
    }
    @Bean
    public org.springframework.jms.core.JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        org.springframework.jms.core.JmsTemplate template = new org.springframework.jms.core.JmsTemplate(connectionFactory);
        template.setSessionTransacted(true);
        return template;
    }
}
/*
** OKafka Java Client version 0.8.
**
** Copyright (c) 2019, 2020 Oracle and/or its affiliates.
** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
*/

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oracle.okafka.clients.consumer;


import java.util.Map;

import org.oracle.okafka.common.Configurable;
import org.oracle.okafka.common.TopicPartition;

/**
 * A plugin interface that allows you to intercept (and possibly mutate) records received by the consumer. A primary use-case
 * is for third-party components to hook into the consumer applications for custom monitoring, logging, etc.
 *
 * <p>
 * This class will get consumer config properties via <code>configure()</code> method, including clientId assigned
 * by KafkaConsumer if not specified in the consumer config. The interceptor implementation needs to be aware that it will be
 * sharing consumer config namespace with other interceptors and serializers, and ensure that there are no conflicts.
 * <p>
 * Exceptions thrown by ConsumerInterceptor methods will be caught, logged, but not propagated further. As a result, if
 * the user configures the interceptor with the wrong key and value type parameters, the consumer will not throw an exception,
 * just log the errors.
 * <p>
 * ConsumerInterceptor callbacks are called from the same thread that invokes
 * {@link org.oracle.okafka.clients.consumer.KafkaConsumer#poll(java.time.Duration)}.
 * <p>
 */
public interface ConsumerInterceptor<K, V> extends Configurable {

    /**
     * This is called just before the records are returned by
     * {@link org.oracle.okafka.clients.consumer.KafkaConsumer#poll(java.time.Duration)}
     * <p>
     * This method is allowed to modify consumer records, in which case the new records will be
     * returned. There is no limitation on number of records that could be returned from this
     * method. I.e., the interceptor can filter the records or generate new records.
     * <p>
     * Any exception thrown by this method will be caught by the caller, logged, but not propagated to the client.
     * <p>
     * Since the consumer may run multiple interceptors, a particular interceptor's onConsume() callback will be called
     * in the order specified by {@link org.oracle.okafka.clients.consumer.ConsumerConfig#INTERCEPTOR_CLASSES_CONFIG}.
     * The first interceptor in the list gets the consumed records, the following interceptor will be passed the records returned
     * by the previous interceptor, and so on. Since interceptors are allowed to modify records, interceptors may potentially get
     * the records already modified by other interceptors. However, building a pipeline of mutable interceptors that depend on the output
     * of the previous interceptor is discouraged, because of potential side-effects caused by interceptors potentially failing
     * to modify the record and throwing an exception. If one of the interceptors in the list throws an exception from onConsume(),
     * the exception is caught, logged, and the next interceptor is called with the records returned by the last successful interceptor
     * in the list, or otherwise the original consumed records.
     *
     * @param records records to be consumed by the client or records returned by the previous interceptors in the list.
     * @return records that are either modified by the interceptor or same as records passed to this method.
     */
    public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> records);

    /**
     * This is called when offsets get committed.
     * <p>
     * Any exception thrown by this method will be ignored by the caller.
     *
     * @param offsets A map of offsets by partition with associated metadata
     */
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets);

    /**
     * This is called when interceptor is closed
     */
    public void close();
}

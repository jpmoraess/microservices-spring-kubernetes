#!/usr/bin/env bash
cd /home/cleber/programs/kafka_2.11-2.3.1;
bin/zookeeper-server-start.sh config/zookeeper.properties

cd /home/cleber/programs/kafka_2.11-2.3.1;
  bin/kafka-server-start.sh config/server.properties

/home/cleber/programs/kafka_2.11-2.3.1/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic transaction
/home/cleber/programs/kafka_2.11-2.3.1/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic transaction_extorno

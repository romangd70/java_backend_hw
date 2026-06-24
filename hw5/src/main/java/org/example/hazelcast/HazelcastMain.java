package org.example.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

public class HazelcastMain {
    public static void main(String[] args) {
        System.setProperty("hazelcast.logging.type", "none");
        ClientConfig config = new ClientConfig();
        config.setClusterName("dev");
        config.getNetworkConfig().addAddress("127.0.0.1:5701");

        HazelcastInstance hazelcast = HazelcastClient.newHazelcastClient(config);
        try {
            IMap<String, String> users = hazelcast.getMap("users");

            users.put("1", "John Doe");
            users.put("2", "Jane Doe");

            System.out.println(users.get("1"));
            System.out.println(users.entrySet());
        } finally {
            hazelcast.shutdown();
        }
    }
}

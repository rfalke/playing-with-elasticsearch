import org.apache.commons.lang.builder.ToStringBuilder;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;

import java.util.Collection;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public class App {
    public static void main(String[] args) throws Exception {
        try (ClientWrapper wrapper = transportClient()) {
            simpleRequest(wrapper.client);
        }
        try (ClientWrapper wrapper = nodeClient()) {
            simpleRequest(wrapper.client);
        }
    }

    private static ClientWrapper transportClient() {
        final Client client = new TransportClient().addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        return new ClientWrapper(client) {
            @Override
            public void close() throws Exception {
                client.close();
            }
        };
    }

    private static ClientWrapper nodeClient() {
        Node node = nodeBuilder().node();
        Client client = node.client();
        return new ClientWrapper(client) {
            @Override
            public void close() throws Exception {
                node.close();
            }
        };
    }

    private static void simpleRequest(Client client) {
        Collection<IndexStats> listOfStats = client.admin().indices().stats(new IndicesStatsRequest()).actionGet().getIndices().values();
        for (IndexStats stats : listOfStats) {
            System.out.println("stats = " + ToStringBuilder.reflectionToString(stats));
        }
    }

    private static abstract class ClientWrapper implements AutoCloseable {
        private final Client client;

        protected ClientWrapper(Client client) {
            this.client = client;
        }
    }
}

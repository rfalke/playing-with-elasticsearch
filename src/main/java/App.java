import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.util.Map;

public class App {
    public static void main(String[] args) {
        connectUsingTransportClient();
    }

    private static void connectUsingTransportClient() {
        Client client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
        ActionFuture<IndicesStatsResponse> stats = client.admin().indices().stats(new IndicesStatsRequest());
        Map<String, IndexStats> indices = stats.actionGet().getIndices();
        System.out.println("indices = " + indices);
    }
}

import org.elasticsearch.action.admin.indices.stats.IndexStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.get.GetResult;
import org.elasticsearch.node.Node;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

@SuppressWarnings("UnusedDeclaration")
public class Main {
    public static void main(String[] args) throws Exception {
        try (ClientWrapper wrapper = transportClient()) {
            simpleRequest(wrapper.client);
            indexAndGetDocumentById(wrapper.client);
        }
//        try (ClientWrapper wrapper = nodeClient()) {
//            simpleRequest(wrapper.client);
//        }
    }

    private static void indexAndGetDocumentById(Client client) throws NoSuchFieldException, IllegalAccessException {
        IndexResponse indexResponse = client.prepareIndex("test-index", "testType", "someFixedId").setSource(getSale()).execute().actionGet();
        System.out.println("indexResponse = " + reflectionToString(indexResponse, SHORT_PREFIX_STYLE));
        System.out.println();

        GetResponse getResponse = client.prepareGet("test-index", "testType", "someFixedId").execute().actionGet();
        GetResult getResult = (GetResult) getField(getResponse, "getResult");
        System.out.println("getResponse = " + reflectionToString(getResult, SHORT_PREFIX_STYLE));
        System.out.println("getResponse.source = " + getResponse.getSource());
    }

    private static Object getField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    private static Map<String, Object> getSale() {
        Map<String, Object> sale = new HashMap<>();
        sale.put("price", 123.45);
        sale.put("country", "de");
        sale.put("product", "shirt");
        return sale;
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
            System.out.println("stats = " + reflectionToString(stats));
        }
    }

    private static abstract class ClientWrapper implements AutoCloseable {
        private final Client client;

        protected ClientWrapper(Client client) {
            this.client = client;
        }
    }
}

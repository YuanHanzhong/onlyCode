import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import util.Movie;

import java.io.IOException;

public class _01_EsDemo {
    
    public static void main(String[] args) throws IOException {
    
        HttpHost httpHost = new HttpHost("hadoop102", 9200);
        RestClientBuilder builder = RestClient.builder(httpHost);
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(builder);
    
        System.out.println("restHighLevelClient = " + restHighLevelClient);
    
        /**
         * 写入, 幂等, docid
         */
    
        Movie movie = new Movie(1, "Tom");
        
        IndexRequest indexRequest = new IndexRequest();
        
        // id
        indexRequest.id(movie.getId().toString());
        // data
        indexRequest.source(JSON.toJSONString(movie), XContentType.JSON);
        // indexName
        indexRequest.index("first");
    
        IndexResponse index = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    
    
        restHighLevelClient.close();
    }
}


import com.mongodb.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.xcontent.XContentType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AppPoliglota {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) throws IOException {
        // --- 1. CONEXÕES ---
        // Conexão com MongoDB (Armazenamento Principal - Tarefa Y)
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase mongoDb = mongoClient.getDatabase("cms_noticias");
        MongoCollection<Document> mongoCollection = mongoDb.getCollection("artigos");

        // Conexão com Elasticsearch (Busca - Tarefa X)
        RestHighLevelClient elasticClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));

        System.out.println(">>> Conectado aos bancos com sucesso!");

        // --- 2. CENÁRIO: JORNALISTA SALVANDO ARTIGO ---
        System.out.println("\n--- [CENÁRIO 1] Jornalista publica uma notícia ---");
        
        String titulo = "Crise na Tecnologia";
        String conteudo = "O mercado de tecnologia enfrenta desafios com a nova guerra comercial entre potências...";
        String tags = "tecnologia, economia";

        // PASSO A: Salvar no MongoDB (Source of Truth)
        Document docMongo = new Document("titulo", titulo)
                .append("conteudo", conteudo) // Conteúdo pesado fica aqui
                .append("tags", tags);
        
        mongoCollection.insertOne(docMongo);
        String idGerado = docMongo.getObjectId("_id").toString();
        System.out.println("1. Artigo salvo no MongoDB. ID: " + idGerado);

        // PASSO B: Indexar no Elasticsearch (Cópia simplificada para busca)
        Map<String, Object> docElastic = new HashMap<>();
        docElastic.put("titulo", titulo);
        docElastic.put("tags", tags);
        // Note que NÃO enviamos o "conteúdo" pesado para o Elastic, apenas o necessário para busca.
        
        IndexRequest indexRequest = new IndexRequest("noticias")
                .id(idGerado) // Usamos O MESMO ID do Mongo para manter o vínculo
                .source(docElastic, XContentType.JSON);
        
        elasticClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println("2. Artigo indexado no Elasticsearch para busca rápida.");

        // Pequena pausa para garantir que o Elastic indexou (apenas para o exemplo rodar direto)
        try { Thread.sleep(1500); } catch (InterruptedException e) {}

        // --- 3. CENÁRIO: USUÁRIO PESQUISANDO ---
        System.out.println("\n--- [CENÁRIO 2] Usuário busca por 'guerra comercial' ---");
        // O usuário buscou um termo que está no meio do texto ou tags
        String termoBusca = "guerra comercial"; // Exemplo citado no PDF

        // PASSO C: Buscar no Elasticsearch (Alta Performance)
        SearchRequest searchRequest = new SearchRequest("noticias");
        searchRequest.source().query(QueryBuilders.matchQuery("titulo", termoBusca));
        // Nota: Para simplificar o exemplo java, farei busca no título, mas o Elastic permite full-text

        SearchResponse response = elasticClient.search(searchRequest, RequestOptions.DEFAULT);
        
        System.out.println("3. Elasticsearch encontrou " + response.getHits().getTotalHits().value + " resultados.");

        for (SearchHit hit : response.getHits()) {
            String idEncontrado = hit.getId();
            float score = hit.getScore();
            System.out.println("   > Resultado (Score de Relevância: " + score + ") - ID: " + idEncontrado);

            // PASSO D: Recuperar conteúdo completo no MongoDB usando o ID
            Document artigoCompleto = mongoCollection.find(new Document("_id", new ObjectId(idEncontrado))).first();
            
            if (artigoCompleto != null) {
                System.out.println("   > [DETALHE DO MONGO]: " + artigoCompleto.getString("conteudo"));
            }
        }

        // Fechar conexões
        elasticClient.close();
        mongoClient.close();
    }
}
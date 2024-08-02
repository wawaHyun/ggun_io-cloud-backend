package store.ggun.chat.config;

import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import store.ggun.chat.domain.UserModel;
import store.ggun.chat.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import reactor.core.publisher.Mono;

import java.util.stream.IntStream;

@Configuration
public class UsersCollectionConfig {
    @Bean
    CommandLineRunner initUsers(UserRepository repo) {
        System.out.println(" >>>> 1 실행 >>>>");
        String uri = "mongodb://root:root@175.45.203.102:27017/ggundb?authSource=admin";
//        String uri = "mongodb://root:root@localhost:27017/admin";
        // Construct a ServerApi instance using the ServerApi.builder() method
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .serverApi(serverApi)
                .build();
        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            System.out.println(" DB 접속 ...");
            MongoDatabase database = mongoClient.getDatabase("ggundb");
            System.out.println(" MyDB 컬렉션 접속 ...");
            try {
                // Send a ping to confirm a successful connection
                Bson command = new BsonDocument("ping", new BsonInt64(1));
                Document commandResult = database.runCommand(command);
                System.out.println(" 핑 연 결 ...");
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
                System.out.println(commandResult);
//                boolean exists = database.listCollectionNames().into(new ArrayList<>()).contains("users");
//                if (!exists) {
//                    MongoCollection<Document> collection = database.getCollection("users");
//                    Document user = new Document()
//                            .append("firstNme", "Joe")
//                            .append("lastName", "Smith")
//                            .append("email", "joe@test.com")
//                            .append("yearsOfService", 3)
//                            .append("password", "1234")
//                            .append("skills", Arrays.asList("java", "spring", "mongodb"))
//                            .append("manager", new Document()
//                                    .append("firstName", "Sally")
//                                    .append("lastName", "Johanson"));
//                    collection.insertOne(user);
//                }
//                System.out.println(" >>>> 8 컬렉션 생성 확인 >>>>");
            } catch (MongoException me) {
                System.out.println(" >>>> 9 에러 발생 >>>>");
                System.err.println(me);
            }
        }
//                //
//                // 4.3 Find documents
//                //
//
//                Document query = new Document("lastName", "Smith");
//                var results = new ArrayList<>();
//                collection.find(query).into(results);
//
//                query = new Document("$or", Arrays.asList(
//                        new Document("lastName", "Smith"),
//                        new Document("firstName", "Joe")));
//                results = new ArrayList<>();
//                collection.find(query).into(results);
//
//                //
//                // 4.4 Update document
//                //
//
//                query = new Document(
//                        "skills",
//                        new Document(
//                                "$elemMatch",
//                                new Document("$eq", "spring")));
//                Document update = new Document(
//                        "$push",
//                        new Document("skills", "security"));
//                collection.updateMany(query, update);
//
//                //
//                // 4.5 Delete documents
//                //
//
//                query = new Document(
//                        "yearsOfService",
//                        new Document("$lt", 0));
//                // collection.deleteMany(query);
//            } catch (MongoException me) {
//                System.out.println(" >>>> 9 에러 발생 >>>>");
//                System.err.println(me);
//            }
//        }
//
        return args -> {
            IntStream.range(0, 1).forEach(i -> {
                Mono<UserModel> userMono = Mono.just(UserModel.builder()
                        .email(i + "@test.com")
                        .firstName("James" + i)
                        .lastName("Byden " + i)
                        .password("aaa")
                        .roles(null)
                        .build());

                repo.insert(userMono);
            });
        };
    }
}

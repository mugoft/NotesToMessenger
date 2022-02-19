package com.mugoft.telegram.apiwrapper;

import com.google.common.base.Strings;
import com.mugoft.notesrepos.aws.models.Note;
import com.mugoft.notesrepos.aws.models.NotesStatus;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.net.URI;
import java.net.URISyntaxException;

import static com.mugoft.LambdaHandler.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(PER_CLASS)
public abstract class AbstractDynamoDbUnitTest {

    static Note noteRecordTest;
    static Note noteRecordTest2;
    static NotesStatus notesStatusRecordTest;
    static NotesStatus notesStatusRecordTest2;

    @Container
    public static final GenericContainer dynamoDbContainer = new GenericContainer("amazon/dynamodb-local").withExposedPorts(8000);

    public static DynamoDbEnhancedClient enhancedClient;

    @BeforeAll
    public void init() throws URISyntaxException {
        var endpointUrl = String.format("http://localhost:%d", dynamoDbContainer.getFirstMappedPort());

        var uri  = new URI(endpointUrl);


        DynamoDbClient dynamoDbClient = DynamoDbClient.builder().endpointOverride(uri).build();
        var tableNameNotesRet = createTable(dynamoDbClient, tableNameNotes, Note.getNoteIdName());
        var tableNameNotesStatusRet = createTable(dynamoDbClient, tableNameNotesStatus, NotesStatus.getChatIdName(), NotesStatus.getNoteIdName());
        Assertions.assertEquals(tableNameNotesRet, tableNameNotes);
        Assertions.assertEquals(tableNameNotesStatusRet, tableNameNotesStatus);

        enhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();

        DynamoDbTable<Note> table = enhancedClient.table(tableNameNotes, TableSchema.fromBean(Note.class));

        // Populate the Table
        noteRecordTest = Note.builder().withNoteId(1535693932765L).withMod(1535693932L).withQuestion("QuestionTest").withAnswer("QuestionAnswer").build();

        notesStatusRecordTest = new NotesStatus();
        notesStatusRecordTest.setChat_id(chatIdQuestions);
        notesStatusRecordTest.setLast_asked_time(0L);
        notesStatusRecordTest.setNote_id(noteRecordTest.getNote_id());


        noteRecordTest2 = Note.builder().withNoteId(1601788885930L).withMod(1601788885L)
                .withQuestion("Please name the difference between Imperative programming vs Declarative")
                .withAnswer("Императивный подход - ты рассказываешь машине, как решить задачу. Программируя императивно, мы описываем конкретные шаги, действия и точный порядок, в котором их нужно исполнять Декларативный подход - ты рассказываешь машине, какой результат от нее хочешь. Примеры декларативных языков - html, css, sql, конфиг nginx. Sql - запрос, в котором ты описываешь, какими свойствами обладают данные, которые ты хочешь получить. А как ищутся и сортируются эти данные, ты не пишешь и грубо говоря тебя это не интересует.")
                .build();

        notesStatusRecordTest2 = new NotesStatus();
        notesStatusRecordTest2.setChat_id(chatIdAnswers);
        notesStatusRecordTest2.setLast_asked_time(0L);
        notesStatusRecordTest2.setNote_id(noteRecordTest2.getNote_id());


//        Create a BatchWriteItemEnhancedRequest object
        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest =
                BatchWriteItemEnhancedRequest.builder()
                        .writeBatches(
                                WriteBatch.builder(Note.class)
                                        .mappedTableResource(table)
                                        .addPutItem(r -> r.item(noteRecordTest))
                                        .addPutItem(r -> r.item(noteRecordTest2))
                                        .build())
                        .build();

        // Add these two items to the table
        enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);

        DynamoDbTable<NotesStatus> tableNotesStatus = enhancedClient.table(tableNameNotesStatus, TableSchema.fromBean(NotesStatus.class));

        batchWriteItemEnhancedRequest =
                BatchWriteItemEnhancedRequest.builder()
                        .writeBatches(
                                WriteBatch.builder(NotesStatus.class)
                                        .mappedTableResource(tableNotesStatus)
                                        .addPutItem(r -> r.item(notesStatusRecordTest))
                                        .addPutItem(r -> r.item(notesStatusRecordTest2))
                                        .build())
                        .build();

        // Add these two items to the table
        enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
    }

    public static String createTable(DynamoDbClient ddb, String tableName, String partitionKey) {
        return createTable(ddb, tableName, partitionKey, null);
    }

    public static String createTable(DynamoDbClient ddb, String tableName, String partitionKey, String sortKey) {
        DynamoDbWaiter dbWaiter = ddb.waiter();

        CreateTableRequest.Builder builder;
        if (!Strings.isNullOrEmpty(sortKey)) {
            builder = CreateTableRequest.builder()
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName(partitionKey)
                                    .attributeType(ScalarAttributeType.N).build(),
                            AttributeDefinition.builder()
                                    .attributeName(sortKey)
                                    .attributeType(ScalarAttributeType.N).build())
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName(partitionKey)
                                    .keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder()
                                    .attributeName(sortKey)
                                    .keyType(KeyType.RANGE).build());
        } else {
            builder = CreateTableRequest.builder()
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName(partitionKey)
                            .attributeType(ScalarAttributeType.N)
                            .build())
                    .keySchema(KeySchemaElement.builder()
                            .attributeName(partitionKey)
                            .keyType(KeyType.HASH)
                            .build());
        }

        var createTableRequest = builder.provisionedThroughput(ProvisionedThroughput.builder()
                .readCapacityUnits(Long.valueOf(10))
                .writeCapacityUnits(Long.valueOf(10))
                .build())
                .tableName(tableName)
                .build();

        String newTable = "";
        try {
            CreateTableResponse response = ddb.createTable(createTableRequest);
            DescribeTableRequest tableRequest = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();

            // Wait until the Amazon DynamoDB table is created
            WaiterResponse<DescribeTableResponse> waiterResponse =  dbWaiter.waitUntilTableExists(tableRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);

            newTable = response.tableDescription().tableName();
            return newTable;
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }
}

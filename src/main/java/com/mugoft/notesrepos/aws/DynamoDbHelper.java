package com.mugoft.notesrepos.aws;


import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import com.mugoft.notesrepos.aws.models.Note;
import com.mugoft.notesrepos.aws.models.NotesStatus;
import com.mugoft.messengers.telegram.apiwrapper.MessageSenderBot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

/**
 * @author mugoft
 * @created 10/09/2021 - 20:46
 * @project NotesToMessenger
 */
public class DynamoDbHelper {
    private static final Logger logger = LogManager.getLogger(MessageSenderBot.class);

    public Map<String, AttributeValue> getItem(DynamoDbClient client, String tableName, Map<String, AttributeValue> keyToGet) {
        Map<String, AttributeValue> item = new HashMap<>();

        try {
            GetItemRequest request = GetItemRequest.builder()
                    .key(keyToGet)
                    .tableName(tableName)
                    .build();

            item = client.getItem(request).item();
        } catch (Exception e) {
            logger.error("Get item request failed for table=" + tableName + keyToGet + e);
        }

        return item;
    }

    public String getNSValueFromAttributeValue(AttributeValue value) {
        if (!Strings.isNullOrEmpty(value.n())) {
            return value.n();
        } else if (!Strings.isNullOrEmpty(value.s())) {
            return value.s();
        } else {
            return null;
        }
    }

    //TODO: we should find notes with the mininmum time. Currently returns only with 0
    public static NotesStatus getNotesStatusAskedTimeMin(DynamoDbEnhancedClient enhancedClient, Long chatId, String tableName) {

        NotesStatus notesStatusRet = null;
        try {
            DynamoDbTable<NotesStatus> mappedTable = enhancedClient.table(tableName, TableSchema.fromBean(NotesStatus.class));

            AttributeValue att = AttributeValue.builder()
                    .n("0")
                    .build();

            Map<String, AttributeValue> expressionValues = new HashMap<>();
            expressionValues.put(":value", att);

            Expression expression = Expression.builder()
                    .expression("last_asked_time = :value")
                    .expressionValues(expressionValues)
                    .build();

            // Create a QueryConditional object that is used in the query operation.
            QueryConditional queryConditional = QueryConditional
                    .keyEqualTo(Key.builder().partitionValue(chatId)
                            .build());

            // Get not asked questions.
            Iterator<NotesStatus> results = mappedTable.query(r -> r.queryConditional(queryConditional).filterExpression(expression)).items().iterator();

            var notesStatus = Streams.stream(results).min(new Comparator<NotesStatus>() {
                @Override
                public int compare(NotesStatus o1, NotesStatus o2) {
                    return o1.getNote_id() < o2.getNote_id() ? -1 : 1;
                }
            });

            if (notesStatus.isPresent()) {

                notesStatusRet = notesStatus.get();
            }

        } catch (DynamoDbException e) {
            logger.error(e.getMessage());
        }

        return notesStatusRet;
    }

    public static Note getNote(DynamoDbEnhancedClient enhancedClient, String tableName, Long noteId) {
        try {
            //Create a DynamoDbTable object
            DynamoDbTable<Note> mappedTable = enhancedClient.table(tableName, TableSchema.fromBean(Note.class));

            //Create a KEY object
            Key key = Key.builder()
                    .partitionValue(noteId)
                    .build();

            // Get the item by using the key
            Note result = mappedTable.getItem(r -> r.key(key));
            return result;

        } catch (DynamoDbException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    public static void deleteDymamoDBItem(DynamoDbClient ddb, String tableName, String key,  AttributeValue value) {

        HashMap<String, AttributeValue> keyToGet =
                new HashMap<String, AttributeValue>();

        keyToGet.put(key, value);

        DeleteItemRequest deleteReq = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(keyToGet)
                .build();


        ddb.deleteItem(deleteReq);
    }

    public static NotesStatus updateNotesStatus(DynamoDbEnhancedClient enhancedClient, String tableName, NotesStatus noteStatusUpdated) {
        try {
            //Create a DynamoDbTable object
            DynamoDbTable<NotesStatus> mappedTable = enhancedClient.table(tableName, TableSchema.fromBean(NotesStatus.class));

//            //Create a KEY object
//            Key key = Key.builder()
//                    .partitionValue(noteStatusUpdated.getChat_id())
//                    .sortValue(noteStatusUpdated.getNote_id())
//                    .build();
//
//            NotesStatus notesStatus = mappedTable.getItem(r->r.key(key));
//            notesStatus.setLast_asked_time(noteStatusUpdated.getLast_asked_time());

            return mappedTable.updateItem(noteStatusUpdated);
        } catch (DynamoDbException e) {
            logger.error(e.getMessage());
        }

        return null;
    }


}

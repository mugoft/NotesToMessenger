package com.mugoft;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mugoft.notesrepos.aws.DynamoDbHelper;
import com.mugoft.messengers.telegram.apiwrapper.MessageSenderBot;
import com.mugoft.notesrepos.aws.ParameterStoreHelper;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author mugoft
 * @created 12/09/2021 - 21:47
 * @project NotesToMessenger
 */
public class LambdaHandler implements RequestHandler<Map<String,String>, String> {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static final String TABLE_NAME_NOTES = "notes";
    public static final String TABLE_NAME_NOTES_STATUS = "notes_status";
    public static final Long CHAT_ID_CHANNEL_QUESTIONS = 1001566093710L;
    public static final Long CHAT_ID_GROUP_ANSWERS = 1001401441309L;

    private static final String API_TOKEN_MUGOFT_BOT_QUESTIONS_KEY = "API_TOKEN_MUGOFT_BOT_QUESTIONS";
    private static final String API_TOKEN_MUGOFT_BOT_ANSWERS_KEY = " API_TOKEN_MUGOFT_BOT_ANSWERS";

    private static final String BAD_REQUEST = "404";

    @Override
    public String handleRequest(Map<String,String> event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        String response = "200 OK";
        // log execution details
        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        logger.log("CONTEXT: "  + gson.toJson(context));

        String apiTokenMugoftBotQuestions = ParameterStoreHelper.getParameter(API_TOKEN_MUGOFT_BOT_QUESTIONS_KEY);
        String apiTokenMugoftBotAnswers = ParameterStoreHelper.getParameter(API_TOKEN_MUGOFT_BOT_ANSWERS_KEY);

        try (DynamoDbClient client = DynamoDbClient.builder().build()) {
            DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(client)
                    .build();

            // find for long time note asked note
            var notesStatus = DynamoDbHelper.getNotesStatusAskedTimeMin(enhancedClient, CHAT_ID_CHANNEL_QUESTIONS, TABLE_NAME_NOTES_STATUS);

            if(notesStatus == null) {
                logger.log("No notes status for channel found");
                return BAD_REQUEST;
            }

            logger.log("Notes status found:" + notesStatus);

            // retreive this note
            var note = DynamoDbHelper.getNote(enhancedClient, TABLE_NAME_NOTES, notesStatus.getNote_id());
            if(note == null) {
                logger.log("No notes for channel found");
                return BAD_REQUEST;
            }

            logger.log("Note found: " + note);

            // send the question from the note to the channel
            MessageSenderBot sender = new MessageSenderBot(CHAT_ID_CHANNEL_QUESTIONS, apiTokenMugoftBotQuestions);
            var msgResponse = sender.sendMessage(note.getQuestion(), null);
            if(msgResponse == null || msgResponse.messageId() == null) {
                logger.log("Message not sent to telegram");
                return BAD_REQUEST;
            }

            // update last asked time for this note to the current one
            Long time = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
            notesStatus.setLast_asked_time(time);

            var notesStatusUpdated = DynamoDbHelper.updateNotesStatus(enhancedClient, TABLE_NAME_NOTES_STATUS, notesStatus);
            if(notesStatusUpdated == null || !notesStatusUpdated.getLast_asked_time().equals(time)) {
                logger.log("Notes status is not updated: " + notesStatusUpdated);
            } else {
                logger.log("Updated notes status: " + notesStatusUpdated);
            }

            // find this question in the the discussion group
            final int N_ATTEMPTS_MAX = 5;
            int curAttempt = 0;

            List<Message> messages = null;
            while (curAttempt++ < N_ATTEMPTS_MAX && (messages == null || messages.isEmpty())) {
                //TODO: find only one message by ID
                logger.log("Getting messages from discussion chat, attempt=" + curAttempt);
                try {
                    Thread.sleep(3000);
                } catch (Exception ex) {
                    logger.log("Interrupted exception: " + ex);
                }
                sender = new MessageSenderBot(CHAT_ID_GROUP_ANSWERS, apiTokenMugoftBotAnswers);
                var updateResponse = sender.getUpdate();

                messages = updateResponse.updates().stream().map(Update::message).filter(msg -> msg.text().equals(note.getQuestion())).collect(Collectors.toList());
            }

            if(messages == null || messages.isEmpty()) {
                logger.log("No messages in discussion chat are found");
                return BAD_REQUEST;
            }

            logger.log("Following messages are found: " +  messages);

            // if multiple messages found, get with the highest ID
            var message = messages.stream().max(Comparator.comparing(Message::messageId));

            // reply to this message with the answer. By this you add comment to the main question channel.
            var msgReplyResponse = sender.sendMessage(note.getAnswer(), message.get().messageId());
            logger.log("Replying to the messageId " + message.get().messageId());

            if(msgReplyResponse != null && msgReplyResponse.replyToMessage() != null && msgReplyResponse.replyToMessage().messageId().equals(message.get().messageId())) {
                logger.log("Success");
            }
        }

        logger.log("EVENT: " + gson.toJson(event));
        logger.log("EVENT TYPE: " + event.getClass());
        return response;
    }
}

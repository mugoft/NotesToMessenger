package com.mugoft;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.common.base.Strings;
import com.mugoft.notesrepos.aws.DynamoDbHelper;
import com.mugoft.messengers.telegram.apiwrapper.MessageSenderBot;
import com.mugoft.notesrepos.aws.ParameterStoreHelper;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.SdkHttpClient;
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
public class LambdaHandler implements RequestHandler<Object, String> {
    public static final String TABLE_NAME_NOTES_ENV_KEY = "NotesTableName";
    public static final String TABLE_NAME_NOTES_STATUS_ENV_KEY = "NotesStatusTableName";
    public static final String CHAT_ID_QUESTIONS_ENV_KEY = "ChatIdQuestions";
    public static final String CHAT_ID_ANSWERS_ENV_KEY = "ChatIdAnswers";

    private static final String API_TOKEN_MUGOFT_BOT_QUESTIONS_KEY_FOR_KEY = "ApiTokenMugoftBotQuestionsKey";
    private static final String API_TOKEN_MUGOFT_BOT_ANSWERS_KEY_FOR_KEY = "ApiTokenMugoftBotAnswersKey";

    private static final String BAD_REQUEST = "404";

    public static final String tableNameNotes = getEnvironmentVarSafe(TABLE_NAME_NOTES_ENV_KEY);
    public static final String tableNameNotesStatus = getEnvironmentVarSafe(TABLE_NAME_NOTES_STATUS_ENV_KEY);
    public static final Long chatIdQuestions = Long.valueOf(getEnvironmentVarSafe(CHAT_ID_QUESTIONS_ENV_KEY));
    public static final Long chatIdAnswers = Long.valueOf(getEnvironmentVarSafe(CHAT_ID_ANSWERS_ENV_KEY));
    public static final String apiTokenMugoftBotQuestionsKey = getEnvironmentVarSafe(API_TOKEN_MUGOFT_BOT_QUESTIONS_KEY_FOR_KEY);
    public static final String apiTokenMugoftBotAnswersKey = getEnvironmentVarSafe(API_TOKEN_MUGOFT_BOT_ANSWERS_KEY_FOR_KEY);

    private static String getEnvironmentVarSafe(String key) {
        String val = System.getenv(key);
        if(!Strings.isNullOrEmpty(val)) {
            return val;
        } else if(!Strings.isNullOrEmpty(val = System.getProperty(key))) {
            return val;
        } else {
            System.out.println("No env. and property  found for key " + key);
            return null;
        }
    }

    public final String apiTokenMugoftBotQuestions;
    public final String apiTokenMugoftBotAnswers;

    public LambdaHandler() {
        SdkHttpClient httpClient = ApacheHttpClient.builder().build();
        apiTokenMugoftBotQuestions = ParameterStoreHelper.getParameter(apiTokenMugoftBotQuestionsKey, httpClient);
        apiTokenMugoftBotAnswers = ParameterStoreHelper.getParameter(apiTokenMugoftBotAnswersKey, httpClient);
    }

    @Override
    public String handleRequest(Object event, Context context)
    {
        LambdaLogger logger = context.getLogger();
        String response = "200 OK";


        return response;
    }
}

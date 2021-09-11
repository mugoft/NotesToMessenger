package com.mugoft.messengers.telegram.apiwrapper;


import com.google.gson.Gson;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

/**
 * @author mugoft
 * @created 09/09/2021 - 17:36
 * @project NotesToMessenger
 */
public class MessageSenderBot {
    private static final Logger logger = LogManager.getLogger(MessageSenderBot.class);

    private String urlStringMain = "https://api.telegram.org/";

    private final Long groupId;
    private final String apiToken;
    public MessageSenderBot(Long groupId, String apiToken) {
        this.groupId = groupId;
        this.apiToken = apiToken;
    }

    public Message sendMessage(String message, Integer replyId) {

        StringBuilder uri = null;
        try {
            var uriSendBuilder = new URIBuilder(urlStringMain);
            uriSendBuilder.setPathSegments(String.format("bot%s", apiToken), "sendMessage");
            uriSendBuilder.addParameter("chat_id", "-" + groupId);
            uriSendBuilder.addParameter("text", message);
            if(replyId != null) {
                uriSendBuilder.addParameter("reply_to_message_id", String.valueOf(replyId));
            }

            uri = new StringBuilder(uriSendBuilder.build().toString());
        } catch (URISyntaxException ex) {
            logger.fatal("URI wrongly formatter", ex);
            return null;
        }

        HttpClient clientGet = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(uri.toString());
        try {
            HttpResponse response = clientGet.execute(getRequest);
            InputStream is = response.getEntity().getContent();
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            SendResponse sendResponse = gson.fromJson(text, SendResponse.class);
            return sendResponse.message();
        } catch (Exception e) {
            logger.error("Error during sending request", e);
            return null;
        }
    }

    public GetUpdatesResponse getUpdate() {
        StringBuilder uri = null;
        try {
            var uriSendBuilder = new URIBuilder(urlStringMain);
            uriSendBuilder.setPathSegments(String.format("bot%s", apiToken), "getUpdates");
            uriSendBuilder.addParameter("chat_id", "-" + groupId);
            uriSendBuilder.addParameter("allowed_updates", "[\"message\"]");

            uri = new StringBuilder(uriSendBuilder.build().toString());
        } catch (URISyntaxException ex) {
            logger.fatal("URI wrongly formatter", ex);
            return null;
        }

        HttpClient clientGet = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(uri.toString());
        try {
            HttpResponse response = clientGet.execute(getRequest);
            InputStream is = response.getEntity().getContent();
            String text = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            var updatesResponse = gson.fromJson(text, GetUpdatesResponse.class);
            return updatesResponse;
        } catch (Exception e) {
            logger.error("Error during sending request", e);
            return null;
        }
    }
}

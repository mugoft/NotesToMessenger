package com.mugoft.notesrepos.aws;

import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

public class ParameterStoreHelper {
    private static final String PARAM_PREFIX = "/PublishNotesToTelegram/";

    public static String getParameter(String key) {
        String valueRet = "";

        String paramName = PARAM_PREFIX + key;

        SsmClient ssmClient = SsmClient.builder().build();

        GetParameterRequest parameterRequest = GetParameterRequest.builder()
                .name(paramName)
                .build();

        GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);
        valueRet = parameterResponse.parameter().value();


        return valueRet;
    }
}

package com.zzyl;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ResponseFormatJsonObject;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

public class QianfanAIModelTest {
    public static void main(String[] args) {
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey("bce-v3/ALTAK-7Pj5D06PLDQGqESNOSznF/ea6d9af68b3ead9f8c0112c4dcbeabe5f029f7a2")
                .baseUrl("https://qianfan.baidubce.com/v2/")
                .build();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage("你好")
                .model("deepseek-r1")
                .responseFormat(ChatCompletionCreateParams.ResponseFormat.ofJsonObject(
                        ResponseFormatJsonObject.builder().build()))
                .build();

        ChatCompletion chatCompletion = client.chat().completions().create(params);
        System.out.println(chatCompletion.choices().get(0).message().content().orElseGet(() -> ""));
    }
}

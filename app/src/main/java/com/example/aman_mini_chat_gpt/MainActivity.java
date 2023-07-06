package com.example.aman_mini_chat_gpt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEdittext;
    ImageButton sendButton;
    MessageAdapter messageAdapter;
    List<MessageModel> messageModelList;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageModelList=new ArrayList<>();
        recyclerView=findViewById(R.id.recycler_view);
        welcomeTextView=findViewById(R.id.welcome_text);
        messageEdittext=findViewById(R.id.message_edit_text);
        sendButton=findViewById(R.id.send_btn);
        // set up the recycler view
        messageAdapter=new MessageAdapter(messageModelList);
        recyclerView.setAdapter(messageAdapter);
        // we are using it bcuz we want to scroll from bottom to top otherwise it will be reversed
        LinearLayoutManager llm=new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        sendButton.setOnClickListener((v)->{
            String question=messageEdittext.getText().toString().trim();
            addToChat(question,MessageModel.SENT_BY_ME);
            messageEdittext.setText("");
            callApi(question);
            welcomeTextView.setVisibility(View.GONE);
        });

    }

    void addToChat(String message,String sentby)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //this operation will change the ui so it is run under ui thread
                messageModelList.add(new MessageModel(message,sentby));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });

    }
    void addResponse(String response)
    {
        messageModelList.remove(messageModelList.size()-1);
        addToChat(response,MessageModel.SENT_BY_BOT);
    }
    void callApi(String question)
    {
        messageModelList.add(new MessageModel("Typing....",MessageModel.SENT_BY_BOT));
        //do the okhttp setup and call the api from here
        JSONObject jsonBody=new JSONObject();
        try {
            jsonBody.put("model","text-davinci-003");
            jsonBody.put("prompt",question);
            jsonBody.put("max_tokens",4000);
            jsonBody.put("temperature",0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body=RequestBody.create(jsonBody.toString(),JSON);
        Request request=new Request.Builder()

                .url("https://api.openai.com/v1/completions")
                .header("Authorization","Bearer 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")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load the message due to"+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful())
                {
                    JSONObject jsonObject= null;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray=jsonObject.getJSONArray("choices");
                        String result=jsonArray.getJSONObject(0).getString("text");
                        addResponse(result.trim());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else
                {
                    addResponse("Failed to load the message due to "+ response.body().toString());
                }
            }
        });

    }


}
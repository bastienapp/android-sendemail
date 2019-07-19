package fr.wildcodeschool.sendemail;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.Consumer;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.hash.Hashing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String MAILJET_API = "https://api.mailjet.com/v3.1/send";
    private static final String MAILJET_KEY = "1c6ce81ec5da8d174d2d428ced068646:0f9ec6c3078bc29f16bee459a292dca5";
    private static final String MAILJET_USER = "bastien@wildcodeschool.fr";

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        Button btSendMail = findViewById(R.id.btSendMail);
        btSendMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User user = new User("bastien@wildcodeschool.fr", "tacostacos");
                String newPassword = generateRandomString();

                // TODO : save modified user into database
                user.setPassword(newPassword);

                sendMail(user, new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) {
                        Toast.makeText(context, "Success : " + success, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void sendMail(User user, final Consumer<Boolean> listener) {

        // TODO : remove this comment from release
        /*if (BuildConfig.DEBUG) {
            // if the application is in debug variant, no email is sent
            return;
        }*/
        try {
            final JSONObject jsonBody = new JSONObject();
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();

            JSONObject from = new JSONObject();
            from.put("Name", context.getString(R.string.email_from));
            from.put("Email", MAILJET_USER);
            message.put("From", from);

            JSONArray tos = new JSONArray();
            JSONObject to = new JSONObject();
            to.put("Name", "");
            to.put("Email", user.getEmail());
            tos.put(to);
            message.put("To", tos);
            message.put("Subject", context.getString(R.string.email_suject));
            String htmlContent = context.getString(R.string.html_email)
                    .replace("{password}", user.getPassword());
            message.put("HTMLPart", htmlContent);

            messages.put(message);
            jsonBody.put("Messages", messages);
            final String requestBody = jsonBody.toString();

            RequestQueue requestQueue = Volley.newRequestQueue(context);
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, MAILJET_API,
                    null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    listener.accept(true);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.accept(false);
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    String auth = "Basic "
                            + Base64.encodeToString(MAILJET_KEY.getBytes(),
                            Base64.NO_WRAP);
                    headers.put("Authorization", auth);
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    try {
                        return requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.e("Unsupported Encoding while trying to get the bytes of %s using %s",
                                requestBody, "utf-8");
                        return null;
                    }
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String generateRandomString() {
        return Hashing.sha256().
                hashString("" + System.currentTimeMillis(), Charset.defaultCharset())
                .toString()
                .substring(0, 8);
    }
}

package com.mixey69.roulettevideochat;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

class WebServiceCoordinator {

    private static final String LOG_TAG = WebServiceCoordinator.class.getSimpleName();

    private final Context context;
    private Listener delegate;

    WebServiceCoordinator(Context context, Listener delegate) {

        this.context = context;
        this.delegate = delegate;
    }

    void fetchSessionConnectionData(String sessionInfoUrlEndpoint) {

        RequestQueue reqQueue = Volley.newRequestQueue(context);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET, sessionInfoUrlEndpoint,
                                            null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String apiKey = context.getResources().getString(R.string.API_KEY);
                    String sessionId = response.getString("sessionID");
                    String token = response.getString("token");
                    Boolean isRoomToConnectEmpty = response.getBoolean("isRoomToConnectEmpty");
                    Log.i(LOG_TAG, "WebServiceCoordinator returned session information");

                    delegate.onSessionConnectionDataReady(apiKey, sessionId, token, isRoomToConnectEmpty);

                } catch (JSONException e) {
                    delegate.onWebServiceCoordinatorError(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                delegate.onWebServiceCoordinatorError(error);
            }
        }));
    }

    interface Listener {

        void onSessionConnectionDataReady(String apiKey, String sessionId, String token, Boolean isRoomToConnectEmpty);
        void onWebServiceCoordinatorError(Exception error);
    }
}


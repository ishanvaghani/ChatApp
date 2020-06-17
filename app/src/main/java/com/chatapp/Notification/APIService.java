package com.chatapp.Notification;

import com.chatapp.Notification.MyResponse;
import com.chatapp.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA_3uS530:APA91bH_Fe-5v_iKWKOzvm66NbvhpYWcH1YT7XDRADe4K3P0voYD1loGJkHzBDaGGw0SQ3FS8b7QJlUE8v4cq2JeahDQR9PfAX071J3-mS2_fK3IN5ZeeyRUE0MLgZSjDMhHwF9QKGj9"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}

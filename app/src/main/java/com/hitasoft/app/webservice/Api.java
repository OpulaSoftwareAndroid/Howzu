package com.hitasoft.app.webservice;


import com.hitasoft.app.model.LikedPeopleModel;
import com.hitasoft.app.model.MatchMackerDetailModel;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

@SuppressWarnings("All")
public interface Api {
    @Multipart
    @POST("api/getlike")
    Call<LikedPeopleModel> getLikedPerson(@Part("register_id") RequestBody registerId);


    @Multipart
    @POST("api/getprofile")
    Call<LikedPeopleModel> getFindPeople(
            @Header("Authorization")
            @Part("register_id") RequestBody registerId);

    @Multipart
    @POST("api/girlprofiledetail")
    Call<MatchMackerDetailModel> getgirlprofiledetail(@Part("register_id") RequestBody registerId,
                                                      @Part("friend_id") RequestBody friendId);


}

package com.hitasoft.app.howzu;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hitasoft.app.model.LikedPeopleModel;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.webservice.RestClient;
import com.squareup.picasso.Picasso;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.ArrayList;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LikedFragment extends Fragment implements DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder> {
    String TAG = "LikedFragment";
    DiscreteScrollView itemPicker;
    ArrayList<LikedPeopleModel.Info> peopleList;
    InfiniteScrollAdapter infiniteAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        return inflater.inflate(R.layout.home_liked_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MainScreenActivity.setToolBar(getActivity(), "liked");

        itemPicker = getView().findViewById(R.id.item_picker);
        itemPicker.setOrientation(DSVOrientation.HORIZONTAL);
        itemPicker.addOnItemChangedListener(this);
        itemPicker.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.9f)
                .build());

        getLikedPeople();
    }

    public void getLikedPeople() {
        if (CommonFunctions.isNetwork(Objects.requireNonNull(getActivity()))) {
            CommonFunctions.showProgressDialog(getContext());
            RequestBody aregId = RequestBody.create(MediaType.parse("text/plain"), GetSet.getUserId());
            new RestClient(getContext()).getInstance().get().getLikedPerson(aregId).enqueue(new Callback<LikedPeopleModel>() {
                @Override
                public void onResponse(Call<LikedPeopleModel> call, Response<LikedPeopleModel> response) {
                    CommonFunctions.hideProgressDialog(getContext());
                    Log.d(TAG,"jigar the json like profile response  in this is "+response.body().toString());

                    if (response.body() != null) {
                        if (response.body().getStatus() == 1) {
                            peopleList = (ArrayList<LikedPeopleModel.Info>) response.body().getInfo();
                            infiniteAdapter = InfiniteScrollAdapter.wrap(new LikedPeopleAdapter(peopleList));
                            itemPicker.setAdapter(infiniteAdapter);
                        } else {
                            Toast.makeText(getContext(), response.body().getMsg(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getContext(), getString(R.string.network_time_out_error), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LikedPeopleModel> call, Throwable t) {
                    try {
                        CommonFunctions.hideProgressDialog(getContext());
                        Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.d(TAG,"jigar the error in this is "+t);
                        e.printStackTrace();
                    }
                }

            });
        } else {
            Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {

    }

    class LikedPeopleAdapter extends RecyclerView.Adapter<LikedPeopleAdapter.ViewHolder> {

        private ArrayList<LikedPeopleModel.Info> data;

        public LikedPeopleAdapter(ArrayList<LikedPeopleModel.Info> data) {
            this.data = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.item_liked_poeple, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LikedPeopleModel.Info model = data.get(position);

//            if (data.get(position)
//                    .getImage()
//                    .equalsIgnoreCase("http://www.ilovemisskey.com/uploads/user/"))
            {
                Picasso.with(getActivity())
                        .load(data.get(position).getImage())
                        .error(R.drawable.user_placeholder)
                        .centerCrop()
                        .fit().centerCrop()
                        .into(holder.imageViewProfileImage);
//                Glide.with(getActivity()).load()
//                        .listener(new RequestListener<String, GlideDrawable>() {
//                            @Override
//                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                                holder.user_name.setTextColor(getResources().getColor(R.color.black));
//                                holder.bio.setTextColor(getResources().getColor(R.color.black));
//                                return false;
//                            }
//
//                            @Override
//                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                                return false;
//                            }
//                        }).placeholder(R.drawable.user_placeholder)
//                        .into(holder.imageViewProfileImage);
            }
//            else

                {

//                Glide.with(getActivity()).load(data.get(position).getImage())
//                        .listener(new RequestListener<String, GlideDrawable>() {
//                            @Override
//                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
//                                return false;
//                            }
//
//                            @Override
//                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                                return false;
//                            }
//                        })
//                        .into(holder.imageViewProfileImage);
            }
            System.out.println("jigar the friend id is "+data.get(position).getMatchMaker());
            System.out.println("jigar the user id like in liked list token is "+data.get(position).getUserIdLikeToken());

          //  if(data.get(position).getMatchMaker().equals("0"))
                if(data.get(position).getMatchMaker()==0.0)
            {
                holder.imageViewLikeMatchMaker.setVisibility(View.GONE);
            }else
            {
                holder.imageViewLikeMatchMaker.setVisibility(View.VISIBLE);

            }
            holder.user_name.setText(model.getName());

            holder.imageViewLikeMatchMaker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("jigar the friend id is "+data.get(position).getMatchMaker());
                    GetSet.setFriendId(String.valueOf(data.get(position).getMatchMaker()));
                    switchContent(new MatchMakerFragment());
                }
            });

            //---------------------------------------------------------------------------------------------------------------------------------
//---------------------------------Commented until data of user id from GET PROFILE come with data of token ---------------------------------------------------------------
            holder.imageViewProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent p = new Intent(getActivity(), MainViewProfileDetailActivity.class);
                    p.putExtra("from", "home");
                    p.putExtra(Constants.TAG_FRIEND_ID, String.valueOf(GetSet.getUserId()));
                    p.putExtra(Constants.TAG_PROFILE_VISITOR_ID_LIKE_TOKEN,String.valueOf(data.get(position).getUserIdLikeToken()));
                    p.putExtra(Constants.TAG_REGISTERED_ID, String.valueOf(data.get(position).getRegisterId()));
                    startActivity(p);

                    //
//                    System.out.println("jigar the clicked user id is "+GetSet.getUserId());
//                    //                    userId = getIntent().getExtras().getString("strFriendID");
//                    System.out.println("jigar the clicked friend id is "+data.get(position).getRegisterId());
//                    System.out.println("jigar the clicked friend like token id is "+data.get(position).getUserIdLikeToken());
//                    System.out.println("jigar the clicked user like token id is "+data.get(position).getUserIdLikeToken());
                }
            });

        }
        public void switchContent(Fragment fragment) {
            try {
               Fragment mContent = fragment;
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack("back")
                        .replace(R.id.content_frame, fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commitAllowingStateLoss();
            } catch (IllegalStateException e) {
                System.out.println("jigar the ex");
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView imageViewProfileImage;
            TextView user_name, bio;
            ImageView imageViewLikeMatchMaker;

            public ViewHolder(View itemView) {
                super(itemView);
                imageViewProfileImage = itemView.findViewById(R.id.imageViewProfileImage);
                user_name = itemView.findViewById(R.id.user_name);
                bio = itemView.findViewById(R.id.textViewProfileLocation);
                imageViewLikeMatchMaker=itemView.findViewById(R.id.imageViewLikeMatchMaker);

            }
        }
    }
}

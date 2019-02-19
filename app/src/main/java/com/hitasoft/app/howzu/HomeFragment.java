package com.hitasoft.app.howzu;// Created by Hitasoft on 2/17/2015.

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
//import com.baidu.mapapi.map.BaiduMap;
import com.github.tommykw.tagview.DataTransform;
import com.github.tommykw.tagview.TagView;
import com.hitasoft.app.customclass.RippleBackground;
import com.hitasoft.app.swipecards.SwipeFlingAdapterView;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.DefensiveClass;
import com.hitasoft.app.utils.GetSet;
import com.hitasoft.app.utils.Item;
import com.squareup.picasso.Picasso;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements View.OnClickListener
//        , BDLocationListener
{
    String TAG = "Home";

    public static ImageView userImage;
    public static RippleBackground rippleBackground;
    public static RelativeLayout linearLayoutSwipeProfileCard;
    public static SwipeFlingAdapterView flingContainer;
    public static ImageView reload,match,customer_care,unmatch,comments;
    Display display;
    LinearLayout nullLay;
    TextView changeLocation;
    public static ArrayList<HashMap<String, String>> peoplesAry = new ArrayList<HashMap<String, String>>();
    public static int screenWidth, screenHeight;
    public static PeopleAdapter peopleAdapter;
    public static Handler handler = new Handler();
    public static Context context;
    HashMap<String, String> temp = new HashMap<String, String>();
    public static String removedUserId = "", declineId = "";
    public static boolean loadmore = false, undoClicked = true, premiumClicked = false, decline = false;
    HashMap<String, String> lastSwipedUser = new HashMap<String, String>();
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    List<Item> arrayListComments = new ArrayList<>();
    Animation slideUp;
    public  LinearLayout container;
    private static String[] labels = {"Hello Bubble", "This is bubble view", "Android", "Github", "Jared", "Bubble with different size", "Yo"};
    boolean isClickedCommentFirst=false;
    ImageView open, close;
    LinearLayout myView;
    LinearLayout lin_open;


    //Slider
    ImageView imageViewShowComments,imageViewAddComments,imageViewMatchMakerProfile;
    TagView<Item> tags;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "home_main_layout: onCreateView");
        return inflater.inflate(R.layout.home_main_layout, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainScreenActivity.setToolBar(getActivity(), "home");

        userImage = getView().findViewById(R.id.user_image);
        rippleBackground = getView().findViewById(R.id.ripple_background);

        rippleBackground.startRippleAnimation();
        linearLayoutSwipeProfileCard = getView().findViewById(R.id.linearLayoutSwipeProfileCard);
        flingContainer = getView().findViewById(R.id.cardView);





        imageViewMatchMakerProfile=getView().findViewById(R.id.imageViewMatchMakerProfile);
        //        BubbleLayout layout = (BubbleLayout) getView().findViewById(R.id.bubble_layout);
//
//        for (String label : labels) {
//            BubbleView bubbleView = new BubbleView(getActivity());
//            bubbleView.setText(label);
//            bubbleView.setGravity(Gravity.CENTER);
//            bubbleView.setPadding(10, 10, 10, 10);
//            bubbleView.setTextColor(getContext().getColor(R.color.white));
//            layout.addViewSortByWidth(bubbleView);
//        }

//        TagView tagGroup = (TagView) getView().findViewById(R.id.tag_view);
////You can add one tag
////        tagGroup.addTag();
////You can add multiple tag via ArrayList
//        tagGroup.addTags(labels);
//Via string array
//        addTags(String[] tags);

//        arrayListComments.add(new Item(1, "Looking Gorgeous"));
//        arrayListComments.add(new Item(2, "Very Nice Pic"));
//        arrayListComments.add(new Item(3, "Nice One"));
//        arrayListComments.add(new Item(4, "Awesome Pic Dear"));
//        arrayListComments.add(new Item(5, "Very Nice Pic"));
//        arrayListComments.add(new Item(6, "Nice One Dear"));


//        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver,
//                new IntentFilter("custom-message"));
        match = getView().findViewById(R.id.match);
        unmatch = getView().findViewById(R.id.unmatch);
        reload = getView().findViewById(R.id.reload);
        //customer_care = getView().findViewById(R.id.imageViewMatchMakerProfile);
        comments = getView().findViewById(R.id.comments);
        nullLay = getView().findViewById(R.id.nullLay);
        changeLocation = getView().findViewById(R.id.change_location);

//Slider
        myView = getView().findViewById(R.id.my_view);
        open = getView().findViewById(R.id.open);
        lin_open = getView().findViewById(R.id.lin_open);
        myView.setVisibility(View.INVISIBLE);
        close = getView().findViewById(R.id.close);
        imageViewAddComments = getView().findViewById(R.id.imageViewAddComments);
        imageViewShowComments = getView().findViewById(R.id.imageViewShowComments);
        getPeopleList();

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.left_swipe);
                myView.setVisibility(View.VISIBLE);
                lin_open.setVisibility(View.INVISIBLE);
                myView.startAnimation(slideUp);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.rigt_swipe);
                lin_open.setVisibility(View.VISIBLE);
                myView.setVisibility(View.INVISIBLE);
                myView.startAnimation(slideDown);
            }
        });


        imageViewShowComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close.performClick();
                Log.d(TAG,"jigar the view comment friend id is "
                        + peoplesAry.get(0).get(Constants.TAG_REGISTERED_ID));
//               String strNewCommentID=peoplesAry.get(0).get(Constants.TAG_REGISTERED_ID);
//                Log.d(TAG,"jigar the view comment friend id is "
//                        + peoplesAry.get(0).get(Constants.TAG_COMMENT));
//                List<String> myList = new ArrayList<String>(Arrays.asList(peoplesAry.get(0).get(Constants.TAG_COMMENT).split(",")));
//                Log.d(TAG,"jigar the view array list comment friend id is "
//                        + myList.size());

                if( peoplesAry.get(0).get(Constants.TAG_COMMENT_TEST)!=null)
                {

                    List<String> myList = new ArrayList<String>(Arrays.asList(peoplesAry.get(0).get(Constants.TAG_COMMENT_TEST).split(",")));
                    Log.d(TAG,"jigar the view string list size comment is "
                            +  myList.size());
                    arrayListComments.clear();
                    tags.setTag(null);

                    for(int i=0;i<myList.size();i++)
                    {
                        arrayListComments.add(new Item(i,myList.get(i)));
                    }
                    tags.setVisibility(View.VISIBLE);
                    tags.startAnimation(slideUp);

                    tags.removeAllViewsInLayout();
                    tags.removeAllViews();
                    tags.refreshDrawableState();


//                    tags.setBackgroundDrawable(getContext().getDrawable(R.drawable.black_curve_background));

                    tags.setTags(arrayListComments, new DataTransform<Item>() {
                        @NotNull
                        @Override
                        public String transfer(Item item) {
                            return item.getName();
                        }
                    });

                    tags.setClickListener(new TagView.TagClickListener<Item>() {
                        @Override
                        public void onTagClick(Item item) {

                            item.getId();
                        }

                    });

                    // close.performClick();
                    //tags.setVisibility(View.VISIBLE);
                    //   tags.startAnimation(slideUp);
                    slideUp.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            tags.setVisibility(View.GONE);
                            arrayListComments.clear();

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                }else
                {
                    Toast.makeText(getContext(),"No Comments Available",Toast.LENGTH_LONG).show();
                }

//                arrayListComments.add(new Item(i, strUserComment));


            }
        });

        imageViewAddComments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        close.performClick();

                        Log.d(TAG,"jigar the add comment friend id is "
                                + peoplesAry.get(0).get(Constants.TAG_REGISTERED_ID));
                        Log.d(TAG,"jigar the add comment user id is "+GetSet.getUserId());
                        LovelyTextInputDialog lovelyTextInputDialog;
                        lovelyTextInputDialog=    new LovelyTextInputDialog(getContext(), R.style.TintTheme);
                        lovelyTextInputDialog.setTopColorRes(R.color.colorPrimary);
                        lovelyTextInputDialog.setTitle("Comment");
                        lovelyTextInputDialog.setMessage("Add Comment");
                        lovelyTextInputDialog.setIcon(R.drawable.ic_comment);
                        lovelyTextInputDialog.setInputFilter("Enter Valid Comment", new LovelyTextInputDialog.TextFilter() {
                            @Override
                            public boolean check(String text) {
                                return text.matches("\\w+");
                            }
                        })
                                .setNegativeButton(R.string.cancel, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        lovelyTextInputDialog.dismiss();
                                    }
                                })
                                .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                                    @Override
                                    public void onTextInputConfirmed(String text) {
                                        addNewComment(text, peoplesAry.get(0).get(Constants.TAG_REGISTERED_ID));
                                        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
                                    }
                                });

                        lovelyTextInputDialog.show();
                    }
                });


        getPeopleList();

        display = getActivity().getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth() * 90 / 100;
        screenHeight = display.getHeight() * 62 / 100;
        context = getActivity();
        pref = getActivity().getApplicationContext().getSharedPreferences("ChatPref",
                MODE_PRIVATE);
        editor = pref.edit();
        //   flingContainer.setVisibility(View.GONE);
        //   flingContainer.getLayoutParams().height = screenWidth + 50;
     //   bottomLay.setVisibility(View.GONE);
        linearLayoutSwipeProfileCard.setVisibility(View.VISIBLE);
        rippleBackground.setVisibility(View.VISIBLE);
        match.setOnClickListener(this);
        unmatch.setOnClickListener(this);
        changeLocation.setOnClickListener(this);

        undoClicked = true;
        lastSwipedUser.clear();
        peoplesAry = new ArrayList<>();
        peopleAdapter = new PeopleAdapter(getActivity(), peoplesAry);
        flingContainer.setAdapter(peopleAdapter);
        peopleAdapter.notifyDataSetChanged();
        tags = getView().findViewById(R.id.tagview);
        tags.getBackground().setAlpha(80);
        slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.from_bottom_to_top);
        slideUp.setDuration(5000);

        float bottomOfScreen = getResources().getDisplayMetrics()
                .heightPixels - (tags.getHeight() * 4);
        //bottomOfScreen is where you want to animate to

        tags.setVisibility(View.GONE);

        //        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                HowzuApplication.getInstance().getRequestQueue().cancelAll(TAG);
//                HowzuApplication.getInstance().addToRequestQueue(getFriendRequest, TAG);
//            }
//        }, 3000);

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loadmore) {
                    peoplesAry.clear();
                    HowzuApplication.getInstance().getRequestQueue().cancelAll(TAG);
            //        HowzuApplication.getInstance().addToRequestQueue(getPeople, TAG);
                }
            }
        });

        Picasso.with(getActivity()).load(GetSet.getImageUrl())
                .placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder)
                .into(userImage);

        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {

            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                try {
                    undoClicked = false;
                    removedUserId = peoplesAry.get(0).get(Constants.TAG_REGISTERED_ID);
                    lastSwipedUser.putAll(peoplesAry.get(0));
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                try {
                    undoClicked = false;
                    if (Constants.pref.getBoolean("isDeclineShown", false)) {
                        peoplesAry.remove(0);
                        peopleAdapter.notifyDataSetChanged();
                        Log.v(TAG, "removedUserId=" + removedUserId);
                        unmatch(removedUserId);
                    } else {
                        declinedialog(getActivity(), temp);
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                try {
                    undoClicked = false;
                    peoplesAry.remove(0);
                    peopleAdapter.notifyDataSetChanged();
                    Log.v(TAG, "removedUserId=" + removedUserId);
                    match(removedUserId);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
                try {
                    Log.v(TAG, "itemsInAdapter" + itemsInAdapter);
                    if (itemsInAdapter == 0) {
                        linearLayoutSwipeProfileCard.setVisibility(View.GONE);
                    } else {
                        linearLayoutSwipeProfileCard.setVisibility(View.VISIBLE);
                    }
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (peoplesAry.size() == 0) {
                                    rippleBackground.setVisibility(View.VISIBLE);
                                    rippleBackground.startRippleAnimation();
                                    linearLayoutSwipeProfileCard.setVisibility(View.GONE);
                          //          bottomLay.setVisibility(View.GONE);
                                    peoplesAry.clear();
//                                HowzuApplication.getInstance().addToRequestQueue(getPeople, TAG);
                                }
                            }
                        }, 3000);

                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

                try {
                    RelativeLayout view = (RelativeLayout) flingContainer.getSelectedView();
                    view.findViewById(R.id.swipe_left).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                    view.findViewById(R.id.swipe_right).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Intent p = new Intent(getActivity(), MainViewProfileDetailActivity.class);
                p.putExtra("from", "home");
                // here friend id means user own id
                // and register id means friend id whos profile we are visiting
                p.putExtra(Constants.TAG_FRIEND_ID, GetSet.getUseridLikeToken());
                p.putExtra(Constants.TAG_PROFILE_VISITOR_ID_LIKE_TOKEN, peoplesAry.get(itemPosition).get(Constants.TAG_ID));
                p.putExtra(Constants.TAG_REGISTERED_ID, peoplesAry.get(itemPosition).get(Constants.TAG_REGISTERED_ID));

                Log.d(TAG,"jigar the profile whom we visited is "+peoplesAry.get(itemPosition).get(Constants.TAG_ID));
                Log.d(TAG,"jigar the our user id in visitor is "+GetSet.getUseridLikeToken());
                startActivity(p);
            }
        });
    }

//    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            // Get extra data included in the Intent
//            String ItemName = intent.getStringExtra("item");
//            String qty = intent.getStringExtra("quantity");
//            Toast.makeText(getContext(),ItemName +" "+qty ,Toast.LENGTH_SHORT).show();
//        }
//    };

    public void undoSwipe() {
        peoplesAry.add(0, lastSwipedUser);
        peopleAdapter.notifyDataSetChanged();
        flingContainer.undo();
    }

    public void declinedialog(final Context ctx, final HashMap<String, String> temp) {
        ((Activity) ctx).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Dialog dialog = new Dialog(ctx);
                    Display display = ((Activity) ctx).getWindowManager().getDefaultDisplay();
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    dialog.setContentView(R.layout.logout_dialog);
                    dialog.getWindow().setLayout(display.getWidth() * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);

                    TextView titleTxt = dialog.findViewById(R.id.headerTxt);
                    TextView subTxt = dialog.findViewById(R.id.subTxt);
                    TextView yes = dialog.findViewById(R.id.yes);
                    TextView no = dialog.findViewById(R.id.no);

                    yes.setText(ctx.getString(R.string.ok));
                    no.setText(ctx.getString(R.string.cancel));
                    titleTxt.setText(ctx.getString(R.string.alert));
                    subTxt.setText(ctx.getString(R.string.decline_alert));

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            Constants.editor.putBoolean("isDeclineShown", true);
                            Constants.editor.commit();
                            flingContainer.getTopCardListener().selectLeft();
                        }
                    });

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            Constants.editor.putBoolean("isDeclineShown", true);
                            Constants.editor.commit();
                            peopleAdapter.notifyDataSetChanged();
                        }
                    });

                    dialog.show();
                } catch (WindowManager.BadTokenException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        premiumClicked = false;
        if (decline) {
            unmatch(declineId);
        }
        if (peopleAdapter != null) {
            peopleAdapter.notifyDataSetChanged();
        }


    }

//    @Override
//    public void onReceiveLocation(BDLocation bdLocation) {
//
//        try {
//            String Province = bdLocation.getProvince();
////            The current location of the city / /equipment
//            String City = bdLocation.getCity();
//
//            System.out.println("jigar the recieve location is "+City+" and province "+Province);
//
//
//
//        }catch (Exception E)
//            {
//                System.out.println("jigar the erro recieve location is "+E);
//                E.printStackTrace();
//            }
//    }

    public class PeopleAdapter extends BaseAdapter {
        ArrayList<HashMap<String, String>> peoples;
        ViewHolder holder = null;
        private Context mContext;

        public PeopleAdapter(Context ctx, ArrayList<HashMap<String, String>> data) {
            mContext = ctx;
            peoples = data;
        }

        @Override
        public int getCount() {
            return peoples.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                if(convertView == null){
                    // LayoutInflater class is used to instantiate layout XML file into its corresponding View objects.
                    LayoutInflater inflater = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.home_people_layout, parent, false);//layout

                }
            holder = new ViewHolder();
            Log.e(TAG, "getView: " + position);
//            holder.myView = convertView.findViewById(R.id.my_view);
//            holder.close = convertView.findViewById(R.id.close);
//            holder.main = convertView.findViewById(R.id.main);
//            holder.open = convertView.findViewById(R.id.open);
//            holder.lin_open = convertView.findViewById(R.id.lin_open);
//            holder. bottomLay = convertView.findViewById(R.id.bottomLay);

            holder.imageViewPeopleProfile = convertView.findViewById(R.id.imageViewPeopleProfile);
            holder.imageViewMatchMakerProfile = convertView.findViewById(R.id.imageViewMatchMakerProfile);
    //        holder.imageViewShowComments= convertView.findViewById(R.id.imageViewShowComments);
       //     holder.imageViewAddComments= convertView.findViewById(R.id.imageViewAddComments);
            holder.userName = convertView.findViewById(R.id.user_name);
            holder.textViewProfileLocation = convertView.findViewById(R.id.textViewProfileLocation);
        //    holder.lin_open.setVisibility(View.VISIBLE);
            convertView.setTag(holder);
//            if (convertView == null) {
//
////Slider
//
//
//
//                //    holder.main.getLayoutParams().height = screenHeight;
//                //    holder.main.getLayoutParams().width = screenWidth;
//
//
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }

  //              holder.myView.setVisibility(View.VISIBLE);

                final HashMap<String, String> tempMap = peoples.get(position);

                //    Log.v("image", "image" + tempMap.get(Constants.TAG_USERIMAGE));
                System.out.println("jigar the comment in bind is member name is "+tempMap.get(Constants.TAG_COMMENT));

                System.out.println("jigar the image src of user is "+tempMap.get(Constants.TAG_IMAGE));
                Picasso.with(getActivity())
                        .load(tempMap.get(Constants.TAG_IMAGE))
                                //.replace("thumbs350", "original")
                        .error(R.drawable.user_placeholder)
                        .placeholder(R.drawable.user_placeholder)
                        .into(holder.imageViewPeopleProfile);


                //                if (tempMap.get(Constants.TAG_SHOW_AGE).equals("true"))
//                {
//                    holder.userName.setText(tempMap.get(Constants.TAG_NAME));
//                }
//
//                else
                    {
                        holder.textViewProfileLocation.setText(tempMap.get(Constants.TAG_LOCATION));
                        holder.userName.setText(tempMap.get(Constants.TAG_NAME) + ", " + tempMap.get(Constants.TAG_AGE));
                    }

                //if (tempMap.get(Constants.TAG_SHOW_LOCATION).equals("true") && !tempMap.get(Constants.TAG_USERID).equals(GetSet.getUserId()))


                    //
//                    holder.textViewProfileLocation.setVisibility(View.INVISIBLE);
//                    holder.textViewProfileLocation.setText("");
//                }
//                //else
//                    {
//                    holder.textViewProfileLocation.setVisibility(View.VISIBLE);
//                    holder.textViewProfileLocation.setText(tempMap.get(Constants.TAG_LOCATION));

                {
                    String ItemName = tempMap.get(Constants.TAG_SPONSOR_ID);
                    String qty = tempMap.get(Constants.TAG_COMMENT);
                    Intent intent = new Intent("custom-message");
                    //            intent.putExtra("quantity",Integer.parseInt(quantity.getText().toString()));
                    intent.putExtra("quantity",qty);
                    intent.putExtra("item",ItemName);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }

                holder.imageViewMatchMakerProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(),"user "+tempMap.get(Constants.TAG_REGISTERED_ID)
                                +"The sponsor id is "+tempMap.get(Constants.TAG_SPONSOR_ID),Toast.LENGTH_LONG ).show();

                        if(!tempMap.get(Constants.TAG_SPONSOR_ID).equals("0"))
                        {
                            GetSet.setUserId(tempMap.get(Constants.TAG_REGISTERED_ID));
                            GetSet.setFriendId(tempMap.get(Constants.TAG_SPONSOR_ID));

                            final FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.replace(R.id.content_frame, new MatchMakerFragment(), "MatchMakerFragment");
                            ft.commit();

                            //                            FragmentManager fm = getFragmentManager();
//                            MatchMakerFragment fragm = (MatchMakerFragment) fm.findFragmentById(R.id.menuItemMatchMaker);
//                            fragm.getTargetFragment();

                        }else
                        {
                            Toast.makeText(getContext(),"Profile Doesn't Have Match Maker",Toast.LENGTH_LONG ).show();

                        }
                    }
                });
//                holder.imageViewAddComments.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//
//                        Log.d(TAG,"jigar the add comment friend id is "+tempMap.get(Constants.TAG_REGISTERED_ID));
//                        Log.d(TAG,"jigar the add comment user id is "+GetSet.getUserId());
//                        LovelyTextInputDialog lovelyTextInputDialog;
//                        lovelyTextInputDialog=    new LovelyTextInputDialog(getContext(), R.style.TintTheme);
//                        lovelyTextInputDialog.setTopColorRes(R.color.colorPrimary);
//                        lovelyTextInputDialog.setTitle("Comment");
//                        lovelyTextInputDialog.setMessage("Add Comment");
//                        lovelyTextInputDialog.setIcon(R.drawable.ic_comment);
//                        lovelyTextInputDialog.setInputFilter("Enter Valid Comment", new LovelyTextInputDialog.TextFilter() {
//                            @Override
//                            public boolean check(String text) {
//                                return text.matches("\\w+");
//                            }
//                        })
//                                .setNegativeButton(R.string.cancel, new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View view) {
//                                        lovelyTextInputDialog.dismiss();
//                                    }
//                                })
//                                .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
//                                    @Override
//                                    public void onTextInputConfirmed(String text) {
//                                        addNewComment(text,tempMap.get(Constants.TAG_REGISTERED_ID));
//                                        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//
//                        lovelyTextInputDialog.show();
//                    }
//                });
//
//                holder. imageViewShowComments.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        arrayListComments.clear();
//                        //getProfileCommentList();
//
//                    }
//                });
//
//                holder.open.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        //                     Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.left_swipe);
////                        holder.myView.setVisibility(View.VISIBLE);
////                        holder.lin_open.setVisibility(View.INVISIBLE);
//
//
//                            if(holder.open.getVisibility() == View.INVISIBLE){
//                                holder.open.setVisibility(View.VISIBLE);
//                            } else {
//                                holder.open.setVisibility(View.INVISIBLE);
//                            }
////                        if(holder.myView.getVisibility()!=View.INVISIBLE) {
////
////                            holder.bottomLay.clearAnimation();
////                            holder.myView.clearAnimation();
////                            holder.lin_open.clearAnimation();
////                            holder.open.clearAnimation();
////                            holder.close.clearAnimation();
////
////                            holder.bottomLay.setVisibility(View.VISIBLE);
////                            holder.myView.setVisibility(View.VISIBLE);
////                            holder.lin_open.setVisibility(View.VISIBLE);
////                            holder.open.setVisibility(View.VISIBLE);
////                            holder.close.setVisibility(View.INVISIBLE);
////                        }else
////                        {
////                            holder.bottomLay.setVisibility(View.INVISIBLE);
////                            holder.myView.setVisibility(View.INVISIBLE);
////                            holder.lin_open.setVisibility(View.INVISIBLE);
////                            holder.open.setVisibility(View.INVISIBLE);
////                            holder.close.setVisibility(View.VISIBLE);
////
////                        }
//                        System.out.println("jigar the i open clicked and visibleity is "+holder.open.getVisibility());
//
/////                        holder.myView.startAnimation(slideUp);
//
//                    }
//                });
//
//                holder.close.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        //         Animation slideDown = AnimationUtils.loadAnimation(getContext(), R.anim.rigt_swipe);
//                        System.out.println("jigar the i close clicked "+holder.myView.getVisibility());
////                        if(holder.myView.getVisibility()==View.INVISIBLE)
////                        {
////                            holder.bottomLay.setVisibility(View.INVISIBLE);
////                            holder.lin_open.setVisibility(View.VISIBLE);
////                            holder.myView.setVisibility(View.VISIBLE);
////
////
////                        }else {
////                            holder.bottomLay.setVisibility(View.VISIBLE);
////                            holder.lin_open.setVisibility(View.VISIBLE);
////                            holder.myView.setVisibility(View.INVISIBLE);
////                            //              holder.myView.startAnimation(slideDown);
////                        }
//                    }
//                });

            } catch (NullPointerException e) {
                Log.d(TAG,"jigar the nulll pointer exception in list is "+e);
                e.printStackTrace();
            } catch (Exception e) {
                Log.d(TAG,"jigar the main  exception in list is "+e);

                e.printStackTrace();
            }
            return convertView;
        }

        public void refreshAdapter(ArrayList<HashMap<String, String>> peoplesAry) {
            this.peoples = peoplesAry;
            this.notifyDataSetChanged();
        }

        class ViewHolder {
            ImageView imageViewPeopleProfile,imageViewMatchMakerProfile,imageViewShowComments,imageViewAddComments,open, close;
            TextView userName, textViewProfileLocation;
            LinearLayout myView,lin_open;
             LinearLayout bottomLay;

            RelativeLayout main;
        }
    }



//    StringRequest getPeople = new StringRequest(Request.Method.POST, Constants.API_GET_PEOPLE,
//            new Response.Listener<String>() {
//                @Override
//                public void onResponse(String res) {
//                    Log.v(TAG, "getPeopleRes: " + res);
//                    try {
//                        JSONObject response = new JSONObject(res);
//                        loadmore = true;
//                        if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("true")) {
//                            JSONArray peoples = response.optJSONArray(Constants.TAG_PEOPLES);
//                            peoplesAry.clear();
//                            for (int i = 0; i < peoples.length(); i++) {
//                                JSONObject values = peoples.optJSONObject(i);
//                                HashMap<String, String> map = new HashMap<String, String>();
//                                map.put(Constants.TAG_USERID, DefensiveClass.optInt(values, Constants.TAG_USERID));
//                                map.put(Constants.TAG_USERNAME, DefensiveClass.optString(values, Constants.TAG_USERNAME));
//                                map.put(Constants.TAG_SEND_MATCH, DefensiveClass.optString(values, Constants.TAG_SEND_MATCH));
//                                map.put(Constants.TAG_AGE, DefensiveClass.optString(values, Constants.TAG_AGE));
//                                map.put(Constants.TAG_BIO, DefensiveClass.optString(values, Constants.TAG_BIO));
//                                map.put(Constants.TAG_LAT, DefensiveClass.optString(values, Constants.TAG_LAT));
//                                map.put(Constants.TAG_LON, DefensiveClass.optString(values, Constants.TAG_LON));
//                                map.put(Constants.TAG_USERIMAGE, DefensiveClass.optString(values, Constants.TAG_USERIMAGE));
//                                map.put(Constants.TAG_ONLINE, DefensiveClass.optInt(values, Constants.TAG_ONLINE));
//                                map.put(Constants.TAG_SHOW_AGE, DefensiveClass.optString(values, Constants.TAG_SHOW_AGE));
//                                map.put(Constants.TAG_SHOW_LOCATION, DefensiveClass.optString(values, Constants.TAG_SHOW_LOCATION));
//                                map.put(Constants.TAG_LOCATION, DefensiveClass.optString(values, Constants.TAG_LOCATION));
//                                peoplesAry.add(map);
//                            }
//
//                            Log.v(TAG, "peoplesAry" + peoplesAry);
//
//                        } else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
//                            CommonFunctions.disabledialog(getActivity(), "Error", response.getString("message"));
//                        } else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("false") &&
//                                (DefensiveClass.optString(response, Constants.TAG_MESSAGE).equalsIgnoreCase("There's no one new around you"))) {
//                            peoplesAry.clear();
//                        }
//                        if (peopleAdapter != null) {
//                            peopleAdapter.refreshAdapter(peoplesAry);
//                        } else {
//                            peopleAdapter = new PeopleAdapter(getActivity(), peoplesAry);
//                            flingContainer.setAdapter(peopleAdapter);
//                            peopleAdapter.notifyDataSetChanged();
//                        }
//
//                        if (peoplesAry.size() > 0) {
//                            rippleBackground.stopRippleAnimation();
//                            rippleBackground.setVisibility(View.GONE);
//                            linearLayoutSwipeProfileCard.setVisibility(View.VISIBLE);
//                            bottomLay.setVisibility(View.VISIBLE);
//                            nullLay.setVisibility(View.GONE);
//                        } else {
//                            nullLay.setVisibility(View.VISIBLE);
//                        }
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    } catch (NullPointerException e) {
//                        e.printStackTrace();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, new Response.ErrorListener() {
//
//        @Override
//        public void onErrorResponse(VolleyError error) {
//            VolleyLog.d(TAG, "Error: " + error.getMessage());
//            VolleyLog.d(TAG, "jigar the error is in the home fragment method call : " + error.getMessage());
//            Log.d(TAG, "jigar the error is in the home fragment method call : " + error.getMessage());
//
//
//            linearLayoutSwipeProfileCard.setVisibility(View.GONE);
//            rippleBackground.setVisibility(View.VISIBLE);
//            rippleBackground.startRippleAnimation();
//            if (peoplesAry.size() > 0) {
//                rippleBackground.stopRippleAnimation();
//                rippleBackground.setVisibility(View.GONE);
//                linearLayoutSwipeProfileCard.setVisibility(View.VISIBLE);
//                bottomLay.setVisibility(View.VISIBLE);
//                nullLay.setVisibility(View.GONE);
//            } else {
//                nullLay.setVisibility(View.VISIBLE);
//            }
//        }
//
//    }) {
//
//        @Override
//        public Map<String, String> getHeaders() throws AuthFailureError {
//            Map<String, String> map = new HashMap<String, String>();
//            map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//            return map;
//        }
//
//        @Override
//        protected Map<String, String> getParams() {
//            Map<String, String> params = new HashMap<String, String>();
////            params.put(Constants.TAG_USERID, GetSet.getUserId());
//            params.put(Constants.TAG_USERID, "87c7a170-1d47-11e9-95a3-8b2fc6cd19c1");
//            params.put(Constants.TAG_OFFSET, "0");
//            params.put(Constants.TAG_LIMIT, "20");
//            params.put(Constants.TAG_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000L));
//            Log.v(TAG, "getPeopleParams=" + params);
//            return params;
//        }
//    };


    public void match(final String followId) {
        StringRequest sendmatch = new StringRequest(Request.Method.POST, Constants.API_NEW_MATCH_AND_FRIEND_REQUEST,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, "matchRes=" + res);
                        System.out.println("jigar the on like profile in json is "+res);

                        try {
                            JSONObject json = new JSONObject(res);
                            String response = json.getString(Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(getActivity(), "Error", json.getString("message"));
                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());
                params.put(Constants.TAG_FRIEND_ID, followId);
                Log.v(TAG, "matchParams=" + params);
                return params;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(sendmatch, "");
    }

    public void unmatch(final String followId) {
        StringRequest sendunmatch = new StringRequest(Request.Method.POST, Constants.API_NEW_UNMATCH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d(TAG, "unmatchRes=" + res);
                        System.out.println("jigar the unmatch response is "+res);
                        try {
                            JSONObject json = new JSONObject(res);
                            String response = json.getString(Constants.TAG_STATUS);
                            if (response.equalsIgnoreCase("true")) {

                            } else if (response.equalsIgnoreCase("error")) {
                                CommonFunctions.disabledialog(getActivity(), "Error", json.getString("message"));
                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                System.out.println("jigar the unmatch volley error response is "+error.getMessage());

            }
        }) {

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                return map;
//            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());
                params.put(Constants.TAG_FRIEND_ID, followId);
                Log.v(TAG, "unmatchParams=" + params);
                System.out.println("jigar the unmatch params is "+params);

                return params;
            }
        };

        HowzuApplication.getInstance().addToRequestQueue(sendunmatch, "");
    }

    @SuppressLint("Range")
    public static void decline() {
        unmatch.startAnimation(AnimationUtils.loadAnimation(HomeFragment.context, R.anim.button_bounce));
        unmatch.setOnClickListener(null);
        try {
            RelativeLayout view = (RelativeLayout) flingContainer.getSelectedView();
            view.findViewById(R.id.swipe_left).setAlpha(255);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                flingContainer.getTopCardListener().selectLeft();
                unmatch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        decline();
                    }
                });
            }
        }, 200);
    }


    /*Onclick Event*/

    @SuppressLint("Range")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.match:
                match.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.button_bounce));
                match.setOnClickListener(null);
                try {
                    RelativeLayout view = (RelativeLayout) flingContainer.getSelectedView();
                    view.findViewById(R.id.swipe_right).setAlpha(255);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        flingContainer.getTopCardListener().selectRight();
                        match.setOnClickListener(HomeFragment.this);
                    }
                }, 200);
                break;
            case R.id.unmatch:
                decline();
                break;
            case R.id.reload:
                reload.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.button_bounce));
                if (GetSet.isPremium()) {
                    if (!undoClicked) {
                        undoClicked = true;
                        undoSwipe();
                    }
                } else {
                    Intent m = new Intent(getActivity(), PremiumDialog.class);
                    startActivity(m);
                }
                break;

                case  R.id.imageViewMatchMakerProfile:
                FragmentManager fm = getFragmentManager();
                MatchMakerFragment fragm = (MatchMakerFragment) fm.findFragmentById(R.id.menuItemMatchmakerFeature);
                fragm.getTargetFragment();

            case R.id.change_location:
                if (GetSet.isPremium()) {
                    HashMap<String, String> fbdata = new HashMap<>();
                    Intent i = new Intent(getActivity(), LocationActivity.class);
                    i.putExtra("data", fbdata);
                    startActivity(i);
                } else {
                    if (!premiumClicked) {
                        premiumClicked = true;
                        Intent m = new Intent(getActivity(), PremiumDialog.class);
                        startActivity(m);
                    }
                }
                break;
        }
    }

//    public void getProfileNotificationList()
//    {
//
//        if (CommonFunctions.isNetwork(Objects.requireNonNull(getActivity()))) {
//
//            CommonFunctions.showProgressDialog(getActivity());
//
//            OkHttpClient client = new OkHttpClient();
//
//
//            //   System.out.println("jigar the before response token is " + pref.getString(Constants.TAG_AUTHORIZATION, ""));
//
//           String strFriendId = peoplesAry.get(0).get(Constants.TAG_REGISTERED_ID);
//            System.out.println("jigar the before comment response user id is " + strFriendId);
//
//
//            RequestBody requestBody = new MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addFormDataPart(Constants.TAG_FRIEND_ID, strFriendId)
//                    .build();
//            okhttp3.Request request = new okhttp3.Request.Builder()
//                    //.header("Authorization", pref.getString(Constants.TAG_AUTHORIZATION, ""))
//                    .post(requestBody)
//                    .url(Constants.API_GET_PROFILE_COMMENTS)
//                    .build();
//
//
//            // Response response = client.newCall(request).execute();
//            client.newCall(request).enqueue(new okhttp3.Callback() {
//                @Override
//                public void onFailure(okhttp3.Call call, IOException e) {
//
//                    System.out.println("jigar the error failure in response we have is " + e);
//
//
//                }
//
//                @Override
//                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
//                    final String responseData = response.body().string();
//                    Headers responseHeaders = response.headers();
//                    for (int i = 0; i < responseHeaders.size(); i++) {
//                        Log.d("DEBUG", responseHeaders.name(i) + ": " + responseHeaders.value(i));
//                    }
//                    if (!response.isSuccessful()) {
//                        throw new IOException("jigar Unexpected code " + response);
////                        System.out.println("jigar the response we have is " + response.body().toString());
//                    }
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            CommonFunctions.hideProgressDialog(getActivity());
//                            System.out.println("jigar the response we have is " + responseData);
//                            setUserCommentsData(responseData);
//
//
//                        }
//                    });
//                }
//            });
//        }else {
//            Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
//        }
//        CommonFunctions.hideProgressDialog(getActivity());
//    }

    public void getProfileCommentList() {

        if (CommonFunctions.isNetwork(Objects.requireNonNull(getActivity()))) {

            CommonFunctions.showProgressDialog(getActivity());

            String strFriendId = peoplesAry.get(0).get(Constants.TAG_REGISTERED_ID);

            StringRequest getCommentList = new StringRequest(Request.Method.POST, Constants.API_GET_PROFILE_COMMENTS,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {
                            Log.d(TAG, "commentParams=" + res);

                            try {
                                JSONObject json = new JSONObject(res);
                                String strStatus = json.getString(Constants.TAG_STATUS);
                                if (strStatus.equals("1")) {
                                    setUserCommentsData(res);
                                    CommonFunctions.hideProgressDialog(getActivity());
                                } else {
                                    String StrMessage = json.getString(Constants.TAG_MSG);
                                    Toast.makeText(getActivity(), StrMessage, Toast.LENGTH_LONG).show();
                                    tags.setVisibility(View.GONE);
                                    //close.performClick();
                                    CommonFunctions.hideProgressDialog(getActivity());
                                }
                            } catch (JSONException e) {
                                System.out.println("jigar the error in json is " + e);
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                System.out.println("jigar the error in null is " + e);

                                e.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("jigar the error main exception in json is " + e);

                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "commentParams Error: " + error.getMessage());
                    CommonFunctions.hideProgressDialog(getActivity());
                    System.out.println("jigar the error volley in json is " + error.getMessage());

                }
            }) {

                //            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                return map;
//            }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
//                params.put(Constants.TAG_FRIEND_ID, strFriendId);
                    params.put(Constants.TAG_REGISTERED_ID, strFriendId);
                    Log.v(TAG, "commentParams=" + params);
                    return params;
                }
            };

            HowzuApplication.getInstance().addToRequestQueue(getCommentList, "");
        }else
            {
                CommonFunctions.hideProgressDialog(getActivity());
            }
    }




//    public void getPeopleList()
//    {
//
//        if (CommonFunctions.isNetwork(Objects.requireNonNull(getActivity()))) {
//
//            CommonFunctions.showProgressDialog(getActivity());
//
//            OkHttpClient client = new OkHttpClient();
//
//            System.out.println("jigar the before response user id is " + GetSet.getUserId());
//            //   System.out.println("jigar the before response token is " + pref.getString(Constants.TAG_AUTHORIZATION, ""));
//
//
//            RequestBody requestBody = new MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addFormDataPart(Constants.TAG_REGISTERED_ID, GetSet.getUserId())
//                    .build();
//            okhttp3.Request request = new okhttp3.Request.Builder()
//                    //.header("Authorization", pref.getString(Constants.TAG_AUTHORIZATION, ""))
//                    .post(requestBody)
//                    .url(Constants.API_GET_PROFILE)
//                    .build();
//
//
//            // Response response = client.newCall(request).execute();
//            client.newCall(request).enqueue(new okhttp3.Callback() {
//                @Override
//                public void onFailure(okhttp3.Call call, IOException e) {
//
//                    System.out.println("jigar the error failure in response we have is " + e);
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                           // CommonFunctions.hideProgressDialog(getActivity());
//                            System.out.println("jigar the error response we have is " + e);
//                            Toast.makeText(getActivity(), getString(R.string.network_time_out_error), Toast.LENGTH_SHORT).show();
//
//
//                        }
//                    });
//                }
//
//
//                @Override
//                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
//                    final String responseData = response.body().string();
//                    Headers responseHeaders = response.headers();
//                    for (int i = 0; i < responseHeaders.size(); i++) {
//                        Log.d("DEBUG", responseHeaders.name(i) + ": " + responseHeaders.value(i));
//                    }
//                    if (!response.isSuccessful()) {
//                        throw new IOException("jigar Unexpected code " + response);
////                        System.out.println("jigar the response we have is " + response.body().toString());
//                    }
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            CommonFunctions.hideProgressDialog(getActivity());
//                            System.out.println("jigar the response we have is " + responseData);
//                            setFindPeoplePageData(responseData);
//
//
//                        }
//                    });
//                }
//            });
//        }else {
//            Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
//        }
//        CommonFunctions.hideProgressDialog(getActivity());
//    }


    public void getPeopleList()
    {

        if (CommonFunctions.isNetwork(Objects.requireNonNull(getActivity()))) {

            CommonFunctions.showProgressDialog(getActivity());
//            String strFriendId = peoplesAry.get(0).get(Constants.TAG_REGISTERED_ID);

            StringRequest getProfileList = new StringRequest(Request.Method.POST, Constants.API_GET_PROFILE,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {
                            Log.d(TAG, "commentParams=" + res);

                            try {
                                JSONObject json = new JSONObject(res);
                                String strStatus = json.getString(Constants.TAG_STATUS);
                                if (strStatus.equals("1")) {
                                    setFindPeoplePageData(res);
                                    CommonFunctions.hideProgressDialog(getActivity());
                                } else {
                                    String StrMessage = json.getString(Constants.TAG_MSG);
                                    Toast.makeText(getActivity(), StrMessage, Toast.LENGTH_LONG).show();
                                    tags.setVisibility(View.GONE);
                                    //close.performClick();
                                    CommonFunctions.hideProgressDialog(getActivity());
                                }
                            } catch (JSONException e) {
                                System.out.println("jigar the error in json is " + e);
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                System.out.println("jigar the error in null is " + e);

                                e.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("jigar the error main exception in json is " + e);

                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "commentParams Error: " + error.getMessage());
                    CommonFunctions.hideProgressDialog(getActivity());
                    System.out.println("jigar the error volley in json is " + error.getMessage());

                }
            }) {

                //            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                return map;
//            }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
//                params.put(Constants.TAG_FRIEND_ID, strFriendId);
                    params.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());
                    Log.v(TAG, "jigar the profileParams=" + params);
                    return params;
                }
            };

            HowzuApplication.getInstance().addToRequestQueue(getProfileList, "");

        }else {
            Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
        CommonFunctions.hideProgressDialog(getActivity());
    }

    public void setUserCommentsData(String strResponse) {
        try {
            JSONObject jsonObject = new JSONObject(strResponse);
       //     JSONObject jsonObject = new JSONObject(strResponse);

            String strStatus = jsonObject.getString(Constants.TAG_STATUS);
            String strMessage = jsonObject.getString(Constants.TAG_MSG);
            System.out.println("jigar the commenter response is " + strResponse);

            if (strStatus.equals("1") && strMessage.equals(Constants.TAG_SUCCESS)) {


                JSONArray jsonArrayProfileInfo = jsonObject.getJSONArray(Constants.TAG_INFO);
                //     JSONObject jsonObjectMember=jsonObjectInfo.getJSONObject(Constants.TAG_MEMBER);


                arrayListComments.clear();
                for (int i = 0; i < jsonArrayProfileInfo.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();

                    JSONObject jsonObjectMemberMainInfo = jsonArrayProfileInfo.getJSONObject(i);

                    String strCommentUserName = jsonObjectMemberMainInfo.getString(Constants.TAG_COMMENT_USER_NAME);
                    String strCommentID = jsonObjectMemberMainInfo.getString(Constants.TAG_COMMENT_ID);
                    String strCommentUserImage = jsonObjectMemberMainInfo.getString(Constants.TAG_COMMENT_USER_IMAGE);
                    String strUserComment = jsonObjectMemberMainInfo.getString(Constants.TAG_COMMENT);
                    arrayListComments.add(new Item(i, strUserComment));
                    System.out.println("jigar the commenter name is " + strCommentUserName);

             //       peoplesAry.add(map);
                }
                System.out.println("jigar the commenter list have is " + arrayListComments.toString());
                System.out.println("jigar the commenter list size is " + arrayListComments.size());
            }
            tags.removeAllViewsInLayout();
            tags.removeAllViews();
            tags.setVisibility(View.GONE);
            tags.setTags(arrayListComments, new DataTransform<Item>() {
                @NotNull
                @Override
                public String transfer(Item item) {
                    return item.getName();
                }
            });

            tags.setClickListener(new TagView.TagClickListener<Item>() {
                @Override
                public void onTagClick(Item item) {

                    item.getId();
                }

            });

           // close.performClick();
            tags.setVisibility(View.VISIBLE);
            tags.startAnimation(slideUp);
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    tags.setVisibility(View.GONE);
                    arrayListComments.clear();

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

//                tags.animate()
//                        .translationY(bottomOfScreen)
//                        .setInterpolator(new ReverseInterpolator())
//                        .setInterpolator(new BounceInterpolator())
//                        .setDuration(30000)
//                        .start();

        }catch (JSONException ex)
        {
            System.out.println("jigar the error in json comment response is "+ex);
        }
    }

    public void addNewComment(String strCommentText,String strFriendID) {
        if (CommonFunctions.isNetwork(Objects.requireNonNull(getActivity()))) {

            CommonFunctions.showProgressDialog(getActivity());
//            String strFriendId = peoplesAry.get(0).get(Constants.TAG_REGISTERED_ID);

            StringRequest getProfileList = new StringRequest(Request.Method.POST, Constants.API_ADD_COMMENT,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {
                            Log.d(TAG, "jigar the add comment response we have is " + res);
                            try {
                                JSONObject json = new JSONObject(res);
                                String strStatus = json.getString(Constants.TAG_STATUS);
                                if (strStatus.equals("1")) {
                                    setFindPeoplePageData(res);
                                    CommonFunctions.hideProgressDialog(getActivity());
                                } else {
                                    String StrMessage = json.getString(Constants.TAG_MSG);
                                    Toast.makeText(getActivity(), StrMessage, Toast.LENGTH_LONG).show();
                                    tags.setVisibility(View.GONE);
                                    //close.performClick();
                                    CommonFunctions.hideProgressDialog(getActivity());
                                }
                            } catch (JSONException e) {
                                System.out.println("jigar the error in commenting json is " + e);
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                System.out.println("jigar the error in commenting null is " + e);

                                e.printStackTrace();
                            } catch (Exception e) {
                                System.out.println("jigar the error main commenting exception in json is " + e);

                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "comment Params Error: " + error.getMessage());
                    CommonFunctions.hideProgressDialog(getActivity());
                    System.out.println("jigar the error volley in json is " + error.getMessage());

                }
            }) {

                //            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));
//                return map;
//            }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
//                params.put(Constants.TAG_FRIEND_ID, strFriendId);
                    params.put(Constants.TAG_REGISTERED_ID, GetSet.getUserId());
                    params.put(Constants.TAG_FRIEND_ID, strFriendID);
                    params.put(Constants.TAG_COMMENT, strCommentText);
                    Log.v(TAG, "jigar the comment parameter are =" + params);
                    return params;
                }
            };

            HowzuApplication.getInstance().addToRequestQueue(getProfileList, "");

        }else {
            Toast.makeText(getActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
        CommonFunctions.hideProgressDialog(getActivity());

    }

    public void setFindPeoplePageData(String strResponse)
    {
        try {
            Log.d(TAG,"jigar the response we profile list have is "+strResponse);
            JSONObject jsonObject=new JSONObject(strResponse);
            String strStatus=jsonObject.getString(Constants.TAG_STATUS);
            String strMessage=jsonObject.getString(Constants.TAG_MSG);

            if(strStatus.equals("1") || strMessage.equals(Constants.TAG_SUCCESS)) {


                JSONArray jsonArrayProfileInfo=jsonObject.getJSONArray(Constants.TAG_INFO);
           //     JSONObject jsonObjectMember=jsonObjectInfo.getJSONObject(Constants.TAG_MEMBER);

                for(int i=0;i<jsonArrayProfileInfo.length();i++)
                {
                    HashMap<String, String> map = new HashMap<String, String>();

                    JSONObject jsonObjectMemberMainInfo=jsonArrayProfileInfo.getJSONObject(i);

                    String strMemberName =jsonObjectMemberMainInfo.getString(Constants.TAG_NAME);
                    String strMemberRegisterID =jsonObjectMemberMainInfo.getString(Constants.TAG_REGISTERED_ID);
                    String strMemberAge =jsonObjectMemberMainInfo.getString(Constants.TAG_AGE);
                    String strMemberImage =jsonObjectMemberMainInfo.getString(Constants.TAG_IMAGE);
                    String strMatchMakerID =jsonObjectMemberMainInfo.getString(Constants.TAG_SPONSOR_ID);
                    String strMemberLocation =jsonObjectMemberMainInfo.getString(Constants.TAG_LOCATION);
                    String strFriendIDLikeToken =jsonObjectMemberMainInfo.getString(Constants.TAG_ID);

//                    JSONObject jsonObjectCommentlist=jsonObjectMemberMainInfo.getJSONObject(Constants.TAG_COMMENT);
//                    JSONArray jsonArrayCommentList = null;
//                    if(jsonObjectCommentlist!=null) {
                    JSONArray  jsonArrayCommentList = jsonObjectMemberMainInfo.getJSONArray(Constants.TAG_COMMENT);
                    System.out.println("jigar the member size comment list name is "+jsonArrayCommentList.length());
                    System.out.println("jigar the member comment list name is "+jsonArrayCommentList.toString());

                    if(jsonArrayCommentList.length()>0)
                    {
                        String strTempComment="";

                        for(int j=0;j<jsonArrayCommentList.length();j++)
                        {

                                    JSONObject jsonObjectCommentMainInfo = jsonArrayCommentList.getJSONObject(j);

                                    String strCommentUserName = jsonObjectCommentMainInfo.getString(Constants.TAG_COMMENT_USER_NAME);
                                    String strCommentID = jsonObjectCommentMainInfo.getString(Constants.TAG_COMMENT_ID);
                                    String strCommentUserImage = jsonObjectCommentMainInfo.getString(Constants.TAG_COMMENT_USER_IMAGE);
                                    String strUserComment = jsonObjectCommentMainInfo.getString(Constants.TAG_COMMENT);
                                    //arrayListComments.add(new Item(i, strUserComment));
                                    System.out.println("jigar the commenter name is " + strCommentUserName);

                                    if(strTempComment.equals(""))
                                    {
                                        strTempComment=strUserComment;
                                    }else
                                    {
                                        strTempComment=strTempComment+","+strUserComment;
                                    }
//                                           peoplesAry.add(map);
         //                       }
//                                System.out.println("jigar the commenter list have is " + arrayListComments.toString());
//                                System.out.println("jigar the commenter list size is " + arrayListComments.size());
           //                 }
                            //setUserCommentsData(jsonArrayCommentList.toString());
                        }
                        System.out.println("jigar the commenter new str temp  list is " + strTempComment);
                        //
                     //   map.put(Constants.TAG_COMMENT, Arrays.toString(arrayListComments.toArray()));
                        map.put(Constants.TAG_COMMENT_TEST, strTempComment);

                        //       map.put(Constants.TAG_COMMENT, strTempComment);

                    }

                    System.out.println("jigar the comment map array have is "+map.get(Constants.TAG_COMMENT));

                    map.put(Constants.TAG_REGISTERED_ID, strMemberRegisterID);
                    map.put(Constants.TAG_ID, strFriendIDLikeToken);
                    map.put(Constants.TAG_NAME, strMemberName);
                    map.put(Constants.TAG_AGE, strMemberAge);
                    map.put(Constants.TAG_IMAGE, strMemberImage);
                    map.put(Constants.TAG_SPONSOR_ID, strMatchMakerID);
                    map.put(Constants.TAG_LOCATION, strMemberLocation);

                    peoplesAry.add(map);
                }
            }
//            else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("error")) {
//                CommonFunctions.disabledialog(getActivity(), "Error", response.getString("message"));
//            } else if (DefensiveClass.optString(response, Constants.TAG_STATUS).equalsIgnoreCase("false") &&
//                    (DefensiveClass.optString(response, Constants.TAG_MESSAGE).equalsIgnoreCase("There's no one new around you"))) {
//                peoplesAry.clear();
//            }
            if (peopleAdapter != null) {
                peopleAdapter.refreshAdapter(peoplesAry);
            } else {
                peopleAdapter = new PeopleAdapter(getActivity(), peoplesAry);
                flingContainer.setAdapter(peopleAdapter);
                peopleAdapter.notifyDataSetChanged();
            }

            if (peoplesAry.size() > 0) {
                rippleBackground.stopRippleAnimation();
                rippleBackground.setVisibility(View.GONE);
                linearLayoutSwipeProfileCard.setVisibility(View.VISIBLE);
                //bottomLay.setVisibility(View.VISIBLE);
                nullLay.setVisibility(View.VISIBLE);
            } else {
                nullLay.setVisibility(View.VISIBLE);
            }


//                for (int i = 0; i < jsonObject)
//                    HashMap<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_USERID, DefensiveClass.optInt(values, Constants.TAG_USERID));
 //           getProfileNotificationList();
        }catch (JSONException ex)
        {
            System.out.println("jigar the error in profile list json is "+ex);
        }

    }


}

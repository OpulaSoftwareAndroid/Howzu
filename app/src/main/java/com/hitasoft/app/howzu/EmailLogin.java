package com.hitasoft.app.howzu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.android.gms.maps.model.LatLng;
import com.hitasoft.app.customclass.CustomTextView;
import com.hitasoft.app.customclass.ProgressWheel;
import com.hitasoft.app.utils.CommonFunctions;
import com.hitasoft.app.utils.Constants;
import com.hitasoft.app.utils.SharedPrefManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
/**
 * Created by hitasoft on 7/3/18.
 */

public class EmailLogin extends AppCompatActivity implements View.OnClickListener
        , BDLocationListener {
    static String TAG = "EmailLogin";
    ImageView back;
    Spinner country_spinner;
    CustomTextView btn_next;
    LocationClient mLocationClient;
    SharedPrefManager sharedPrefManager;
    ArrayList<String> arrayListCountryCode = new ArrayList<>();
    ArrayList<String> arrayListCountryID = new ArrayList<>();
    EditText editTextDOB, editTextMobileNumber, editTextName;
    ProgressWheel progressWheel;
    RadioButton radioButtonMale,radioButtonFemale;
    RadioGroup radioGroupGender;
    String strLocationAddress="";
    double doubleLatitude=0,doubleLongitude=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        initViews();

        back.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        editTextDOB = (EditText) findViewById(R.id.editTextBirthDate);
        editTextMobileNumber = (EditText) findViewById(R.id.editTextMobileNumber);
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextDOB = (EditText) findViewById(R.id.editTextBirthDate);
        radioButtonFemale=findViewById(R.id.radioFemale);
        radioButtonMale=findViewById(R.id.radioMale);
        radioGroupGender=findViewById(R.id.radioSex);

        mLocationClient = new LocationClient(this);
        LocationClientOption Option = new LocationClientOption ();
        Option.setIsNeedAddress (true);
        Option.setAddrType ("all");
        mLocationClient.setLocOption (Option);
        mLocationClient.registerLocationListener (this);
        mLocationClient.start ();

        country_spinner = findViewById(R.id.country_spinner);
//        ArrayList<String> spinnerArray = new ArrayList<String>();
//        spinnerArray.add("Select Country Code");
        arrayListCountryID.add("0");
        arrayListCountryCode.add("Select Country Code");

        ArrayAdapter<String> langAdapter = new ArrayAdapter<String>(getApplicationContext()
                , R.layout.layout_spinner_text, arrayListCountryCode);
        langAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
        country_spinner.setAdapter(langAdapter);

        TextWatcher textWatcher = new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "DDMMYYYY";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().equals(current)) {
                    String clean = charSequence.toString().replaceAll("[^\\d.]|\\.", "");
                    String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int j = 2; j <= cl && j < 6; j += 2) {
                        sel++;
                    }
                    //Fix for pressing delete next to a forward slash
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8) {
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        //This part makes sure that when we finish entering numbers
                        //the date is correct, fixing it otherwise
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int mon = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                        cal.set(Calendar.MONTH, mon - 1);
                        year = (year < 1900) ? 1900 : (year > 2100) ? 2100 : year;
                        cal.set(Calendar.YEAR, year);
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012

                        day = (day > cal.getActualMaximum(Calendar.DATE)) ? cal.getActualMaximum(Calendar.DATE) : day;
                        clean = String.format("%02d%02d%02d", day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    editTextDOB.setText(current);
                    editTextDOB.setSelection(sel < current.length() ? sel : current.length());

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        editTextDOB.addTextChangedListener(textWatcher);
        progressWheel = findViewById(R.id.interestProgress);
        progressWheel.setVisibility(View.GONE);
//        progressWheel.spin();

//        dialog.setMessage(getString(R.string.please_wait));
//        dialog.setIndeterminate(true);
//        dialog.setCancelable(false);
//        dialog.setCanceledOnTouchOutside(false);
        setCountryCode();

    }

    private void initViews() {
        back = findViewById(R.id.backbtn);
        btn_next = findViewById(R.id.btn_next);

    }


    /*OnClick Event*/

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.btn_next:

                String strDateOfBirth = editTextDOB.getText().toString();
                String strName = editTextName.getText().toString();
                String strMobileNumber = editTextMobileNumber.getText().toString();
                String strCountryCode = country_spinner.getSelectedItem().toString();
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                int radioButtonID = radioGroupGender.getCheckedRadioButtonId();
                View radioButton = radioGroupGender.findViewById(radioButtonID);
                int indexOfSelectedGender = radioGroupGender.indexOfChild(radioButton);
                String strGender;

                String selectedtext = radioButtonMale.getText().toString();

                if(indexOfSelectedGender==0)
                {
                    strGender="men";
                }else
                {
                    strGender="women";

                }
                System.out.println("jigar the selected gender is " + indexOfSelectedGender);
                int intDate = 0, intMonth = 0, intYear = 0;

                try {
                    Date date = format.parse(strDateOfBirth);

                    final Calendar c = Calendar.getInstance();

                    c.setTime(format.parse(strDateOfBirth));
                    System.out.println("jigar Year = " + c.get(Calendar.YEAR));
                    System.out.println("jigar Month = " + (c.get(Calendar.MONTH)));
                    System.out.println("jigar Day = " + c.get(Calendar.DAY_OF_MONTH));

                    intDate = c.get(Calendar.DAY_OF_MONTH);
                    intMonth = c.get(Calendar.MONTH);
                    intYear = c.get(Calendar.YEAR);

                    System.out.println("jigar the outer date validation says is " +
                            intDate + intMonth + intYear);
                    System.out.println("jigar the outer date validation says is " +
                            intDate + intMonth + intYear);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//                    Calendar myDate = Calendar.getInstance();
                    Date currentDate = Calendar.getInstance().getTime();
                    Date dateBirthDate = sdf.parse(strDateOfBirth);

                    System.out.println("jigar date1 : " + sdf.format(dateBirthDate));
                    System.out.println("jigar date2 : " + sdf.format(currentDate));

                    if (dateBirthDate.compareTo(currentDate) > 0) {
                        System.out.println("jigar the birth Date is after current Date");
                        editTextDOB.setError("Please Enter Valid Birth Date");
                    } else if (country_spinner.getSelectedItemPosition() == 0) {
                        Toast.makeText(this, "Please Select Country Code", Toast.LENGTH_LONG).show();
                    } else if (strMobileNumber.equals("")) {
                        editTextMobileNumber.setError("Enter Valid Mobile Number");
                    } else if (!isValidMobile(strMobileNumber)) {
                        editTextMobileNumber.setError("Enter Valid Mobile number");
                    } else if (strName.equals("")) {
                        editTextName.setError("Enter Valid Name");
                    } else if (strDateOfBirth.equals("")) {
                        editTextDOB.setError("Enter Valid Birth Date");
                    } else {
                        Intent intent = new Intent(getApplicationContext(), SendOTPActivity.class);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_NAME, strName);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_COUNTRY_CODE, strCountryCode);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_PHONE_NUMBER, strMobileNumber);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_BIRTHDATE, strDateOfBirth);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_LATITUDE, String.valueOf(doubleLatitude));
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_LONGITUDE, String.valueOf(doubleLongitude));
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_DETAIL_LOCATION, strLocationAddress);
                        intent.putExtra(Constants.TAG_LOGIN_INTENT_GENDER, strGender);
                        startActivity(intent);
                    }
                } catch (ParseException e) {
                    System.out.println("jigar the error in date is " + e);
                    e.printStackTrace();
                }

//                System.out.println("jigar the validartio say is "+isValidMobile(strMobileNumber));


//                Intent intent = new Intent(getApplicationContext(), SendOTPActivity.class);
//                startActivity(intent);

        }
    }

    public void setCountryCode() {
        if (CommonFunctions.isNetwork(Objects.requireNonNull(EmailLogin.this))) {
            CommonFunctions.showProgressDialog(this);
            StringRequest req = new StringRequest(Request.Method.POST, Constants.API_COUNTRY_CODE,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {
                            try {
                                Log.v(TAG, "getSettingsRes=" + res);

                                System.out.println("jigar the response on country code is " + res);

                                JSONObject jsonObjectResponseMain = new JSONObject(res);
                                String strStatus = jsonObjectResponseMain.getString(Constants.TAG_STATUS);
                                String strMsg = jsonObjectResponseMain.getString(Constants.TAG_MSG);


                                JSONArray jsonArrayInfo = jsonObjectResponseMain.getJSONArray(Constants.TAG_INFO);
                                for (int i = 0; i < jsonArrayInfo.length(); i++) {
                                    JSONObject jsonObjectCountryCodeInfo = jsonArrayInfo.getJSONObject(i);
                                    String strCountryID = jsonObjectCountryCodeInfo.getString(Constants.TAG_COUNTRY_ID);
                                    String strCountryCode = jsonObjectCountryCodeInfo.getString(Constants.TAG_COUNTRY_CODE);

                                    arrayListCountryCode.add(strCountryCode);
                                    arrayListCountryID.add(strCountryID);
//                                    spinnerArray.add(strCountryCode);

                                }

                                ArrayAdapter<String> langAdapter = new ArrayAdapter<String>(getApplicationContext()
                                        , R.layout.layout_spinner_text, arrayListCountryCode);
                                langAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown);
                                country_spinner.setAdapter(langAdapter);
//                                progressWheel.stopSpinning();
//                                progressWheel.setVisibility(View.GONE);
                                CommonFunctions.hideProgressDialog(EmailLogin.this);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    progressWheel.stopSpinning();
                    progressWheel.setVisibility(View.GONE);
                }

            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<String, String>();
                    Log.v(TAG, "getSettingsParams=" + map);
                    return map;
                }
            };
            HowzuApplication.getInstance().addToRequestQueue(req, TAG);
        }else
        {
            Toast.makeText(this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            CommonFunctions.hideProgressDialog(EmailLogin.this);

        }
    }

    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    public static boolean isValidDate(String pDateString) throws ParseException {
        Date date = new SimpleDateFormat("dd/MM/yyyy").parse(pDateString);

        return new Date().before(date);
    }

    public static boolean isAfterToday(int year, int month, int day) {

        Calendar today = Calendar.getInstance();
        Calendar myDate = Calendar.getInstance();
        Date c = Calendar.getInstance().getTime();
        // System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = df.format(c);

        int intCurrentDate, intCurrentMonth, intCurrentYear;
        //  System.out.println("jigar the current date is " + formattedDate);
        myDate.set(year, month, day);
        //System.out.println("jigar the date is " + year + " and " + month + " and " + day);

        if (myDate.before(formattedDate)) {
            return false;
        }
        return true;
    }


    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        try {
            String Province = bdLocation.getProvince();

            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            System.out.println("jigar the location we have is " + bdLocation.getLatitude());
            System.out.println("jigar the location we have is " + Province);

            doubleLatitude=bdLocation.getLatitude();
            doubleLongitude=bdLocation.getLongitude();
            if(bdLocation.getLatitude()==4.9E-324) {
                doubleLatitude = 21.189838;
                doubleLongitude = 72.812643;
            }
//            if(strBuildingName!=null)
//            {
//                strLocationAddress=strBuildingName;
//            }
            strLocationAddress= bdLocation.getCity()+","+bdLocation.getProvince()+","+bdLocation.getCountry();
            if(bdLocation.getCity().equals("null") || bdLocation.getCity()==null)
            {
                strLocationAddress="Surat,Gujarat,India";
            }

            System.out.println("jigar the location we have is " + strLocationAddress);
        }catch (Exception e)
        {
            System.out.println("jigar the error in location we have is " + e);

        }
    }
}

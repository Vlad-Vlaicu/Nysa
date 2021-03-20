package nysa.nysa_20.model.main_activity_fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nysa.nysa_20.R;
import nysa.nysa_20.model.library_res.CardViewRecyclerView;
import nysa.nysa_20.model.library_res.ListItem;


public class FragmentLibrary extends Fragment {

    private RecyclerView recyclerView;
    private CardViewRecyclerView adapter;
    private List<ListItem> itemList;
    private ImageButton DeleteQuery;
    private EditText SearchField;
    private TextView NotFound;
    private ConstraintLayout CollapsableHeading;
    private ConstraintLayout Heading;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private JsonArrayRequest request;
    private RequestQueue requestQueue;
    private View view;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_library, container, false);
        initComponents();

        return view;
    }

    private void initComponents(){
        recyclerView = view.findViewById(R.id.recyclerview_cardview);
        itemList = new ArrayList<>();

        // TODO: get url of JSON and initializeList(url)
        initializeList(new String("https://api.jsonbin.io/b/5edd33a8655d87580c45b0c9"));

        DeleteQuery = view.findViewById(R.id.delete_btn);
        SearchField = view.findViewById(R.id.search_field);
        NotFound = view.findViewById(R.id.not_found);
        Heading = view.findViewById(R.id.lib_heading);
        CollapsableHeading = view.findViewById(R.id.collapsable_heading);
        DeleteQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchField.setText("");
                NotFound.setVisibility(View.GONE);
                if(!SearchField.hasFocus()) {
                    Expand(CollapsableHeading);
                }
            }
        });
        SearchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    SearchField.clearFocus();
                    return true;
                }
                return false;
            }
        });
        SearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    List<ListItem> filteredList = FilterList(itemList, s.toString());
                    adapter.animateTo(filteredList);
                    DeleteQuery.setVisibility(View.VISIBLE);
                    if(filteredList.size() == 0) {
                        NotFound.setVisibility(View.VISIBLE);
                    } else {
                        NotFound.setVisibility(View.GONE);
                    }

                } else {
                    adapter.animateTo(itemList);
                    DeleteQuery.setVisibility(View.GONE);
                    NotFound.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }



    private void initializeList(final String JSON_URL) {
        

        for(int i = 0; i < itemList.size() - 1; i++){
            for(int j = i+1; j < itemList.size(); j++) {
                ListItem l1 = itemList.get(i);
                ListItem l2 = itemList.get(j);
                if(l1.getTitle().compareTo(l2.getTitle()) > 0) {
                    Collections.swap(itemList, i, j);
                }
            }
        }

        request = new JsonArrayRequest(JSON_URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject Object;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Object = response.getJSONObject(i);
                        ListItem item = new ListItem(Object.getString("title"), Object.getString("description"), Object.getJSONArray("article_data"));
                        itemList.add(item);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < itemList.size() - 1; i++) {
                    for (int j = i + 1; j < itemList.size(); j++) {
                        ListItem l1 = itemList.get(i);
                        ListItem l2 = itemList.get(j);
                        if (l1.getTitle().compareTo(l2.getTitle()) > 0) {
                            Collections.swap(itemList, i, j);
                        }
                    }
                }
                adapter = new CardViewRecyclerView(itemList, view.getContext());
                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
                recyclerView.setNestedScrollingEnabled(false);
                recyclerView.setAdapter(adapter);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue = Volley.newRequestQueue(view.getContext());
        requestQueue.add(request);
    }

    private List<ListItem> FilterList(List<ListItem> items ,String query) {
        query = query.toLowerCase();
        List<ListItem> filteredList = new ArrayList<>();
        for(int i = 0; i < items.size(); i++) {
            ListItem l = items.get(i);
            if(l.getTitle().toLowerCase().contains(query)) {
                filteredList.add(l);
            }
        }
        return filteredList;
    }

    private static void Expand(final View v) {
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ConstraintLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private static void Collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration((int)(initialHeight * 2/ v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
}

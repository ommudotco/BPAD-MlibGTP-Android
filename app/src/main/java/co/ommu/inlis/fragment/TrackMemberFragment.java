package co.ommu.inlis.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.ommu.inlis.MainActivity;
import co.ommu.inlis.R;
import co.ommu.inlis.components.AsynRestClient;
import co.ommu.inlis.components.CheckConnection;
import co.ommu.inlis.components.OnLoadMoreListener;
import co.ommu.inlis.components.Utility;
import co.ommu.inlis.inlis.adapter.TrackAdapter;
import co.ommu.inlis.inlis.model.TrackModel;
import cz.msebera.android.httpclient.Header;

public class TrackMemberFragment extends Fragment {
    public ArrayList<TrackModel> array = new ArrayList<TrackModel>();
    private String name = null;
    String url;
    String itemCount = "0", pageSize = "0", nextPage = "";
    String nextPager = "-";
    int pos = 0;

    RecyclerView recycleNotNull;
    TrackAdapter adapter;

    RelativeLayout btnError;
    ProgressBar pb;
    TextView tvKosong;

    public TrackMemberFragment(String name) {
        this.name = name;
        if (this.name == null || this.name == "views") {
            url = Utility.inlisViewListPathURL + "/data/JSON";
            pos = 0;
        } else if (this.name == "bookmarks") {
            url = Utility.inlisBookmarkListPathURL + "/data/JSON";
            pos = 1;
        } else if (this.name == "likes") {
            url = Utility.inlisLikeListPathURL + "/data/JSON";
            pos = 2;
        } else if (this.name == "favourites") {
            url = Utility.inlisFavouriteListPathURL + "/data/JSON";
            pos = 0;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_member, container, false);


        recycleNotNull = (RecyclerView) view.findViewById(R.id.recycleView);


        pb = (ProgressBar) view.findViewById(R.id.progressBar);
        btnError = (RelativeLayout) view.findViewById(R.id.rl_error);
        tvKosong = (TextView) view.findViewById(R.id.tv_null);

        Log.i("url member", url);
        buildError();
        if(CheckConnection.isOnline(getActivity())) {
            setList();
        } else {
            buildError();
        }
        return view;
    }

    private void build() {
        btnError.setVisibility(View.GONE);
        pb.setVisibility(View.GONE);

        if (array.size() == 0)
            tvKosong.setVisibility(View.VISIBLE);
        else
            tvKosong.setVisibility(View.GONE);


    }


    private void buildError() {

        pb.setVisibility(View.GONE);
        btnError.setVisibility(View.VISIBLE);
        tvKosong.setVisibility(View.GONE);
        btnError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setList();

            }
        });

    }

    private void setList() {

        array = new ArrayList<>();
        btnError.setVisibility(View.GONE);
        tvKosong.setVisibility(View.GONE);
        pb.setVisibility(View.VISIBLE);


        recycleNotNull.setHasFixedSize(true);
        recycleNotNull.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new TrackAdapter(getActivity(), array, recycleNotNull, true);
        recycleNotNull.setAdapter(adapter);
        getRequest(false, adapter);

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Log.e("haint", "Load More member");
                array.add(null);
                adapter.notifyItemInserted(array.size() - 1);
                //Load more data for reyclerview
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("haint", "Load More 2 member");
                        //Remove loading item
                       // array.remove(array.size() - 1);
                       // adapter.notifyItemRemoved(array.size());

                        if (!nextPager.equals("-")) {
                            getRequest(true, adapter);
                            Log.i("data load","atas"+nextPager);
                        }
                        else {
                            removeProgres();
                            Log.i("data load","bawah"+nextPager);
                        }


                    }
                }, 1000);
            }
        });

    }

    private void getRequest(final boolean isLoadmore, final TrackAdapter adap) {

        RequestParams params = new RequestParams();
        //params.put("token", MainActivity.token);

        String urlReq = "";
        if (!isLoadmore) {

            urlReq = url;

        } else {
            String[] split = nextPager.split(Utility.baseURL);
            urlReq = split[1];
        }


        AsynRestClient.post(getActivity(), urlReq, params, new JsonHttpResponseHandler() {

            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // TODO Auto-generated method stub
                try {


                    if (isLoadmore) {
                        removeProgres();
                    }

                    JSONArray ja = response.getJSONArray("data");

                    array.addAll(TrackModel.fromJson(ja, true));


                    JSONObject jo = response.getJSONObject("pager");
                    itemCount = jo.getString("itemCount");
                    pageSize = jo.getString("pageSize");
                    nextPage = jo.getString("nextPage");

                    nextPager = response.getString("nextPager");

                    // Log.i("DEBUG search", "_" + ja.toString());

                    if (!isLoadmore) {
                        build();
                    }


                    adap.notifyDataSetChanged();
                    adap.setLoaded();

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.i("infffffooo", "ada parsingan yg salah");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] header, String res, Throwable e) {
                // TODO Auto-generated method stub
                Log.i("data", "_" + statusCode);
                if (!isLoadmore) {
                    buildError();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] header, Throwable e, JSONObject jo) {
                if (!isLoadmore) {
                    buildError();
                }
            }


        });


    }
    private void removeProgres() {
        array.remove(array.size() - 1);
        adapter.notifyItemRemoved(array.size());
    }

}

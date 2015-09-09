package moosa.pana.com.fireauth;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;


public class LoggedInFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView textView;
    // TODO: Rename and change types of parameters
    private String userId;
    private String name;
    private String profilePicture;
    private String email;
    private ImageLoader imageLoader = MyApplication.getInstance().getImageLoader();
    private TextView nameText, emailText, idText;
    private NetworkImageView profilePic;

    public LoggedInFragment() {
        // Required empty public constructor
    }

    public static LoggedInFragment newInstance(Bundle bundle) {
        LoggedInFragment fragment = new LoggedInFragment();
        Bundle args = bundle;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("uid");
            name = getArguments().getString("name");
            profilePicture = getArguments().getString("dp");
            email = getArguments().getString("email");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_logged_in, container, false);
        nameText = (TextView) view.findViewById(R.id.name);
        idText = (TextView) view.findViewById(R.id.userId);
        emailText = (TextView) view.findViewById(R.id.email);
        profilePic = (NetworkImageView) view.findViewById(R.id.networkImageView);
        profilePic.setImageUrl(profilePicture, imageLoader);
        nameText.setText(name);
        idText.setText(userId);
        emailText.setText(email);

        return view;
    }

}

package com.midi.midi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by leejw on 2018-07-18.
 */

public class FragmentThird extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.main_page, container, false);
        TextView tv = v.findViewById(R.id.firstFrag);

        tv.setText(getArguments().getString("msg"));

        return v;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    public static FragmentThird newInstance(String text) {
        FragmentThird f = new FragmentThird();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }
}

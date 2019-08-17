package com.watchback2.android.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.watchback2.android.databinding.FragmentWalkthroughBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WalkthroughFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WalkthroughFragment extends Fragment {

    public static final String INTENT_IMAGE_URL = "imageUrl";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WalkthroughFragment.
     */
    public static WalkthroughFragment newInstance() {
        return new WalkthroughFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentWalkthroughBinding binding = FragmentWalkthroughBinding.inflate(inflater, container,
                false);

        assert getArguments() != null;
        binding.setImgSrc(getArguments().getString(INTENT_IMAGE_URL));
        binding.executePendingBindings();

        return binding.getRoot();
    }

}

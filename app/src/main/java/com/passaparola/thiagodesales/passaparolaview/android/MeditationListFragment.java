package com.passaparola.thiagodesales.passaparolaview.android;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.passaparola.thiagodesales.passaparolaview.R;
import com.passaparola.thiagodesales.passaparolaview.adapters.MeditationListAdapter;
import com.passaparola.thiagodesales.passaparolaview.facade.Facade;
import com.passaparola.thiagodesales.passaparolaview.listeners.MeditationListener;
import com.passaparola.thiagodesales.passaparolaview.listeners.MyOnOptionsClickListener;
import com.passaparola.thiagodesales.passaparolaview.model.RSSMeditationItem;
import com.passaparola.thiagodesales.passaparolaview.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class MeditationListFragment extends Fragment implements MyOnOptionsClickListener, MeditationListener {

    private RecyclerView meditationListRecyclerView;
    private ArrayList<RSSMeditationItem> meditationsList;
    private MeditationListAdapter listAdapter;
    private Context context;
    private Facade facade;
    private String languageId;
    private List<String> supportedLanguageList;
    private MeditationListener meditationListener;

    public MeditationListFragment(Context context, String languageId, MeditationListener meditationListener) {
        this.context = context;
        this.languageId = languageId;
        this.meditationListener = meditationListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_scrolling, container, false);
        meditationListRecyclerView = (RecyclerView) view.findViewById(R.id.meditationsRecyclerView);

//        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
        meditationListRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        meditationsList = new ArrayList<>();
        listAdapter = new MeditationListAdapter(meditationsList, this, meditationListRecyclerView, languageId);
        listAdapter.setCurrentParolaLanguage(languageId);
        meditationListRecyclerView.setAdapter(listAdapter);

        meditationListRecyclerView.addItemDecoration(
                new DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("onStart", "MeditationListFram started!");
        supportedLanguageList = Arrays.asList(getResources().getStringArray(R.array.supported_meditations));
        Log.d("supportedLanguageList", "supportedLanguageList criada!");
        facade = Facade.getInstance(this.context);
        facade.addMeditationListeners(this);
        requestMeditations();
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        Log.d("onInflate", "onInflate started!");
    }

    //TODO Está deixando a lista view vazia quando não há suporte ao idioma e vem NOVAS meditações de download
    public void setCurrentParolaLanguage(String languageId) {
        this.languageId = languageId;

        if (supportedLanguageList.contains(languageId)) {
            listAdapter.setCurrentParolaLanguage(languageId);
            meditationListRecyclerView.invalidate();
            meditationListRecyclerView.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
        }

    }

    public void requestMeditations() {
        ArrayList<RSSMeditationItem> localMeditationsList = facade.getAllMeditations();
        Log.d("requestMeditations", "Numero retornado: " + localMeditationsList.size());

        //TODO this is also at database. Must go to utils.
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        df.setTimeZone(TimeZone.getTimeZone( "GMT-03:00" )); //Brazil :)
        String today = df.format(Calendar.getInstance().getTime());

        if (localMeditationsList.size() > 0 && !Utils.isFirstAfterSecond(today, localMeditationsList.get(0).getPublishedDate())) {
            Log.d("requestMeditations", "A primeira é " + localMeditationsList.get(0).getParolas().get("pt"));
            Log.d("requestMeditations", "VERDADEIRAO");
            feedUI(localMeditationsList);
        } else {
            Log.d("requestMeditations", "baixando da internet");
            facade.downloadMeditations();
        }
    }

    private void feedUI(ArrayList<RSSMeditationItem> meditationList) {
        Log.d("feedUI", "preenchendo UI da lista " + meditationList.get(0).getPublishedDate());
        meditationsList.addAll(meditationList);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNewMeditation(ArrayList<RSSMeditationItem> meditations) {
        Log.d("onNewMeditation", "Chegou meditacoes da rede: " + meditations.size());
        feedUI(meditations);
    }

    @Override
    public void onReadMeditation(RSSMeditationItem meditationItem) {
        Log.d("onRead", "Ler meditacao " + meditationItem.getPublishedDate());
        ArrayList<RSSMeditationItem> meditationList = new ArrayList<>();
        meditationList.add(meditationItem);
        meditationListener.onNewMeditation(meditationList);

    }

    @Override
    public void onReadExperiences(RSSMeditationItem meditationItem) {
        Log.d("onRead", "Ler experiencias " + meditationItem.getPublishedDate());
    }

    @Override
    public void onWriteExperiences(RSSMeditationItem meditationItem) {
        Log.d("onRead", "Escrever experiencias" + meditationItem.getPublishedDate());
    }

    @Override
    public void onShareMeditation(RSSMeditationItem meditationItem) {
        Log.d("onRead", "Compartilhar meditacao " + meditationItem.getPublishedDate());
    }

    //    @Override
//    public void onClick(View view) {
//        int id = view.getId();
//
////
//        switch (id) {
//            case R.id.view_meditation_image_view:
//                Log.d("onClick", "view_meditation_image_view");
//                int meditationSelectedPosition = meditationListRecyclerView.getChildAdapterPosition(view);
//                RSSMeditationItem selectedMeditation = meditationsList.get(meditationSelectedPosition);
//                Log.d("onClick", "view sleecionada: " + selectedMeditation.getPublishedDate());
//                break;
//            case R.id.meditation_text_view:
//                Log.d("onClick", "meditation_text_view");
//                break;
//            case R.id.share_meditation_image_view:
//                Log.d("onClick", "share_meditation_image_view");
//                break;
//        }
//
////        int meditationSelectedPosition = meditationListRecyclerView.getChildAdapterPosition(view);
////        RSSMeditationItem selectedMeditation = meditationsList.get(meditationSelectedPosition);
//
//    }
}

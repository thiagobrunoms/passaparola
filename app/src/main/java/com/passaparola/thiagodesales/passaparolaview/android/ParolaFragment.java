package com.passaparola.thiagodesales.passaparolaview.android;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.passaparola.thiagodesales.passaparolaview.R;
import com.passaparola.thiagodesales.passaparolaview.connection.Connections;
import com.passaparola.thiagodesales.passaparolaview.database.DatabaseDataManagement;
import com.passaparola.thiagodesales.passaparolaview.facade.Facade;
import com.passaparola.thiagodesales.passaparolaview.listeners.ParolaListener;
import com.passaparola.thiagodesales.passaparolaview.model.Parola;

import java.util.HashMap;

public class ParolaFragment extends Fragment implements ParolaListener {

    private TextView parolaPraseTextView;
    private TextView parolaDateTextView;
    private ImageView flagImageView;
    private View view;
    private DatabaseDataManagement db;
    private String currentLanguageId;
    private Connections connectionManager;
    private Context context;
    private Facade facade;

    public ParolaFragment(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d("onCreateView da Parola", "Criando view da parola");
        view = inflater.inflate(R.layout.parola_main_phrase, container, false);

        parolaPraseTextView = view.findViewById(R.id.parola_phrase_id);
        parolaDateTextView = view.findViewById(R.id.passa_parola_date_textview_id);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        facade = Facade.getInstance(this.context);
        facade.addParolaListerner(this);

        requestParola();
    }

    public void setCurrentParolaLanguage(String languageId) {
        currentLanguageId = languageId;
    }

    public void requestParola() {
        HashMap<String, Parola> lastParolas = facade.readParolas();

        if (lastParolas.size() == 0 || lastParolas.get(currentLanguageId) == null) {
            facade.downloadParola(currentLanguageId);
        } else
            feedUI(lastParolas.get(currentLanguageId));

        flagImageView = view.findViewById(R.id.flag);
        flagImageView.setImageResource(getResources().getIdentifier(currentLanguageId, "drawable", getContext().getPackageName()));
    }

    private void feedUI(Parola parola) {
        parolaDateTextView.setText(parola.getDate());
        parolaPraseTextView.setText(parola.getParola());
    }

    @Override
    public void onNewParola(Parola parola) {
        feedUI(parola);
    }
}

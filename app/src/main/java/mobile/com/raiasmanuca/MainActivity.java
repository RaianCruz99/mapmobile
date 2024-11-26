package mobile.com.raiasmanuca;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        // botão que navega para a tela de configuração de mapa
        Button btnMapConfig = findViewById(R.id.btnMapConfig);

        // navega para tela de gravação de trilha
        Button btnRecordTrail = findViewById(R.id.btnRecordTrail);

        // navega para a tela de visualização de trilha
        Button btnViewTrail = findViewById(R.id.btnViewTrail);

        // cada botão tem um onClickListener para iniciar a atividade correspondente
        // ao próprio

        // código em lambda que substitui a estrutura tradicional do onClick.
        // cria um intent, descrevendo uma ação a ser realizada, que no caso é a classe.
        btnMapConfig.setOnClickListener(v ->
                startActivity(new Intent(this, MapConfigActivity.class)));

        btnRecordTrail.setOnClickListener(v ->
                startActivity(new Intent(this, TrailRecordActivity.class)));

        btnViewTrail.setOnClickListener(v ->
                startActivity(new Intent(this, TrailViewActivity.class)));
    }
}
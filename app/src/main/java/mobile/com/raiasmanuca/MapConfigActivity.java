package mobile.com.raiasmanuca;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

// classe para configuração do mapa
public class MapConfigActivity extends AppCompatActivity {
    // armazena (salva) configurações persistentes do mapa:
    // isSatelite: define a visualização do mapa em modo satelite ou não
    // isCourseUp: define se o mapa rotaciona conforme o usuário rotacionar.
    private SharedPreferences prefs;

    // método que é executado assim que a atividade é criada
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        // layout da tela com xml
        setContentView(R.layout.activity_map_config);

        prefs = getSharedPreferences("MapSettings", MODE_PRIVATE);

        RadioGroup mapTypeGroup = findViewById(R.id.mapTypeGroup);
        RadioGroup navigationModeGroup = findViewById(R.id.navigationModeGroup);
        Button saveButton = findViewById(R.id.saveButton);

        // CARREGA AS PREFERÊNCIAS SALVAS:
        // isSatellite Indica se o tipo do mapa está configurado como satélite (true)
        boolean isSatellite = prefs.getBoolean("isSatellite", false);
        // isCourseUp: Indica se a orientação da câmera segue o curso (true)
        boolean isCourseUp = prefs.getBoolean("isCourseUp", false);


        ((RadioButton) findViewById(isSatellite ? R.id.satelliteType : R.id.normalType)).setChecked(true);
        ((RadioButton) findViewById(isCourseUp ? R.id.courseUp : R.id.northUp)).setChecked(true);

        // Save preferences on button click
        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isSatellite", mapTypeGroup.getCheckedRadioButtonId() == R.id.satelliteType);
            editor.putBoolean("isCourseUp", navigationModeGroup.getCheckedRadioButtonId() == R.id.courseUp);
            editor.apply();
            Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show();
        });
    }
}
package mobile.com.raiasmanuca;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

// Esta classe é responsável por exibir no mapa uma trilha salva no banco de dados,
// calcular a distância e a velocidade média do trajeto,
// e apresentar algumas informações sobre o percurso.
public class TrailViewActivity extends AppCompatActivity {
    // Variável que armazena o objeto GoogleMap
    private GoogleMap mMap;
    // Instância de DatabaseHelper, que é responsável por gerenciar o banco de dados do aplicativo
    private DatabaseHelper dbHelper;
    // Um TextView usado para exibir informações sobre a trilha, como a duração, distância e velocidade média.
    private TextView infoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_trail_view);

        dbHelper = new DatabaseHelper(this);
        // Inicializa o TextView onde as informações sobre a trilha serão exibidas.
        infoText = findViewById(R.id.trailInfoText);

        //  Obtém uma referência ao SupportMapFragment, que exibe o mapa na tela.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            // Solicita o carregamento assíncrono do mapa e, quando o mapa estiver pronto, o método onMapReady será chamado.
            mapFragment.getMapAsync(this::onMapReady);
        }
    }

    // Este método é chamado quando o GoogleMap está pronto para ser usado.
    private void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // é chamado para carregar os dados da trilha e exibi-los no mapa.
        loadTrailData();
    }

    // é responsável por carregar os dados da trilha do banco de dados e exibi-los no mapa.
    @SuppressLint("Range")
    private void loadTrailData() {
        // Consulta o banco de dados para obter todos os pontos de trilha.
        // Consulta da tabela TABLE_TRAILS
        // Coluna COLUMN_TIMESTAMP em ordem crescente
        Cursor cursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_TRAILS,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_TIMESTAMP + " ASC" // Ordem cronológica
        );

        // Se não houver dados, uma mensagem é exibida informando que nenhuma trilha foi encontrada.
        if (cursor == null || !cursor.moveToFirst()) {
            infoText.setText("Nenhuma trilha encontrada.");
            return;
        }

        // Armazena os pontos de latitude e longitude da trilha.
        ArrayList<LatLng> points = new ArrayList<>();
        long startTime = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP));
        long endTime = startTime;
        float totalDistance = 0f;
        Location lastPoint = null;
        // Usado para ajustar a visualização do mapa com base nos pontos da trilha.
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        // O do-while percorre cada linha do cursor (resultado da consulta ao banco de dados)
        // e extrai as informações de latitude, longitude e timestamp.
        do {
            double lat = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LATITUDE));
            double lng = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LONGITUDE));
            long timestamp = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP));

            LatLng point = new LatLng(lat, lng);
            points.add(point);
            boundsBuilder.include(point);

            // Calcula a distância entre pontos consecutivos
            if (lastPoint != null) {
                Location currentPoint = new Location("");
                currentPoint.setLatitude(lat);
                currentPoint.setLongitude(lng);
                // A cada ponto, é calculada a distância entre o ponto atual e o ponto anterior utilizando
                // o método distanceTo() da classe Location.
                totalDistance += lastPoint.distanceTo(currentPoint);
            }

            Location currentLocation = new Location("");
            currentLocation.setLatitude(lat);
            currentLocation.setLongitude(lng);
            lastPoint = currentLocation;

            endTime = timestamp;
        } while (cursor.moveToNext());
        cursor.close();

        // Adiciona os pontos ao mapa e
        // calcula os limites da trilha (para centralizar o mapa na área da trilha).
        // Se a lista de pontos não estiver vazia,
        // é criada uma PolylineOptions que representa a trilha, com os pontos da lista.
        if (!points.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(points)
                    .width(5)
                    .color(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            //  A polyline é adicionada ao mapa com a cor definida
            mMap.addPolyline(polylineOptions);

            // O mapa é centralizado nos limites da trilha utilizando animateCamera() e a LatLngBounds calculada.
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
        }

        // Calcula a duração e a velocidade média
        // A duração total da trilha é calculada subtraindo o timestamp de startTime de endTime.
        long durationMillis = endTime - startTime;
        // A duração é convertida de milissegundos para horas.
        float durationHours = durationMillis / (3600f * 1000);
        //  A velocidade média é calculada dividindo a distância total pela duração em horas.
        float averageSpeed = (durationHours > 0) ? (totalDistance / 1000f) / durationHours : 0;

        // Logs para depuração
        Log.d("TrailViewActivity", "Duração em milissegundos: " + durationMillis);
        Log.d("TrailViewActivity", "Duração em horas: " + durationHours);
        Log.d("TrailViewActivity", "Distância total (km): " + (totalDistance / 1000f));
        Log.d("TrailViewActivity", "Velocidade média (km/h): " + averageSpeed);

        // Formata os dados e exibe na interface com simple date format.
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        long durationSeconds = durationMillis / 1000;
        String info = String.format(Locale.getDefault(),
                "Início: %s\nDuração: %02d:%02d:%02d\nDistância: %.2f km\nVelocidade Média: %.2f km/h",
                sdf.format(startTime),
                durationSeconds / 3600, (durationSeconds % 3600) / 60, (durationSeconds % 60),
                totalDistance / 1000f, averageSpeed);
        // o texto formatado (na String info) é exibido na interface usando o TextView
        infoText.setText(info);
    }
}
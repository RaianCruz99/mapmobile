package mobile.com.raiasmanuca;

import android.Manifest;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.UUID;

// Responsável por gerenciar a gravação de uma trilha
public class TrailRecordActivity extends AppCompatActivity {
    //Código de solicitação de permissão de localização.
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Objeto para manipular o mapa do Google Maps.
    private GoogleMap mMap;
    // Cliente para obter a localização atual do dispositivo.
    private FusedLocationProviderClient fusedLocationClient;
    // Callback para receber atualizações de localização.
    private LocationCallback locationCallback;
    // Acesso ao banco de dados para armazenar as coordenadas.
    private DatabaseHelper dbHelper;
    // ID único da trilha
    private String currentTrailId;
    // Cronômetro para medir o tempo de gravação da trilha.
    private Chronometer chronometer;
    //  Exibem a velocidade e a distância percorrida em tempo real.
    private TextView speedText;
    private TextView distanceText;
    // Distância total percorrida.
    private float totalDistance = 0;
    // Armazena a última localização para calcular a distância.
    private Location lastLocation;
    //  Para recuperar as configurações de mapa salvas (como tipo de mapa e modo de navegação).
    private SharedPreferences prefs;
    // Configurações para desenhar a linha da trilha no mapa.
    private PolylineOptions polylineOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        // Define o layout da atividade.
        setContentView(R.layout.activity_trail_record);

        //  Carrega as configurações de mapa salvas.
        prefs = getSharedPreferences("MapSettings", MODE_PRIVATE);
        //  Inicializa o helper do banco de dados.
        dbHelper = new DatabaseHelper(this);
        // Gera um ID único para a trilha. UUID faz com que o ID fique quase impossível de ser
        // repetido!!11!111!!
        currentTrailId = UUID.randomUUID().toString();
        // Recupera o cronômetro da interface.
        chronometer = findViewById(R.id.chronometer);
        // Recuperam as TextView para exibir a velocidade e distância.
        speedText = findViewById(R.id.speedText);
        distanceText = findViewById(R.id.distanceText);

        // Configura o estilo da linha que será desenhada no mapa.
        polylineOptions = new PolylineOptions().width(5).color(ContextCompat.getColor(this, android.R.color.holo_red_dark));

        // Recupera o fragmento do mapa e o configura.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this::onMapReady);

        // Verifica e pede permissões de localização, caso necessário,
        // e configura a atualização da localização.
        if (checkLocationPermission()) {
            setupLocationUpdates();
        }
        // Inicia o cronômetro.
        chronometer.start();
    }


    private void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Habilita a localização no mapa, se a permissão de localização foi concedida.
        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }

        // Apply saved settings
        boolean isSatellite = prefs.getBoolean("isSatellite", false);
        boolean isCourseUp = prefs.getBoolean("isCourseUp", false);

        // se isSatellite for true, valor após interrogação será escolhido.
        // se false, será escolhido o valor após dois pontos
        mMap.setMapType(isSatellite ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);

        // Se isCourseUp for verdadeiro:
        // Se isCourseUp for verdadeiro, ativa a rotação e o uso da bússola.
        // Caso contrário, desativa essas opções.
        if (isCourseUp) {
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        } else {
            mMap.getUiSettings().setRotateGesturesEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
        }
    }

    // Verifica se a permissão de acesso à localização foi concedida.
    // Se não for concedida, solicita a permissão ao usuário.
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }


    private void setupLocationUpdates() {
        // Obtém o cliente para atualizar a localização.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Define uma solicitação de localização com alta precisão
        // e intervalo de 5 segundos entre atualizações.
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000);

        // Define o callback para quando uma nova localização for recebida,
        // chamando o método updateLocationInfo(location)

        locationCallback = new LocationCallback() {
            @Override
            // chamado automaticamente sempre que o sistema obtém uma nova localização.
            public void onLocationResult(LocationResult locationResult) {
                //  Esse parâmetro contém os dados de localização atualizados.
                // getLastLocation() é chamado para obter a última localização disponível dentro do objeto
                Location location = locationResult.getLastLocation();
                // responsável por atualizar a interface de usuário (UI) com as novas informações
                updateLocationInfo(location);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Inicia as atualizações de localização se a permissão for concedida.
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    // responsável por processar e atualizar as informações da localização que foi obtida
    private void updateLocationInfo(Location location) {
        // o método verifica se a localização fornecida é nula. Se for nula, o método retorna imediatamente,
        //  já que não faz sentido tentar processar uma localização inexistente.
        if (location == null) return;

        // cria um objeto LatLng que representa as coordenadas de latitude e longitude da localização.
        // Esse objeto será usado para atualizar o mapa com a nova posição.
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Atualiza mapa
        // Se o mapa (mMap) não for nulo, o método executa as seguintes ações:
        if (mMap != null) {
            // O mapa será movido para a nova posição, usando as coordenadas currentLatLng e um nível de zoom de 17 (o zoom mais próximo).
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17));
            // A nova localização é adicionada à linha poligonal que representa a trilha que está sendo percorrida.
            polylineOptions.add(currentLatLng);
            // O mapa é atualizado com a linha que conecta todas as localizações anteriores à nova.
            mMap.addPolyline(polylineOptions);

            // Se CourseUp estiver ativado:
            // A câmera do mapa será rotacionada para acompanhar a direção da trilha.
            if (prefs.getBoolean("isCourseUp", false) && location.hasBearing()) {
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                        new com.google.android.gms.maps.model.CameraPosition.Builder()
                                .target(currentLatLng)
                                .zoom(17)
                                .bearing(location.getBearing())
                                .build()));
            }
        }

        //  se lastLocation não for nulo:
        if (lastLocation != null) {
            float distance = lastLocation.distanceTo(location);
            // A distância total percorrida é acumulada
            totalDistance += distance;
            //  A distância e a velocidade (convertida de metros por segundo para km/h)
            //  são atualizadas na interface de usuário, nas TextViews
            distanceText.setText(String.format("Distância: %.2f km", totalDistance / 1000));
            speedText.setText(String.format("Velocidade: %.1f km/h", location.getSpeed() * 3.6));
        }

        // A localização (latitude, longitude) e o timestamp atual são salvos no banco de dados.
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TRAIL_ID, currentTrailId);
        values.put(DatabaseHelper.COLUMN_LATITUDE, location.getLatitude());
        values.put(DatabaseHelper.COLUMN_LONGITUDE, location.getLongitude());
        values.put(DatabaseHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());

        dbHelper.getWritableDatabase().insert(DatabaseHelper.TABLE_TRAILS, null, values);

        // Por fim, a lastLocation é atualizada para a nova localização
        lastLocation = location;
    }

    @Override
    protected void onDestroy() {
        // garante que o comportamento padrão de destruição da Activity seja executado corretamente.
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            // para as atualizações de localização e libera os recursos utilizados para receber essas atualizações.
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        // para o cronômetro
        chronometer.stop();
        // dá close no SQLite
        dbHelper.close();
    }
}
package mobile.com.raiasmanuca;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// SQLiteOpenHelper é uma classe que usa o SQLite.
// Provê métodos para criar e atualizar DBs.
public class DatabaseHelper extends SQLiteOpenHelper {
    // nome do banco
    public static final String DATABASE_NAME = "TrailDatabase.db";
    // versão do banco. todas as vezes que for incrementada, chamará o método onUpgrade
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_TRAILS = "trails";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TRAIL_ID = "trail_id"; // Nova coluna para identificar a trilha
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // define a tabela trails, especifica os nomes das colunas e tipos de dados
    private static final String CREATE_TABLE_TRAILS =
            "CREATE TABLE " + TABLE_TRAILS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TRAIL_ID + " TEXT NOT NULL, " +
                    COLUMN_LATITUDE + " REAL NOT NULL, " +
                    COLUMN_LONGITUDE + " REAL NOT NULL, " +
                    COLUMN_TIMESTAMP + " INTEGER NOT NULL)";

    // chama o construtor da classe SQLiteOpenHelper
    // Context: Contexto do APP, DB_NAME: nome do db, null: cursor de consultas e versão do banco
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // chame automaticamente na primeira vez que o banco é acessado
    // cria a estrutura inicial do banco
    // executa o comando sql pra criar a tabela trails
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TRAILS);
    }

    // chamado quando o banco é acessado e sua versão é incrementada.
    // atualiza o banco para um esquema mais recente
    // garante a compatibilidade entre versões

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_TRAILS + " ADD COLUMN " + COLUMN_TRAIL_ID + " TEXT NOT NULL DEFAULT ''");
        }
    }
}

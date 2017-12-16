/*
 * Copyright 2017 sukawasatoru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.tinyport.tinytable.example.addcolumn;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jp.tinyport.tinytable.Table;

class DbOpenHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "add-column.db";

    final Table table;
    private Consumer<SQLiteDatabase> mInitialPopulation;
    private final SparseArray<Consumer<SQLiteDatabase>> mMigrateConsumers;

    DbOpenHelper(Context context, Version version) {
        super(context, DATABASE_NAME, null, version.version);
        switch (version) {
            case OLD:
                table = new OldTable();
                break;
            case INTERMEDIATE:
                throw new UnsupportedOperationException("skip");
            case LATEST:
                table = new LatestTable();
                break;
            default:
                throw new UnsupportedOperationException();
        }

        mMigrateConsumers = new SparseArray<>();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(table.createSql());
        if (mInitialPopulation != null) {
            mInitialPopulation.accept(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgrade(db, oldVersion, newVersion);
        diffMigrate(db, table);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        downgrade(db, oldVersion, newVersion);
        diffMigrate(db, table);
    }

    public void setInitialPopulation(Consumer<SQLiteDatabase> consumer) {
        mInitialPopulation = consumer;
    }

    public void addMigrateStep(int version, Consumer<SQLiteDatabase> consumer) {
        mMigrateConsumers.put(version, consumer);
    }

    private void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int index = 0, l = mMigrateConsumers.size(); index < l; index++) {
            final int version = mMigrateConsumers.keyAt(index);
            if (oldVersion < version && version <= newVersion) {
                mMigrateConsumers.valueAt(index).accept(db);
            }
        }
    }

    private void downgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException("TODO");
    }

    private static void diffMigrate(SQLiteDatabase db, Table table) {
        final String tmpTable = "tmp_old_table_" + System.currentTimeMillis();
        db.execSQL("ALTER TABLE " + table.tableName + " RENAME TO " + tmpTable);
        db.execSQL(table.createSql());
        final List<String> source =
                new ArrayList<>(Arrays.asList(retrieveColumns(db, tmpTable)));
        final List<String> target = Arrays.stream(table.getColumns())
                .map(column -> column.name)
                .collect(Collectors.toList());

        source.retainAll(target);
        db.execSQL("INSERT INTO " + table.tableName + " (" + TextUtils.join(", ", source) +
        ") SELECT " + TextUtils.join(", ", source) + " FROM " + tmpTable);
        db.execSQL("DROP TABLE " + tmpTable);
    }

    private static String[] retrieveColumns(SQLiteDatabase db, String table) {
        try (Cursor c = db.query(table, null, null, null, null, null, null, "1")) {
            return c.getColumnNames();
        }
    }

    enum Version {
        OLD(1), INTERMEDIATE(2), LATEST(3);

        final int version;

        Version(int version) {
            this.version = version;
        }
    }

    static class OldTable extends Table {
        static final Column ID = new Column("id", INTEGER, PRIMARY_KEY, AUTOINCREMENT, NOT_NULL);
        static final Column SPECIAL_COLUMN = new Column("special_column", TEXT);

        OldTable() {
            super("add_column",
                    ID,
                    SPECIAL_COLUMN
            );
        }

        OldTable(String tableName, Column column, Column... rest) {
            super(tableName, column, rest);
        }
    }

    @SuppressWarnings("unused")
    static class IntermediateTable extends OldTable {
        static final Column INTERMEDIATE_COLUMN = new Column("intermediate", TEXT);

        IntermediateTable() {
            super("add_column",
                    ID,
                    SPECIAL_COLUMN,
                    INTERMEDIATE_COLUMN
            );
        }

        IntermediateTable(String tableName, Column column, Column... rest) {
            super(tableName, column, rest);
        }
    }

    static class LatestTable extends IntermediateTable {
        static final Column NEW_COLUMN = new Column("val", TEXT);

        LatestTable() {
            super("add_column",
                    ID,
                    SPECIAL_COLUMN,
                    INTERMEDIATE_COLUMN,
                    NEW_COLUMN
            );
        }
    }
}

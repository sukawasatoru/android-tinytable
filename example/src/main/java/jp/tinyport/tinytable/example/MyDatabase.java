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

package jp.tinyport.tinytable.example;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import jp.tinyport.tinytable.Table;

class MyDatabase extends SQLiteOpenHelper {
    final ExampleTable exampleTable;
    final Example2Table example2Table;
    final Example3Table example3Table;
    final Example4Table example4Table;

    MyDatabase(Context context) {
        super(context, "example.db", null, 1);

        exampleTable = new ExampleTable();
        example2Table = new Example2Table();
        example3Table = new Example3Table(example2Table);
        example4Table = new Example4Table();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(exampleTable.createSql());
        db.execSQL(example2Table.createSql());
        db.execSQL(example3Table.createSql());
        db.execSQL(example4Table.createSql());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new UnsupportedOperationException("TODO");
    }

    static class ExampleTable extends Table {
        ExampleTable() {
            super("example",
                    new Column("id", INTEGER, PRIMARY_KEY, AUTOINCREMENT, NOT_NULL),
                    new Column("ex_text", TEXT),
                    new Column("ex_numeric", NUMERIC),
                    new Column("ex_real", REAL),
                    new Column("ex_blob", BLOB),
                    new Column("ex_default", TEXT, DEFAULT("def"))
            );
        }
    }

    static class Example2Table extends Table {
        static final Column ID = new Column("id", INTEGER, NOT_NULL);
        static final Column VAL = new Column("val", TEXT);
        static final Column TEXT1 = new Column("text1", TEXT);
        static final Column TEXT2 = new Column("text2", TEXT);

        Example2Table() {
            super("example2",
                    ID,
                    VAL,
                    TEXT1,
                    TEXT2,
                    new PrimaryKey(ID.name, VAL.name),
                    new Unique(TEXT1.name, TEXT2.name)
            );
        }
    }

    static class Example3Table extends Table {
        Example3Table(Example2Table example2Table) {
            super("example3",
                    new Column("id", INTEGER, PRIMARY_KEY, NOT_NULL),
                    new ForeignKey("id", example2Table, Example2Table.ID)
            );
        }
    }

    static class Example4Table extends Table {
        Example4Table() {
            super("example4",
                    new Column("ex_integer", INTEGER),
                    new Column("ex_int", INT),
                    new Column("ex_tinyint", TINYINT),
                    new Column("ex_smallint", SMALLINT),
                    new Column("ex_mediumint", MEDIUMINT),
                    new Column("ex_bugint", BIGINT),
                    new Column("ex_unsigned_bigint", UNSIGNED_BIG_INT),
                    new Column("ex_int2", INT2),
                    new Column("ex_int8", INT8),
                    new Column("ex_text", TEXT),
                    new Column("ex_clob", CLOB),
                    new Column("ex_blob", BLOB),
                    new Column("ex_real", REAL),
                    new Column("ex_double", DOUBLE),
                    new Column("ex_double_precision", DOUBLE_PRECISION),
                    new Column("ex_float", FLOAT),
                    new Column("ex_numeric", NUMERIC),
                    new Column("ex_boolean", BOOLEAN),
                    new Column("ex_date", DATE),
                    new Column("ex_datetime", DATETIME)
            );
        }
    }
}

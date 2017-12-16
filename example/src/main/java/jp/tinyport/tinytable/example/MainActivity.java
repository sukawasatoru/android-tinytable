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

import android.app.Activity;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import jp.tinyport.tinytable.example.addcolumn.AddColumnDatabase;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("[MainActivity] Hello");

        final MyDatabase db = new MyDatabase(getApplicationContext());
        deleteDatabase(db.getDatabaseName());

        log("[MainActivity] ExampleTable=%s", db.exampleTable.createSql());
        log("[MainActivity] Example2Table=%s", db.example2Table.createSql());
        log("[MainActivity] Example3Table=%s", db.example3Table.createSql());
        log("[MainActivity] Example4Table=%s", db.example4Table.createSql());
        db.getWritableDatabase();

        deleteDatabase(AddColumnDatabase.DATABASE_NAME);

        final AddColumnDatabase addColumnDatabase = AddColumnDatabase.instance();
        final SQLiteDatabase oldDb = addColumnDatabase.getDatabaseOld(this);
        try (Cursor c = oldDb.query(addColumnDatabase.oldTable.tableName, null,
                null, null, null, null, null)) {
            log("[MainActivity] OldTable=%s", DatabaseUtils.dumpCursorToString(c));
        }
        oldDb.close();

        final SQLiteDatabase latestDb = addColumnDatabase.getDatabase(this);
        try (Cursor c = latestDb.query(addColumnDatabase.table.tableName, null,
                null, null, null, null, null)) {
            log("[MainActivity] newTable=%s", DatabaseUtils.dumpCursorToString(c));
        }

        log("[MainActivity] Bye");
        finish();
    }

    private static void log(String message, Object... args) {
        Log.i("TINYTABLE", String.format(message, args));
    }
}

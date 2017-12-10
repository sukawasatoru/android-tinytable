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
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("[MainActivity] Hello");

        final MyDatabase db = new MyDatabase(getApplicationContext());
        final File dbFile = getDatabasePath(db.getDatabaseName());
        if (dbFile.exists()) {
            final boolean ret = dbFile.delete();
            if (!ret) {
                throw new AssertionError();
            }
        }

        log("[MainActivity] ExampleTable=%s", db.exampleTable.createSql());
        log("[MainActivity] Example2Table=%s", db.example2Table.createSql());
        log("[MainActivity] Example3Table=%s", db.example3Table.createSql());
        final SQLiteDatabase database = db.getWritableDatabase();
        final ContentValues values = new ContentValues();
        values.put("id", 1);
        database.insert(db.example3Table.tableName, null, values);

        log("[MainActivity] Bye");
        finish();
    }

    private static void log(String message, Object... args) {
        Log.i("TINYTABLE", String.format(message, args));
    }
}

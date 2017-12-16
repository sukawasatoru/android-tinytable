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

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import jp.tinyport.tinytable.Table;

public final class AddColumnDatabase {
    public static final String DATABASE_NAME = DbOpenHelper.DATABASE_NAME;

    public Table oldTable;
    public Table table;

    private DbOpenHelper mOpenHelperOld;
    private DbOpenHelper mOpenHelper;

    private AddColumnDatabase() {
    }

    public static AddColumnDatabase instance() {
        return Holder.INSTANCE;
    }

    public synchronized SQLiteDatabase getDatabaseOld(Context context) {
        if (mOpenHelperOld == null) {
            mOpenHelperOld = new DbOpenHelper(context, DbOpenHelper.Version.OLD);
            oldTable = mOpenHelperOld.table;

            mOpenHelperOld.setInitialPopulation(db -> {
                final ContentValues values = new ContentValues();
                values.put(DbOpenHelper.OldTable.SPECIAL_COLUMN.name, SpecialType.NONE.type);
                values.put(DbOpenHelper.OldTable.SPECIAL_COLUMN.name,
                        SpecialType.SPECIAL.type);
                db.insert(mOpenHelperOld.table.tableName, null, values);
            });
        }

        return mOpenHelperOld.getWritableDatabase();
    }

    public synchronized SQLiteDatabase getDatabase(Context context) {
        if (mOpenHelper == null) {
            mOpenHelper = new DbOpenHelper(context, DbOpenHelper.Version.LATEST);
            table = mOpenHelper.table;

            mOpenHelper.addMigrateStep(DbOpenHelper.Version.INTERMEDIATE.version, db -> {
                db.execSQL(DbOpenHelper.IntermediateTable.INTERMEDIATE_COLUMN.createAddSql(
                        mOpenHelper.table.tableName));

                final ContentValues values = new ContentValues();
                values.put(DbOpenHelper.IntermediateTable.INTERMEDIATE_COLUMN.name,
                        "special!");
                db.update(mOpenHelper.table.tableName, values,
                        DbOpenHelper.IntermediateTable.SPECIAL_COLUMN.name + " = ?",
                        new String[]{
                                SpecialType.SPECIAL.type
                        });
            });
        }

        return mOpenHelper.getWritableDatabase();
    }

    enum SpecialType {
        SPECIAL("special"), NONE("none");

        final String type;

        SpecialType(String type) {
            this.type = type;
        }
    }

    private static class Holder {
        static final AddColumnDatabase INSTANCE = new AddColumnDatabase();

        Holder() {
        }
    }
}

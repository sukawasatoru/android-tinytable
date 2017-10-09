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

package jp.tinyport.tinytable;

import android.database.DatabaseUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Array;

public abstract class Table {
    @NonNull
    public final String tableName;

    protected static final Type TEXT = new Type("TEXT");
    protected static final Type NUMERIC = new Type("NUMERIC");
    protected static final Type INTEGER = new Type("INTEGER");
    protected static final Type REAL = new Type("REAL");
    protected static final Type BLOB = new Type("BLOB");
    protected static final Attribute PRIMARY_KEY = new Attribute("PRIMARY KEY");
    protected static final Attribute UNIQUE = new Attribute("UNIQUE");
    protected static final Attribute NOT_NULL = new Attribute("NOT NULL");
    protected static final Attribute AUTOINCREMENT = new Attribute("AUTOINCREMENT");

    @NonNull
    private final Column[] mColumns;

    public Table(@NonNull String tableName, @NonNull Column column, @Nullable Column... rest) {
        this.tableName = tableName;
        mColumns = concat(column, rest);
    }

    @NonNull
    public String createSql() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ")
                .append(tableName)
                .append('(');

        for (Column column : mColumns) {
            builder.append(column.name)
                    .append(' ')
                    .append(column.mType);

            if (column.mAttributes != null) {
                builder.append(' ');
                join(builder, " ", column.mAttributes);
            }
            builder.append(',');
        }

        final int length = builder.length();
        builder.replace(length - 1, length, ")");

        return builder.toString();
    }

    @NonNull
    protected static Attribute DEFAULT(Object value) {
        return new Attribute("DEFAULT " + DatabaseUtils.sqlEscapeString(value.toString()));
    }

    /**
     * The join utility to reduce {@link StringBuilder}.
     *
     * @param builder   the StringBuilder where you would like output to go
     * @param delimiter the delimiter to be joined
     * @param tokens    an array objects to be joined
     * @see android.text.TextUtils#join(CharSequence, Object[])
     */
    private static void join(
            @NonNull StringBuilder builder,
            @NonNull CharSequence delimiter, @NonNull Object[] tokens) {
        boolean firstTime = true;
        for (Object attribute : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                builder.append(delimiter);
            }
            builder.append(attribute);
        }
    }

    @NonNull
    private static <T> T[] concat(@NonNull T first, @Nullable T[] rest) {
        final T[] result =
                (T[]) Array.newInstance(first.getClass(), rest == null ? 1 : rest.length + 1);

        result[0] = first;

        if (rest != null) {
            System.arraycopy(rest, 0, result, 1, rest.length);
        }

        return result;
    }

    public static class Column {
        @NonNull
        public final String name;

        @NonNull
        private final Type mType;

        @Nullable
        private final Attribute[] mAttributes;

        public Column(@NonNull String name, @NonNull Type type, @Nullable Attribute... attribute) {
            this.name = name;
            mType = type;
            mAttributes = attribute;
        }
    }

    public static class PrimaryKey extends Column {
        public PrimaryKey(Object key , Object... rest) {
            super(PRIMARY_KEY.toString(), new BracketType(key, rest));
        }
    }

    public static class ForeignKey extends Column {
        public ForeignKey(Object key, Table refTable, Column refColumn) {
            super("FOREIGN KEY", new Type("" +
                    "(" +
                    key +
                    ") REFERENCES " +
                    refTable.tableName +
                    "(" +
                    refColumn.name +
                    ")"
            ));
        }
    }

    public static class Unique extends Column {
        public Unique(Object key, Object... rest) {
            super(UNIQUE.toString(), new BracketType(key, rest));
        }

    }

    protected static class Attribute {
        @NonNull
        private final String mAttribute;

        private Attribute(@NonNull String attribute) {
            mAttribute = attribute;
        }

        @NonNull
        @Override
        public String toString() {
            return mAttribute;
        }
    }

    private static class Type {
        @NonNull
        private final String mName;

        private Type(@NonNull String name) {
            mName = name;
        }

        @NonNull
        @Override
        public String toString() {
            return mName;
        }
    }
    private static class BracketType extends Type {
        BracketType(Object key, Object... rest) {
            super(createType(key, rest));
        }

        private static String createType(Object key, Object... rest) {
            final StringBuilder builder = new StringBuilder("(");
            join(builder, ",", concat(key, rest));

            return builder.append(")").toString();
        }
    }
}

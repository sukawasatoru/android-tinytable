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

import java.lang.reflect.Array;
import java.util.Objects;

public abstract class Table {
    public final String tableName;

    protected static final Type INTEGER = new Type("INTEGER");
    protected static final Type INT = new Type("INT");
    protected static final Type TINYINT = new Type("TINYINT");
    protected static final Type SMALLINT = new Type("SMALLINT");
    protected static final Type MEDIUMINT = new Type("MEDIUMINT");
    protected static final Type BIGINT = new Type("BIGINT");
    protected static final Type UNSIGNED_BIG_INT = new Type("UNSIGNED BIG INT");
    protected static final Type INT2 = new Type("INT2");
    protected static final Type INT8 = new Type("INT8");
    protected static final Type TEXT = new Type("TEXT");
    protected static final Type CLOB = new Type("CLOB");
    protected static final Type BLOB = new Type("BLOB");
    protected static final Type REAL = new Type("REAL");
    protected static final Type DOUBLE = new Type("DOUBLE");
    protected static final Type DOUBLE_PRECISION = new Type("DOUBLE PRECISION");
    protected static final Type FLOAT = new Type("FLOAT");
    protected static final Type NUMERIC = new Type("NUMERIC");
    protected static final Type BOOLEAN = new Type("BOOLEAN");
    protected static final Type DATE = new Type("DATE");
    protected static final Type DATETIME = new Type("DATETIME");
    protected static final Attribute PRIMARY_KEY = new Attribute("PRIMARY KEY");
    protected static final Attribute UNIQUE = new Attribute("UNIQUE");
    protected static final Attribute NOT_NULL = new Attribute("NOT NULL");
    protected static final Attribute AUTOINCREMENT = new Attribute("AUTOINCREMENT");

    private final Column[] mColumns;

    public Table(String tableName, Column column, Column... rest) {
        this.tableName = Objects.requireNonNull(tableName);
        mColumns = concat(Objects.requireNonNull(column), Objects.requireNonNull(rest));
    }

    public String createSql() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ")
                .append(tableName)
                .append('(');

        for (Column column : mColumns) {
            builder.append(column.createStatement())
                    .append(", ");
        }

        final int length = builder.length();
        builder.replace(length - 2, length, ")");

        return builder.toString();
    }

    public Column[] getColumns() {
        return mColumns;
    }

    protected static Attribute DEFAULT(Object value) {
        return new Attribute("DEFAULT " + escapeString(value.toString()));
    }

    private static void join(StringBuilder builder, char delimiter, Object[] tokens) {
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

    private static <T> T[] concat(T first, T[] rest) {
        final int restLength = Objects.requireNonNull(rest).length;
        final T[] result =
                (T[]) Array.newInstance(Objects.requireNonNull(first).getClass(), restLength + 1);
        result[0] = first;

        if (0 < restLength) {
            System.arraycopy(rest, 0, result, 1, restLength);
        }

        return result;
    }

    private static CharSequence escapeString(String string) {
        final StringBuilder builder =
                new StringBuilder(Objects.requireNonNull(string).length() + 2);
        builder.append('\'');
        if (string.indexOf('\'') == -1) {
            builder.append(string);
        } else {
            for (int i = 0, l = string.length(); i < l; i++) {
                final char c = string.charAt(i);
                if (c == '\'') {
                    builder.append('\'');
                }

                builder.append(c);
            }
        }
        builder.append('\'');

        return builder;
    }

    public static class Column {
        public final String name;

        private final Type mType;
        private final Attribute[] mAttributes;

        public Column(String name, Type type, Attribute... attribute) {
            this.name = Objects.requireNonNull(name);
            mType = Objects.requireNonNull(type);
            mAttributes = Objects.requireNonNull(attribute);
        }

        public String createAddSql(String tableName) {
            return "ALTER TABLE " + Objects.requireNonNull(tableName) + " ADD " + createStatement();
        }

        private CharSequence createStatement() {
            final StringBuilder builder = new StringBuilder(name)
                    .append(' ')
                    .append(mType);

            if (0 < mAttributes.length) {
                builder.append(' ');
                join(builder, ' ', mAttributes);
            }

            return builder;
        }
    }

    public static class PrimaryKey extends Column {
        public PrimaryKey(Object key, Object... rest) {
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
        private final String mAttribute;

        private Attribute(String attribute) {
            mAttribute = Objects.requireNonNull(attribute);
        }

        @Override
        public String toString() {
            return mAttribute;
        }
    }

    private static class Type {
        private final String mName;

        private Type(String name) {
            mName = Objects.requireNonNull(name);
        }

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
            join(builder, ',', concat(key, rest));

            return builder.append(")").toString();
        }
    }
}

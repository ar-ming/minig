package org.minig.cache;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class RowMapper<T> {

	abstract public T  mapRow(ResultSet rs, int rowNum) throws SQLException;
}

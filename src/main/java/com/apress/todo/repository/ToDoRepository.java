package com.apress.todo.repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.apress.todo.repository.domain.ToDo;

public class ToDoRepository implements CommonRepository<ToDo> {
	private static final String SQL_INSERT = "insert into todo (id,"
			+ "description, created, modified, completed) values (:id,:description," + ":created,:modified,:completed)";

	private static final String SQL_QUERY_FIND_ALL = "select id,"
			+ "description, created, modified, completed from todo";

	private static final String SQL_QUERY_FIND_BY_ID = SQL_QUERY_FIND_ALL + " where id = :id";

	private static final String SQL_UPDATE = "update todo set description ="
			+ ":description, modified = :modified, completed = :completed" + "where id = :id";

	private static final String SQL_DELETE = "delete from todo where id = :id";

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public ToDoRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public ToDo save(final ToDo domain) {
		ToDo result = findById(domain.getId());
		if (result != null) {
			result.setDescription(domain.getDescription());
			result.setCompleted(domain.isCompleted());
			result.setModified(LocalDateTime.now());
			return upsert(result, SQL_UPDATE);
		}
		return upsert(domain, SQL_INSERT);
	}

	private ToDo upsert(final ToDo toDo, final String sql) {
		Map<String, Object> namedParameters = new HashMap<>();
		namedParameters.put("id", toDo.getId());
		namedParameters.put("description", toDo.getDescription());
		namedParameters.put("created", java.sql.Timestamp.valueOf(toDo.getCreated()));
		namedParameters.put("modified", java.sql.Timestamp.valueOf(toDo.getModified()));
		namedParameters.put("completed", toDo.isCompleted());
		this.jdbcTemplate.update(sql, namedParameters);
		return findById(toDo.getId());
	}

	@Override
	public void delete(final ToDo domain) {
		Map<String, String> namedParameters = Collections.singletonMap("id", domain.getId());
		this.jdbcTemplate.update(SQL_DELETE, namedParameters);
	}

	@Override
	public ToDo findById(String id) {
		try {
			Map<String, String> namedParameters = Collections.singletonMap("id", id);
			return this.jdbcTemplate.queryForObject(SQL_QUERY_FIND_BY_ID, namedParameters, toDoRowMapper);
		} catch (EmptyResultDataAccessException ex) {
			return null;
		}
	}

	@Override
	public Iterable<ToDo> findAll() {
		return this.jdbcTemplate.query(SQL_QUERY_FIND_ALL, toDoRowMapper);
	}
}
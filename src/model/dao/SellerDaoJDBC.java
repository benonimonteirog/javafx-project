package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.DB;
import db.DBException;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {

	private Connection conn;
	
	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public void insert(Seller obj) {
		PreparedStatement st = null;
		
		try {
		st = conn.prepareStatement(
			"INSERT INTO seller "
			+ "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
			+ "VALUES "
			+ "(?, ?, ?, ?, ?)",
			Statement.RETURN_GENERATED_KEYS);
		
		st.setString(1, obj.getName());
		st.setString(2, obj.getEmail());
		st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
		st.setDouble(4, obj.getBaseSalary());
		st.setInt(5, obj.getDepartment().getId());
						
		int rowsAffected = st.executeUpdate();
		
		if (rowsAffected > 0) {
			ResultSet rs = st.getGeneratedKeys();
			
			if (rs.next()) {
				int id = rs.getInt(1);
				obj.setId(id);
			}
			
			DB.closeResultSet(rs);
		} else {
			throw new DBException("Unexpected error! No rows were affected!");
		}
		
		} catch (SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void update(Seller obj) {
		PreparedStatement st = null;
		
		try {
		st = conn.prepareStatement(
			"UPDATE seller "
			+ "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? "
			+ "WHERE Id = ?");
		
		st.setString(1, obj.getName());
		st.setString(2, obj.getEmail());
		st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
		st.setDouble(4, obj.getBaseSalary());
		st.setInt(5, obj.getDepartment().getId());
		st.setInt(6, obj.getId());
						
		st.executeUpdate();
		
		} catch (SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public void deleteById(Integer id) {
		PreparedStatement st = null;
		
		try {
			st = conn.prepareStatement("DELETE FROM seller WHERE id = ?");
			st.setInt(1, id);
			st.executeUpdate();
			
		} catch (SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public Seller findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			st = conn.prepareStatement(
			"SELECT seller.*, department.Name as DepName "
			+ "FROM seller INNER JOIN department "
			+ "ON seller.DepartmentId = department.Id "
			+ "WHERE seller.Id = ?");
		
		st.setInt(1, id);
		rs = st.executeQuery();
		
		if (rs.next()) {
			Department department = instantiateDepartment(rs);
			Seller seller = instantiateSeller(rs, department);
			return seller;
		}
		
		} catch (SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);			
			DB.closeStatement(st);
		}
		return null;
	}

	private Seller instantiateSeller(ResultSet rs, Department department) throws SQLException {
		Seller seller = new Seller();
		seller.setId(rs.getInt("Id"));
		seller.setName(rs.getString("Name"));
		seller.setEmail(rs.getString("Email"));
		seller.setBirthDate(new java.util.Date(rs.getTimestamp("BirthDate").getTime()));
		seller.setBaseSalary(rs.getDouble("BaseSalary"));
		seller.setDepartment(department);
		return seller;
	}

	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		Department department = new Department();
		department.setId(rs.getInt("DepartmentId"));
		department.setName(rs.getString("DepName"));		
		return department;
	}

	@Override
	public List<Seller> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
		st = conn.prepareStatement(
				"SELECT seller.*, department.Name as DepName "
				+ "FROM seller INNER JOIN department "
				+ "ON seller.DepartmentId = department.Id "
				+ "Order By Name");
		
		rs = st.executeQuery();
		
		List<Seller> list = new ArrayList<>();
		Map<Integer, Department> map = new HashMap<>();
		
		while (rs.next()) {
			Department dep = map.get(rs.getInt("DepartmentId"));
			
			if (dep == null) {
				dep = instantiateDepartment(rs);
				map.put(rs.getInt("DepartmentId"), dep);
			}
			
			Seller seller = instantiateSeller(rs, dep);
			list.add(seller);
		}
		
		return list;
		} catch (SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(st);
		}
	}

	@Override
	public List<Seller> findByDepartment(Department department) {
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
		st = conn.prepareStatement(
				"SELECT seller.*, department.Name as DepName "
				+ "FROM seller INNER JOIN department "
				+ "ON seller.DepartmentId = department.Id "
				+ "WHERE DepartmentId = ? "
				+ "Order By Name");
		
		st.setInt(1, department.getId());
		rs = st.executeQuery();
		
		Department dep = null;
		List<Seller> list = new ArrayList<>();
		
		while (rs.next()) {
			if (dep == null) {
				dep = instantiateDepartment(rs);
			}
			
			Seller seller = instantiateSeller(rs, dep);
			list.add(seller);
		}
		
		return list;
		} catch (SQLException e) {
			throw new DBException(e.getMessage());
		} finally {
			DB.closeResultSet(rs);
			DB.closeStatement(st);
		}
	}
}

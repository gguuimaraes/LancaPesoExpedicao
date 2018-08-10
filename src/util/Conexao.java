package util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
	private static String url = "jdbc:postgresql://192.168.0.84/quebrachapa";
	private static Connection conexao = null;

	public static Connection getInstance() throws SQLException {
		if (conexao == null)
			conexao = DriverManager.getConnection(url, "postgres", "postgres");
		return conexao;
	}
}

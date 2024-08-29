package umg.dem01.dao;

import umg.dem01.db.DatabaseConnection;
import umg.dem01.model.userCuestionario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class daoCuestionario {


    public void insertUser(userCuestionario cuestionario) throws SQLException {
        String query = "INSERT INTO tb_respuestas(seccion,telegram_id,respuesta_texto,pregunta_id,nombre_id) VALUES (?,?,?,?,?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, cuestionario.getSeccion());
            statement.setLong(2, cuestionario.getTelegramid());
            statement.setString(3, cuestionario.getResponse());
            statement.setInt(4, cuestionario.getPreguntaid());
            statement.setString(5, cuestionario.getNombreid());

            statement.executeUpdate();
        }
    }














}

package umg.dem01.service;

import umg.dem01.dao.daoCuestionario;
import umg.dem01.db.DatabaseConnection;
import umg.dem01.db.TransactionManager;
import umg.dem01.model.userCuestionario;
import umg.dem01.model.User;
import java.sql.Connection;
import java.sql.SQLException;

public class serviceCuestionario {


    private daoCuestionario Daocuestionario = new daoCuestionario();

    public void crearUsuario(userCuestionario cuestionario) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            TransactionManager tm = new TransactionManager(connection);
            tm.beginTransaction();
            try {
                Daocuestionario.insertUser(cuestionario);
                tm.commit();
            } catch (SQLException e) {
                tm.rollback();
                throw e;
            }
        }
    }

























}

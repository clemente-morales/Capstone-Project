package mx.com.labuena.services.dao;

import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mx.com.labuena.services.models.Client;
import mx.com.labuena.services.models.ClientDao;

/**
 * Created by moracl6 on 8/3/2016.
 */

public class MysqlClientDao extends BaseDao implements ClientDao {
    private static final Logger log = Logger.getLogger(MysqlClientDao.class.getName());

    @Inject
    public MysqlClientDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public List<Client> getAll() throws InternalServerErrorException {
        List<Client> clients = new ArrayList<>();
        Connection connection = connectionProvider.get();
        try {
            String clientsQuery = "select client.id_client, client.email, client.name, " +
                    "client.cloud_messaging_token from la_buena_db.client client;";
            ResultSet resultSet = connection.prepareStatement(clientsQuery).executeQuery();

            while (resultSet.next()) {
                int cliendId = resultSet.getInt("id_client");
                String email = resultSet.getString("email");
                String name = resultSet.getString("name");
                String token = resultSet.getString("cloud_messaging_token");

                clients.add(new Client(cliendId, email, name, token));
            }
            resultSet.close();
            closeConnection(connection);
            return clients;
        } catch (SQLException e) {
            closeConnection(connection);
            log.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public void save(Client client) throws InternalServerErrorException {
        try {
            Connection connection = connectionProvider.get();
            try {

                String saveClientLocationQuery = "insert into la_buena_db.client " +
                        "(id_client, email, name, cloud_messaging_token) values (0, ?, ?, ?);";
                connection.setAutoCommit(false);
                PreparedStatement preparedStatement = connection.prepareStatement(saveClientLocationQuery);
                preparedStatement.setString(1, client.getEmail());
                preparedStatement.setString(2, client.getName());
                preparedStatement.setString(3, client.getFcmToken());
                preparedStatement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                log.log(Level.SEVERE, e.getMessage(), e);
                throw new InternalServerErrorException(e);
            } finally {
                connection.setAutoCommit(true);
                closeConnection(connection);
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Client findByEmail(String email) throws InternalServerErrorException {
        Connection connection = connectionProvider.get();
        try {
            Client client = null;
            String clientsQuery = "select client.id_client, client.email, client.name, " +
                    "client.cloud_messaging_token from la_buena_db.client client " +
                    "where client.email = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(clientsQuery);
            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String clientEmail = resultSet.getString("email");
                String token = resultSet.getString("cloud_messaging_token");
                String name = resultSet.getString("name");
                int clientId = resultSet.getInt("id_client");

                client = new Client(clientId, clientEmail, name, token);
            }
            resultSet.close();
            closeConnection(connection);
            return client;
        } catch (SQLException e) {
            closeConnection(connection);
            log.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalServerErrorException(e);
        }
    }
}

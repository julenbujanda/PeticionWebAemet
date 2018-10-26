import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

public class PeticionWeb {

    private static Properties propiedades;
    private static Connection connection;

    private static Scanner scanner;

    private static HashMap<Integer, String> provincias;

    public static int seleccionarProvincia() throws SQLException {
        System.out.println("Seleccionar Provincia:");
        ResultSet resultSet = connection.prepareStatement("SELECT id, provincia FROM provincias;").executeQuery();
        while (resultSet.next()) {
            int idProvincia = resultSet.getInt(1);
            String nombreProvincia = resultSet.getString(2);
            provincias.put(idProvincia, nombreProvincia);
            System.out.println(idProvincia + ") " + nombreProvincia);
        }
        return scanner.nextInt();
    }

    public static void main(String[] args) throws Exception {
        propiedades = new Properties();
        provincias = new HashMap<>();
        scanner = new Scanner(System.in);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            propiedades.load(new FileInputStream(new File("config.ini")));
            connection = DriverManager.getConnection(propiedades.getProperty("url"), propiedades.getProperty("user"),
                    propiedades.getProperty("password"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(seleccionarProvincia());
    }

}

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class PeticionWeb {

    private static Properties propiedades;
    private static Connection connection;

    private static Scanner scanner;

    public static void main(String[] args) throws Exception {
        propiedades = new Properties();
        //provincias = new HashMap<>();
        scanner = new Scanner(System.in);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            propiedades.load(new FileInputStream(new File("config.ini")));
            connection = DriverManager.getConnection(propiedades.getProperty("url"), propiedades.getProperty("user"),
                    propiedades.getProperty("password"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        int idProvincia = seleccionarProvincia();
        int idMunicipio = seleccionarMunicipio(idProvincia);
        generarFichero(generarHTML(parsearXML(leerXML(idProvincia, idMunicipio))));
    }

    public static int seleccionarProvincia() throws SQLException {
        System.out.println("Seleccionar provincia:");
        ResultSet resultSet = connection.prepareStatement("SELECT id, provincia FROM provincias;").executeQuery();
        while (resultSet.next()) {
            System.out.println(resultSet.getInt(1) + ") " + resultSet.getString(2));
        }
        return scanner.nextInt();
    }

    public static int seleccionarMunicipio(int idProvincia) throws SQLException {
        System.out.println("Seleccionar municipio:");
        ResultSet resultSet = connection.prepareStatement(
                "SELECT @rownum := @rownum + 1 as row_number, id, municipio FROM municipios" +
                        " cross join (select @rownum := 0) r" +
                        " WHERE provincia_id = " + idProvincia + " order by id ;").executeQuery();
        int num = 0;
        while (resultSet.next()) {
            System.out.println(resultSet.getInt(1) + ") " + resultSet.getString(3));
        }
        return scanner.nextInt();
    }

    private static Document leerXML(int idProvincia, int idMunicipio) {
        String strProvincia = String.format("%02d", idProvincia);
        String strMunicipio = String.format("%03d", idMunicipio);
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = null;
        try {
            System.out.println("http://www.aemet.es/xml/municipios/localidad_" + strProvincia + strMunicipio + ".xml");
            URL url = new URL("http://www.aemet.es/xml/municipios/localidad_" + strProvincia + strMunicipio + ".xml");
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream("./localidad.xml");
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } catch (Exception e) {
            System.out.println("No se ha podido descargar el archivo.");
        }
        try {
            document = saxBuilder.build(new File("./localidad.xml"));
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    private static Localidad parsearXML(Document document) {
        Element root = document.getRootElement();
        String nombre = root.getChild("nombre").getText();
        String provincia = root.getChild("provincia").getText();
        List<Element> ePrediccion = root.getChild("prediccion").getChildren("dia");
        LinkedList<Prediccion> predicciones = new LinkedList<>();
        for (Element prediccion : ePrediccion) {
            String fecha = prediccion.getAttributeValue("fecha");
            int tempMax = Integer.parseInt(prediccion.getChild("temperatura").getChild("maxima").getText());
            int tempMin = Integer.parseInt(prediccion.getChild("temperatura").getChild("minima").getText());
            int estadoCielo;
            List<Element> estados = prediccion.getChildren("estado_cielo");
            Element estado = estados.get(0);
            if (estado.getText().equals("")) {
                boolean encontrado = false;
                for (Element est : estados) {
                    if (!est.getText().equals("") && !encontrado) {
                        estado = est;
                        encontrado = true;
                    }
                }
            }
            estadoCielo = !estado.getText().equals("") ? Integer.parseInt(estado.getText()) : 0;
            predicciones.add(new Prediccion(fecha, tempMax, tempMin, estadoCielo));
        }
        return new Localidad(nombre, provincia, predicciones);
    }

    private static String generarHTML(Localidad localidad) {
        // language=HTML
        String html = "<html\n><head>\n<meta charset='UTF-8'>\n" +
                "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css\">\n" +
                "</head>\n<body>\n" +
                "<div style=\"text-align: center;\"><h1>" + localidad.getNombre() + "</h1>\n" +
                "<h2>" + localidad.getProvincia() + "</h2></div>\n" +
                "<table class='centered' style='margin: auto; width: 800px'>\n<tbody>\n" +
                "<tr><td>Fecha</td><td>T. Máxima</td><td>T. Mínima</td><td>Estado del Cielo</td></tr>\n";
        for (Prediccion prediccion :
                localidad.getPredicciones()) {
            // language=HTML
            html += "<tr><td>" + prediccion.getFecha() + "</td><td>" + prediccion.getTempMaxima() +
                    "</td><td>" + prediccion.getTempMinima() + "</td>" +
                    "<td>" + (prediccion.getEstadoCielo() != 0 ? "<img src=\"http://www.aemet.es/imagenes/png/estado_cielo/" + prediccion.getEstadoCielo() + "_g.png\">" : "No disponible") + "</td></tr>\n";
        }
        // language=HTML
        html += "\n</tbody></table>\n</body>\n</html>\n";
        return html;
    }

    private static void generarFichero(String htmlPagina) {
        try {
            FileWriter fw = new FileWriter("./localidad.html", false);
            fw.write(htmlPagina);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

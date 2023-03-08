import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String srcFileName1 = "data.csv";
        String targetFileName1 = "data1.json";
        String srcFileName2 = "data.xml";
        String targetFileName2 = "data2.json";
        List<Employee> list;
        String json;

        list = parseCSV(columnMapping, srcFileName1);
        json = listToJson(list);
        writeString(json, targetFileName1);

        list = parseXML(srcFileName2);
        json = listToJson(list);
        writeString(json, targetFileName2);

        json = readString(targetFileName1);
        list = jsonToList(json);
        list.forEach(System.out::println);
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {

            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();

            return csv.parse();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Employee> parseXML(String fileName) {
        List<Employee> employeeList = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            Node root = doc.getDocumentElement();
            NodeList nodeList = root.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    Employee employee = new Employee();
                    Element element = (Element) node;
                    NodeList employeeNodeList = element.getChildNodes();
                    for (int j = 0; j < employeeNodeList.getLength(); j++) {
                        Node employeeNode = employeeNodeList.item(j);
                        if (Node.ELEMENT_NODE == employeeNode.getNodeType()) {
                            Element employeeElement = (Element) employeeNode;
                            Text textNode = (Text) employeeElement.getFirstChild();
                            String text = textNode.getData();
                            switch (employeeElement.getTagName()) {
                                case "id":
                                    employee.id = Integer.parseInt(text);
                                    break;
                                case "firstName":
                                    employee.firstName = text;
                                    break;
                                case "lastName":
                                    employee.lastName = text;
                                    break;
                                case "country":
                                    employee.country = text;
                                    break;
                                case "age":
                                    employee.age = Integer.parseInt(text);
                            }
                        }
                    }
                    employeeList.add(employee);
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return employeeList;
    }

    public static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        return gson.toJson(list, listType);
    }

    public static void writeString(String data, String fileName) {
        try (FileWriter fw = new FileWriter(fileName)) {
            fw.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readString(String fileName) {
        StringBuilder sb = new StringBuilder();
        String s;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static List<Employee> jsonToList(String json) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        return gson.fromJson(json, listType);
    }
}

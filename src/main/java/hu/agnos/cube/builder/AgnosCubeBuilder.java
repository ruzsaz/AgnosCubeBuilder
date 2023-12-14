package hu.agnos.cube.builder;

import hu.agnos.cube.specification.entity.CubeSpecification;
import hu.agnos.cube.specification.exception.InvalidPostfixExpressionException;
import hu.agnos.cube.specification.exception.NameOfHierarchySpecificationNotUniqueException;
import hu.agnos.cube.specification.exception.NameOfMeasureSpecificationNotUniqueException;
import hu.agnos.cube.specification.repo.CubeSpecificationRepo;
import hu.agnos.cube.Cube;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author parisek
 */
public class AgnosCubeBuilder {

    private static int result = 0;
    public static String xmlPath = null;
    public static String imputTableName = null;
    public static String outputDirName = null;
    public static String cubeUniqueName = null;

    private static final Logger logger = LoggerFactory.getLogger(AgnosCubeBuilder.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NameOfHierarchySpecificationNotUniqueException, 
            NameOfMeasureSpecificationNotUniqueException, IOException, InvalidPostfixExpressionException {

        String badParamsStg = "Params:\n"
                + "\t--xml: path of xml file (mandatory)\n"
                + "\t--table: name of imput table (mandatory)\n"
                + "\t--name: cube unique name (mandatory)\n"
                + "\t--output: path of destination directory (mandatory)\n";

        if (args.length != 4) {
            System.out.println(badParamsStg);
            result = 1;
        } else {
            parseArgs(args);

            if (xmlPath == null
                    || imputTableName == null
                    || outputDirName == null
                    || cubeUniqueName == null) {
                System.out.println(badParamsStg);
                result = 1;
            } else {
                logger.info("Started... ");
//                System.out.println("Started: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                CubeSpecificationRepo cubeRepo = new CubeSpecificationRepo();
                CubeSpecification xmlCube = cubeRepo.findCubeSpecificationByPath(xmlPath);
                if (xmlCube != null) {

                    Cube cube = createCube(cubeUniqueName, imputTableName, xmlCube);
                    if (cube != null) {
                        saveCube(cube, outputDirName);
                    }
                } else {
                    result = 2;
                }
            }
        }

        if (result == 0) {
            logger.info("Make cube from " + xmlPath + " was successful.");
        } else {
            logger.info("Make cube from " + xmlPath + " was unsuccessful.");
        }
        System.exit(result);
    }

    private static Cube createCube(String cubeUniqueName, String imputTableName, CubeSpecification xmlCube) {
        Connection conn = null;
        Statement statement = null;
        Cube cube = null;
        try {
            cube = CubeMaker.createCube(statement, cubeUniqueName, imputTableName, xmlCube);
        } catch (ClassNotFoundException | SQLException ex) {
            logger.error(ex.getMessage());
            result = 3;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    statement = null;
                } catch (SQLException ex1) {
                    logger.error(ex1.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                    conn = null;
                } catch (SQLException ex1) {
                    logger.error(ex1.getMessage());
                }
            }
        }
        return cube;
    }

    private static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--xml=")) {
                xmlPath = args[i].substring(6);
            } else if (args[i].startsWith("--table=")) {
                imputTableName = args[i].substring(8);
            } else if (args[i].startsWith("--output=")) {
                outputDirName = args[i].substring(9);
            } else if (args[i].startsWith("--name=")) {
                cubeUniqueName = args[i].substring(7);
            }
        }
    }

    public static void saveCube(Cube cube, String path) {
        try {

            if (!path.endsWith("/")) {
                path = path + "/";
            }

            FileOutputStream fileOut = new FileOutputStream(path + cube.getName() + ".cube");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(cube);
            out.close();
            fileOut.close();
            logger.info("Serialized data is saved in " + path + cube.getName() + ".cube");
//            System.out.println("Serialized data is saved in " + path + cube.getName() + ".cube");
        } catch (IOException i) {
            logger.error(i.getMessage());
        }
    }

}

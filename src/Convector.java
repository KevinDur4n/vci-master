import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;



public class Main {

    private static final Stack<String> stack = new Stack<String>();
    private static final Map<Integer, ArrayList<String>> TABLE_OPERATORS = new HashMap<Integer, ArrayList<String>>();
    private static final Stack<Integer> priors = new Stack<Integer>();
    private static final ArrayList<String> VCI = new ArrayList<String>();
    private static boolean foundOperator, op, restatuto, untiltrue = false;
    private static final Stack<String> estatuto = new Stack<String>();
    private static final Stack<Integer> direccion = new Stack<Integer>();
    private static final List<String> LIST_STATUS = Arrays.asList(new String[]{"while", "do", "end", "repeat", "until"});

    public static void main(String args[]) throws IOException {

        File file = new File("Lectura.txt");
        if (file.exists()){
            FileReader lector = new FileReader(file);
            convector(lector);
        }else{
            System.err.println("El fichero no existe o no se encontro.");
        }

    }

    private static void convector(FileReader reader) throws IOException {

        ArrayList<String> tokens = new ArrayList<>(); //tokens
        String buffertoken = ""; //tokenbuffer
        int i = reader.read();
        createTableOperators();
        try {
            do {
                if (buffertoken.equals("begin")) {
                    buffertoken = "";
                    //Si "i" no es un espacio blanco
                } else if (!(String.valueOf((char) i).isBlank())) {
                    buffertoken = buffertoken + String.valueOf((char) i);
                    //Si "i" es un espacio en blanco o el string "buffertoken" tiene algo
                } else if (String.valueOf((char) i).isBlank() && !buffertoken.isBlank()) {

                    //Agrega el string "buffertoken" al arreglo "cadena"
                    tokens.add(buffertoken);
                    buffertoken = "";
                }

                // Si "i" es igual a un "!"
                if (String.valueOf((char) i).equals("!")) {
                    buffertoken = "";
                    // Cadena de prueba Z = 4 * ( a * b ) * ( 100 / 15 - b ) ;
                    System.out.println("Cadena de entrada" + " : " + tokens);
                    System.out.println();
                    System.out.println("V C I---> " + vci(tokens));
                    System.out.println("------------------");
                    System.out.println();
                    tokens.clear();
                    VCI.clear(); // <--- LIMPIA VCI

                }

            } while ((i = reader.read()) != -1);// Hasta que no tenga nada que leer

            if (!buffertoken.isEmpty()) { //Si sobre algo en el "buffertoken" guardarlo en el arreglo de "cadena"
                tokens.add(buffertoken);
            }

            System.out.println("Cadena de entrada" + " : " + tokens);
            System.out.println();
            System.out.println("V C I---> " + vci(tokens));
            System.out.println("------------------");
            System.out.println();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    //Coloca los operadores en una pila.
    private static void createTableOperators() {

        TABLE_OPERATORS.put(60, new ArrayList<>(Arrays.asList("*", "/")));

        TABLE_OPERATORS.put(50, new ArrayList<>(Arrays.asList("+", "-")));

        TABLE_OPERATORS.put(40, new ArrayList<>(Arrays.asList(">", "<", "==", ">=", "<=")));

        TABLE_OPERATORS.put(30, new ArrayList<>(Arrays.asList("not")));

        TABLE_OPERATORS.put(20, new ArrayList<>(Arrays.asList("and")));

        TABLE_OPERATORS.put(10, new ArrayList<>(Arrays.asList("or")));

        TABLE_OPERATORS.put(0, new ArrayList<>(Arrays.asList("=", "(", ")", ";")));

    }

    private static ArrayList<String> vci(ArrayList<String> tokens) {
        for (String token : tokens) { // Obtener cada token del arreglo

            if (LIST_STATUS.contains(token)) { // ¿El token es una estructura de control?
                handleEstatuto(token);
            } else {

                for (Map.Entry<Integer, ArrayList<String>> entry : TABLE_OPERATORS.entrySet()) { //Obtiene los operadores en un Map

                    for (String operator : entry.getValue()) { //Por cada operador
                        if (operator.equals(token)) {//Si concide el operador

                            handleStack(token, entry.getKey());
                            break;// Ya no busca mas operadores

                        }
                    }
                    if (foundOperator) { // Si ya se manejo el operador ya no busca mas operadores
                        foundOperator = false;
                        break;
                    }
                }

            }

            if (!op && !restatuto) { // Si es un identificador o una constante
                VCI.add(token);

            }
            op = false;
            restatuto = false;
        }
        return VCI;
    }

    private static void handleEstatuto(String cadena) { //Maneja la estructura de control WHILE

        restatuto = true;

        switch (cadena) {
            case "while":
            case "repeat":
                estatuto.push(cadena); //Agrega a la pila de estatuto
                direccion.push(VCI.size()); //Agrega la ultima posicion del VCI "Direccion"
                break;
                // Archivo salida y que en el vci muestre la posicion
                // El condicional if else leopoldo
            case "do":
                String tfalso = "";
                VCI.add(tfalso); //Genera el token falso

                direccion.push(VCI.size() - 1); //Coloca el comienzo de la primera instruccion
                VCI.add("do");// Agrega el estatuto do

                break;
            case "until":
                untiltrue = true;
                break;
            case "end":
                if (estatuto.peek().equals("while")) {//Si es un while el estatuto
                    Integer directionpeek = direccion.peek(); //Obtienes la ultima direccion de la pila de direcciones
                    VCI.add(direccion.pop(), String.valueOf(((VCI.size() - 1) + 3))); // Agregas la direccion mas dos

                    VCI.add(String.valueOf(direccion.pop())); // Se agrega el valor de la direccion

                    VCI.remove(directionpeek + 1); // Se quita el valor
                    VCI.add("end-while");
                }

                estatuto.pop(); // Sacas el estatuto
                break;
        }
    }

    private static void handleStack(String cadena, Integer prior) {
        foundOperator = true;
        op = true;
        //Si la pila no esta vacia, la prioridad del operador es menor y no es opérador nulo
        if (!stack.empty() && prior <= priors.peek() &&  !Arrays.asList("(",")",";","=").contains(cadena)) {

            while (prior <= priors.peek()) { //Mientras la prioridad del operador entrante es menor
                VCI.add(stack.pop()); //Se saca el operador de la pila

                priors.pop(); //Se saca la prioridad de ese operador
            }

            stack.push(cadena); //Se agrega el operador entrante a la pila
            priors.push(prior); //Se agrega la prioridad a la pila

        } else {

            switch (cadena) {
                case ";":
                    while (!stack.isEmpty()) {//Si la pila de operadores no esta vacia

                        //Vaciamos los operadores
                        VCI.add(stack.pop());
                        priors.pop();
                    }
                    break;
                case ")":

                    while (!stack.peek().equals("(")) { //Hasta que se encuentre el parentesis "("
                        VCI.add(stack.pop());
                        priors.pop();
                    }
                    // Saca el parentesis "("
                    stack.pop();
                    priors.pop();

                    if (untiltrue) {// Si se encuentra un until
                        VCI.add(String.valueOf(direccion.pop())); // Saca la direccion de la pila
                        VCI.add("until");
                        untiltrue = false;
                    }

                    break;

                default: //Por naturaleza se agrega los operadores
                    stack.push(cadena);
                    priors.push(prior);
                    break;
            }

}

}
}
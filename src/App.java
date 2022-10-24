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


public class App { //Main
    private static boolean foundOperator, op, restatuto, untiltrue = false;
    private static final Stack<String> estatuto = new Stack<String>();
    private static final Stack<Integer> direccion = new Stack<Integer>();
    private static final List<String> LIST_STATUS = Arrays.asList(new String[]{"while", "do", "end", "repeat", "until"});
    private static final Stack<String> stack = new Stack<String>();
    private static final Map<String, Integer> MAP_OPERATORS = new HashMap<String, Integer>();

    private static final Stack<Integer> priors = new Stack<Integer>();
    private static final ArrayList<String> VCI = new ArrayList<String>();


    public static void main(String args[]) throws IOException {

        File file = new File("Lectura.txt");
        if (file.exists()) {
            FileReader lector = new FileReader(file);
            readFile(lector);
        } else {
            System.err.println("El fichero no existe o no se encontro.");
        }

    }

    private static void readFile(FileReader reader) throws IOException {
        //Agrupar los metodos  en clases estaticas
        //Devidir cada funcion en cosas especificas

        String buffertoken = ""; //tokenbuffer
        int intChar = reader.read();
        String character = "";
        createMapOperators();
        System.out.println("Cadena de Entrada");
        try {

            do {
                character = String.valueOf((char) intChar);
                if (buffertoken.equals("begin")) {
                    buffertoken = "";
                    //Si "character" no es un espacio blanco
                } else if (!(character.isBlank())) {
                    buffertoken = buffertoken + character;
                    //Si "character" es un espacio en blanco o el  "buffertoken" tiene algo
                } else if (character.isBlank() && !buffertoken.isBlank()) {

                    System.out.print(buffertoken + " ");
                    convector(buffertoken);
                    buffertoken = "";
                }

                intChar = reader.read();
            } while (intChar != -1);// Hasta que no tenga nada que leer

            System.out.println();
            System.out.println("V C I---> " + VCI);
            System.out.println("------------------");
            System.out.println();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    //Coloca los operadores en una pila.


    /**
     * Analiza el token entregado, y elige que hacer con cada token.
     * Si es una estatuto llama handleEstatuo o si es un operador llama handleStack.
     * En el caso de que es un identifcador o variable guarda directamente el token en el VCI.
     * @param token
     */
    private static void convector(String token) {

        if (LIST_STATUS.contains(token)) { // ¿El token es una estructura de control?
            handleEstatuto(token);
        } else if (MAP_OPERATORS.containsKey(token)) {
            int priorOp = MAP_OPERATORS.get(token);
            handleStack(token, priorOp);
        } else {// Si es un identificador o una constante
            VCI.add(token);
        }
    }



    private static void handleEstatuto(String cadena) { //Maneja la estructura de control WHILE
        switch (cadena) { // hace lo del repeat
            case "repeat":
                estatuto.push(cadena); //Entra "repeat" a la pila de estatutos
                direccion.push(VCI.size()); //Entra la direccion corresponidete a la [ila de direcciones
                break;
            case "until":
                untiltrue = true;
                break;
            case "end":
                estatuto.pop();
                break;
        }
    }

    private static void handleStack(String cadena, Integer prior) {
        //Si la pila no esta vacia, la prioridad del operador es menor y no es opérador nulo
        if (!stack.empty() && prior <= priors.peek() && !Arrays.asList("(", ")", ";", "=").contains(cadena)) {

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

    private static void createMapOperators() {

        MAP_OPERATORS.put("*",60);
        MAP_OPERATORS.put("/",60);
        MAP_OPERATORS.put("+",50);
        MAP_OPERATORS.put("-",50);

        MAP_OPERATORS.put(">",40);
        MAP_OPERATORS.put("<",40);
        MAP_OPERATORS.put("==",40);
        MAP_OPERATORS.put(">=",40);
        MAP_OPERATORS.put("<=",40);

        MAP_OPERATORS.put("not",30);

        MAP_OPERATORS.put("and",20);

        MAP_OPERATORS.put("or",10);


        MAP_OPERATORS.put("=",0);
        MAP_OPERATORS.put("(",0);
        MAP_OPERATORS.put(")",0);
        MAP_OPERATORS.put(";",0);
    }
}
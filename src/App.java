import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;


public class App { //Main
    private static boolean untiltrue = false,readyVCI = false;
    private static int dirIfEnd = 0;
    private static  StringBuilder dataFile = new StringBuilder();
    private static final Stack<String> estatuto = new Stack<String>();
    private static final Stack<Integer> direccion = new Stack<Integer>();
    private static final Stack<String> stack = new Stack<String>();
    private static final Stack<Integer> priors = new Stack<Integer>();
    private static final List<String> LIST_STATUS = Arrays.asList(new String[]{"while", "do", "end", "repeat", "until","if","then","else"});
    private static final Map<String, Integer> MAP_OPERATORS = new HashMap<String, Integer>();
    private static final ArrayList<String> VCI = new ArrayList<String>();

    private static int index;

    public static void main(String args[]) throws IOException {
        createMapOperators(); //Inicializa el mapa de operadores

        File file = new File("Lectura.txt");
        if (file.exists()) {
            FileReader lector = new FileReader(file);
            readFile(lector);
        } else {
            System.err.println("El fichero no existe o no se encontro.");
        }
        if(readyVCI){
            File outFile = new File("outFile.txt");
            writeFile(outFile,true);
        }


    }

    private static void readFile(FileReader reader) throws IOException {

        String buffertoken = "", character; //tokenbuffer
        int intChar = reader.read();
        try {
            do {
                character = String.valueOf((char) intChar);
                if (buffertoken.equals("begin")) {
                    dataFile.append(buffertoken + " ");
                    buffertoken = "";
                    //Si "character" no es un espacio blanco
                } else if (!(character.isBlank())) {
                    buffertoken = buffertoken + character;
                    //Si "character" es un espacio en blanco o el  "buffertoken" tiene algo
                } else if (character.isBlank() && !buffertoken.isBlank()) {
                    dataFile.append(buffertoken + " ");
                    convector(buffertoken);
                    buffertoken = "";
                }
                intChar = reader.read();
            } while (intChar != -1);
            readyVCI = true;
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



    /**
     * Analiza el token entregado, y elige que hacer con cada token.
     * Si es una estatuto llama handleEstatuo o si es un operador llama handleStack.
     * En el caso de que es un identifcador o variable guarda directamente el token en el VCI.
     * @param token
     */
    private static void convector(String token) {
        if (LIST_STATUS.contains(token)) {
            handleEstatuto(token);
        } else if (MAP_OPERATORS.containsKey(token)) {
            int priorOp = MAP_OPERATORS.get(token);
            handleStack(token, priorOp);
        } else {
            VCI.add(token);
        }
    }


    /**
     * Maneja la estructura de control WHILE-DO, IF-THEN-ELSE, REPEAT-UNTIL
     * @param token es el estatuto que se va manejar.
     */
    private static void handleEstatuto(String token) {

        switch (token) {
            case "while" ,"repeat":
                estatuto.push(token);
                direccion.push(VCI.size());
                break;
            // Archivo salida y que en el vci muestre la posicion
            // El condicional if else leopoldo
            case "do":
                String tfalso = "";
                VCI.add(tfalso);
                direccion.push(VCI.size() - 1); //Coloca el comienzo de la primera instruccion
                VCI.add("do");
                break;
            case "until":
                untiltrue = true;
                break;
            case "end":
                if(estatuto.isEmpty()){
                   break;
                }
                if (estatuto.peek().equals("while")) {
                    Integer directionpeek = direccion.peek();
                    VCI.add(direccion.pop(), String.valueOf(((VCI.size() - 1) + 3)));

                    VCI.add(String.valueOf(direccion.pop()));

                    VCI.remove(directionpeek + 1);
                    VCI.add("end-while");
                }

                if(estatuto.peek().equals("if")){
                    dirIfEnd = direccion.peek();
                    popDireccionApuntador();
                }
                if (estatuto.peek().equals("else")){
                    popDireccionApuntador();
                }
                estatuto.pop();

                break;

            case "if":
                estatuto.push(token);
                break;
            case "then":
                while (!stack.empty()){
                    VCI.add(stack.pop());
                    priors.pop();
                }
                direccion.push(VCI.size());
                VCI.add(token);
                break;

            case "else":
                estatuto.push(token);
                popDireccionApuntador2(dirIfEnd);
                direccion.push(VCI.size());
                VCI.add(token);
                break;



}
}

    private static void handleStack(String cadena, Integer prior) {
        //Si la pila no esta vacia, la prioridad del operador es menor y no es opérador nulo
        if (!stack.empty() && prior <= priors.peek() && !Arrays.asList("(", ")", ";", "=").contains(cadena)) {
            //Mientras la prioridad del operador entrante es menor
            while (prior <= priors.peek()) {
                VCI.add(stack.pop());

                priors.pop();
            }

            stack.push(cadena);
            priors.push(prior);

        } else {

            switch (cadena) {
                case ";":

                    while (!stack.isEmpty()) {

                        VCI.add(stack.pop());
                        priors.pop();
                    }
                    break;
                case ")":
                    //Hasta que se encuentre el parentesis "("
                    while (!stack.peek().equals("(")) {
                        VCI.add(stack.pop());
                        priors.pop();
                    }
                    stack.pop();
                    priors.pop();

                    if (untiltrue) {
                        VCI.add(String.valueOf(direccion.pop()));
                        VCI.add("until");
                        untiltrue = false;
                    }

                    break;

                default:
                    stack.push(cadena);
                    priors.push(prior);
                    break;
            }

        }

    }

    private static void writeFile(File file,Boolean printOut) throws IOException{
        try{
            FileWriter writer = new FileWriter(file);
            int minLen =  (VCI.size() + "").length();
            String out = "";
            for(String token :  VCI){
                var len = Math.max(token.length(), minLen);
                out += String.format("|%-" + len +"s",token);
            }
            out += "\n";
            index = 0;
            for(String token :  VCI){
                var len = Math.max(token.length(), minLen);
                out += String.format("|%-" + len +"s",index++);
            }
            writer.write(out);
            writer.close();
            if(printOut){
                System.out.println(out);
            }
        }catch (IOException e){
            throw new IOException("Error en la escritura del archivo");
        }

    }
    private static void popDireccionApuntador(){
        VCI.add(direccion.pop(), String.valueOf(VCI.size()+1));
    }
    private static void popDireccionApuntador2(int direccion){
        VCI.set(direccion,String.valueOf(VCI.size()+2));

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
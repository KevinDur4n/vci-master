import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guest Mode
 */
public class Main {

    private static final Stack<String> stack = new Stack<String>();
    private static final Map<Integer, ArrayList<String>> operators = new HashMap<Integer, ArrayList<String>>();
    private static final Stack<Integer> priors = new Stack<Integer>();
    private static final ArrayList<String> VCI = new ArrayList<String>();
    private static boolean check, op, restatuto, untiltrue = false;
    private static final Stack<String> estatuto = new Stack<String>();
    private static final Stack<Integer> direccion = new Stack<Integer>();
    private static final List<String> estatutos = Arrays.asList(new String[]{"while", "do", "end", "repeat", "until"});

    public static void main(String args[]) throws IOException {

        FileReader fr = null;
        File tf = new File("Lectura.txt");
        fr = new FileReader(tf);
        Lectura(fr);

    }

    private static void Lectura(FileReader fr) throws IOException {

        ArrayList<String> cadena = new ArrayList<>();
        String add = "";
        int i = fr.read();
        OperatorsIntialize();
        try {
            do {
                if (add.equals("begin")) {
                    add = "";
                    //Si "i" no es un espacio blanco
                } else if (!(String.valueOf((char) i).isBlank())) {
                    add = add + String.valueOf((char) i);
                    //Si "i" es un espacio en blanco o el string "add" tiene algo
                } else if (String.valueOf((char) i).isBlank() && !add.isBlank()) {

                    //Agrega el string "add" al arreglo "cadena"
                    cadena.add(add);
                    add = "";
                }

                // Si "i" es igual a un "!"
                if (String.valueOf((char) i).equals("!")) {
                    add = "";
                    // Cadena de prueba Z = 4 * ( a * b ) * ( 100 / 15 - b ) ;
                    System.out.println("Cadena de entrada" + " : " + cadena);
                    System.out.println();
                    System.out.println("V C I---> " + readerchain(cadena));
                    System.out.println("------------------");
                    System.out.println();
                    cadena.clear();
                    VCI.clear(); // <--- LIMPIA VCI

                }

            } while ((i = fr.read()) != -1);// Hasta que no tenga nada que leer

            if (!add.isEmpty()) { //Si sobre algo en el "add" guardarlo en el arreglo de "cadena"
                cadena.add(add);
            }

            System.out.println("Cadena de entrada" + " : " + cadena);
            System.out.println();
            System.out.println("V C I---> " + readerchain(cadena));
            System.out.println("------------------");
            System.out.println();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    //Coloca los operadores en una pila.
    private static void OperatorsIntialize() {

        operators.put(60, new ArrayList<>(Arrays.asList("*", "/")));

        operators.put(50, new ArrayList<>(Arrays.asList("+", "-")));

        operators.put(40, new ArrayList<>(Arrays.asList(">", "<", "==", ">=", "<=")));

        operators.put(30, new ArrayList<>(Arrays.asList("not")));

        operators.put(20, new ArrayList<>(Arrays.asList("and")));

        operators.put(10, new ArrayList<>(Arrays.asList("or")));

        operators.put(0, new ArrayList<>(Arrays.asList("=", "(", ")", ";")));

    }

    private static ArrayList<String> readerchain(ArrayList<String> cadena) {
        for (String cadena1 : cadena) { // Obtener cada cadena del arreglo

            if (estatutos.contains(cadena1)) { // ¿Contiene una estructura de control?
                handleEstatuto(cadena1);
            } else {

                for (Map.Entry<Integer, ArrayList<String>> entry : operators.entrySet()) { //Obtiene los operadores en un Map

                    for (String test : entry.getValue()) { //Por cada operador
                        if (test.equals(cadena1)) {//Si concide el operador

                            handleStack(cadena1, entry.getKey());
                            break;// Ya no busca mas operadores

                        }
                    }
                    if (check) {
                        check = false;
                        break;
                    }
                }

            }

            if (!op && !restatuto) { // Si es un identificador o una constante
                VCI.add(cadena1);

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
        check = true;
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
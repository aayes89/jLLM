package ex1llm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Slam
 */
public class Ex1LLM {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int opciones = -1;
        boolean[] huboCambios = {false};
        SwingWorker<Void, Void> worker = null;
        ImprovedLanguageModel languageModel = new ImprovedLanguageModel(3); // Usar trigramas
        File model = new File("/ruta/al/llm/model.dat");
        if (model.exists()) {
            System.out.println("Modelo detectado!\n");
            // Menu principal
            while (true) {
                System.out.println("\n¿Qué desea hacer?");
                System.out.println("1. Interactuar con el modelo.");
                System.out.println("2. Actualizar conjunto de datos.");
                System.out.println("0. Salir del programa.");
                opciones = in.nextInt();
                switch (opciones) {
                    case 0:
                        if (huboCambios[0]) {
                            System.out.println("Guardando conjunto de datos en el modelo y saliendo del programa ...");
                            try {
                                // Guardar el modelo
                                languageModel.saveModel("/ruta/al/llm/model.dat");
                            } catch (IOException ex) {
                                Logger.getLogger(Ex1LLM.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        System.out.println("Vuelva pronto");
                        System.exit(opciones);
                        break;

                    case 1:
                        try {
                            System.out.println("Cargando modelo de lenguaje...");
                            languageModel.loadModel(model.getAbsolutePath());

                            String textInput = "";
                            System.out.println("Modelo cargado.\n\nAyuda:\n\tPara detener la interacción escriba 'exit' sin comillas.\n\nIniciar charla:");

                            while (!(textInput).contains("exit")) {
                                System.out.print("Tú: ");
                                textInput = in.next().concat(in.nextLine());
                                // Generar texto
                                String generated = languageModel.generate(textInput, 20, 0.7); // Temperatura
                                // Respuesta del asistente
                                System.out.println("Asistente: " + generated);
                            }

                        } catch (IOException | ClassNotFoundException ex) {
                            System.out.println("Exception: " + ex.getMessage());
                        }
                        break;

                    case 2:
                        int[] nGramSize = {0};
                        worker = new SwingWorker<Void, Void>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                try {
                                    System.out.println("Cargando modelo de lenguaje...");
                                    languageModel.loadModel(model.getAbsolutePath());
                                    // obtiene lo nGram del modelo cargado
                                    nGramSize[0] = languageModel.getnGramCounts();
                                    System.out.println("Buscando conjunto de datos para entrenamiento ...");
                                    // Usar invokeLater para abrir JFileChooser en EDT
                                    File[] mainFile = new File[1];
                                    try {
                                        SwingUtilities.invokeAndWait(() -> {
                                            mainFile[0] = openFile();
                                        });
                                    } catch (InterruptedException | InvocationTargetException e) {
                                        System.err.println("Error al abrir el JFileChooser: " + e.getMessage());
                                    }
                                    if (mainFile[0] == null) {
                                        System.err.println("Error!\nNo se pudo cargar el modelo");
                                        return null;
                                    }
                                    System.out.println("Cargando conjunto de datos para entrenamiento ...");
                                    String text = readTextFromFile(mainFile[0]);
                                    List<String> sentences = new ArrayList<>();
                                    sentences.add(text);
                                    System.out.println("Conjunto de datos cargados.");

                                    System.out.println("Entrenando modelo con nuevos datos ...");
                                    languageModel.train(sentences);
                                    System.out.println("Entrenamiento finalizado.");

                                } catch (IOException e) {
                                    System.err.println("IOE: " + e.getMessage());
                                } catch (ClassNotFoundException ex) {
                                    System.err.println("ClassNotFoundException: " + ex.getMessage());
                                }
                                return null;
                            }

                            @Override
                            protected void done() {
                                // valida que hayan actualizado con nuevos datos el modelo
                                // evita repetir el proceso de salvado del modelo posteriormente
                                if (languageModel.getnGramCounts() != nGramSize[0]) {
                                    huboCambios[0] = true;
                                    try {
                                        // Guardar el modelo
                                        languageModel.saveModel("/ruta/al/llm/model.dat");
                                    } catch (IOException ex) {
                                        System.err.println("IOE: " + ex.getMessage());
                                    }
                                } else {
                                    System.out.println("El conjunto de datos ingresado existe en el modelo.\n"
                                            + "Intente con otro conjunto, por favor.\n\n");
                                }
                            }
                        };
                        worker.execute();
                        while (worker.getProgress() != -1) {
                            if (worker.isDone() || worker.isCancelled()) {
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                System.err.println(ex.getMessage());
                            }
                            System.out.print(".");

                        }

                        break;

                    default:
                        System.err.println("Opción no válida!\nIntente nuevamente.");
                }
            }
        } else {
            System.out.println("No se encontró un modelo con el que trabajar, desea entrenar uno nuevo? (s/n) o (y/n)");
            String respuesta = in.nextLine();
            if (respuesta.startsWith("S") || respuesta.startsWith("s") || respuesta.startsWith("Y") || respuesta.startsWith("y")) {
                // Entrenamiento con novelas, biblia valera, diccionarios y textos genéricos (usar más texto en la práctica)
                List<String> sentences = new ArrayList<>();
                sentences.addAll(Arrays.asList(
                        "el gato juega en el jardín",
                        "el perro corre rápidamente",
                        "el gato y el perro juegan juntos en el parque",
                        "el jardín es hermoso",
                        "el perro y el gato son amigos"
                ));
                // Entrenando modelo
                languageModel.train(sentences);

                try {
                    // Guardar el modelo
                    languageModel.saveModel("/ruta/al/llm/model.dat");
                } catch (IOException ex) {
                    Logger.getLogger(Ex1LLM.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println("No se pudo abrir el archivo para entrenamiento.");
            }
        }
    }

    private static File openFile() {
        JFileChooser jfc = new JFileChooser("/ruta/al/llm/");
        FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("Text file", "txt");
        jfc.setAcceptAllFileFilterUsed(false); // Desactivar filtro de aceptación de todos los archivos
        jfc.setFileFilter(extensionFilter);
        int returnValue = jfc.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            return jfc.getSelectedFile();
        } else {
            System.out.println("No se seleccionó ningún archivo.");
            return null;
        }
    }

    private static String readTextFromFile(File f) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n"); // Agregar salto de línea si es necesario
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Archivo no encontrado: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Error de entrada/salida: " + ex.getMessage());
        }
        return sb.toString();
    }
}
